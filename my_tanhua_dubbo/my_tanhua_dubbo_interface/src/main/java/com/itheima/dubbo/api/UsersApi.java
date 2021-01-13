package com.itheima.dubbo.api;

import com.itheima.dubbo.pojo.Users;

public interface UsersApi {
    /**
     * 保存好友  保存到MongoDB中
     *
     * @param users
     * @return
     */
    String saveUsers(Users users);

    boolean removeUsers(Users users);
}
