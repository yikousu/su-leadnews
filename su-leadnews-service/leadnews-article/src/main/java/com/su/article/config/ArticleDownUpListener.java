package com.su.article.config;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.su.article.mapper.ApArticleConfigMapper;
import com.su.article.service.ApArticleService;
import com.su.model.common.article.pojos.ApArticleConfig;
import com.su.model.common.wemedia.dtos.WmnewsStatusDto;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


/**
 * kafka上下架文章
 */
@Component
public class ArticleDownUpListener {
    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;
    @Autowired
    private ApArticleService apArticleService;

    @KafkaListener(topics = "Article_DownUp", groupId = "downup_group")
    public void listen(ConsumerRecord<String, String> record) {
        //监听可以抽取为service
        //因为是string类型序列化
        String value = record.value();
        WmnewsStatusDto dto = JSON.parseObject(value, WmnewsStatusDto.class);

        ApArticleConfig apArticleConfig = new ApArticleConfig();
        apArticleConfig.setIsDown(dto.getEnable() == 1 ? true : false);


        //wrapper就是限制条件
        UpdateWrapper<ApArticleConfig> wrapper = new UpdateWrapper<>();
        wrapper.eq("article_id",dto.getArticleId());

        apArticleConfigMapper.update(apArticleConfig,wrapper);

        //上下架发送消息给ES  创建or删除索引
        apArticleService.creatArticleESIndex(dto.getArticleId(),dto.getEnable());
    }

}
