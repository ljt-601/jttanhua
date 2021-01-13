package com.tanhua.sso.service;

import com.tanhua.sso.pojo.UserInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UserInfoServiceTest {
    @Autowired
    private UserInfoService userInfoService;

    @Test
    public void test(){
        UserInfo userInfo = this.userInfoService.queryUserInfoByID(1L);
        System.out.println(userInfo);
    }
}