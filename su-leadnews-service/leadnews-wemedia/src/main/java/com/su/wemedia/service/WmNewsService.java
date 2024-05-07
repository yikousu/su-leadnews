package com.su.wemedia.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.wemedia.dtos.WmNewsDto;
import com.su.model.common.wemedia.dtos.WmNewsPageReqDto;
import com.su.model.common.wemedia.dtos.WmnewsStatusDto;
import com.su.model.common.wemedia.pojos.WmNews;
import net.sourceforge.tess4j.TesseractException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public interface WmNewsService extends IService<WmNews> {


    ResponseResult newsList(WmNewsPageReqDto dto);

    ResponseResult submit(WmNewsDto dto) throws InvocationTargetException, IllegalAccessException, IOException, TesseractException;

    void del_news(int id);

    ResponseResult downUp(WmnewsStatusDto dto);

}
