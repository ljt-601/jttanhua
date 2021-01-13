package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.server.mapper.UserInfoMapper;
import com.tanhua.server.pojo.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.convert.QueryMapper;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;
    //查询数据库，查找用户信息
    public UserInfo queryUserInfoByID(Long id) {
        UserInfo userInfo = this.userInfoMapper.selectById(id);
        return userInfo;
    }

    public List<UserInfo> queryUserInfoList(QueryWrapper queryWapper) {
        return this.userInfoMapper.selectList(queryWapper);

    }
}
