package com.itheima.dubbo.api;

import com.itheima.dubbo.pojo.Users;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestUsers {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Test
    public void saveUsers(){
        this.mongoTemplate.save(new Users(ObjectId.get(),1L, 2L, System.currentTimeMillis()));
        this.mongoTemplate.save(new Users(ObjectId.get(),1L, 3L, System.currentTimeMillis()));
        this.mongoTemplate.save(new Users(ObjectId.get(),1L, 4L, System.currentTimeMillis()));
        this.mongoTemplate.save(new Users(ObjectId.get(),1L, 5L, System.currentTimeMillis()));
        this.mongoTemplate.save(new Users(ObjectId.get(),1L, 6L, System.currentTimeMillis()));
    }


}
