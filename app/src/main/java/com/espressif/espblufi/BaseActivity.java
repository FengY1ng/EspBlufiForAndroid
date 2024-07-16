package com.espressif.espblufi;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.espressif.espblufi.design.MyUAObServer;
import com.espressif.espblufi.service.CESDeviceService;

import java.io.IOException;

public abstract class BaseActivity extends AppCompatActivity implements MyUAObServer {


    private boolean ServerDataPrm;
    private boolean DeviceStatePrm;
    private boolean UIShow;
    private MyCESDeviceServiceConn conCESDeviceConn;
    private CESDeviceService myCESDeviceService;
    public BaseActivity() {
        super();
    }
    public boolean isServerDataPrm() {
        return ServerDataPrm;
    }
    public void setServerDataPrm(boolean serverDataPrm) {
        ServerDataPrm = serverDataPrm;
    }
    public boolean isDevicePrm() {
        return DeviceStatePrm;
    }
    public void setDevicePrm(boolean devicePrm) {
        DeviceStatePrm = devicePrm;
    }
    public boolean isUIShow() {
        return UIShow;
    }
    public void setUIShow(boolean UIShow) {
        this.UIShow = UIShow;
    }

    public CESDeviceService getCESDeviceService() {
        return myCESDeviceService;
    }

    @SuppressLint("HandlerLeak")
    Handler myhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null) {
                updataUI(msg);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setState();
        if (isDevicePrm()) {
            conCESDeviceConn = new MyCESDeviceServiceConn();
            bindService(new Intent(this, CESDeviceService.class), conCESDeviceConn, BIND_AUTO_CREATE);
            Log.d("BActivity", "bindService_MyUAServiceConn");
        }
    }

    public void sendMessagetoDevice(byte[] mess) throws IOException {
        try {
            myCESDeviceService.sendMessage(mess);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean getCES_State_Info() {
        return isDevicePrm();
    }

    @Override
    public boolean getCES_Service_Info() {
        return isServerDataPrm();
    }

    /**
     * 是否注册Device业务
     * @param  canUseDevice
     */
    public void setCanUseDevice(boolean canUseDevice) {
        setDevicePrm(canUseDevice);
    }

    /**
     * 是否注册在页面显示DATA变化
     * @param canUiShow
     */
    public void setCanUiShow(boolean canUiShow) {
        setUIShow(canUiShow);
    }

    /**
     * 是否注册服务器业务
     * @param canUseServer
     */
    public void setCanUseServer(boolean canUseServer) {
        setServerDataPrm(canUseServer);
    }

    /**
     * 创建设备状态读取与指令下发服务连接
     */
    private class MyCESDeviceServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myCESDeviceService = ((CESDeviceService.LocalBinder) service).getService();
            Log.d("TAG", "onServiceConnected by uart");
            myCESDeviceService.registerObserver(BaseActivity.this);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            myCESDeviceService.removeObserver(BaseActivity.this);
            Log.d("BAcivity", "myuaService_onServiceDisconnected");
            myCESDeviceService = null;
        }
    }

    /**
     * DATA变化，刷新页面显示
     * @param dataType
     * @param data
     */
    @Override
    public void update(int dataType, String data) {

        if (isUIShow()) {
            Message myMessage = new Message();
            myMessage.what = dataType;
            myMessage.obj = data;
            myhandler.sendMessage(myMessage);
        }
        updataNoUI(dataType, data);
    }


    public abstract void updataUI(Message urData);

    public abstract void updataNoUI(int dataType, String data);

    /**
     * 关闭
     */
    public void closeUaService() {
        if (myCESDeviceService != null) {
            myCESDeviceService.closeSocketThread();
            myCESDeviceService.stopMyService();
            Log.d("BAcivity", "closeUAWFService!");
        }
    }


    /**
     * setCanUseDevice(T/F) 是否注册Device业务
     * setCanUseServer(T/F)是否注册服务器业务
     * setCanUiShow(T/F) 是否开启页面显示
     */
    protected abstract void setState();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myCESDeviceService != null) {
            if (conCESDeviceConn != null) {
                unbindService(conCESDeviceConn);
                Log.d("test", "Send");
            }
        }
    }
}