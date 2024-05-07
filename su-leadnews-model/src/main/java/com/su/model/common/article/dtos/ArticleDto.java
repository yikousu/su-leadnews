package com.su.model.common.article.dtos;

import com.su.model.common.article.pojos.ApArticle;
import lombok.Data;

@Data
public class ArticleDto extends ApArticle {
    private String content;
}
