package com.su.search.service.impl;

import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.search.dtos.UserSearchDto;
import com.su.search.service.AssociateSearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssociateSearchServiceImpl implements AssociateSearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 单词自动补全     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult search(UserSearchDto dto) throws IOException {
        //1)新建一个SearchRequest
        SearchRequest request = new SearchRequest("hmtt");
        //2)创建一个单词自动补全配置 Suggest，给它取个别名
        //3)搜索的前缀、搜索的字段
        request.source().suggest(new SuggestBuilder().addSuggestion(
                //别名
                "hmtt_suggest",
                SuggestBuilders   //建议查询构建器类 提供多种方法
                        .completionSuggestion("suggestion")
                        .prefix(dto.getSearchWords())
                        .skipDuplicates(true)
                        .size(10)
        ));

        //4)执行搜索
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        //5)解析结果集
        CompletionSuggestion suggests = response.getSuggest().getSuggestion("hmtt_suggest");
        //屈服于前端
        List<Map<String,String>> options = new ArrayList<>();
        for (CompletionSuggestion.Entry.Option option : suggests.getOptions()) {
            HashMap<String, String> map = new HashMap<>();
            map.put("associateWords",option.getText().toString());
            options.add(map);
        }
        return ResponseResult.okResult(options);
    }
}
