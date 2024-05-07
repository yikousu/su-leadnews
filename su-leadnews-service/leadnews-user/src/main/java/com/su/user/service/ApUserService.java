package com.su.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.user.dtos.LoginDto;
import com.su.model.common.user.pojos.ApUser;
public interface ApUserService extends IService<ApUser> {
    ResponseResult longin(LoginDto loginDto);
}
