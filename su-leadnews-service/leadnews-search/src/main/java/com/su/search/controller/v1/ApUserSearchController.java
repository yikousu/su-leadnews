package com.su.search.controller.v1;

import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.search.dtos.HistorySearchDto;
import com.su.search.service.ApUserSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/v1/history")
@RestController
public class ApUserSearchController {
    @Autowired
    private ApUserSearchService apUserSearchService;
    /**
     * 把搜索历史查询出来
     */
    @PostMapping(value="/load")
    public ResponseResult load(){
        return apUserSearchService.load();
    }

    @PostMapping(value = "/del")
    public ResponseResult del(@RequestBody HistorySearchDto dto){
        return apUserSearchService.del(dto);
    }
}
