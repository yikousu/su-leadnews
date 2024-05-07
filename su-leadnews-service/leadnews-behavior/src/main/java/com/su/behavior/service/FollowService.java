package com.su.behavior.service;

import com.su.model.common.behavior.dtos.FollowBehaviorDto;
import com.su.model.common.dtos.ResponseResult;

public interface FollowService {
    ResponseResult userFollow(FollowBehaviorDto dto);

}
