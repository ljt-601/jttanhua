package com.tanhua.server.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itheima.dubbo.api.QuanZiApi;
import com.itheima.dubbo.pojo.Publish;
import com.itheima.dubbo.vo.PageInfo;
import com.tanhua.server.pojo.User;
import com.tanhua.server.pojo.UserInfo;
import com.tanhua.server.utils.RelativeDateFormat;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.server.vo.Movements;
import com.tanhua.server.vo.MovementsParam;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.PicUploadResult;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class MovementsService {

    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;
    @Autowired
    private PicUploadService picUploadService;
    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    //用于查询缓存中的点赞，喜欢等评论数
    private RedisTemplate<String, String> redisTemplate;

    public Boolean saveMovements(MovementsParam movementsParam, MultipartFile[] multipartFile) {
        //判断用户是否存在
        User user = UserThreadLocal.get();
        //构建动态对象，将传过来的参数封装
        Publish publish = new Publish();
        publish.setUserId(user.getId());
        publish.setText(movementsParam.getTextContent());
        publish.setLocationName(movementsParam.getLocation());
        publish.setLatitude(movementsParam.getLatitude());
        publish.setLongitude(movementsParam.getLongitude());

        //todO意思就是等会再处理
        //TODO 图片   //图片上传
        //创建集合用于存储图片的路径
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : multipartFile) {
            PicUploadResult picUploadResult = this.picUploadService.qiniuUpload(file);
            //得到图片的路径
            imageUrls.add(picUploadResult.getName());
        }
        publish.setMedias(imageUrls);
        //rpc调用服务存储动态对象
        return this.quanZiApi.savePublish(publish);
    }

    public PageResult queryPublishList(User user, Integer page, Integer pageSize) {

        Long userId = null;  //查询推荐动态
        if (user != null) {
            //查询好友动态
            userId = user.getId();
        }
        //User user = UserThreadLocal.get();
        //准备对象给调用者返回
        PageResult pageResult = new PageResult();
        PageInfo<Publish> pageInfo = this.quanZiApi.queryPublishList(userId, page, pageSize);
        //查询完成后，依然需要获取到当前的登录用户
        user=UserThreadLocal.get();

        pageResult.setCounts(0);
        pageResult.setPages(0);
        pageResult.setPagesize(pageSize);
        pageResult.setPage(page);

        List<Publish> records = pageInfo.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            //没有查到数据
            return pageResult;
        }

        //遍历records对象，拿到每一个Movements对象并封装
        List<Movements> movements = this.fillValueToMovements(records);
        pageResult.setItems(movements);

        return pageResult;
    }


    private List<Movements> fillValueToMovements(List<Publish> records){
        User user = UserThreadLocal.get();
        //设置一个集合准备装Movements对象
        List<Movements> movementsList = new ArrayList<>();
        //设置集合为了通过循环拿到好友的userid
        List<Long> userIds = new ArrayList<>();
        for (Publish record : records) {
            Movements movements = new Movements();
            movements.setId(record.getId().toHexString());
            movements.setUserId(record.getUserId());
            //一个用户可能发布多条动态，为了避免重复查找，设置条件
            if (!userIds.contains(record.getUserId())) {
                userIds.add(record.getUserId());
            }

            String loveUserCommentKey = "QUANZI_COMMENT_LOVE_USER" + user.getId() + "_" + movements.getId();
            movements.setHasLoved(this.redisTemplate.hasKey(loveUserCommentKey) ? 1 : 0); //是否喜欢

            String loveCommentKey = "QUANZI_COMMENT_LOVE_" + movements.getId();
            String loveValue = this.redisTemplate.opsForValue().get(loveCommentKey);
            if (StringUtils.isNotEmpty(loveValue)) {
                movements.setLoveCount(Integer.valueOf(loveValue));  //喜欢数
            } else {
                movements.setLoveCount(0);
            }

            String likeUserCommentKey = "QUANZI_COMMENT_LIKE_USER" + user.getId() + "_" + movements.getId();
            movements.setHasLiked(this.redisTemplate.hasKey(likeUserCommentKey) ? 1 : 0); //是否点赞

            String likeCommentKey = "QUANZI_COMMENT_LIKE_" + movements.getId();
            String value = this.redisTemplate.opsForValue().get(likeCommentKey);
            if (StringUtils.isNotEmpty(value)) {
                movements.setLikeCount(Integer.valueOf(value));  //点赞数
            } else {
                movements.setLikeCount(0);
            }
            movements.setDistance("1.2公里");  //todo 距离

            movements.setCommentCount(30);  //todo 评论数
            //设置创建时间，要与现在时间做对比计算得出，所以创建了一个工具类
            //record.getCreated()得出的是一个时间戳类型，所以先转成date对象，再进行计算
            movements.setCreateDate(RelativeDateFormat.format(new Date(record.getCreated())));
            movements.setTextContent(record.getText());
            //需要的是String类型的数组
            movements.setImageContent(record.getMedias().toArray(new String[]{}));
            movementsList.add(movements);
        }

        //通过数据库查询用户信息，为了补全
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<UserInfo>().in("user_id", userIds);

        List<UserInfo> userInfoList = this.userInfoService.queryUserInfoList(userInfoQueryWrapper);
        for (Movements movements : movementsList) {
            for (UserInfo userInfo : userInfoList) {
                if (movements.getUserId().longValue() == userInfo.getUserId().longValue()) {
                    //继续填充数据，补全movements
                    movements.setTags(StringUtils.split(userInfo.getTags()));
                    movements.setNickname(userInfo.getNickName());
                    movements.setGender(userInfo.getSex().name().toLowerCase(Locale.ROOT));
                    movements.setAge(userInfo.getAge());
                }
            }
        }
        return movementsList;
    }

    public PageResult queryRecommendPublishList(Integer page, Integer pageSize) {
        return this.queryPublishList(null, page, pageSize);

    }

    public PageResult queryUserPublishList(Integer page, Integer pageSize) {
        return this.queryPublishList(UserThreadLocal.get(), page, pageSize);
    }

    public Long likeComment(String publishId) {
        User user = UserThreadLocal.get();
        boolean bool = this.quanZiApi.saveLikeComment(user.getId(), publishId);
        if (!bool) {
            //保存失败
            return null;
        }
        //保存成功后获取该动态的点赞数量进行返回，让用户可以看到多少个赞了
        //通过redis进行查询，因为有很多地方需要用到这些数量
        String likeCommentKey = "QUANZI_COMMENT_LIKE_" + publishId;
        Long likeCount = 0L;
        if (!this.redisTemplate.hasKey(likeCommentKey)) {
            //如果不包含就需要去mongodb中查
            Long count = this.quanZiApi.queryCommentCount(publishId, 1);
            likeCount = count;
            //查到后存入redis
            this.redisTemplate.opsForValue().set(likeCommentKey, String.valueOf(likeCount));

        } else {
            //如果存在就将数量加一
            likeCount = this.redisTemplate.opsForValue().increment(likeCommentKey);
        }

        //补全movements信息，用户是否点赞
        String likeUserCommentKey = "QUANZI_COMMENT_LIKE_USER_" + user.getId() + "_" + publishId;
        //做标记，记录当前用户已经点赞
        this.redisTemplate.opsForValue().set(likeUserCommentKey, "1");
        return likeCount;

    }

    public Long disLikeComment(String publishId) {
        User user = UserThreadLocal.get();
        if (!this.quanZiApi.removeComment(user.getId(), publishId, 1)) {
            return null;
        }
        //redis中的点赞数需要减少1
        String likeCommentKey = "QUANZI_COMMENT_LIKE_" + publishId;
        Long count = this.redisTemplate.opsForValue().decrement(likeCommentKey);


        //删除该用户的标记点赞
        String likeUserCommentKey = "QUANZI_COMMENT_LIKE_USER_" + user.getId() + "_" + publishId;
        //做标记，记录当前用户已经点赞
        this.redisTemplate.delete(likeUserCommentKey);

        return count;

    }

    public Long loveComment(String publishId) {
        User user = UserThreadLocal.get();
        boolean bool = this.quanZiApi.saveLoveComment(user.getId(), publishId);
        if (!bool) {
            //保存失败
            return null;
        }
        //保存成功后获取该动态的喜欢数量进行返回，让用户可以看到多少个赞了
        //通过redis进行查询，因为有很多地方需要用到这些数量
        String loveCommentKey = "QUANZI_COMMENT_LOVE_" + publishId;
        Long loveCount = 0L;
        if (!this.redisTemplate.hasKey(loveCommentKey)) {
            //如果不包含就需要去mongodb中查
            Long count = this.quanZiApi.queryCommentCount(publishId, 3);
            loveCount = count;
            //查到后存入redis
            this.redisTemplate.opsForValue().set(loveCommentKey, String.valueOf(loveCount));

        } else {
            //如果存在就将数量加一
            loveCount = this.redisTemplate.opsForValue().increment(loveCommentKey);
        }

        //补全movements信息，用户是否进行喜欢
        String loveUserCommentKey = "QUANZI_COMMENT_LOVE_USER_" + user.getId() + "_" + publishId;
        //做标记，记录当前用户已经喜欢
        this.redisTemplate.opsForValue().set(loveUserCommentKey, "1");
        return loveCount;

    }

    public Long unLoveComment(String publishId) {
        User user = UserThreadLocal.get();
        if (!this.quanZiApi.removeComment(user.getId(), publishId, 3)) {
            return null;
        }
        //redis中的喜欢数需要减少1
        String loveCommentKey = "QUANZI_COMMENT_LOVE_" + publishId;
        Long count = this.redisTemplate.opsForValue().decrement(loveCommentKey);


        //删除该用户的标记喜欢
        String loveUserCommentKey = "QUANZI_COMMENT_LOVE_USER_" + user.getId() + "_" + publishId;
        //做标记，记录当前用户已经喜欢
        this.redisTemplate.delete(loveUserCommentKey);

        return count;
    }

    public Movements queryMovementsById(String publishId) {
        Publish publish = this.quanZiApi.queryPublishById(publishId);
        if (null==publish){
            return null;
        }
        //查询到数据后，进行数据的填充，给Movements对象中
        List<Movements> movementsList = this.fillValueToMovements(Arrays.asList(publish));

        return movementsList.get(0);
    }
}
