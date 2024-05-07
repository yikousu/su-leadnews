package com.su.es.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.su.es.pojo.SearchArticleVo;
import com.su.model.common.article.pojos.ApArticle;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ApArticleMapper extends BaseMapper<ApArticle> {

    public List<SearchArticleVo> loadArticleList();

}
