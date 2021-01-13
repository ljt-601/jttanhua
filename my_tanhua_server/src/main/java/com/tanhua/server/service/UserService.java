package com.tanhua.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.server.pojo.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class UserService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${tanhua.sso.url}")
    private String url;
    //用户将json数据反序列化
    private static final ObjectMapper MAPPER=new ObjectMapper();

    //通过http调用sso系统中的查询token
    public User queryUserByToken(String token){
        //通过远程调用查询到json,反序列化后返回User对象,不然返回null
        String jsonData = this.restTemplate.getForObject(url + "/user/" + token, String.class);
        if (StringUtils.isNotEmpty(jsonData)){
            try {
                return MAPPER.readValue(jsonData,User.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;

    }
}
