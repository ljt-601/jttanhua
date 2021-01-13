package com.itheima.dubbo.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.dubbo.pojo.Users;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Service(version = "1.0.0")
public class UserApiImpl implements UsersApi{

    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    //保存好友信息
    public String saveUsers(Users users) {
        //校验是否为空
        if (users.getFriendId() == null || users.getUserId() == null) {
            return null;
        }
        //判断好友关系是否已经存在
        Users oldUsers = this.mongoTemplate.findOne(Query.query(Criteria.where("userId").is(users.getUserId()).and("friendId").is(users.getFriendId())), Users.class);
        if (null!=oldUsers){
            //已存在好友关系
            return null;
        }
        //没有的加入进去
        Long userId = users.getUserId();
        Long friendId = users.getFriendId();
        //注册我与好友的关系
        this.mongoTemplate.save(users);

        //注册好友与我的关系
        users.setId(ObjectId.get());
        users.setUserId(friendId);
        users.setFriendId(userId);
        this.mongoTemplate.save(users);
        //返回好友的id
        return users.getId().toHexString();
    }

    @Override
    public boolean removeUsers(Users users) {
        Long userId = users.getUserId();
        Long friendId = users.getFriendId();

        Query query1 = Query.query(Criteria.where("userId").is(userId)
                .and("friendId").is(friendId));

        //删除我与好友的关系数据
        long count1 = this.mongoTemplate.remove(query1, Users.class).getDeletedCount();

        Query query2 = Query.query(Criteria.where("userId").is(friendId)
                .and("friendId").is(userId));
        //删除好友与我的关系数据
        long count2 = this.mongoTemplate.remove(query2, Users.class).getDeletedCount();

        return count1 > 0 && count2 > 0;
    }
}
