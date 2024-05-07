package com.su.wemedia.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.su.model.common.dtos.PageResponseResult;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.wemedia.dtos.WmMaterialDto;
import com.su.model.common.wemedia.pojos.WmMaterial;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface WmMaterialService extends IService<WmMaterial> {


    ResponseResult<WmMaterial> upload(MultipartFile file) throws IOException;

    PageResponseResult pageList(WmMaterialDto dto);

    void collect(Integer id);

    void delete_pic(int id);
}