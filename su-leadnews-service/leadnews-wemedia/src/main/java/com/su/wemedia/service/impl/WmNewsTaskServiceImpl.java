package com.su.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.su.feign.schedule.IScheduleClient;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.enums.TaskTypeEnum;
import com.su.model.common.schedule.dtos.Task;
import com.su.model.common.wemedia.pojos.WmNews;
import com.su.utils.common.ProtostuffUtil;
import com.su.wemedia.service.WmNewsAutoScanService;
import com.su.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

@Service
@Slf4j
public class WmNewsTaskServiceImpl implements WmNewsTaskService {
    @Autowired
    private IScheduleClient scheduleClient;
    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    /**
     * 消费任务  审核文章
     */
    @Override
    @Scheduled(fixedRate = 1000)//每一秒去拉取一次
    public void scanNewsByTask() throws TesseractException, IOException {
        log.info("消费任务，审核文章");
        ResponseResult responseResult = scheduleClient.poll(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType(), TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        Integer code = responseResult.getCode();
        Object data = responseResult.getData();
        if (code.equals(200) && data != null) {
            // 将响应数据转换为任务对象
            Task task = JSON.parseObject(JSON.toJSONString(responseResult.getData()), Task.class);

            // 从任务中提取参数并反序列化为 WmNews 对象
            WmNews wmNews = ProtostuffUtil.deserialize(task.getParameters(), WmNews.class);
            //审核
            wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        }
    }


    /**
     * wemedia发布文章然后调用schedule定时发布【此刻未来发布 都给schedule】
     * 添加任务到延时任务中
     */
    @Override
    @Async//因为发布文章要异步调用  因为不可能用户发文章等5mins才结束本次发布文章
    public void addNewsToTask(Integer id, Date publishTime) {
        log.info("添加任务到延时服务中===============begin");
        Task task = new Task();
        task.setExecuteTime(publishTime.getTime());//！！！！！！
        //下面两个必备 因为定时任务操作redis需要组合成key
        task.setTaskType(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType());
        task.setPriority(TaskTypeEnum.NEWS_SCAN_TIME.getPriority());

        WmNews wmNews = new WmNews();
        wmNews.setId(id);
        task.setParameters(ProtostuffUtil.serialize(wmNews));

        scheduleClient.addTask(task);
        log.info("添加任务到延时服务中=================end");

    }
}
