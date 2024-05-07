package com.su.behavior.doc;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(value = "ap_comment_behavior")
public class ApCommentBehavior {
    private String id;
    private Long userId;
    private Long articleId;
    private String comment;
    private Date createdTime;
}
