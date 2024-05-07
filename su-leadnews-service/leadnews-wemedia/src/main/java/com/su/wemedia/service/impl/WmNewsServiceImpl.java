package com.su.wemedia.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.su.model.common.dtos.PageResponseResult;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.wemedia.dtos.WmNewsDto;
import com.su.model.common.wemedia.dtos.WmNewsPageReqDto;
import com.su.model.common.wemedia.dtos.WmnewsStatusDto;
import com.su.model.common.wemedia.pojos.WmNews;
import com.su.wemedia.config.RequestContextUtil;
import com.su.wemedia.mapper.WmNewsMapper;
import com.su.wemedia.service.WmNewsAutoScanService;
import com.su.wemedia.service.WmNewsService;
import com.su.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {
    @Autowired
    private WmNewsTaskService wmNewsTaskService;
    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 上下架
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult downUp(WmnewsStatusDto dto) {
        //用动态sql
        //1 下架更改状态
        int count = wmNewsMapper.downUp(dto);
        if (count > 0) {
            WmNews wmNews = wmNewsMapper.selectById(dto.getId());
            dto.setArticleId(wmNews.getArticleId());
            //发送给kafka
            kafkaTemplate.send("Article_DownUp", JSON.toJSONString(dto));
        }
        return ResponseResult.okResult(null);
    }

    /**
     * 文章列表查询
     */

    @Override
    public PageResponseResult newsList(WmNewsPageReqDto dto) {
        // 封装page  !!需要传入两个参数
        Page<WmNews> page = new Page<>(dto.getPage(), dto.getSize());

        // 封装查询条件
        QueryWrapper<WmNews> queryWrapper = new QueryWrapper<>();
        if (!ObjectUtils.isEmpty(dto.getStatus())) {
            queryWrapper.eq("status", dto.getStatus());
        }

        if (!ObjectUtils.isEmpty(dto.getKeyword())) {
            queryWrapper.like("title", dto.getKeyword());
        }

        if (!ObjectUtils.isEmpty(dto.getChannelId())) {
            queryWrapper.eq("channel_id", dto.getChannelId());
        }
        if (!ObjectUtils.isEmpty(dto.getBeginPubDate()) && !ObjectUtils.isEmpty(dto.getEndPubDate())) {
            queryWrapper.between("publish_time", dto.getBeginPubDate(), dto.getEndPubDate());
        }


        Integer userId = RequestContextUtil.get("apUserId");
        // 因为每个自媒体用户只操作自己的相关数据
        queryWrapper.eq("user_id", userId);


        // 执行查询条件
        Page<WmNews> pageInfo = wmNewsMapper.selectPage(page, queryWrapper);

        // 封装返回结果
        PageResponseResult pageResponseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) pageInfo.getTotal());
        pageResponseResult.setData(pageInfo.getRecords());// 封装数据结果集
        return pageResponseResult;

        // return ResponseResult.okResult(pageInfo.getRecords());

    }

    /**
     * 添加发布
     */
    @Override
    public ResponseResult submit(WmNewsDto dto) throws InvocationTargetException, IllegalAccessException {
        // 1)将WmNewsDto转换成WmNews
        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(wmNews, dto);

        wmNews.setCreatedTime(new Date());

        // 2)status=自动->从内容中提取图片->作为封面[ 不要超过3张]
        if (wmNews.getType() == -1) {
            parseImage(dto);
        }

        // 3) 图片[封面]要转换 以逗号隔开
        String images = StringUtils.join(dto.getImages(), ",");
        wmNews.setImages(images);

        // 保存
        if (wmNews.getId() == null) {
            // 新增
            wmNews.setUserId(RequestContextUtil.get("apUserId"));
            wmNewsMapper.insert(wmNews);
        } else {
            // 修改
            wmNewsMapper.updateById(wmNews);
        }

        // 去审核
        /**
         * ！！！！！！！！！！！
         */
        //wmNewsAutoScanService.autoScanWmNews(wmNews.getId());

        /**
         * 定时发布
         */
        //添加到延时服务中
        wmNewsTaskService.addNewsToTask(wmNews.getId(), wmNews.getPublishTime());


        return ResponseResult.okResult(null);

    }


    public void parseImage(WmNewsDto dto) {
        // 取数据
        String content = dto.getContent();
        List<Map> maps = JSON.parseArray(content, Map.class);
        // 遍历
        List<String> images = new ArrayList<>(); // 存储内容中的图片
        for (Map map : maps) {
            String type = map.get("type").toString();
            if (type.equals("image")) {
                images.add(map.get("value").toString());
                if (images.size() >= 3) {
                    break;
                }
            }
        }
        dto.setImages(images);
    }


    /**
     * 文章删除
     * 1. 删内容列表news(不删除minio  上传了图片去了minio 在素材管理哪里删除)
     * 2. 不用删除asrtcle 因为只能删除没上架的or审核未通过的【处于待审核状态】
     * 其实这个功能不合理
     * 但是为了数据库垃圾数据少点，想把不用的删除了
     * 自己写的
     */
    @Override
    public void del_news(int id) {
        //1删除news
        wmNewsMapper.deleteById(id);
    }


}
