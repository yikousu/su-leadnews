package com.su.model.common.behavior.dtos;

import lombok.Data;

@Data
public class CommentBehaviorDto {
    private Long articleId;
    private String comment;
}
