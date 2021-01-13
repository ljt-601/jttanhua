package com.tanhua.server.utils;

import java.lang.annotation.*;

@Target(ElementType.METHOD) //表名注解可以写到的地方
@Retention(RetentionPolicy.RUNTIME)  //说明在运行时有效
@Documented //标记注解
public @interface NoAuthorization {

}