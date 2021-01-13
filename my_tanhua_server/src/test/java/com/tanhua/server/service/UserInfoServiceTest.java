package com.tanhua.server.service;

import com.tanhua.server.mapper.UserInfoMapper;
import com.tanhua.server.pojo.UserInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserInfoServiceTest {
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Test
    public void queryUserInfoByID() {
        UserInfo userInfo = this.userInfoMapper.selectById(1L);
        System.out.println(userInfo);

    }
}