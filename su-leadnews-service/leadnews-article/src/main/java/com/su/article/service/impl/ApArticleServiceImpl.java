package com.su.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.su.article.mapper.ApArticlaContentMapper;
import com.su.article.mapper.ApArticleConfigMapper;
import com.su.article.mapper.ApArticleMapper;
import com.su.article.service.ApArticleService;
import com.su.file.service.FileStorageService;
import com.su.model.common.article.dtos.ArticleDto;
import com.su.model.common.article.dtos.ArticleHomeDto;
import com.su.model.common.article.pojos.ApArticle;
import com.su.model.common.article.pojos.ApArticleConfig;
import com.su.model.common.article.pojos.ApArticleContent;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.search.vos.SearchArticleVo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.ByteArrayInputStream;
import java.util.*;

@Service
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {
    @Autowired
    private ApArticleMapper apArticleMapper;
    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;

    @Autowired
    private ApArticlaContentMapper apArticleContentMapper;

    @Autowired
    private Configuration configuration;
    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 分页查
     */
    @Override
    public List<SearchArticleVo> searchPage(Integer index, Integer size) {
        return apArticleMapper.searchPage(index, size);
    }

    /**
     * 查询数据总条数
     */
    @Override
    public Long searchTotal() {
        return apArticleMapper.searchTotal();
    }

    @Override
    public ResponseResult loadArticleList(int loadTypeMore, ArticleHomeDto dto) {
        // 防止非法请求 不带时间
        if (dto.getMaxBeHotTime() == null) {
            dto.setMaxBeHotTime(new Date());
        }
        if (dto.getMinBeHotTime() == null) {
            dto.setMinBeHotTime(new Date());
        }

        List<ApArticle> articles = apArticleMapper.loadArticleList(loadTypeMore, dto);

        return ResponseResult.okResult(articles);

    }

    @Override
    public ResponseResult html(Long id) throws Exception {
        // 1查询数据
        // QueryWrapper<ApArticleContent> wrapper = new QueryWrapper<ApArticleContent>().eq("articleId", id);
        LambdaQueryWrapper<ApArticleContent> lambdaQueryWrapper = new LambdaQueryWrapper<ApArticleContent>().eq(ApArticleContent::getArticleId, id);

        ApArticleContent content = apArticleContentMapper.selectOne(lambdaQueryWrapper);

        // 2freemarker生成静态页   5个步骤
        Map<String, Object> map = new HashMap<>();
        // 将从数据库里面查询到的内容  转为list类型  因为前端需要
        List<Map> maps = JSON.parseArray(content.getContent(), Map.class);
        map.put("content", maps);
        Template template = configuration.getTemplate("article.ftl");
        // 模板和数据结合返回内容
        String s = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);


        // 3上传到minio服务器
        ByteArrayInputStream is = new ByteArrayInputStream(s.getBytes());
        String url = fileStorageService.uploadHtmlFile("", id + ".html", is);

        // 把静态页url存入数据库
        ApArticle article = new ApArticle();
        article.setId(id);
        article.setStaticUrl(url);
        apArticleMapper.updateById(article);


        return ResponseResult.okResult(null);
    }

    /**
     * 1. 保存文章
     * 2. 增量同步到ES【creatArticleESIndex】
     *      用kafka发消息
     *      实现增量同步
     */
    @Override
    public ResponseResult<Long> saveArticle(ArticleDto dto) throws Exception {
        if (dto.getId() == null) {
            // 增加
            // 1 ApArticle
            ApArticle apArticle = dto;
            apArticleMapper.insert(apArticle);

            // 2  ApArticleConfig
            ApArticleConfig apArticleConfig = new ApArticleConfig();
            apArticleConfig.setIsComment(true);
            apArticleConfig.setIsDown(false);
            apArticleConfig.setIsForward(false);
            apArticleConfig.setIsDelete(false);
            apArticleConfig.setArticleId(dto.getId());
            apArticleConfigMapper.insert(apArticleConfig);

            // 3 ApArticleContent
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setContent(dto.getContent());
            apArticleContent.setArticleId(dto.getId());
            apArticleContentMapper.insert(apArticleContent);
            //生成静态页
            html(dto.getId());
            // 送消息给kafka   上架1
            creatArticleESIndex(dto.getId(),1);
            return ResponseResult.okResult(dto.getId());
        } else {
            /**
             * 修改
             */
            // 1
            apArticleMapper.updateById(dto);
            // 2
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setContent(dto.getContent());
            apArticleContent.setArticleId(dto.getId());
            //生成静态页
            html(dto.getId());

            //发送消息给kafka  因为修改先下架
            creatArticleESIndex(dto.getId(),0);

        }
        return ResponseResult.okResult(dto.getId());
    }




    @Autowired
    private KafkaTemplate kafkaTemplate;
    /**
     * 文章同步到ES
     * 向MQ发送消息
     * @param id
     */
    @Override
    public void creatArticleESIndex(Long id,int enable){
        //1查出文章信息  内容
        ApArticle apArticle = apArticleMapper.selectById(id);
        LambdaQueryWrapper<ApArticleContent> apArticleContentLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ApArticleContent content = apArticleContentMapper.selectOne(apArticleContentLambdaQueryWrapper.eq(ApArticleContent::getArticleId, id));
        //2封装成searchArticleVo对象
        SearchArticleVo vo = new SearchArticleVo();
        vo.setId(apArticle.getId());
        vo.setTitle(apArticle.getTitle());
        vo.setPublishTime(apArticle.getPublishTime());
        vo.setLayout(Integer.valueOf(apArticle.getLayout()));
        vo.setImages(apArticle.getImages());
        vo.setAuthorName(apArticle.getAuthorName());
        vo.setStaticUrl(apArticle.getStaticUrl());
        vo.setContent(content.getContent());
        vo.setEnable(enable);//区分上下架

        //3向searchArticle发送MQ
        kafkaTemplate.send("article_sync_es_topic",JSON.toJSONString(vo));
    }


}
