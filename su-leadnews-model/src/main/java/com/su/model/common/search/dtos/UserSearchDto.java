package com.su.model.common.search.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class UserSearchDto {
    private String searchWords;
    private int pageNum;
    private int pageSize;
    private Date minBehotTime;
}
