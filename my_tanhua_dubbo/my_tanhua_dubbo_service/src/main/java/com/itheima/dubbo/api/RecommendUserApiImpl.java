package com.itheima.dubbo.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.dubbo.pojo.RecommendUser;
import com.itheima.dubbo.vo.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
@Service(version = "1.0.0")
public class RecommendUserApiImpl implements RecommendUserApi {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    //今日佳人  //在mongodb中查询推荐的佳人
    public RecommendUser queryWithMaxScore(Long userId) {
        //根据用户id构建查询条件
        Criteria criteria = Criteria.where("toUserId").is(userId);
        //查到后按照推荐分数倒序排列，获取第一条数据
        Query query = Query.query(criteria).with(Sort.by(Sort.Order.desc("score"))).limit(1);
        return this.mongoTemplate.findOne(query,RecommendUser.class) ;
    }


    @Override
    //推荐列表
    public PageInfo<RecommendUser> queryPageInfo(Long userId, Integer pageNum, Integer pageSize) {
        //根据用户id构建查询条件
        Criteria criteria = Criteria.where("toUserId").is(userId);
        //因为是查询列表，所以一定要用到分页,所以.with中只能用到参数为Pageable的
        Pageable pageable=PageRequest.of(pageNum-1,pageSize,Sort.by(Sort.Order.desc("score")));
        Query query = Query.query(criteria).with(pageable);

        return new PageInfo<>(0,pageNum,pageSize,this.mongoTemplate.find(query,RecommendUser.class));
    }
}
