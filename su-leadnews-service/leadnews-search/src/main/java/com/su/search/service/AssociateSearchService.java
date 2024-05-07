package com.su.search.service;

import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.search.dtos.UserSearchDto;

import java.io.IOException;

public interface AssociateSearchService {
    ResponseResult search(UserSearchDto dto) throws IOException;

}
