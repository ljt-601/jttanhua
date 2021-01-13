package com.tanhua.sso.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.sso.enums.SexEnum;
import com.tanhua.sso.mapper.UserInfoMapper;
import com.tanhua.sso.pojo.User;
import com.tanhua.sso.pojo.UserInfo;
import com.tanhua.sso.vo.PicUploadResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class UserInfoService {

    private static final Logger LOGGER= LoggerFactory.getLogger(UserInfoService.class);
    @Autowired
    private UserService userService;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private FaceEngineService faceEngineService;
    @Autowired
    private PicUploadService picUploadService;


    public Boolean saveUserInfo (Map<String,String> param,String token){
        //通过token查看是否登陆成功
        User user = this.userService.queryUserByToken(token);
        if (user==null){
            return false;
        }
        //将body中的数据封装到用户详细信息类中UserInfo
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setSex(StringUtils.equals(param.get("gender"),"man") ? SexEnum.MAN:SexEnum.WOMAN);
        userInfo.setNickName(param.get("nickname"));
        userInfo.setBirthday(param.get("birthday"));
        userInfo.setCity(param.get("city"));

        //保存用户数据到数据库
        this.userInfoMapper.insert(userInfo);
        return true;
    }

    //检验是否为图片为头像，是的话存储头像数据
    public Boolean saveLogo(MultipartFile file,String token){
        //通过token查看是否登陆成功
        User user = this.userService.queryUserByToken(token);
        if (user==null){
            return false;
        }
        try {
            //检验是否为人像
            boolean isPortrait =this.faceEngineService.checkIsPortrait(file.getBytes());
            if (!isPortrait){
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("检测人像图片出错！",e);
            return false;
        }
        // 图片上传到七牛云
        PicUploadResult uploadResult = this.picUploadService.qiniuUpload(file);

        UserInfo userInfo = new UserInfo();
        userInfo.setLogo(uploadResult.getName());

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", user.getId());
        this.userInfoMapper.update(userInfo, queryWrapper);
        return true;
    }

    //查询数据库，查找用户信息
    public UserInfo queryUserInfoByID(Long id) {
        UserInfo userInfo = this.userInfoMapper.selectById(id);
        return userInfo;
    }
}
