package com.su.behavior.doc;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 存入mongodb
 */
@Data
@Document(value = "ap_fan_behavior")
public class ApFanBehavior {
    private String id;
    private Long userId;
    private Integer fanId;
    private Date createTime;
}
