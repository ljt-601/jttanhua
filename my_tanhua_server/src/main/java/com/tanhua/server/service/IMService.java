package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.dubbo.api.UsersApi;
import com.itheima.dubbo.pojo.Users;
import com.tanhua.server.pojo.User;
import com.tanhua.server.utils.UserThreadLocal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IMService {

    @Reference(version = "1.0.0")
    private UsersApi usersApi;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${tanhua.sso.url}")
    private String url;

    public boolean contactUser(Long userId) {
        //保存数据到MongoDB
        User user = UserThreadLocal.get();

        Users users = new Users();
        users.setUserId(user.getId());
        users.setFriendId(userId);

        String id = this.usersApi.saveUsers(users);

        if (StringUtils.isNotEmpty(id)) {
            //注册好友关系到环信
            try {
                String targetUrl = url + "/user/huanxin/contacts/" + users.getUserId() + "/" + users.getFriendId();
                ResponseEntity<Void> responseEntity = this.restTemplate.postForEntity(targetUrl, null, Void.class);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    return true;
                }
            } catch (Exception e) {
                //添加好友失败，删除Mongodb中的好友数据
                this.usersApi.removeUsers(users);

               // log.error("添加环信好友失败！userId = "+ user.getId()+", friendId = " + userId);
            }
            return false;
        }

        return false;
    }


}
