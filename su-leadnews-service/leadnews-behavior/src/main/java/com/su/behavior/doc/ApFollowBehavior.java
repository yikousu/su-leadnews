package com.su.behavior.doc;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 存入mongodb
 */
@Data
@Document(value = "ap_follow_behavior")
public class ApFollowBehavior {
    private String id;
    //用户id   作者id
    private Long userId;
    private Integer followId;
    private Date createTime;
}
