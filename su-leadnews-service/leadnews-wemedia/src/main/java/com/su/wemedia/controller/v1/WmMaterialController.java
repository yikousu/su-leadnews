package com.su.wemedia.controller.v1;

import com.su.model.common.dtos.PageResponseResult;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.wemedia.dtos.WmMaterialDto;
import com.su.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/material")
public class WmMaterialController {

    @Autowired
    private WmMaterialService wmMaterialService;

    /**
     * 分页查询
     */
    @PostMapping(value="/list")
    public PageResponseResult list(@RequestBody WmMaterialDto dto){
        return wmMaterialService.pageList(dto);
    }

    /**
     * 上传图片
     */

    @PostMapping(value="/upload_picture")
    //multipartFile
    public ResponseResult upload_picture(MultipartFile multipartFile) throws IOException {
        wmMaterialService.upload(multipartFile);
        return null;
    }

    /**
     * 素材管理
     * 点击收藏
     */
    @GetMapping(value="/collect/{id}")
    public ResponseResult<String> collect(@PathVariable(value = "id")Integer id){
        wmMaterialService.collect(id);
        return ResponseResult.okResult("收藏成功");
    }

    /**
     * 素材管理
     * 点击删除【】自己写
     * 有关联？minio
     */
    @GetMapping(value = "/del_picture/{id}")
    public ResponseResult del_picture(@PathVariable("id") int id){
        wmMaterialService.delete_pic(id);
        return ResponseResult.okResult("删除成功");

    }


}
