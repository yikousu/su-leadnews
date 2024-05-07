package com.su.schedule.service;

import com.su.model.common.schedule.dtos.Task;

public interface TaskService {
    /**
     * 添加任务
     * @param task
     * @return
     */
    public long addTask(Task task);

    /**
     * 取消任务
     * @param taskId
     * @return
     */
    public boolean cancelTask(long taskId);

    /**
     * 拉去任务
     * @param type
     * @param priority
     * @return
     */
    public Task poll(int type,int priority);
}
