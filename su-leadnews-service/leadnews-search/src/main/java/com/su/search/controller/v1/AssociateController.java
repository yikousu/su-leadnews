package com.su.search.controller.v1;

import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.search.dtos.UserSearchDto;
import com.su.search.service.AssociateSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 自动补全
 */
@RestController
@RequestMapping(value = "/api/v1/associate")
public class AssociateController {
    @Autowired
    private AssociateSearchService associateSearchService;

    @PostMapping(value = "/search")
    public ResponseResult search(@RequestBody UserSearchDto dto) throws IOException {
        return associateSearchService.search(dto);
    }
}
