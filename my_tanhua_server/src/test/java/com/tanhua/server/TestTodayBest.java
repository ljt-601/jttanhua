package com.tanhua.server;

import com.tanhua.server.service.TodayBestService;
import com.tanhua.server.vo.RecommendUserQueryParam;
import com.tanhua.server.vo.TodayBest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestTodayBest {

    @Autowired
    private TodayBestService todayBestService;
    @Test
    public void testQueryTodayBest(){
        String token="eyJhbGciOiJIUzI1NiJ9.eyJtb2JpbGUiOiIxMzQ4ODE4MDIzOCIsImlkIjoxfQ.LztL55ewCA93ODEfDC3QHqlmY4fmlEWZqllnT8PJmVA";
        TodayBest todayBest = this.todayBestService.queryTodayBest();
        System.out.println(todayBest);
    }
    @Test
    public void testQueryTodayBestList(){
        String token="eyJhbGciOiJIUzI1NiJ9.eyJtb2JpbGUiOiIxMzQ4ODE4MDIzOCIsImlkIjoxfQ.LztL55ewCA93ODEfDC3QHqlmY4fmlEWZqllnT8PJmVA";
        System.out.println(this.todayBestService.queryRecommendUserList(new RecommendUserQueryParam()));
    }
}
