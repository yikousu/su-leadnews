package com.su.feign.schedule;

import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.schedule.dtos.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("leadnews-schedule")
public interface IScheduleClient {
    @PostMapping("/api/v1/task/add")
    public ResponseResult addTask(@RequestBody Task task);

    @GetMapping("/api/v1/task/{taskId}")
    public ResponseResult cancelTask(@PathVariable("taskId") long taskId);

    @GetMapping("/api/v1/task/{type}/{priority}")
    public ResponseResult poll(@PathVariable("type") int type, @PathVariable("priority") int priority);
}
