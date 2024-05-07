package com.su.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.su.model.common.article.pojos.ApArticle;
import com.su.model.common.article.pojos.ApArticleContent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public  interface ApArticlaContentMapper extends BaseMapper<ApArticleContent> {
}
