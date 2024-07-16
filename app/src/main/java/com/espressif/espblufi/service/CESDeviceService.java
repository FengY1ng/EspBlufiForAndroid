package com.espressif.espblufi.service;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.net.DhcpInfo;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;


import com.espressif.espblufi.data.DataType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


/**
 * HTTP或TCP/IP 通信服务,与底端设备
 */
public class CESDeviceService extends MyService {

    //服务端 套接字
    private String ipAddress="127.0.0.1";
    private String ssid;
    private int port = 3333;
    private ServerSocket socket;
    private Socket clientSocket;
    private InputStream inputStream;
    private BufferedReader reader;
    private OutputStream outputStream;
    private String ThreadName = "deviceThread";
    private Thread deviceThread;
    private boolean isWork = true;

    private static final MutableLiveData<Integer> numberLiveData = new MutableLiveData<>();

    private Timer timer;

    public String getIpAddress() {
        return ipAddress;
    }

    public String getSsid() {
        return ssid;
    }

    public int getPort() {
        return port;
    }

    public CESDeviceService() {

    }

    /**
     * 创建IBinder远程对象
     */
    private IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        startSendingRandomNumbers();
        return mBinder;
    }


    public final class LocalBinder extends Binder {
        public CESDeviceService getService() {
            try {
                openSocket();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return CESDeviceService.this;
        }
    }


    public int getWifiApState(Context mContext) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApState");
            int i = (Integer) method.invoke(wifiManager);
            DhcpInfo info=wifiManager.getDhcpInfo();
            Log.d("WifiInfoService","wifi state: " + i);
            Log.d("WifiInfoService","info sip: " + info.serverAddress);


            return i;
        } catch (Exception e) {
            Log.d("WifiInfoService","Cannot get WiFi AP state" + e);
            return 0;
        }
    }

    public void getNetinfo(boolean ishost) {
        if(ishost)
        {
            getWifiApState( getApplicationContext());
        }
        else {
            // 获取WiFiManager实例
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            // 获取当前连接的WiFi信息
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            ssid = wifiInfo.getSSID();
            ipAddress = intToIp(wifiInfo.getIpAddress());

            // 打印WiFi信息
            Log.d("WifiInfoService", "Connected to WiFi SSID: " + ssid);
            Log.d("WifiInfoService", "IP Address: " + ipAddress);
        }

    }

    private String intToIp(int ipAddress) {
        return ((ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                ((ipAddress >> 24) & 0xFF));
    }

    public boolean isWork() {
        return isWork;
    }

    public void setWork(boolean work) {
        isWork = work;
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
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand: executed");
        return super.onStartCommand(intent, flags, startId);
    }


    //可用方法
    private String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Not available";
    }

    /**
     * 开启Wifi Socket与数据读取线程
     */
    private void openSocket() throws IOException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new ServerSocket(port);
                    getNetinfo(false);//ipAddress = getLocalIpAddress();
                    Log.d("WifiInfoService", "socket:"+ipAddress);
                    // 等待客户端连接
                    clientSocket = socket.accept();
                    Log.d("WifiInfoService", "Client connected");
                    outputStream = clientSocket.getOutputStream();
                    inputStream = clientSocket.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    OpenSocketThread();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 关闭Wifi Socket 及 线程
     */
    private void closeSocket() throws IOException {
        closeSocketThread();
        outputStream.close();
        inputStream.close();
        socket.close();
    }

    /**
     * 开启Command State Control 线程
     */
    public void OpenSocketThread() {
        if (deviceThread == null) {
            deviceThread = new Thread(new Runnable() {
                String message;

                @Override
                public void run() {
                    while (isWork) {
                        if (reader != null) {
                            try {
                                message = reader.readLine();
                                //Object dataOp(message) 数据解析
                                notifyObserver(DataType.WIFI_COMMAND_STATE, message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }, ThreadName);
            deviceThread.start();
        }
    }

    /**
     * Activity通过服务下发command
     *
     * @param Command
     * @throws IOException
     */
    public void sendMessage(byte[] Command) throws IOException {
        if (Command != null) {
            if (Command.length > 0) {
                outputStream.write(Command);
                Log.d(TAG,"sendMessage:"+Command.length);
            }
        }

    }

    /**
     * 关闭Command State 读取 Control 线程
     */
    public void closeSocketThread() {
        setWork(false);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    private void startSendingRandomNumbers() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int randomNumber = generateRandomNumber();
                numberLiveData.postValue(randomNumber);
            }
        }).start();
    }

    private int generateRandomNumber() {
        Random random = new Random();
        return random.nextInt(80) + 20;
    }

    public static MutableLiveData<Integer> getNumberLiveData() {
        return numberLiveData;
    }


    /**
     * 结束服务
     */
    public void stopMyService() {
        closeSocketThread();
        try {
            closeSocket();
        } catch (IOException e) {
            Log.e(TAG, "Error closing UART device:", e);
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        Log.d(TAG, "onDestroy: executed");

    }
}
