package com.tanhua.server.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itheima.dubbo.api.QuanZiApi;
import com.itheima.dubbo.pojo.Comment;
import com.itheima.dubbo.vo.PageInfo;
import com.tanhua.server.pojo.User;
import com.tanhua.server.pojo.UserInfo;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.server.vo.Comments;
import com.tanhua.server.vo.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentsService {

    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    public PageResult queryCommentsList(String publishId, Integer page, Integer pageSize) {

        //拿到User对象
        User user = UserThreadLocal.get();

        PageResult pageResult = new PageResult();
        pageResult.setPage(page);

        pageResult.setPagesize(pageSize);
        pageResult.setCounts(0);
        pageResult.setPages(0);

        PageInfo<Comment> pageInfo = this.quanZiApi.queryCommentList(publishId, page, pageSize);
        List<Comment> records = pageInfo.getRecords();

        if (CollectionUtils.isEmpty(records)){
            return pageResult;
        }
        //用于存储评论用户的id
        List<Long> userIds = new ArrayList<>();
        for (Comment record : records) {
            //去重重复
            if (!userIds.contains(record.getUserId())){
                userIds.add(record.getUserId());
            }
        }

        //封装comments对象
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id",userIds);
        List<UserInfo> userInfoList = this.userInfoService.queryUserInfoList(queryWrapper);


        List<Comments> commentsList=new ArrayList<>();
        for (Comment record : records) {
            Comments comments = new Comments();
            comments.setId(record.getId().toHexString());
            comments.setCreateDate(new DateTime(record.getCreated()).toString("yyyy年MM月dd日 HH:mm"));
            comments.setContent(record.getContent());

            //还需要用户相关的信息
            for (UserInfo userInfo : userInfoList) {
                if (record.getUserId().longValue()==userInfo.getUserId().longValue()){
                    comments.setNickname(userInfo.getNickName());
                    comments.setAvatar(userInfo.getLogo());
                    break;
                }
            }
            String likeUserCommentKey = "QUANZI_COMMENT_LIKE_USER" + user.getId() + "_" + comments.getId();
            comments.setHasLiked(this.redisTemplate.hasKey(likeUserCommentKey) ? 1 : 0); //是否点赞

            String likeCommentKey = "QUANZI_COMMENT_LIKE_" + comments.getId();
            String value = this.redisTemplate.opsForValue().get(likeCommentKey);
            if (StringUtils.isNotEmpty(value)) {
                comments.setLikeCount(Integer.valueOf(value));  //点赞数
            } else {
                comments.setLikeCount(0);
            }


            commentsList.add(comments);
        }
        pageResult.setItems(commentsList);

        return null;
    }

    public boolean saveComments(String publishId, String content) {
        //this.quanZiApi.saveComment(publishId,)
        return false;
    }
}
