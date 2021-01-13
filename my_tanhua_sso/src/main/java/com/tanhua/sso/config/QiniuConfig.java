package com.tanhua.sso.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:qiniu.properties")
@ConfigurationProperties(prefix = "qiniu")
@Data
public class QiniuConfig {
    private String ak ;
    private String sk ;
    private String preFix ;
    private String bucketName ;

}