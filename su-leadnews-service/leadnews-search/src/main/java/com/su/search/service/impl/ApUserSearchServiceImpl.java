package com.su.search.service.impl;

import com.mongodb.client.result.DeleteResult;
import com.su.common.util.RequestContextUtil;
import com.su.model.common.dtos.ResponseResult;
import com.su.model.common.search.dtos.HistorySearchDto;
import com.su.search.doc.ApUserSearch;
import com.su.search.service.ApUserSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApUserSearchServiceImpl implements ApUserSearchService {
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 把搜索历史查询出来mongodb
     */
    @Override
    public ResponseResult load() {
        Integer userId = RequestContextUtil.get("id");
        if(userId!=0){
            //用户登录了
            Query query = Query.query(Criteria.where("userId").is(userId));
            query.with(Sort.by(Sort.Direction.DESC,"createdTime"));
            query.limit(5);
            List<ApUserSearch> apUserSearches = mongoTemplate.find(query, ApUserSearch.class);
            return ResponseResult.okResult(apUserSearches);
        }
        return ResponseResult.okResult(null);

    }

    @Override
    public ResponseResult del(HistorySearchDto dto) {
        Integer userId = RequestContextUtil.get("id");
        Query query = Query.query(Criteria.where("userId").is(userId).and("id").is(dto.getId()));

        DeleteResult remove = mongoTemplate.remove(query, ApUserSearch.class);
        boolean b = remove.wasAcknowledged();//删除成功返回true
        return ResponseResult.okResult(null);
    }
}
