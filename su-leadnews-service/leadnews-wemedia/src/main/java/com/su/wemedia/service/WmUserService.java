package com.su.wemedia.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.wemedia.dtos.WmLoginDto;
import com.su.model.common.wemedia.pojos.WmUser;

public interface WmUserService extends IService<WmUser> {

    /**
     * 自媒体端登录
     * @param dto
     * @return
     */
    public ResponseResult login(WmLoginDto dto);

}