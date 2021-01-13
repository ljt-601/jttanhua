package com.tanhua.sso.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.sso.mapper.UserMapper;
import com.tanhua.sso.pojo.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    //需要一个日志对象记录异常
    private static final Logger LOGGER= LoggerFactory.getLogger(UserService.class);

    @Autowired
    //为了校验验证码
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    //为了验证账号是否存在
    private UserMapper userMapper;
    //将jwt的言注入进来
    @Value("${jwt.secret}")
    private String secret;

    //为了序列化user对象存入redis
    private final static ObjectMapper MAPPER=new ObjectMapper();

    //注册用户到环信
    @Autowired
    private HuanXinService huanXinService;

    //为了发送消息
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    //判断登录是否正确
    public String login(String mobile, String code) {
        //存入redis中的key
        String redisKey="CHECK_CODE_"+mobile;
        //在redis中校验验证码是否正确
        String value = this.redisTemplate.opsForValue().get(redisKey);
        if(StringUtils.isEmpty(value)){
            //验证码失效
            return null;
        }
        if (!StringUtils.equals(value,code)){
            //验证码输入错误
            return null;
        }

        //登录最后返回的时候需要有是否是新用户的标识
        Boolean isNew=false;//默认不是新用户的标识

        //走到这儿说明验证码已经判断正确
        //接下来就该校验手机号是否注册了，如果没注册就需要注册一个账号，如果已经注册直接登录就行
        //要实现就需要查询数据库，先构建条件
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("mobile",mobile);
        User user = this.userMapper.selectOne(queryWrapper);
        if (null==user){
            //说明该手机未注册
            user  = new User();
            user.setMobile(mobile);
            //密码暂时设置为了以后备用，使用md5做加密做默认密码
            user.setPassword(DigestUtils.md5Hex("123456"));
            //保存到数据库中
            this.userMapper.insert(user);
            //标记为新用户
            isNew=true;

            //注册用户到环信
            this.huanXinService.register(user.getId());
        }

        //成功后得生成一个token
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("mobile", mobile);
        //user.getId()在前面添加后会自动回显id所以可以拿到
        claims.put("id", user.getId());

        // 生成token
        String token = Jwts.builder()
                .setClaims(claims) //设置响应数据体
                //第二个参数要的是jwt的言，在配置文件中已经配置了，只需要注入进来即可
                .signWith(SignatureAlgorithm.HS256, secret) //设置加密方法和加密盐
                .compact();

        try {
            //将token存储到redis中
            String redisTokenKey="Token_"+token;
            //value就存序列化后的user对象,通过MAPPER做序列化
            String redisTokenValue=MAPPER.writeValueAsString(user);
            //第三个参数是需要一个时间限制，说保存一小时
            this.redisTemplate.opsForValue().set(redisTokenKey,redisTokenValue, Duration.ofHours(1));
        } catch (Exception e) {
            LOGGER.error("存储token出错",e);
            return null;
        }

        try {
            //利用RocketMQ给目标tanhua-sso—login发送用户的信息，告诉别的接口该用户登录成功了
            HashMap<String, Object> msg = new HashMap<>();
            msg.put("id",user.getId());
            msg.put("mobile",mobile);
            msg.put("date",new Date());
            this.rocketMQTemplate.convertAndSend("tanhua-sso-login",msg);
        } catch (Exception e) {
            LOGGER.error("发送消息出错",e);
            return null;
        }
        return isNew+"|"+token;
    }
    //查询用户的token
    public User queryUserByToken(String token){
        try {
            String redisToken="Token_"+token;
            String cacheData=this.redisTemplate.opsForValue().get(redisToken);
            if (StringUtils.isEmpty(cacheData)){
                return null;
            }
            //刷新一下用户的token,每次浏览都刷新重置限时时间
            this.redisTemplate.expire(redisToken,1, TimeUnit.HOURS);
            return MAPPER.readValue(cacheData,User.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
