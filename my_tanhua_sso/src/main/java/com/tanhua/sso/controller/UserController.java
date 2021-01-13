package com.tanhua.sso.controller;

import com.tanhua.sso.pojo.User;
import com.tanhua.sso.service.UserService;
import com.tanhua.sso.vo.ErrorResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("user")
@RestController
public class UserController {

    @Autowired
    private UserService userService;
    @PostMapping("loginVerification")
    public ResponseEntity<Object> login(@RequestBody Map<String,String> param){
        try {
            //从请求体中拿到用户电话和验证码
            String mobile = param.get("phone");
            String code = param.get("verificationCode");
            //调用逻辑层判断登录是否成功，登录成功返回token,登录失败返回null
            String token=this.userService.login(mobile,code);
            if (StringUtils.isNotEmpty(token)){
                //说明用户登陆成功了
                String[] ss = StringUtils.split(token,"|");
                Boolean isNew = Boolean.valueOf(ss[0]);
                String tokenStr=ss[1];

                Map<String,Object> result=new HashMap<>();
                result.put("isNew",isNew);
                result.put("token",tokenStr);

                return ResponseEntity.ok(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //登陆失败后返回Error对象
        ErrorResult.ErrorResultBuilder builder = ErrorResult.builder().errCode("000000").errMessage("登录失败");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(builder.build());
    }
    //检查登陆状态
    @GetMapping("{token}")
    public User queryUserByToken(@PathVariable("token") String token) {
        return this.userService.queryUserByToken(token);
    }


}
