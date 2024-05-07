package com.su.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.su.model.common.article.dtos.ArticleDto;
import com.su.model.common.article.dtos.ArticleHomeDto;
import com.su.model.common.article.pojos.ApArticle;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.search.vos.SearchArticleVo;

import java.io.IOException;
import java.util.List;

public interface ApArticleService extends IService<ApArticle> {


    ResponseResult loadArticleList(int loadTypeMore, ArticleHomeDto dto);

    ResponseResult html(Long id) throws IOException, Exception;


    ResponseResult<Long> saveArticle(ArticleDto dto) throws Exception;

    List<SearchArticleVo> searchPage(Integer index, Integer size);

    Long searchTotal();


    /**
     * 发消息给kafka
     * @param id
     */
    void creatArticleESIndex(Long id,int enable);
}
