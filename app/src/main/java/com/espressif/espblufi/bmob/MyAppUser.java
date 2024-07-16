package com.espressif.espblufi.bmob;

import cn.bmob.v3.BmobUser;

/**
 * 基于BmobUser扩展的用户类
 * 可以根据自己实际业务需求扩展其他属性字段或功能
 */
public class MyAppUser extends BmobUser {
    /**
     * 昵称
     */
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

}
