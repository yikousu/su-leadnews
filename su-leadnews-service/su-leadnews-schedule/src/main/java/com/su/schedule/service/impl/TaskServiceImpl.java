package com.su.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.su.common.constants.ScheduleConstants;
import com.su.common.redis.CacheService;
import com.su.model.common.schedule.dtos.Task;
import com.su.model.common.schedule.pojos.Taskinfo;
import com.su.model.common.schedule.pojos.TaskinfoLogs;
import com.su.schedule.mapper.TaskinfoLogsMapper;
import com.su.schedule.mapper.TaskinfoMapper;
import com.su.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;


@Transactional
@Slf4j
@Service
public class TaskServiceImpl implements TaskService {


    @Autowired
    private TaskinfoMapper taskinfoMapper;
    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;
    @Autowired
    private CacheService cacheService;
    //万一这个服务启动多个就会出现问题

    /**
     * 数据库中数据定时同步到redis中
     */
    @PostConstruct //当对象被实例化并完成依赖注入后，注解标记的方法将被自动调用。  服务重启了自动重新执行这个方法
    @Scheduled(cron = "0 */5 * * * ?")
    public void reloadData() {
        //清理缓存任务【防止小于5min的任务重复】因为执行了的任务在数据库中会被删除 没执行的还在  所以删缓存没影响
        clearCache();
        //2去数据库中查询小于5min的数据
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE,5);
        Date time = calendar.getTime();

        List<Taskinfo> taskinfoList = taskinfoMapper.selectList(Wrappers.<Taskinfo>lambdaQuery().lt(Taskinfo::getExecuteTime, time));
        // 3添加到zset中
        if(taskinfoList!=null && taskinfoList.size()>0){
            for (Taskinfo taskinfo : taskinfoList) {
                Task task = new Task();
                task.setTaskType(taskinfo.getTaskType());
                task.setParameters(taskinfo.getParameters());
                task.setPriority(taskinfo.getPriority());
                task.setTaskId(taskinfo.getTaskId());
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());

                addTaskToCache(task);
            }
        }
        log.info("数据同步到redis中");

    }

    /**
     * 清理缓存
     */
    public void clearCache() {
        //查询全部键  根据键删除list zset中数据
        Set<String> topicKeys = cacheService.scan(ScheduleConstants.TOPIC + "*");
        Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");

        cacheService.delete(topicKeys);
        cacheService.delete(futureKeys);
    }

    /**
     * 每分钟去zset中吧某些数据同步到list
     */
    @Scheduled(cron = "0 */1 * * * ?")//指定时间 每过时间t执行一次任务
    public void refresh() {

        //是Spring框架中用于指定定时任务执行时间的注解
        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);
        //token有值表示获取到了key 所以应执行下面代码
        if (StringUtils.isNotBlank(token)) {
            System.out.println("定时任务============开始");

            //取key
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
            for (String futureKey : futureKeys) {
                String topicKey = ScheduleConstants.TOPIC + futureKey.split(ScheduleConstants.FUTURE)[1];
                //取value
                //tasks 是一个集合，包含了要添加到 List 中并从 ZSet 中移除的元素。
                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());
                if (!tasks.isEmpty()) {
                    cacheService.refreshWithPipeline(futureKey, topicKey, tasks);
                    System.out.println("定时任务============结束");
                }
            }
        }
    }

    /**
     * 根据类型and优先级去list拉去任务
     * 执行忍任务
     */
    @Override
    public Task poll(int type, int priority) {
        Task task = null;
        try {
            String key = type + "_" + priority;
            //1从redis中pop
            String task_json = cacheService.lRightPop(ScheduleConstants.TOPIC + key);
            if (StringUtils.isNotBlank(task_json)) {
                task = JSON.parseObject(task_json, Task.class);
                //2修改数据库信息
                updateDb(task.getTaskId(), ScheduleConstants.EXECUTED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("poll task exception");
        }
        return task;
    }


    /**
     * 添加任务
     *
     * @param task
     * @return
     */
    @Override
    public long addTask(Task task) {
        // 1添加到数据库
        log.info("=====>数据添加到数据库");
        boolean success = addTaskToDb(task);
        if (success) {
            // 2添加到redis
            log.info("=====>数据添加到redis");
            addTaskToCache(task);
        }
        return task.getTaskId();
    }

    /**
     * 取消任务
     * @param taskId
     * @return
     */
    @Override
    public boolean cancelTask(long taskId) {
        boolean flag = false;
        //1删除任务 更新日志【操作数据库】
        Task task = updateDb(taskId, ScheduleConstants.CANCELLED);
        //2删除redis中数据
        if (task != null) {
            removeTaskFromCache(task);
            flag = true;
        }
        return flag;
    }

    /**
     * 把任务从redis中移除
     *
     * @param task
     */

    private void removeTaskFromCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lRemove(ScheduleConstants.TOPIC + key, 0, JSON.toJSONString(task));
        } else {
            cacheService.zRemove(ScheduleConstants.FUTURE + key, JSON.toJSONString(task));
        }
    }

    /**
     * 删除数据库中数据
     *
     * @param taskId
     * @param status
     * @return
     */
    private Task updateDb(long taskId, int status) {
        Task task = null;
        try {
            //删除任务
            taskinfoMapper.deleteById(taskId);

            //更新任务日志
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);

            task = new Task();
            task.setTaskId(taskId);
            task.setTaskType(taskinfoLogs.getTaskType());
            task.setParameters(taskinfoLogs.getParameters());
            task.setPriority(taskinfoLogs.getPriority());
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());

        } catch (Exception e) {
            log.error("task cancel exception taskId={}", taskId);
        }

        return task;
    }


    /**
     * 把任务添加到redis中
     *
     * @param task
     */

    private void addTaskToCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        long nextScheduleTime = calendar.getTimeInMillis();

        //2.1 如果任务的执行时间小于等于当前时间，存入list
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lLeftPush(ScheduleConstants.TOPIC + key, JSON.toJSONString(task));
        } else if (task.getExecuteTime() <= nextScheduleTime) {
            //2.2 如果任务的执行时间大于当前时间 && 小于等于预设时间（未来5分钟） 存入zset中
            cacheService.zAdd(ScheduleConstants.FUTURE + key, JSON.toJSONString(task), task.getExecuteTime());
        }


    }

    /**
     * 把任务添加到数据库中
     *
     * @param task
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private boolean addTaskToDb(Task task) {
        boolean flag = false;
        try {
            // 1添加任务表
            Taskinfo taskinfo = new Taskinfo();
//            BeanUtils.copyProperties(task, taskinfo);
            taskinfo.setTaskType(task.getTaskType());
            taskinfo.setParameters(task.getParameters());
            taskinfo.setPriority(task.getPriority());
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));

            taskinfoMapper.insert(taskinfo);

            task.setTaskId(taskinfo.getTaskId());

            // 2添加任务日志数据
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            /**
             * 注意用BeanUtils.copyProperties拷贝时候 变量名相同但是类型不同不可以拷贝
             * 否则出错
             */
//            BeanUtils.copyProperties(taskinfo,taskinfoLogs);
            taskinfoLogs.setTaskId(taskinfo.getTaskId());
            taskinfoLogs.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoLogs.setTaskType(task.getTaskType());
            taskinfoLogs.setParameters(task.getParameters());
            taskinfoLogs.setPriority(task.getPriority());
            taskinfoLogs.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoLogs.setVersion(1);
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
            taskinfoLogsMapper.insert(taskinfoLogs);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
}
