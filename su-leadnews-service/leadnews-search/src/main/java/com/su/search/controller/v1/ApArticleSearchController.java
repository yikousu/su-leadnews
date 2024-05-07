package com.su.search.controller.v1;

import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.search.dtos.UserSearchDto;
import com.su.search.service.ApArticleSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequestMapping(value = "/api/v1/article/search")
@RestController
public class ApArticleSearchController {
    @Autowired
    private ApArticleSearchService apArticleSearchService;
    /**
     *
     */
    @PostMapping(value = "/search")
    public ResponseResult search(@RequestBody UserSearchDto dto) throws IOException {
       return apArticleSearchService.search(dto);
    }

}
