package com.tanhua.server.controller;

import com.tanhua.server.service.TodayBestService;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.RecommendUserQueryParam;
import com.tanhua.server.vo.TodayBest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("tanhua")
public class TodayBestController {
    @Autowired
    private TodayBestService todayBestService;

    @GetMapping("todayBest")
    @Cacheable(value = "todayBest")
    public TodayBest queryTodayBest(){

        System.out.println("我走数据库了，没走缓存");
        return this.todayBestService.queryTodayBest();
    }
    @GetMapping("recommendation")
    @Cacheable(value = "recommendation")
    public PageResult queryRecommendUserList(RecommendUserQueryParam queryParam ){
        return this.todayBestService.queryRecommendUserList(queryParam);
    }
}
