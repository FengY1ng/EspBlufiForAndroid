package com.espressif.espblufi.bmob;

import android.app.Application;

import cn.bmob.v3.Bmob;

public class BmobApp extends Application {
    public static final String TAG = "bmob";
    // 短信模版名称
    public static final String SMS_TEMPLATE_NAME = "测试";
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 Bmob SDK
        Bmob.initialize(this,"8847293e15bf3afcc1dc731eca5a0b6e");
    }
}
