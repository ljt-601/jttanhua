package com.tanhua.sso.service;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.sso.config.AliyunSMSConfig;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
@Service
public class SmsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmsService.class);

    @Autowired  //用来给第三方服务发送http请求
    private RestTemplate restTemplate;
    //利用云之询发送时所需
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired  //操作redis用，配置文件配过了，所以直接注入
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private AliyunSMSConfig aliyunSMSConfig;

    //发送验证码
    public Map<String,Object> sendCheckCode(String mobile){
        //来个集合得存结果给接口返回
        Map<String,Object> result=new HashMap<>(2);
        try {
            //先查看redis中有没有该手机验证码的数据，有的话代表验证码还未失效
            String redisKey ="CHECK_CODE_"+mobile; //redis存的key
            String value = this.redisTemplate.opsForValue().get(redisKey);
            if (StringUtils.isNotEmpty(value)){
                result.put("code",1);
                result.put("msg","上次发送的验证码还未失败");
                return result;
            }
            //如果判断redis中没有的话，调用当前阿里云短信服务发送
            //String code = this.sendSmsAliyun(mobile);
            String code=("123456");
            //如果发送失败，返回代码2
            if (null==code){
                result.put("code",2);
                result.put("msg","发送短信验证码失败");
                return result;
            }
            result.put("code",3);
            result.put("msg","ok");
            //发送成功后现将验证码暂存redis中，2分钟后失效
            this.redisTemplate.opsForValue().set(redisKey,code, Duration.ofMinutes(2));
            return result;
        }catch (Exception e){
            LOGGER.error("发送验证码出错！"+mobile,e);
            result.put("code",4);
            result.put("msg","发送验证码出现异常");
            return result;
        }
    }

    /**
     * 通过阿里云发送验证码短信
     *
     * @param mobile
     */
    public String sendSmsAliyun(String mobile) {
        DefaultProfile profile = DefaultProfile.getProfile(
                this.aliyunSMSConfig.getRegionId(),
                this.aliyunSMSConfig.getAccessKeyId(),
                this.aliyunSMSConfig.getAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);

        String code = RandomUtils.nextInt(100000, 999999) +"";

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain(this.aliyunSMSConfig.getDomain());
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", this.aliyunSMSConfig.getRegionId());
        request.putQueryParameter("PhoneNumbers", mobile);
        request.putQueryParameter("SignName", this.aliyunSMSConfig.getSignName());
        request.putQueryParameter("TemplateCode", this.aliyunSMSConfig.getTemplateCode());
        request.putQueryParameter("TemplateParam", "{\"code\":\""+code+"\"}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            if(StringUtils.contains(response.getData(), "\"Code\":\"OK\"")){
                return code;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
