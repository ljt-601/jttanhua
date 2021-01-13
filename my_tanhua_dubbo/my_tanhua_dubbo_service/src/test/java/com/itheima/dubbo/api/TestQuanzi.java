package com.itheima.dubbo.api;

import com.itheima.dubbo.pojo.Publish;
import com.itheima.dubbo.pojo.TimeLine;
import com.itheima.dubbo.vo.PageInfo;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestQuanzi {
    @Autowired
    private QuanZiApi quanZiApi;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void testSavePublish(){
        Publish publish = new Publish();
        publish.setUserId(1L);
        publish.setLocationName("海市");
        publish.setSeeType(1);
        publish.setText("今fdsaf不错~");
        publish.setMedias(Arrays.asList("https://itcast-tanhua.oss-cn-shanghai.aliyuncs.com/images/quanzi/1.jpg"));
        boolean result = this.quanZiApi.savePublish(publish);
        System.out.println(result);
    }


    @Test
    public void testRecommendPublish(){
        //查询用户id为2的好友动态作为推荐动态的数据
        PageInfo<Publish> pageInfo = this.quanZiApi.queryPublishList(2L, 1, 10);
        System.out.println(pageInfo);
        for (Publish record : pageInfo.getRecords()) {

            TimeLine timeLine = new TimeLine();
            timeLine.setId(ObjectId.get());
            timeLine.setPublishId(record.getId());
            timeLine.setUserId(record.getUserId());
            timeLine.setDate(System.currentTimeMillis());
            System.out.println("ffffffffffffffffffffffffffffffff");
            TimeLine quanzi_time_line_recommend = this.mongoTemplate.save(timeLine, "quanzi_time_line_recommend");
            System.out.println(quanzi_time_line_recommend);

        }
    }
}
