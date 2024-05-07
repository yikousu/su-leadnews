package com.su.wemedia.controller.v1;

import com.su.model.common.dtos.ResponseResult;
import com.su.wemedia.service.WmChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/api/v1/channel")
public class WmChannelController {
    @Autowired
    private WmChannelService wmChannelService;

    /**
     * 查询标签
     * @return
     */
    @GetMapping(value="/channels")
    public ResponseResult list(){
        //直接合并成一行代码
        return ResponseResult.okResult(wmChannelService.list());
    }
}
