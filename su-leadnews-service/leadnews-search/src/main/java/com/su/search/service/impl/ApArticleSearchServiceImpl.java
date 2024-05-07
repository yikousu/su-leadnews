package com.su.search.service.impl;

import com.su.common.util.RequestContextUtil;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.search.dtos.UserSearchDto;
import com.su.search.doc.ApUserSearch;
import com.su.search.service.ApArticleSearchService;
import com.su.utils.common.MD5Utils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ApArticleSearchServiceImpl implements ApArticleSearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private MongoTemplate mongoTemplate;

    private static ExecutorService pool = Executors.newFixedThreadPool(20);


    /**
     * 实现搜索关键字历史记录保存
     */
    public void saveHistory(String keyword, Integer userId) {
        //为了不影响搜索  开个线程慢慢执行
        pool.execute(() -> {
            //封装ApUserSearch对象
            ApUserSearch apUserSearch = new ApUserSearch();
            apUserSearch.setUserId(userId);
            apUserSearch.setKeyword(keyword);
            apUserSearch.setCreatedTime(new Date());

            //ID MD5(xxx) 明文相同，MD5值一定相同，根据这个特性确定相同用户搜索 相同关键词的时候，ID唯一
            //利用mongodb中save特性
            String id = MD5Utils.encode(userId + "_" + keyword);
            apUserSearch.setId(id);

            //2利用mongoTemplate进行保存
            mongoTemplate.save(apUserSearch);
        });
    }


    /**
     * 实现搜索
     * 1. 搜出来
     * 2. 高亮
     * 3. 封装返回结果
     */
    @Override
    public ResponseResult search(UserSearchDto dto) throws IOException {

        //1)创建SearchRequest
        SearchRequest request = new SearchRequest("hmtt");//索引库一定要指定

        //2.1)封装询条件 如果搜索关健词不为空，则根据标题或者内容搜索
        if (!ObjectUtils.isEmpty(dto.getSearchWords())) {
            //关键词不空  用户登录了   则保存这个关键词历史
            Integer userId = RequestContextUtil.get("id");
            if(userId!=0){
                saveHistory(dto.getSearchWords(),userId);
            }
            request.source().query(QueryBuilders.multiMatchQuery(dto.getSearchWords(), "title", "content"));
        } else {
            //2.2)如果搜索关键词为空，则查询所有
            request.source().query(QueryBuilders.matchAllQuery());
        }

        //3)高亮
        request.source().highlighter(
                new HighlightBuilder()
                        .field("title")
                        .preTags("<font style='color:red;font-size:inherit;'>")
                        .postTags("</font>")
        );

        //4)分页
        int index = (dto.getPageNum()) * dto.getPageSize();
        request.source().from(index).size(dto.getPageSize());

        //5)排序
        request.source().sort("publishTime", SortOrder.DESC);

        //6)结果集解析
        //查询
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        //解析结果
        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> map = hit.getSourceAsMap();
            map.put("h_title", map.get("title"));

            if (hit.getHighlightFields() != null && hit.getHighlightFields().get("title") != null) {
                HighlightField highlightField = hit.getHighlightFields().get("title");
                String highlightTitle = StringUtils.join(highlightField.getFragments(), "..");
                map.put("h_title", highlightTitle);
            }
            list.add(map);
        }

        return ResponseResult.okResult(list);
    }
}