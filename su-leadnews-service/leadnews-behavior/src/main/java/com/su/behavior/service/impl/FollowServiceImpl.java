package com.su.behavior.service.impl;

import com.su.behavior.doc.ApFanBehavior;
import com.su.behavior.doc.ApFollowBehavior;
import com.su.behavior.service.FollowService;
import com.su.common.util.RequestContextUtil;
import com.su.model.common.behavior.dtos.FollowBehaviorDto;
import com.su.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
@Service
public class FollowServiceImpl implements FollowService {
    @Autowired
    private MongoTemplate mongoTemplate;
    /**
     * 关注 取关
     * @param dto
     * @return
     */
    @Override
    public ResponseResult userFollow(FollowBehaviorDto dto) {
        //从当前线程中获取用户自己的id
        Integer userId = RequestContextUtil.get("id");
        System.out.println("userId:"+userId);
        if(dto.getOperation()==0){
            ApFollowBehavior apFollowBehavior = new ApFollowBehavior();
            ////防止一下点两次  所以可以把id写死 因为id同  mongo不会继续加
            apFollowBehavior.setId(userId+"-hmtt-"+dto.getAuthorId());
            apFollowBehavior.setUserId(Long.valueOf(userId));
            apFollowBehavior.setFollowId(dto.getAuthorId());
            apFollowBehavior.setCreateTime(new Date());
            mongoTemplate.save(apFollowBehavior);

            ApFanBehavior apFanBehavior = new ApFanBehavior();
            apFanBehavior.setUserId(Long.valueOf(dto.getAuthorId()));
            apFanBehavior.setId(dto.getAuthorId()+"-hmtt-"+userId);
            apFanBehavior.setFanId(userId);
            apFanBehavior.setCreateTime(new Date());
            mongoTemplate.save(apFanBehavior);
            System.out.println("存入成功！！！！！！！！！！！");

        }else{
            Query followQuery = Query.query(Criteria
                    .where("followId").is(dto.getAuthorId())
                    .and("userId").is(userId));
            mongoTemplate.remove(followQuery,ApFollowBehavior.class);

            Query fanQuery = Query.query(Criteria
                   .where("fanId").is(userId)
                   .and("userId").is(dto.getAuthorId()));
            mongoTemplate.remove(fanQuery,ApFanBehavior.class);
            System.out.println("删除成功！！！！！！！！！！！");

        }

        return ResponseResult.okResult(null);

    }
}
