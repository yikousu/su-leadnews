package com.su.wemedia.service;

import net.sourceforge.tess4j.TesseractException;

import java.io.IOException;
import java.util.Date;

/**
 * wm_news -> schedule -> wm_article
 * 添加任务到延时任务中
 * 从延时任务中取数据
 */



public interface WmNewsTaskService {
    /**
     * 添加任务到延时任务中
     */
    public void addNewsToTask(Integer id, Date publishTime);

    /**
     * 消费任务  审核文章
     */
    public void scanNewsByTask() throws TesseractException, IOException;
}
