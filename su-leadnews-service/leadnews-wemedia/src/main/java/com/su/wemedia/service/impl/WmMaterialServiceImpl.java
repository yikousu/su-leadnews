package com.su.wemedia.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.su.file.service.FileStorageService;
import com.su.model.common.dtos.PageResponseResult;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.wemedia.dtos.WmMaterialDto;
import com.su.model.common.wemedia.pojos.WmMaterial;
import com.su.wemedia.config.RequestContextUtil;
import com.su.wemedia.mapper.WmMaterialMapper;
import com.su.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private WmMaterialMapper wmMaterialMapper;

    /**
     * 文件上传
     * 1、文件上传->MinI0->集成MinI0
     * 2、获取请求头中的用户信息->定义拦截器->将请求头信息存入到ThreadLocal
     * 3、封装一个WmMaterial->存入到数据库
     *
     * @param file
     */

    @Override
    public ResponseResult upload(MultipartFile file) throws IOException {
        // 文件名相同会覆盖原来的图片  所以改名字
        String fileName = file.getOriginalFilename();

        String suffix = StringUtils.getFilenameExtension(fileName);// 取后缀

        String newFileName = UUID.randomUUID().toString() + "." + suffix;

        // 上传minio
        String url = fileStorageService.uploadImgFile("", newFileName, file.getInputStream());

        // 取数据  获取id
        //  WmUser wmUser = WmThreadLocalUtils.get(); //原始拦截器
        Integer userId = RequestContextUtil.get("apUserId");

        // 存入数据库
        WmMaterial wmMaterial = new WmMaterial();

        wmMaterial.setCreatedTime(new Date());
        wmMaterial.setUserId(userId);//id不能错  不是wmUser.getId()  此id几乎没作用 每张表第一个均为id
        wmMaterial.setIsCollection((short) 0);
        wmMaterial.setType((short) 0);
        wmMaterial.setUrl(url);

        //保存到数据库
        wmMaterialMapper.insert(wmMaterial);
        return ResponseResult.okResult(wmMaterial);
    }

    /**
     * 分页查询
     */
    @Override
    public PageResponseResult pageList(WmMaterialDto dto) {
        // 1)创建Page
        //dto.getPage() 获取分页查询中当前页码的方法
        if(dto.getPage()==null){ //因为发布文章 点击收藏 前端这个传过来为空值
            dto.setPage(1);
        }
        IPage<WmMaterial> page = new Page<>(dto.getPage(), dto.getSize());

        // 2)封装Wrapper条件信息
        QueryWrapper<WmMaterial> queryWrapper = new QueryWrapper<>();

        // WmUser wmUser = WmThreadLocalUtils.get();//原始拦截器
        Integer userId = RequestContextUtil.get("apUserId");//优化后
        queryWrapper.eq("user_id",userId);

        /*
         *素材管理
         * 全部  收藏  两个请求路径相同 请求参数不同
         * 当查收藏时 构建查询条件 多一个条件
         */
        if(dto.getIsCollection()==1){
            queryWrapper.eq("is_collection",dto.getIsCollection());
        }

        // 3)分页查询
        IPage<WmMaterial> pageInfo = wmMaterialMapper.selectPage(page, queryWrapper);

        // 4)返回对象封装
        PageResponseResult pageResponseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) pageInfo.getTotal());
        pageResponseResult.setData(pageInfo.getRecords());
        return pageResponseResult;
    }

    /**
     * 素材管理
     * 点击收藏
     */

    @Override
    public void collect(Integer id) {
        QueryWrapper<WmMaterial> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id);
        WmMaterial wmMaterial = wmMaterialMapper.selectOne(queryWrapper);
        wmMaterial.setIsCollection((short) 1);
        wmMaterialMapper.updateById(wmMaterial);
    }
    /**
     * 素材管理
     * 点击删除【】自己写
     * 有关联？minio
     */

    @Override
    public void delete_pic(int id) {
        WmMaterial wmMaterial = wmMaterialMapper.selectById(id);
        String url = wmMaterial.getUrl();
        fileStorageService.delete(url);
        wmMaterialMapper.deleteById(id);
    }
}
