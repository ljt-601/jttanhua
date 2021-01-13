package com.tanhua.server.controller;

import com.tanhua.server.pojo.User;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.server.vo.HuanXinUser;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("huanxin")
//app通过该接口就可以登录到环信，就可以进行消息发送
public class HuanXinController {

    /**
     * 查询环信用户信息
     * @return
     */
    @GetMapping("user")
    public ResponseEntity<HuanXinUser> queryUser(){
        //思路：1.拿到当前登录用户
        User user = UserThreadLocal.get();
        //2.和给环信上注册用户时的信息保持一致，直接利用当前用户id创建
        HuanXinUser huanXinUser = new HuanXinUser();
        huanXinUser.setUsername(user.getId().toString());
        huanXinUser.setPassword(DigestUtils.md5Hex(user.getId() + "_itcast_tanhua"));
        return ResponseEntity.ok(huanXinUser);

    }
}
