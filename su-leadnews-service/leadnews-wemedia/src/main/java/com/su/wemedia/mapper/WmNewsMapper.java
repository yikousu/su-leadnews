package com.su.wemedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.su.model.common.wemedia.dtos.WmnewsStatusDto;
import com.su.model.common.wemedia.pojos.WmNews;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface WmNewsMapper  extends BaseMapper<WmNews> {

    int downUp(WmnewsStatusDto dto);
}
