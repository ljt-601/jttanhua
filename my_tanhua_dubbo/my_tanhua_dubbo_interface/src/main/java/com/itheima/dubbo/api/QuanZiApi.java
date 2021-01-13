package com.itheima.dubbo.api;

import com.itheima.dubbo.pojo.Comment;
import com.itheima.dubbo.pojo.Publish;
import com.itheima.dubbo.vo.PageInfo;

public interface QuanZiApi {

    //发布动态的接口
    boolean savePublish(Publish publish);

    //查询动态
    PageInfo<Publish> queryPublishList(Long userId,Integer page,Integer pageSize);

    //给动态点赞  哪个用户给哪个动态点赞
    boolean saveLikeComment(Long userId,String publishId);

    //取消点赞，喜欢等   评论类型，1-点赞，2-评论，3-喜欢
    boolean removeComment(Long userId,String publishId,Integer commentType);

    //点击喜欢    哪个用户给哪个动态点击喜欢
    boolean saveLoveComment(Long  userId,String publishId);

    //保存评论   哪个用户给哪个动态发表评论，内容是。
    boolean saveComment(Long  userId,String publishId,Integer type,String content);

    //查询评论数  查询某个类型的评论数，比如。喜欢，点赞都属于评论类型
    Long queryCommentCount(String publishId,Integer type);

    //根据动态的id进行查询单条动态
    Publish queryPublishById(String publishId);

    //查询评论
    PageInfo<Comment> queryCommentList(String publishId,Integer page,Integer pageSize);


}
