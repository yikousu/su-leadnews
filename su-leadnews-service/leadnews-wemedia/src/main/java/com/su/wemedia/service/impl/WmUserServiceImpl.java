package com.su.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.enums.AppHttpCodeEnum;
import com.su.model.common.wemedia.dtos.WmLoginDto;
import com.su.model.common.wemedia.pojos.WmUser;
import com.su.utils.common.AppJwtUtil;
import com.su.wemedia.mapper.WmUserMapper;
import com.su.wemedia.service.WmUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class WmUserServiceImpl extends ServiceImpl<WmUserMapper, WmUser> implements WmUserService {

    @Override
    public ResponseResult login(WmLoginDto dto) {
        //1.检查参数
        if(StringUtils.isBlank(dto.getName()) || StringUtils.isBlank(dto.getPassword())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"用户名或密码为空");
        }

        ExecutorService pool = Executors.newFixedThreadPool(10);
        pool.execute(()->{

        });

        //2.查询用户
        LambdaQueryWrapper<WmUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmUser::getName, dto.getName());
        WmUser wmUser = getOne(wrapper);

        //WmUser wmUser = getOne(Wrappers.<WmUser>lambdaQuery().eq(WmUser::getName, dto.getName()));
        if(wmUser == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        //3.比对密码
        String salt = wmUser.getSalt();
        String pswd = dto.getPassword();
        pswd = DigestUtils.md5DigestAsHex((pswd + salt).getBytes());
        if(pswd.equals(wmUser.getPassword())){
            //4.返回数据  jwt
            Map<String,Object> map  = new HashMap<>();
            wmUser.setPassword("");//不能设置为空  否则会报空指针异常
            wmUser.setSalt("");
            //法1
            Map<String,Object> beanMap = JSON.parseObject(JSON.toJSONString(wmUser), Map.class);
            //法2
            // Map<String,Object> beanMap = BeanMap.create(wmUser);
            // map.put("token", AppJwtUtil.getToken(wmUser.getId().longValue()));//1
            map.put("token", AppJwtUtil.token(beanMap));//2优化后 npe
            // wmUser.setSalt("");
            // wmUser.setPassword("");
            map.put("user",wmUser);
            return ResponseResult.okResult(map);

        }else {
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
        }
    }
}