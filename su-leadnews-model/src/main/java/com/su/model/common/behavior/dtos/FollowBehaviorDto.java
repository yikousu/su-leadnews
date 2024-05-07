package com.su.model.common.behavior.dtos;

import lombok.Data;

@Data
public class FollowBehaviorDto {
    //文章id【可有可无 因为是关注】
    private Long articleId;
    //作者id
    private Integer authorId;
    /*
    操作方式
    0 关注
    1 取关
     */
    private short operation;

}
