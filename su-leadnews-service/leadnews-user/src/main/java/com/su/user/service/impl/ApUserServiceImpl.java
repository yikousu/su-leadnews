package com.su.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.enums.AppHttpCodeEnum;
import com.su.model.common.user.dtos.LoginDto;
import com.su.model.common.user.pojos.ApUser;
import com.su.user.mapper.ApUserMapper;
import com.su.user.service.ApUserService;
import com.su.utils.common.AppJwtUtil;
import com.su.utils.common.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;

@Service
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService   {
    @Autowired
    private ApUserMapper apUserMapper;
    @Override
    public ResponseResult longin(LoginDto loginDto) {

        if(!ObjectUtils.isEmpty(loginDto.getPhone()) && !ObjectUtils.isEmpty(loginDto.getPassword())){
            //如果查询到 说明数据库中有user
            ApUser user = apUserMapper.selectOne(Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, loginDto.getPhone()));
            if(user!=null){
                //再次验证密码
                String pwd = MD5Utils.encodeWithSalt(loginDto.getPassword(),user.getSalt());
                if(pwd.equals(user.getPassword())){
                    //密码正确
                    HashMap<String, Object> map = new HashMap<>();
                    user.setPassword(null);
                    user.setSalt(null);
                    map.put("user",user);
                    map.put("token", AppJwtUtil.getToken(user.getId().longValue()));
                    return ResponseResult.okResult(map);
                }else{
                    //密码错误
                    return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
                }

            }else{
                //没注册
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
            }
        }else{
            //游客模式
            String token = AppJwtUtil.getToken(0L);
            return ResponseResult.okResult(token);

        }
    }
}
