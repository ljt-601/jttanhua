package com.itheima.dubbo.api;

import com.itheima.dubbo.pojo.RecommendUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class RecommendUserApiImplTest {
    @Autowired
    private RecommendUserApi recommendUserApi;
    @Test
    public void queryWithMaxScore() {
    RecommendUser recommendUser = this.recommendUserApi.queryWithMaxScore(1L);
        System.out.println(recommendUser);
}

    @Test
    public void queryPageInfo() {
        System.out.println(this.recommendUserApi.queryPageInfo(1L, 1, 5));
        System.out.println(this.recommendUserApi.queryPageInfo(1L, 2, 5));
        System.out.println(this.recommendUserApi.queryPageInfo(1L, 3, 5));

    }
}