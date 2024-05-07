package com.su.search.config;

import com.alibaba.fastjson.JSON;
import com.su.model.common.search.vos.SearchArticleVo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.IOException;

/**
 * ES增量同步
 * 文章修改【增加 删除】
 */
@Component
public class SyncArticleToESIndex {
    @Autowired
    private RestHighLevelClient restHighLevelClient;


    @KafkaListener(topics = "article_sync_es_topic")
    public void listener(ConsumerRecord<String, String> record) throws IOException {
        String message = record.value();

        if (!ObjectUtils.isEmpty(message)) {
            SearchArticleVo vo = JSON.parseObject(message, SearchArticleVo.class);
            if (vo.getEnable() == 1) {//上架  则增加到ES索引中     增加
                addIndex(vo);
            } else {//删除
                deleteIndex(vo);
            }
        }
    }

    /**
     * 删除索引
     *
     * @param vo
     */
    public void deleteIndex(SearchArticleVo vo) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest().id(vo.getId().toString());
        restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);

    }

    /**
     * 增加索引
     *
     * @param vo
     */

    public void addIndex(SearchArticleVo vo) throws IOException {
        IndexRequest request = new IndexRequest("hmtt")
                .id(vo.getId().toString())
                .source(JSON.toJSONString(vo), XContentType.JSON);
        restHighLevelClient.index(request, RequestOptions.DEFAULT);

    }
}
