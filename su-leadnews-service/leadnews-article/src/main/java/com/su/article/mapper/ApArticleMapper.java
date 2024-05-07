package com.su.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.su.model.common.article.dtos.ArticleHomeDto;
import com.su.model.common.article.pojos.ApArticle;
import com.su.model.common.search.vos.SearchArticleVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApArticleMapper extends BaseMapper<ApArticle> {
    List<ApArticle> loadArticleList(@Param("type") int loadTypeMore, @Param("dto") ArticleHomeDto dto);

    List<SearchArticleVo> searchPage(@Param("index")Integer index, @Param("size") Integer size);

    Long searchTotal();

}
