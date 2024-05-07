package com.su.model.common.behavior.dtos;

import lombok.Data;

@Data
public class LikesBehaviorDto {
    /*
        针对那篇文章操作
     */
    private Long articleId;
    /*
        点赞的类型
        0 文章
        1 动态
        2 评论
     */
    private short type;
    /*
        操作 点赞？取消点赞？
        0 点赞
        1 取消点赞
     */
    private short operation;
}
