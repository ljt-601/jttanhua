package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.dubbo.api.RecommendUserApi;
import com.itheima.dubbo.pojo.RecommendUser;
import com.itheima.dubbo.vo.PageInfo;
import com.tanhua.server.vo.TodayBest;
import org.springframework.stereotype.Service;

@Service
public class RecommendUserService {
    @Reference(version = "1.0.0")
    private RecommendUserApi recommendUserApi;

    public TodayBest queryTodayBest(Long userId){
        //远程调用，查到被推荐的人
        RecommendUser recommendUser = this.recommendUserApi.queryWithMaxScore(userId);
        if (null==recommendUser){
            return null;
        }
        //查到后转为TodayBest类型的再返回
        TodayBest todayBest = new TodayBest();
        //将得分直接向下取整
        double score = Math.floor(recommendUser.getScore());
        //实体类中要的是long类型的缘分值，所以要转
        todayBest.setFateValue(Double.valueOf(score).longValue());

        todayBest.setId(recommendUser.getUserId()); //用户id
        return todayBest;

    }

    public PageInfo<RecommendUser> queryRecommendUserList(Long id, Integer page, Integer pagesize) {
        return this.recommendUserApi.queryPageInfo(id, page, pagesize);
    }
}
