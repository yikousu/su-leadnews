package com.su.behavior.service;

import com.su.model.common.behavior.dtos.CommentBehaviorDto;
import com.su.model.common.behavior.dtos.LikesBehaviorDto;
import com.su.model.common.dtos.ResponseResult;

public interface BehaviorService {
    ResponseResult likesBehavior(LikesBehaviorDto dto);

    ResponseResult comment(CommentBehaviorDto dto);
}
