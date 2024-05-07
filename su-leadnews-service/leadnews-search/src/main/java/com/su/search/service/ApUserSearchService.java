package com.su.search.service;

import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.search.dtos.HistorySearchDto;

public interface ApUserSearchService {
    /**
     * 把搜索历史查询出来
     */
    ResponseResult load();

    ResponseResult del(HistorySearchDto dto);
}
