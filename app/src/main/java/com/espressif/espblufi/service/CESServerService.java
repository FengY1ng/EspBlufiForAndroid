package com.espressif.espblufi.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;


import com.espressif.espblufi.data.DataType;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * HTTP或TCP/IP 通信服务
 */
public class CESServerService extends MyService {


    private String url;
    private int port;
    private OkHttpClient client;
    private Request getRequest;
    private Request postRequest;
    private Response getResponse;
    private Response postResponse;


    private boolean isWork = true;
    private static final String TAG = "NetworkService";
    private String SERVER_URL = "http://目标服务器IP地址:端口号";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * 服务初始化
     */
    public CESServerService(String url, int port) {
        this.url = url;
        this.port = port;
    }
    /**
     * 服务生命周期函数：
     * onCreate()
     * onStartCommand(Intent intent, int flags, int startId)
     * onUnbind(Intent intent)
     * onRebind(Intent intent)
     * onDestroy()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        //SERVER_URL 根据url和port，创建SERVER_URL
        client = new OkHttpClient();
        Log.d(TAG, "onCreate: executed");
    }


    /**
     * 创建IBinder远程对象
     */
    private IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public final class LocalBinder extends Binder {
        public CESServerService getService() {
            Log.d(TAG, "getService");
            return CESServerService.this;

        }
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand: executed");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: executed");

    }


    /**
     * 通过服务发送信息
     * @param Message
     */
    public void sendMessageByOkHttp(String Message) {
        // 在Service中执行网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                // GET请求示例
                getRequest = new Request.Builder()
                        .url(SERVER_URL)
                        .build();

                // POST请求示例
                String postBody = "{\"key\": \""+Message+"\"}";
                postRequest = new Request.Builder()
                        .url(SERVER_URL)
                        .post(RequestBody.create(JSON, postBody))
                        .build();

                try {
                    // 发起GET请求
                    getResponse = client.newCall(getRequest).execute();
                    String getResponseData = getResponse.body().string();
                    Log.d(TAG, "GET Response: " + getResponseData);

                    // 发起POST请求
                    postResponse = client.newCall(postRequest).execute();
                    String postResponseData = postResponse.body().string();
                    Log.d(TAG, "POST Response: " + postResponseData);

                    //刷新活动UI
                    notifyObserver(DataType.WIFI_DATA_OP, postResponseData);
                } catch (IOException e) {
                    Log.e(TAG, "Error: " + e.getMessage());
                }
            }
        }).start();
        Log.d(TAG, "startloaddetail: executed");
    }

}
