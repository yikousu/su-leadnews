package com.su.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.su.common.aliyun.GreenImageScan;
import com.su.common.aliyun.GreenTextScan;
import com.su.common.tess4j.Tess4jUtils;
import com.su.feign.article.IArticleClient;
import com.su.file.service.FileStorageService;
import com.su.model.common.article.dtos.ArticleDto;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.wemedia.pojos.WmChannel;
import com.su.model.common.wemedia.pojos.WmNews;
import com.su.model.common.wemedia.pojos.WmUser;
import com.su.utils.common.SensitiveWordUtil;
import com.su.wemedia.config.RequestContextUtil;
import com.su.wemedia.mapper.WmChannelMapper;
import com.su.wemedia.mapper.WmNewsMapper;
import com.su.wemedia.mapper.WmUserMapper;
import com.su.wemedia.service.WmNewsAutoScanService;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Service
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Autowired
    private GreenTextScan greenTextScan;
    @Autowired
    private GreenImageScan greenImageScan;
    @Autowired
    private IArticleClient articleClient;
    @Autowired
    private WmChannelMapper wmChannelMapper;
    @Autowired
    private Tess4jUtils tess4jUtils;
    @Autowired
    private WmUserMapper wmUserMapper;

    /**
     * wm_news发布文章->审核->存到article
     * 吧绿色注释的打开就有阿里云文字图片检测功能了
     *
     * @param id  wmNews.getId()  文章id
     */
    @Override
    @Async // 该方法会异步执行
    public void autoScanWmNews(Integer id) throws TesseractException, IOException {
        int status = 9;
        // 1查文章
        // 把文章查出来了 以后都是对这个文章对象处理
        WmNews wmNews = wmNewsMapper.selectById(id);
        List<String> textList = parseContent(wmNews.getContent(), "text");
        String content = StringUtils.join(textList, ",") + wmNews.getLabels().toString() + wmNews.getTitle().toString();

        /**
         * 图片上的文字识别出来检测
         */
        // 下载图片  图片中文字也要啊检测
        List<byte[]> bytes = minioDownload(wmNews);
        String imageContent = tess4jUtils.doOCR(bytes);//提取图片中文字
        content = content + imageContent;

        //先本地审核
        Map<String, Integer> localVerifyResult = SensitiveWordUtil.matchWords(content);
        if (localVerifyResult.size() == 0) {
            /** 调aliyun
             status = aliyunVerfy(content, bytes, wmNews);
             */
        } else {
            // 本地审核不通过
            status = 2;
        }

        // 因为有定时发布 【先看是否9  有敏感词就不执行了】
        if (status == 9 && wmNews.getPublishTime().getTime() > System.currentTimeMillis()) {
            status = 8;
        }

        wmNews.setStatus((short) status);
        if (status == 9) {  // 因为可能审核不通过
            // 远程同步  wmNews->article
            // 通过则调feign 把数据存到article微服务相关数据库
            ArticleDto dto = new ArticleDto();
            WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());

            dto.setContent(wmNews.getContent());
            dto.setComment(0);
            dto.setChannelName(wmChannel.getName());
            dto.setChannelId(wmNews.getChannelId());
            dto.setFlag((byte) 0);
            dto.setLabels(wmNews.getLabels());
            dto.setTitle(wmNews.getTitle());
            dto.setLayout(wmNews.getType());
            dto.setCreatedTime(wmNews.getPublishTime());
//            dto.setPublishTime();  定时才有这个时间
            dto.setLikes(0);
            dto.setSyncStatus(false);
            dto.setImages(wmNews.getImages());
            dto.setCollection(0);
            dto.setViews(0);
            /**
             * 补充作者名字 id  表：ap_article
             */
            Integer userId = wmNews.getUserId();
            dto.setAuthorId(Long.valueOf(userId));
            WmUser wmUser = wmUserMapper.selectById(userId);
            dto.setAuthorName( wmUser.getName());

            // dto.setStaticUrl(？？？);
            if (wmNews.getId() != null) {// 因为分：增加  修改
                dto.setId(wmNews.getArticleId());
            }

            ResponseResult<Long> resultId = articleClient.save(dto);

            if (wmNews.getArticleId() == null) {
                wmNews.setArticleId(resultId.getData());// getData()&#x662F;ResponseResult&#x4E2D;&#x4E00;&#x4E2A;&#x65B9;&#x6CD5;
            }
        }
        wmNewsMapper.updateById(wmNews);// 因为添加了article的id or status【审核失败or定时发布】
    }

    public List<byte[]> minioDownload(WmNews wmNews) {
        // 3minio中下载图片
        List<String> images = parseContent(wmNews.getContent(), "image");
        String cover_images = wmNews.getImages();
        String[] splits = StringUtils.split(cover_images, ",");
        images.addAll(Arrays.asList(splits));
        Set<String> urls = new HashSet<>();
        urls.addAll(images);

        List<byte[]> imageBytes = new ArrayList<>();
        for (String url : urls) {
            byte[] bytes = fileStorageService.downLoadFile(url);
            imageBytes.add(bytes);
        }
        return imageBytes;
    }

    /**
     * ailiyun检测抽出来  先本地检测 再aliyun检测
     */
    private int aliyunVerfy(String content, List<byte[]> imageBytes, WmNews wmNews) throws UnsupportedEncodingException {
        // 2调阿里文本审核   content+title+labels
        Map<String, String> textVerifyResult = greenTextScan.verify(content);

        // 4调阿里图片审核
        Map<String, String> imageVerifyResult = greenImageScan.verfy(imageBytes);

        imageVerifyResult.putAll(textVerifyResult);


        // 判断审核是否通过

        int status = 9;
        for (Map.Entry<String, String> entry : imageVerifyResult.entrySet()) {

            String value = entry.getValue();
            if (value.equals("block")) {
                status = 2;
                break;
            } else if (value.equals("review")) {
                status = 3;
            }
        }
        return status;

    }

    /**
     * 封面图片+内容图片 没用这个方法
     */
    public Set<String> images(List<String> text_image, List<String> cover_image) {
        text_image.addAll(cover_image);
        Set<String> set = new HashSet<>();
        set.addAll(text_image);
        return set;
    }

    /**
     * 解析数据中的text
     *
     * @param content
     * @param match
     * @return
     */

    public List<String> parseContent(String content, String match) {
        List<Map> maps = JSON.parseArray(content, Map.class);
        // 因为json字符串中可能有多个text
        List<String> result = new ArrayList<>();

        for (Map map : maps) {
            String type = map.get("type").toString();
            if (type.equals(match)) {
                result.add(map.get("value").toString());
            }
        }

        return result;
    }
}
