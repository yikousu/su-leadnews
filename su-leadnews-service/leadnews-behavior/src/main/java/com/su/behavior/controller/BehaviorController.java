package com.su.behavior.controller;

import com.su.behavior.service.BehaviorService;
import com.su.model.common.behavior.dtos.CommentBehaviorDto;
import com.su.model.common.behavior.dtos.LikesBehaviorDto;
import com.su.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1")
public class BehaviorController {
    @Autowired
    private BehaviorService behaviorService;

    /**
     * 文章点赞
     * @param dto 前端参数
     * @return
     */
    @PostMapping(value = "/likes_behavior")
    public ResponseResult likesBehavior(@RequestBody LikesBehaviorDto dto) {
        return behaviorService.likesBehavior(dto);
    }
    /**
     *
     * 评论【自己写】
     */
    @PostMapping(value = "/comment/save")
    public ResponseResult comment(@RequestBody CommentBehaviorDto dto) {
        return behaviorService.comment(dto);
    }
}
