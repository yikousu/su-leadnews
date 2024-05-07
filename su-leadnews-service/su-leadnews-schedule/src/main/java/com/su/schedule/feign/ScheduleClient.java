package com.su.schedule.feign;

import com.su.feign.schedule.IScheduleClient;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.schedule.dtos.Task;
import com.su.schedule.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ScheduleClient implements IScheduleClient {
    @Autowired
    private TaskService taskService;
    @Override
    @PostMapping("/api/v1/task/add")
    public ResponseResult addTask(@RequestBody Task task) {
        return ResponseResult.okResult(taskService.addTask(task));
    }

    @GetMapping("/api/v1/task/{taskId}")
    public ResponseResult cancelTask(@PathVariable("taskId") long taskId) {
        return ResponseResult.okResult(taskService.cancelTask(taskId));
    }

    @Override
    @GetMapping("/api/v1/task/{type}/{priority}")
    public ResponseResult poll(@PathVariable("type") int type, @PathVariable("priority") int priority) {
        return ResponseResult.okResult(taskService.poll(type,priority));
    }
}



