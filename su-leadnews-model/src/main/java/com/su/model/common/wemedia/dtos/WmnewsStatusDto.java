package com.su.model.common.wemedia.dtos;

import lombok.Data;

import java.io.Serializable;

/**
 * 用于上下架文章
 */
@Data
public class WmnewsStatusDto implements Serializable {
    private  Integer id;
    private Short enable;
    private Long articleId;
}
