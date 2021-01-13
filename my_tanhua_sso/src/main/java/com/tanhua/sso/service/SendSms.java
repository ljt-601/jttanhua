package com.tanhua.sso.service;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
/*
pom.xml
<dependency>
  <groupId>com.aliyun</groupId>
  <artifactId>aliyun-java-sdk-core</artifactId>
  <version>4.5.3</version>
</dependency>
*/
public class SendSms {
    public static void main(String[] args) {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou","LTAI4GJvyURehtiJGrgq3zTV", "7luDSpvOXZ38GS7OxEzEyuVBW4Hzh6");
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", "13488180238"); //目标手机号
        request.putQueryParameter("SignName", "聚贤庄"); //签名名称
        request.putQueryParameter("TemplateCode", "SMS_208640690"); //短信模板code
        request.putQueryParameter("TemplateParam", "{\"code\":\"123456\"}");//模板中变量替换
        try {
            CommonResponse response = client.getCommonResponse(request);

            //{"Message":"OK","RequestId":"EC2D4C9A-0EAC-4213-BE45-CE6176E1DF23","BizId":"110903802851113360^0","Code":"OK"}
            System.out.println(response.getData());
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }
}