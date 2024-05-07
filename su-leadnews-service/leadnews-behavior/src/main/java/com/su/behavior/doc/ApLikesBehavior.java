package com.su.behavior.doc;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(value = "ap_likes_behavior")
public class ApLikesBehavior implements Serializable {
    private static final long serialVersionUID =1L;
    private String id;
    private Long userId;
    private Long articleId;
    private Short type;
    private Date createdTime;
}
