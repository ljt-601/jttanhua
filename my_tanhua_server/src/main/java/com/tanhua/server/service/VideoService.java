package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.itheima.dubbo.api.QuanZiApi;
import com.itheima.dubbo.api.VideoApi;
import com.itheima.dubbo.pojo.Video;
import com.itheima.dubbo.vo.PageInfo;
import com.tanhua.server.pojo.User;
import com.tanhua.server.pojo.UserInfo;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.PicUploadResult;
import com.tanhua.server.vo.VideoVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class VideoService {

    @Autowired  //用于上传封面的图片
    private PicUploadService picUploadService;
    @Autowired  //上传图片
    private FastFileStorageClient storageClient;
    @Autowired
    private FdfsWebServer fdfsWebServer;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    @Reference(version = "1.0.0")
    private VideoApi videoApi;

    //保存视频
    public Boolean saveVideo(MultipartFile picFile, MultipartFile videoFile) {
        User user = UserThreadLocal.get();
        Video video = new Video();
        video.setUserId(user.getId());
        video.setSeeType(1);

        try {
            //上传视频图片
            PicUploadResult picUploadResult = this.picUploadService.qiniuUpload(picFile);
            video.setPicUrl(picUploadResult.getName());

            //上传视频
            StorePath storePath = storageClient.uploadFile(videoFile.getInputStream(),
                    videoFile.getSize(),
                    StringUtils.substringAfter(videoFile.getOriginalFilename(), "."),
                    null);
            video.setVideoUrl(fdfsWebServer.getWebServerUrl() + storePath.getFullPath());
            return this.videoApi.saveVideo(video);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
//查询小视频列表
    public PageResult queryVideoList(Integer page, Integer pageSize) {
        User user = UserThreadLocal.get();

        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);
        pageResult.setPages(0);
        pageResult.setCounts(0);

        PageInfo<Video> pageInfo = this.videoApi.queryVideoList(page, pageSize);

        List<Video> records = pageInfo.getRecords();
        List<VideoVo> videoVoList = new ArrayList<>();
        List<Long> userIds = new ArrayList<>();
        for (Video record : records) {
            VideoVo videoVo = new VideoVo();
            videoVo.setUserId(record.getUserId());
            videoVo.setCover(record.getPicUrl());
            videoVo.setVideoUrl(record.getVideoUrl());
            videoVo.setId(record.getId().toHexString());
            videoVo.setSignature("我就是我~");  //todo 签名

            Long commentCount = this.quanZiApi.queryCommentCount(videoVo.getId(), 2);
            videoVo.setCommentCount(commentCount==null?0: commentCount.intValue()); // 评论数
            videoVo.setHasFocus(0);  //todo 是否关注

            String likeUserCommentKey = "QUANZI_COMMENT_LIKE_USER" + user.getId() + "_" + videoVo.getId();
            videoVo.setHasLiked(this.redisTemplate.hasKey(likeUserCommentKey) ? 1 : 0); //是否点赞

            String likeCommentKey = "QUANZI_COMMENT_LIKE_" + videoVo.getId();
            String value = this.redisTemplate.opsForValue().get(likeCommentKey);
            if (StringUtils.isNotEmpty(value)) {
                videoVo.setLikeCount(Integer.valueOf(value));  //点赞数
            } else {
                videoVo.setLikeCount(0);
            }
            videoVo.setHasLiked(0);  //todo 是否点赞
            videoVo.setLikeCount(40);  //todo 点赞数

            videoVoList.add(videoVo);
        }

        pageResult.setItems(videoVoList);

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper();
        queryWrapper.in("user_id", userIds);
        List<UserInfo> userInfos = this.userInfoService.queryUserInfoList(queryWrapper);

        for (VideoVo videoVo : videoVoList) {
            for (UserInfo userInfo : userInfos) {
                if (videoVo.getUserId().longValue() == userInfo.getUserId().longValue()) {

                    videoVo.setNickname(userInfo.getNickName());
                    videoVo.setAvatar(userInfo.getLogo());

                    break;
                }
            }

        }

        return pageResult;
    }

    /**
     * 关注用户
     * @param userId  将被关注用户的id
     * @return 是否关注成功
     */
    public Boolean followUser(Long userId) {
        //思路：1.调用dubbo服务中VideoApi中的关注用户方法
        //     2.方法中需要当前登陆用户的id,在当前线程中获取当前用户的id
        //     3.在redis中单独的用户表记录关注用户这一操作，之后用.
        User user = UserThreadLocal.get();
        this.videoApi.followUser(user.getId(),userId);
        //记录用户关注了哪个用户，1表示关注  0表示取消关注
        this.redisTemplate.opsForValue().set("VIDEO_FOLLOW_USER_"+user.getId() + "_" + userId,"1");
        return true;

    }

    /**
     * 取消关注用户
     * @param userId 将被取消关注用户的id
     * @return 是否取消关注成功
     */
    public Boolean disFollowUser(Long userId) {
        //思路 同 关注用户
        User user = UserThreadLocal.get();
        this.videoApi.disFollowUser(user.getId(), userId);

        String followUserKey = "VIDEO_FOLLOW_USER_" + user.getId() + "_" + userId;
        this.redisTemplate.delete(followUserKey);

        return true;
    }
}
