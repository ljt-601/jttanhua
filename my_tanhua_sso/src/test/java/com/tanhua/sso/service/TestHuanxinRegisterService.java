package com.tanhua.sso.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestHuanxinRegisterService {
    @Autowired
    private HuanXinService huanxinService;

    @Test
    public void testHuanxinService() {
        System.out.println(this.huanxinService.register(10L));
    }
}

