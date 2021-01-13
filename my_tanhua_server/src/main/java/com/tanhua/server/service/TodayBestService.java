package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itheima.dubbo.pojo.RecommendUser;
import com.itheima.dubbo.vo.PageInfo;
import com.tanhua.server.pojo.User;
import com.tanhua.server.pojo.UserInfo;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.RecommendUserQueryParam;
import com.tanhua.server.vo.TodayBest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
public class TodayBestService {

    @Value("${tanhua.sso.default.user}")
    private Long defaultUserId;

    @Autowired
    //补全用户信息
    private UserInfoService userInfoService;

    @Autowired
    private RecommendUserService recommendUserService;

    public TodayBest queryTodayBest() {
        User user= UserThreadLocal.get();

        //不为null后就要去查询被推荐的用户
        TodayBest todayBest = this.recommendUserService.queryTodayBest(user.getId());
        if (todayBest == null) {
            //如果没找到用户，就设置一个默认推荐用户
            todayBest = new TodayBest();
            todayBest.setId(defaultUserId);
            todayBest.setFateValue(95L);
        }

        //补全用户信息
        UserInfo userInfo = this.userInfoService.queryUserInfoByID(todayBest.getId());
        if (null!=userInfo){
            todayBest.setAge(userInfo.getAge());
            todayBest.setAvatar(userInfo.getLogo());
            //前台要的数据是小写的，通过数据库查到的经过枚举项处理成大写的了，所以要转
            todayBest.setGender(userInfo.getSex().toString());
            todayBest.setGender(userInfo.getSex().name().toLowerCase(Locale.ROOT));

            todayBest.setNickname(userInfo.getNickName());
            todayBest.setTags(StringUtils.split(userInfo.getTags(),","));
        }
        return todayBest;

    }

    public PageResult queryRecommendUserList(RecommendUserQueryParam queryParam) {
        User user =UserThreadLocal.get();

        //查询到用户推荐列表，分页查
        PageInfo<RecommendUser> pageInfo = this.recommendUserService.queryRecommendUserList(user.getId(), queryParam.getPage(), queryParam.getPagesize());
        //拿到PageInfo<RecommendUser>中的内容  拿到推荐用户列表，
        List<RecommendUser> records = pageInfo.getRecords();
        //需要查询用户的详细信息，并根据条件查询  RecommendUser中只有用户id
        List<Long> userIds=new ArrayList<>();
        for (RecommendUser record : records) {
            userIds.add(record.getUserId());
        }
        //必要的查询条件
        QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<>();
        queryWrapper.in("user_id",userIds);
        //其他可选择筛选条件
        if (queryParam.getAge()!=null){
            queryWrapper.lt("age",queryParam.getAge());  //年龄判断
        }
        if (StringUtils.isNotEmpty(queryParam.getCity())){
            queryWrapper.eq("city",queryParam.getCity());
        }
        //通过条件查询到UserInfo信息，但是返回对象要求是TodayBest，所以转换一下
        List<UserInfo> userInfos= this.userInfoService.queryUserInfoList(queryWrapper);
        ArrayList<TodayBest> todayBests = new ArrayList<>();
        for (UserInfo userInfo : userInfos) {
            TodayBest todayBest = new TodayBest();
            todayBest.setId(userInfo.getUserId());
            todayBest.setAge(userInfo.getAge());
            todayBest.setAvatar(userInfo.getLogo());
            todayBest.setGender(userInfo.getSex().name().toLowerCase());
            todayBest.setNickname(userInfo.getNickName());
            todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));
            //设置缘分值 从RecommendUser列表中那
            for (RecommendUser record : records) {
                if (record.getUserId().longValue()==todayBest.getId().longValue()){
                    double score = Math.floor(record.getScore());
                    todayBest.setFateValue(Double.valueOf(score).longValue());
                }
            }
            todayBests.add(todayBest);
        }

        //对结果集重新排序，按照缘分值排序  通过比较器
        Collections.sort(todayBests,(o1,o2) ->Long.valueOf(o2.getFateValue()-o1.getFateValue()).intValue());
        return new PageResult(0,queryParam.getPagesize(),0,queryParam.getPage(),todayBests);
    }
}
