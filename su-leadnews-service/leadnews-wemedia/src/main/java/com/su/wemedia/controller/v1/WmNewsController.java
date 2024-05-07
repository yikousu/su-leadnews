package com.su.wemedia.controller.v1;

import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.wemedia.dtos.WmNewsDto;
import com.su.model.common.wemedia.dtos.WmNewsPageReqDto;
import com.su.model.common.wemedia.dtos.WmnewsStatusDto;
import com.su.model.common.wemedia.pojos.WmNews;
import com.su.wemedia.service.WmNewsService;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@RestController
@RequestMapping(value="/api/v1/news")
public class WmNewsController {
    @Autowired
    private WmNewsService wmNewsService;
    /**
     * 上下架文章
     * 1. 修改wmnews数据库
     * 2. 发送给kafka
     * 3. 消费数据
     * 4. 更新article——config
     */
    @PostMapping(value="/down_or_up")
    public ResponseResult downUp(@RequestBody WmnewsStatusDto dto){
        ResponseResult responseResult = wmNewsService.downUp(dto);
        return responseResult;
    }

    /**
     * 文章列表查询
     * @return
     */
    @PostMapping(value = "list")
    public ResponseResult newsList(@RequestBody WmNewsPageReqDto dto){
        return wmNewsService.newsList(dto);
    }

    /**
     * 添加发布
     */
    @PostMapping(value = "submit")
    public ResponseResult submit(@RequestBody WmNewsDto dto) throws TesseractException, IOException, InvocationTargetException, IllegalAccessException {
        return wmNewsService.submit(dto);
    }

    /**
     * 根据id查询文章信息
     * 因为更改的适合先获取数据  查询一条数据很简单
     */
    @GetMapping("/one/{id}")
    public ResponseResult one(@PathVariable(value="id")Integer id){
        WmNews one = wmNewsService.getById(id);
        return ResponseResult.okResult(one);
    }

    /**
     * 文章删除
     * 1. 删内容列表news
     * 2. 删除srtcle
     */
    @GetMapping(value="/del_news/{id}")
    public ResponseResult del_news(@PathVariable("id") int id){
        wmNewsService.del_news(id);
        return ResponseResult.okResult("删除成功");
    }

}
