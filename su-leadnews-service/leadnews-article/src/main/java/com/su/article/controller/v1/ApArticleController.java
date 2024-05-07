package com.su.article.controller.v1;

import com.su.article.service.ApArticleService;
import com.su.common.util.ArticleConstant;
import com.su.model.common.article.dtos.ArticleDto;
import com.su.model.common.article.dtos.ArticleHomeDto;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.search.vos.SearchArticleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/article")
public class ApArticleController {
    @Autowired
    private ApArticleService apArticleService;

    /**
     * Feign远程调用【article数据批量导入到ES中】
     * 分页查
     */
    @GetMapping(value = "/search/page/{index}/{size}")
    public List<SearchArticleVo> searchPage(@PathVariable("index") Integer index, @PathVariable("size") Integer size) {
        return apArticleService.searchPage(index, size);
    }

    /**
     * Feign远程调用【article数据批量导入到ES中】
     * 查询数据总条数
     * 因为不对接前端页面  所以就不需要同意封装返回结果
     */
    @GetMapping(value = "/search/total")
    public Long searchTotal() {
        return apArticleService.searchTotal();
    }


    /**
     * Feign远程调用
     * 文章保存  news中发布文章 通过审核之后要保存到这个表中
     */
    @PostMapping(value = "/save")
    public ResponseResult<Long> save(@RequestBody ArticleDto dto) throws Exception {
        return apArticleService.saveArticle(dto);
    }

    /**
     * 加载首页
     */
    @PostMapping(value = "load")
    public ResponseResult load(@RequestBody ArticleHomeDto dto) {
        return apArticleService.loadArticleList(ArticleConstant.LOAD_TYPE_INDEX, dto);
    }


    /**
     * 加载更多
     */
    @PostMapping(value = "loadmore")
    public ResponseResult loadMore(@RequestBody ArticleHomeDto dto) {
        return apArticleService.loadArticleList(ArticleConstant.LOAD_TYPE_MORE, dto);
    }


    /**
     * 加载最新
     */
    @PostMapping(value = "loadnew")
    public ResponseResult loadNew(@RequestBody ArticleHomeDto dto) {
        return apArticleService.loadArticleList(ArticleConstant.LOAD_TYPR_NEW, dto);
    }


    /**
     * 生成静态页
     */
    @GetMapping(value = "/html/{id}")
    public ResponseResult html(@PathVariable("id") Long id) throws Exception {
        return apArticleService.html(id);
    }

}
