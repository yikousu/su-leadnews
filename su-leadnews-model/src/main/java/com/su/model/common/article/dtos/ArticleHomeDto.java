package com.su.model.common.article.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class ArticleHomeDto {
    private Date maxBeHotTime;
    private Date minBeHotTime;
    private int size;
    private String tag;
}
