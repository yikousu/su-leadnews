package com.su.user.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.su.model.common.user.pojos.ApUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApUserMapper extends BaseMapper<ApUser> {
}
