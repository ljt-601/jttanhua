package com.itheima.dubbo.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.dubbo.pojo.FollowUser;
import com.itheima.dubbo.pojo.Video;
import com.itheima.dubbo.vo.PageInfo;
import com.mongodb.client.result.DeleteResult;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Service(version = "1.0.0")
public class VideoApiImpl implements VideoApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override //保存小视频
    public Boolean saveVideo(Video video) {
        if (video.getUserId()==null){
            return false;
        }
        video.setId(ObjectId.get());
        video.setCreated(System.currentTimeMillis());
        this.mongoTemplate.save(video);
        return true;
    }

    @Override
    public PageInfo<Video> queryVideoList(Integer page, Integer pageSize) {
        //通过mongodb分页查询视频列表
        List<Video> videos = this.mongoTemplate.find(new Query().with(PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("created")))), Video.class);
        PageInfo<Video> pageInfo=new PageInfo<>();
        pageInfo.setRecords(videos);
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(0);
        return pageInfo;

    }

    @Override
    //关注好友
    public Boolean followUser(Long userId, Long followUserId) {
        try {
            FollowUser followUser = new FollowUser();
            followUser.setId(ObjectId.get());
            followUser.setUserId(userId);
            followUser.setFollowUserId(followUserId);
            followUser.setCreated(System.currentTimeMillis());
            this.mongoTemplate.save(followUser);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    /**
     * 取消关注好友
     * userId:当前用户的id
     * followUserId:已关注好友的id
     */
    public Boolean disFollowUser(Long userId, Long followUserId) {
        //思路：1.在MongoDB中通过当前用户的id和已关注好友的id在follow_user表中进行删除操作
        //     2.返回值类型为布尔，所以删除操作后通过getDeletedCount得到删除条数值，如果>0则返回true
        Query query = Query.query(Criteria.where("userId").is(userId).and("followUserId").is(followUserId));
        DeleteResult deleteResult = this.mongoTemplate.remove(query, FollowUser.class);
        return deleteResult.getDeletedCount()>0;
    }


}
