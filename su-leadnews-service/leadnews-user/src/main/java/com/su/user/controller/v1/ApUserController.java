package com.su.user.controller.v1;

import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.user.dtos.LoginDto;
import com.su.user.service.ApUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value="api/v1")
public class ApUserController {
    @Autowired
    private ApUserService apUserService;

    /**
     * 登录
     * @param loginDto
     * @return
     */
    @PostMapping(value="login/login_auth")
    public ResponseResult login(@RequestBody LoginDto loginDto){
        return apUserService.longin(loginDto);
    }

    /**
     * 查询用户列表
     */
    @GetMapping(value="list")
    public ResponseResult list(){
        return ResponseResult.okResult(apUserService.list());
    }

}
