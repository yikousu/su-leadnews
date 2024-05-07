package com.su.behavior.service.impl;

import com.su.behavior.doc.ApCommentBehavior;
import com.su.behavior.doc.ApLikesBehavior;
import com.su.behavior.service.BehaviorService;
import com.su.common.util.RequestContextUtil;
import com.su.model.common.behavior.dtos.CommentBehaviorDto;
import com.su.model.common.behavior.dtos.LikesBehaviorDto;
import com.su.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class BehaviorServiceImpl implements BehaviorService {
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 点赞
     * @param dto
     * @return
     */
    @Override
    public ResponseResult likesBehavior(LikesBehaviorDto dto) {
        Integer userId = RequestContextUtil.get("id");

        if (dto.getOperation() == 0) {
            ApLikesBehavior apLikesBehavior = new ApLikesBehavior();
            apLikesBehavior.setId(userId + "-article-" + dto.getArticleId());
            apLikesBehavior.setUserId(Long.valueOf(userId));
            apLikesBehavior.setArticleId(dto.getArticleId());
            apLikesBehavior.setType(dto.getType());
            apLikesBehavior.setCreatedTime(new Date());
            mongoTemplate.save(apLikesBehavior);
            System.out.println("存入成功！！！！！！！！！！！");
        } else {
            Query query = Query.query(Criteria
                    .where("userId").is(userId)
                    .and("articleId").is(dto.getArticleId()));
            mongoTemplate.remove(query, ApLikesBehavior.class);
            System.out.println("删除成功！！！！！！！！！！！");

        }


        return ResponseResult.okResult(null);
    }

    /**
     * 评论
     * @param dto
     * @return
     */
    @Override
    public ResponseResult comment(CommentBehaviorDto dto) {

        Integer userId = RequestContextUtil.get("id");
        ApCommentBehavior apCommentBehavior = new ApCommentBehavior();
        apCommentBehavior.setId(userId + "-article-" + dto.getArticleId());
        apCommentBehavior.setUserId(Long.valueOf(userId));
        apCommentBehavior.setArticleId(dto.getArticleId());
        apCommentBehavior.setComment(dto.getComment());
        apCommentBehavior.setCreatedTime(new Date());
        mongoTemplate.save(apCommentBehavior);
        System.out.println("评论："+dto.getComment());
        return ResponseResult.okResult(null);
    }
}
