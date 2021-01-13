package com.itheima.dubbo.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.dubbo.pojo.*;
import com.itheima.dubbo.vo.PageInfo;
import com.mongodb.client.result.DeleteResult;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

@Service(version = "1.0.0")  //发布为服务
public class QuanziApiImpl implements QuanZiApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public boolean savePublish(Publish publish) {
        //判断publish对象是否为空
        if (publish.getUserId()==null){
            return false;
        }

        try {
            //填充数据，保存到发布表中
            publish.setId(ObjectId.get());
            publish.setCreated(System.currentTimeMillis()); //发布时间
            publish.setSeeType(1);  //查看的权限
            //保存动态信息到发布表中，保存所有用户发布的动态信息
            this.mongoTemplate.save(publish);
            //再写入到只保存自己发布动态信息的相册表中
            Album album = new Album();
            album.setId(ObjectId.get());
            album.setCreated(System.currentTimeMillis());   //当前的时间
            album.setPublishId(publish.getId());  //发布的动态信息的ID
            //保存到当前使用用户的相册表中，每个用户一张表  保存到mongodb中
            this.mongoTemplate.save(album,"quanzi_album_"+publish.getUserId());
            //先查询到当前用户的好友数据
            Query query=Query.query(Criteria.where("userId").is(publish.getUserId()));
            List<Users> users = this.mongoTemplate.find(query, Users.class);
            for (Users user : users) {
                TimeLine timeLine = new TimeLine();
                timeLine.setId(ObjectId.get());
                timeLine.setUserId(publish.getUserId());
                timeLine.setPublishId(publish.getId());
                timeLine.setDate(System.currentTimeMillis());

                this.mongoTemplate.save(timeLine,"quanzi_time_line_"+user.getFriendId());

            }
        } catch (Exception e) {
            e.printStackTrace();
            //有异常做事务的回滚
            //TODO 事务回滚
        }
        return true;
    }

    //在自己的时间线表中查询好友动态ID,从而分页查询好友动态
    @Override
    public PageInfo<Publish> queryPublishList(Long userId, Integer page, Integer pageSize) {
        //构建分页查询条件  //根据发布时间做倒序排序
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("date")));
        Query query = new Query().with(pageable);

        String tableName="quanzi_time_line_";
        if (null==userId){
            //查询推荐动态
            tableName+="recommend";
        }else {
            //查询好友动态
            tableName+=userId;
        }

        //查询自己的时间线  主要是为了要拿到好友的动态ID
        List<TimeLine> timeLines = this.mongoTemplate.find(query, TimeLine.class, tableName);
        //拿到好友发布的动态id后，就构建条件查询动态的详细信息
        List<ObjectId> ids = new ArrayList<>();
        for (TimeLine timeLine : timeLines) {

            ids.add(timeLine.getPublishId());
        }

        //根据好友id进行匹配查找，注意得根据生成时间重新排序
        Query queryPublish = Query.query(Criteria.where("id").in(ids)).with(Sort.by(Sort.Order.desc("created")));
        //拿到用户好友发布的动态信息
        List<Publish> publishList = this.mongoTemplate.find(queryPublish, Publish.class);
        //将数据再封装到PageInfo分页对象中，完成分页，并返回
        PageInfo<Publish> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(0); //暂时不提供总数，因为是安卓端
        pageInfo.setRecords(publishList);
        return pageInfo;
    }

    @Override
    //点赞操作
    public boolean saveLikeComment(Long userId, String publishId) {
        //防止因为种种原因，用户重复点赞，所以加判断，是否已经点赞
        //构建查询条件，根据用户Id和被点赞的动态的Id和指定类型是1，表示查的是点赞类型的评论
        Query query = Query.query(Criteria.where("userId").is(userId).and("publishId").is(new ObjectId(publishId)).and("commentType").is(1));
        long count = this.mongoTemplate.count(query, Comment.class);
        if (count>0){
            //说明该用户已经点过赞了
            return false;
        }
        return this.saveComment(userId,publishId,1,null);
    }

    @Override
    public boolean removeComment(Long userId, String publishId, Integer commentType) {
        Query query = Query.query(Criteria.where("userId").is(userId).
                and("publishId").is(new ObjectId(publishId)).
                and("commentType").is(commentType));
        DeleteResult remove = this.mongoTemplate.remove(query, Comment.class);

        return remove.getDeletedCount()>0;  //如果大于0说明删除成功，返回true
    }

    @Override
    //喜欢操作
    public boolean saveLoveComment(Long userId, String publishId) {
        //防止因为种种原因，用户重复点赞，所以加判断，是否已经点赞
        //构建查询条件，根据用户Id和被点赞的动态的Id和指定类型是1，表示查的是点赞类型的评论
        Query query = Query.query(Criteria.where("userId").is(userId).
                and("publishId").is(new ObjectId(publishId)).
                and("commentType").is(3));
        long count = this.mongoTemplate.count(query, Comment.class);
        if (count>0){
            //说明该用户已经点过赞了
            return false;
        }
        return this.saveComment(userId,publishId,3,null);
    }

    @Override
    //保存评论
    public boolean saveComment(Long userId, String publishId, Integer type, String content) {
        //构建评论对象
        try {
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setIsParent(true);
            comment.setCommentType(type);
            comment.setPublishId(new ObjectId(publishId));
            comment.setId(ObjectId.get());
            comment.setUserId(userId);
            comment.setCreated(System.currentTimeMillis());
            //暂时对评论的树型设定不做处理
            //comment.setParentId();
            this.mongoTemplate.save(comment);//所有的评论都保存到同一张表
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    @Override
    //查询评论数，比如点赞数，喜欢数
    public Long queryCommentCount(String publishId, Integer type) {
        return  this.mongoTemplate.count(Query.query(Criteria. where("publishId").is(new ObjectId(publishId)).
                and("commentType").is(type)),Comment.class);
    }

    @Override
    public Publish queryPublishById(String publishId) {
        return this.mongoTemplate.findById(new ObjectId(publishId),Publish.class);
    }

    @Override
    public PageInfo<Comment> queryCommentList(String publishId, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.asc("created")));
        List<Comment> commentList = this.mongoTemplate.find(Query.query(Criteria.where("publishId").is(new ObjectId(publishId)).and("commentType").is(2)).with(pageRequest), Comment.class);
        PageInfo<Comment> pageInfo = new PageInfo<>();
        pageInfo.setTotal(0);
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setRecords(commentList);
        return null;
    }
}
