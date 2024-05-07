package com.su.behavior.controller;

import com.su.behavior.service.FollowService;
import com.su.model.common.behavior.dtos.FollowBehaviorDto;
import com.su.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/user")
public class FollowController {
    @Autowired
    private FollowService followService;
    @PostMapping(value = "/user_follow")
    public ResponseResult userFollow(@RequestBody FollowBehaviorDto dto) {
        return followService.userFollow(dto);
    }

}
