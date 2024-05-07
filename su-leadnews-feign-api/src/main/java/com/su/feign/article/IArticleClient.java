package com.su.feign.article;

import com.su.model.common.article.dtos.ArticleDto;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.search.vos.SearchArticleVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "leadnews-article", path = "/api/v1/article")
public interface IArticleClient {
    //保存文章
    @PostMapping(value = "/save")
    ResponseResult<Long> save(@RequestBody ArticleDto dto);

    /**
     * 分页查
     */
    @GetMapping(value = "/search/page/{index}/{size}")
    List<SearchArticleVo> searchPage(@PathVariable("index") Integer index, @PathVariable("size") Integer size);

    /**
     * 查询数据总条数
     * 因为不对接前端页面  所以就不需要同意封装返回结果
     */
    @GetMapping(value = "/search/total")
    Long searchTotal();
}
