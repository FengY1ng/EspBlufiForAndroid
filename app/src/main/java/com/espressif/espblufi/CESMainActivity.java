package com.espressif.espblufi;


import static com.espressif.espblufi.data.DataType.OP_MODE_POS_STA;
import static com.espressif.espblufi.data.DataType.OP_MODE_VALUES;
import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.core.app.ActivityCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.fastjson.JSONException;
import com.espressif.espblufi.app.BlufiApp;
import com.espressif.espblufi.app.BlufiLog;
import com.espressif.espblufi.constants.BlufiConstants;
import com.espressif.espblufi.constants.SettingsConstants;
import com.espressif.espblufi.service.MusicService;
import com.espressif.espblufi.tools.WifiUtils;
import com.espressif.espblufi.ui.BlufiActivity;
import com.espressif.espblufi.ui.ConfigureOptionsActivity;
import com.espressif.espblufi.ui.LoginActivity;
import com.espressif.espblufi.ui.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import blufi.espressif.BlufiCallback;
import blufi.espressif.BlufiClient;
import blufi.espressif.params.BlufiConfigureParams;
import blufi.espressif.params.BlufiParameter;
import blufi.espressif.response.BlufiScanResult;
import blufi.espressif.response.BlufiStatusResponse;
import blufi.espressif.response.BlufiVersionResponse;

public class CESMainActivity extends BaseActivity {
    public int REQUEST_WIFI_CONFIGURE = 9527;

    private SharedPreferences sharedPreferences;
    private boolean socketisReady = false;
    private String ip ;
    private int port;
    private volatile boolean mConnected;
    private String def_SSID = "EMAT";
    private String def_PASSWORD = "12345678";
    private BlufiClient mBlufiClient;
    private String getBLEMAC_BY_QR ="GET_BLEMAC_BY_QR";
    private ExecutorService mThreadPool;
    private static final long TIMEOUT_SCAN = 4000L;
    private static final int REQUEST_PERMISSION = 0x01;
    private static final int REQUEST_BLUFI = 0x10;
    private final BlufiLog mLog = new BlufiLog(getClass());
    private List<ScanResult> mBleList;
    private Map<String, ScanResult> mDeviceMap;
    private ScanCallback mScanCallback;
    private String mBlufiFilter;
    private volatile long mScanStartTime;
    private Future<Boolean> mUpdateFuture;
    private MusicService musicPlayerService;
    private boolean isBound = false;
    private Fragment currentFragment;
    private static  int CANNOT_BLUETOOTHS = 0;
    private static  int IS_BLUETOOTHS_STATE = 1;
    private static  int BLUETOOTHS_ALLREADY = 2;
    private int fl_bt_goState = IS_BLUETOOTHS_STATE;
    private FloatingActionButton floatingActionButton;
    private String TAG = "CESMainActivity";
    private BluetoothDevice mDevice;
    private WifiManager wifiManager ;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private List<Fragment> fragmentList;
    private Button btnCreatePlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cesmain);

        initSharedPreference();

        btnCreatePlan = findViewById(R.id.btnCreatePlan);

        btnCreatePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CESMainActivity.this, CreatePlanActivity.class);
                startActivity(intent);
            }
        });

        viewPager = findViewById(R.id.viewPager);
        fragmentList = new ArrayList<>();
        fragmentList.add(new CESMainFragment());
        fragmentList.add(new CESOtherFragment());

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(viewPagerAdapter);


        floatingActionButton = findViewById(R.id.FAbt_scanQr);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        CESMainFragment cesMainFragment = new CESMainFragment();
        // transaction.replace(R.id.fragment_container, cesMainFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        mThreadPool = Executors.newSingleThreadExecutor();
        // 加载FragmentA
        // loadFragment(new CESMainFragment());
        wifiManager =  (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        getSSIDRawData(info);


        //BlufiConfigureParams params = checkInfo(OP_MODE_POS_STA,SSID,PASSWORD) ;
        Log.d(TAG,"SSID:"+info.getSSID()+"，getBSSID:"+info.getBSSID());
        //更新fragment 测试
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (currentFragment instanceof CESMainFragment) {
                    //((CESMainFragment)currentFragment).updateChart(number);
                }
            }
        });//.start();

        if (musicPlayerService == null) {
            Intent intent = new Intent(CESMainActivity.this, MusicService.class);
            //intent.putExtra(MusicService.MUSIC_KEY,musicSelcetid);
            //intent.putExtra(MusicService.TIME_KEY,time);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        mBleList = new LinkedList<>();
        mDeviceMap = new HashMap<>();
        mScanCallback = new ScanCallback();

        List<String> permissionList = new ArrayList<>();
        permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionList.add(Manifest.permission.BLUETOOTH_SCAN);
            permissionList.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        ActivityCompat.requestPermissions(
                this,
                permissionList.toArray(new String[0]),
                REQUEST_PERMISSION
        );
    }

    private void initSharedPreference() {
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
    }

    private boolean isMusicServiceRun()
    {
        if(musicPlayerService!=null)
        {
           return true;
        }
        else {
            Toast.makeText(this,"音乐服务未启动.",Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public void setMusic(int musicId , int mytreatmentTimes)
    {
        if(isMusicServiceRun())
        {
            musicPlayerService.setMusic(musicId,mytreatmentTimes);
        }
    }

    public void startMusic()
    {
        if(isMusicServiceRun())
        {
            musicPlayerService.startTreatmentMusicStart();;
        }
    }
    public void stopMusic()
    {
        if(isMusicServiceRun())
        {
            musicPlayerService.stopMusic();
        }
    }
    public void pauseMusic()
    {
        if(isMusicServiceRun())
        {
            musicPlayerService.pauseMusic();
        }
    }

    public void resumeMusic()
    {
        if(isMusicServiceRun())
        {
            musicPlayerService.resumeMusic();
        }
    }
    public int getMusicState()
    {
        return musicPlayerService.getMusicState();
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicPlayerBinder binder = (MusicService.MusicPlayerBinder) service;
            musicPlayerService = binder.getService();
            musicPlayerService.setMusicPlayerListener(CESMainActivity.this);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicPlayerService = null;
            isBound = false;
        }
    };

    public void onProgressUpdate(int progress) {
        Log.d(TAG,"progress:"+progress);
        if (currentFragment instanceof CESMainFragment){
            ((CESMainFragment)currentFragment).updateProgress(progress);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }
    public boolean isSocketisReady() {
        return socketisReady;
    }

    public void setSocketisReady(boolean socketisReady) {
        this.socketisReady = socketisReady;
    }
    @Override
    public void updataUI(Message urData) {
        //线图刷新 解urData 获取number
        if (currentFragment instanceof CESMainFragment) {
            //((CESMainFragment)currentFragment).updateChart(number);
        }
    }

    @Override
    public void updataNoUI(int dataType, String data) {

    }

    @Override
    protected void setState() {
        setCanUseDevice(true);
        setCanUiShow(true);
    }
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
        currentFragment = fragment;
    }

    /**
     * 1.fl_bt_goState == IS_BLUETOOTHS_STATE   蓝牙业务进行中
     * 2.fl_bt_goState == BLUETOOTHS_ALLREADY   蓝牙业务完成 再次点击切换页面
     * 3.fl_bt_goState == CANNOT_BLUETOOTHS     蓝牙不可用 应用无法完成功能
     * @param view
     */
    public void switchFragment(View view) {
        if(fl_bt_goState == IS_BLUETOOTHS_STATE)
        {
            //蓝牙扫描
            IntentIntegrator integrator = new IntentIntegrator(CESMainActivity.this);
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
            //FL按钮锁死 直到蓝牙扫描成功或失败
            floatingActionButton.setEnabled(false);
        }else if(fl_bt_goState == BLUETOOTHS_ALLREADY)
        {
            //Fragment切换
            Fragment fragment;
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            //Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
            //根据当前显示的Fragment切换到另一个Fragment
            if (currentFragment instanceof CESMainFragment) {
                fragment = new CESOtherFragment();
            } else {
                fragment = new CESMainFragment();
            }
            // transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }else if(fl_bt_goState == CANNOT_BLUETOOTHS)
        {
            //蓝牙不可用提示
            Toast.makeText(CESMainActivity.this,"本机蓝牙不可用。",Toast.LENGTH_LONG).show();
        }

    }

    private void scan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (!adapter.isEnabled() || scanner == null) {
            Toast.makeText(this, R.string.main_bt_disable_msg, Toast.LENGTH_SHORT).show();
            //mBinding.refreshLayout.setRefreshing(false);
            //fl_bt_goState = CANNOT_BLUETOOTHS;
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check location enable
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean locationEnable = locationManager != null && LocationManagerCompat.isLocationEnabled(locationManager);
            if (!locationEnable) {
                Toast.makeText(this, R.string.main_location_disable_msg, Toast.LENGTH_SHORT).show();
                //mBinding.refreshLayout.setRefreshing(false);
                //fl_bt_goState = CANNOT_BLUETOOTHS;
                return;
            }
        }
        mDeviceMap.clear();
        mBleList.clear();
        mBlufiFilter = (String) BlufiApp.getInstance().settingsGet(SettingsConstants.PREF_SETTINGS_KEY_BLE_PREFIX,
                BlufiConstants.BLUFI_PREFIX);
        mScanStartTime = SystemClock.elapsedRealtime();

        mLog.d("Start scan ble");
        scanner.startScan(null, new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(),
                mScanCallback);
        //每秒对设备列表重新排序
        mUpdateFuture = mThreadPool.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                long scanCost = SystemClock.elapsedRealtime() - mScanStartTime;
                if (scanCost > TIMEOUT_SCAN) {
                    break;
                }
                onIntervalScanUpdate(false);
            }

            BluetoothLeScanner inScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
            if (inScanner != null) {
                inScanner.stopScan(mScanCallback);
            }
            onIntervalScanUpdate(true);
            mLog.d("Scan ble thread is interrupted");
            return true;
        });
    }

    private void stopScan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (scanner != null) {
            scanner.stopScan(mScanCallback);
        }
        if (mUpdateFuture != null) {
            mUpdateFuture.cancel(true);
        }
        mLog.d("Stop scan ble");
    }

    /**
     * 根据序号对设备重新排序[可去]
     * @param over
     */
    private void onIntervalScanUpdate(boolean over) {
        List<ScanResult> devices = new ArrayList<>(mDeviceMap.values());
        Collections.sort(devices, (dev1, dev2) -> {
            Integer rssi1 = dev1.getRssi();
            Integer rssi2 = dev2.getRssi();
            return rssi2.compareTo(rssi1);
        });
        runOnUiThread(() -> {
            mBleList.clear();
            mBleList.addAll(devices);
            if (over) {
                //mBinding.refreshLayout.setRefreshing(false);
                //fl_bt_goState = CANNOT_BLUETOOTHS;
            }
        });
    }

    public void closeScanBle()
    {
        stopScan();
        mThreadPool.shutdownNow();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int size = permissions.length;
        for (int i = 0; i < size; ++i) {
            String permission = permissions[i];
            int grant = grantResults[i];

            if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (grant == PackageManager.PERMISSION_GRANTED) {
                    //mBinding.refreshLayout.setRefreshing(true);
                    fl_bt_goState = IS_BLUETOOTHS_STATE;
                    scan();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE)
        {
            if(result != null) {
                if(result.getContents() == null) {
                    Log.d(TAG,"Cancelled:");
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG,"Scanned: " + result.getContents());
                    getBLEMAC_BY_QR = result.getContents();
                    Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                    //通过二维码扫描拿到蓝牙MAC，开始scan业务
                    scan();
                }
            } else {
                Log.d(TAG,"result:null");
            }
        }
        if (requestCode == REQUEST_BLUFI) {
            //mBinding.refreshLayout.setRefreshing(true);
            fl_bt_goState = IS_BLUETOOTHS_STATE;
            scan();
            return;
        }
        if (requestCode == REQUEST_WIFI_CONFIGURE) {
            if (!mConnected) {
                return;
            }
            if (resultCode == RESULT_OK) {
                BlufiConfigureParams params =
                        (BlufiConfigureParams) data.getSerializableExtra(BlufiConstants.KEY_CONFIGURE_PARAM);
                configure(params);
                ip = getCESDeviceService().getIpAddress();
                port = getCESDeviceService().getPort();
                Log.d(TAG,"socket ip:"+ip+",port:"+port);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(7000);
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("ip", ip);
                            jsonObject.put("port",port);
                            //String jsonStr = "{\"ip\": \""+ip+"\",\"port\": "+port+"}";
                            //Log.d(TAG,"jsonStr:"+jsonStr);
                            byte[] message = jsonObject.toJSONString().getBytes();
                            Log.d(TAG,"message:"+byteArrayToString(message));
                            sendSocketParm(message);

                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).start();
                /**
                JSONObject jsonObject = new JSONObject();
                try {

                    jsonObject.put("ip", ip);
                    jsonObject.put("port",port);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                String jsonStr = jsonObject.toJSONString();
                byte[] message = jsonStr.getBytes();
                sendSocketParm(message);**/
            }

            return;
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String byteArrayToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(b).append(" ");
        }
        return sb.toString();
    }

    private class ScanCallback extends android.bluetooth.le.ScanCallback {

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                onLeScan(result);
            }
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            onLeScan(result);
        }

        private void onLeScan(ScanResult scanResult) {
            String name = scanResult.getDevice().getName();
            if (!TextUtils.isEmpty(mBlufiFilter)) {
                if (name == null || !name.startsWith(mBlufiFilter)) {
                    return;
                }
            }
            //这里判断是有蓝牙MAC与get_BLEMAC_BYQR相等
            if(scanResult.getDevice().getAddress().equals(getBLEMAC_BY_QR))
            {
                Log.d(TAG,"QR:"+getBLEMAC_BY_QR+"发现目标 -- 元神~启动！");
                mDevice = scanResult.getDevice();
                //发现目标 -- 元神~启动！
                Log.d(TAG,"QR:"+getBLEMAC_BY_QR+"发现目标 -- 开始链接！");
                connect();
                //get！别找了 歇着吧！
                stopScan();
            }
            else {
                Log.d(TAG,"QR:"+getBLEMAC_BY_QR+",BLE SCAN:"+scanResult.getDevice().getAddress());
            }

            mDeviceMap.put(scanResult.getDevice().getAddress(), scanResult);
        }
    }

    /**
     * Try to connect device
     */
    private void connect() {
        //mContent.blufiConnect.setEnabled(false);
        if (mBlufiClient != null) {
            mBlufiClient.close();
            mBlufiClient = null;
        }

        mBlufiClient = new BlufiClient(getApplicationContext(), mDevice);
        mBlufiClient.setGattCallback(new GattCallback());
        mBlufiClient.setBlufiCallback(new BlufiCallbackMain());
        mBlufiClient.setGattWriteTimeout(BlufiConstants.GATT_WRITE_TIMEOUT);
        mBlufiClient.connect();
    }
    /**
     * Request device disconnect the connection.
     */
    private void disconnectGatt() {
        //mContent.blufiDisconnect.setEnabled(false);
        if (mBlufiClient != null) {
            mBlufiClient.requestCloseConnection();
        }
    }
    /**
     * mBlufiClient call onCharacteristicWrite and onCharacteristicChanged is required
     */
    private class GattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String devAddr = gatt.getDevice().getAddress();
            mLog.d(String.format(Locale.ENGLISH, "onConnectionStateChange addr=%s, status=%d, newState=%d",
                    devAddr, status, newState));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        //onGattConnected();
                        mConnected = true;
                        updateMessage(String.format("Connected %s", devAddr), false);
                        Log.d(TAG,"status == BluetoothGatt.GATT_SUCCESS");

                        Intent intent = new Intent(CESMainActivity.this, ConfigureOptionsActivity.class);
                        startActivityForResult(intent, REQUEST_WIFI_CONFIGURE);
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        gatt.close();
                        mConnected = false;
                        //onGattDisconnected();
                        updateMessage(String.format("Disconnected %s", devAddr), false);
                        break;
                }
            } else {
                gatt.close();
                //onGattDisconnected();
                updateMessage(String.format(Locale.ENGLISH, "Disconnect %s, status=%d", devAddr, status),
                        false);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            mLog.d(String.format(Locale.ENGLISH, "onMtuChanged status=%d, mtu=%d", status, mtu));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                updateMessage(String.format(Locale.ENGLISH, "Set mtu complete, mtu=%d ", mtu), false);
            } else {
                mBlufiClient.setPostPackageLengthLimit(20);
                updateMessage(String.format(Locale.ENGLISH, "Set mtu failed, mtu=%d, status=%d", mtu, status), false);
            }
            //onGattServiceCharacteristicDiscovered();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            mLog.d(String.format(Locale.ENGLISH, "onServicesDiscovered status=%d", status));
            if (status != BluetoothGatt.GATT_SUCCESS) {
                gatt.disconnect();
                updateMessage(String.format(Locale.ENGLISH, "Discover services error status %d", status), false);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            mLog.d(String.format(Locale.ENGLISH, "onDescriptorWrite status=%d", status));
            if (descriptor.getUuid().equals(BlufiParameter.UUID_NOTIFICATION_DESCRIPTOR) &&
                    descriptor.getCharacteristic().getUuid().equals(BlufiParameter.UUID_NOTIFICATION_CHARACTERISTIC)) {
                String msg = String.format(Locale.ENGLISH, "Set notification enable %s", (status == BluetoothGatt.GATT_SUCCESS ? " complete" : " failed"));
                updateMessage(msg, false);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                gatt.disconnect();
                updateMessage(String.format(Locale.ENGLISH, "WriteChar error status %d", status), false);
            }
        }
    }

    public void sendSocketParm(byte[] parmBytes)
    {
        if(mBlufiClient!=null) {
            mBlufiClient.postCustomData(parmBytes);
        }else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CESMainActivity.this,"没有建立socket。",Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
    private class BlufiCallbackMain extends BlufiCallback {
        @Override
        public void onGattPrepared(
                BlufiClient client,
                BluetoothGatt gatt,
                BluetoothGattService service,
                BluetoothGattCharacteristic writeChar,
                BluetoothGattCharacteristic notifyChar
        ) {
            if (service == null) {
                mLog.w("Discover service failed");
                gatt.disconnect();
                updateMessage("Discover service failed", false);
                return;
            }
            if (writeChar == null) {
                mLog.w("Get write characteristic failed");
                gatt.disconnect();
                updateMessage("Get write characteristic failed", false);
                return;
            }
            if (notifyChar == null) {
                mLog.w("Get notification characteristic failed");
                gatt.disconnect();
                updateMessage("Get notification characteristic failed", false);
                return;
            }

            updateMessage("Discover service and characteristics success", false);

            int mtu = BlufiConstants.DEFAULT_MTU_LENGTH;
            mLog.d("Request MTU " + mtu);
            boolean requestMtu = gatt.requestMtu(mtu);
            if (!requestMtu) {
                mLog.w("Request mtu failed");
                updateMessage(String.format(Locale.ENGLISH, "Request mtu %d failed", mtu), false);
                //onGattServiceCharacteristicDiscovered();
            }
        }

        @Override
        public void onNegotiateSecurityResult(BlufiClient client, int status) {
            if (status == STATUS_SUCCESS) {
                updateMessage("Negotiate security complete", false);
            } else {
                updateMessage("Negotiate security failed， code=" + status, false);
            }
        }

        @Override
        public void onPostConfigureParams(BlufiClient client, int status) {
            if (status == STATUS_SUCCESS) {
                updateMessage("Post configure params complete", false);
            } else {
                updateMessage("Post configure params failed, code=" + status, false);
            }
        }

        @Override
        public void onDeviceStatusResponse(BlufiClient client, int status, BlufiStatusResponse response) {
            if (status == STATUS_SUCCESS) {
                updateMessage(String.format("Receive device status response:\n%s", response.generateValidInfo()),
                        true);
            } else {
                updateMessage("Device status response error, code=" + status, false);
            }
        }

        @Override
        public void onDeviceScanResult(BlufiClient client, int status, List<BlufiScanResult> results) {
            if (status == STATUS_SUCCESS) {
                StringBuilder msg = new StringBuilder();
                msg.append("Receive device scan result:\n");
                for (BlufiScanResult scanResult : results) {
                    msg.append(scanResult.toString()).append("\n");
                }
                updateMessage(msg.toString(), true);
            } else {
                updateMessage("Device scan result error, code=" + status, false);
            }
        }

        @Override
        public void onDeviceVersionResponse(BlufiClient client, int status, BlufiVersionResponse response) {
            if (status == STATUS_SUCCESS) {
                updateMessage(String.format("Receive device version: %s", response.getVersionString()),
                        true);
            } else {
                updateMessage("Device version error, code=" + status, false);
            }
        }

        @Override
        public void onPostCustomDataResult(BlufiClient client, int status, byte[] data) {
            String dataStr = new String(data);
            String format = "Post data %s %s";
            if (status == STATUS_SUCCESS) {
                updateMessage(String.format(format, dataStr, "complete"), false);
            } else {
                updateMessage(String.format(format, dataStr, "failed"), false);
            }
        }

        @Override
        public void onReceiveCustomData(BlufiClient client, int status, byte[] data) {
            if (status == STATUS_SUCCESS) {
                String customStr = new String(data);
                updateMessage(String.format("Receive custom data:\n%s", customStr), true);
            } else {
                updateMessage("Receive custom data error, code=" + status, false);
            }
        }

        @Override
        public void onError(BlufiClient client, int errCode) {
            updateMessage(String.format(Locale.ENGLISH, "Receive error code %d", errCode), false);
            if (errCode == CODE_GATT_WRITE_TIMEOUT) {
                updateMessage("Gatt write timeout", false);
                client.close();
            } else if (errCode == CODE_WIFI_SCAN_FAIL) {
                updateMessage("Scan failed, please retry later", false);
            }
        }
    }
    private void updateMessage(String message, boolean isNotificaiton) {
        Log.d(TAG,"mess:"+isNotificaiton+":"+message);
    }
    /**
     * Request to configure station or softap
     *
     * @param params configure params
     */
    private void configure(BlufiConfigureParams params) {
        mBlufiClient.configure(params);
    }

    private BlufiConfigureParams checkInfo(int deviceModeSp,String ssid,String possword ) {
        if(deviceModeSp<0||deviceModeSp>2) {
            Log.d(TAG,"超出类型选择范围！");
            return null ;
        }
        BlufiConfigureParams params = new BlufiConfigureParams();
        int deviceMode = OP_MODE_VALUES[deviceModeSp];
        params.setOpMode(deviceMode);
        switch (deviceMode) {
            case BlufiParameter.OP_MODE_NULL:
                return params;
            case BlufiParameter.OP_MODE_STA:
                if (checkSta(params,ssid,possword)) {
                    return params;
                } else {
                    return null;
                }
            case BlufiParameter.OP_MODE_SOFTAP:
                Log.d(TAG,"添加方法：checkSoftAP(params)");
                return null;
            case BlufiParameter.OP_MODE_STASOFTAP:
                Log.d(TAG,"添加关于OP_MODE_STASOFTAP的方法");
                return null;
        }
        return null;
    }

    private boolean checkSta(BlufiConfigureParams params,String ssid,String password) {
        if(ssid==null) return false;
        //params.setOpMode(BlufiParameter.OP_MODE_STA);
        params.setStaSSIDBytes(ssid.getBytes());
        Log.d(TAG,TAG+"_ssid:"+params.getStaSSIDBytes().toString());
        params.setStaPassword(password);
        return true;
    }


    private byte[] getSSIDRawData(WifiInfo info) {
        try {
            Method method = info.getClass().getMethod("getWifiSsid");
            method.setAccessible(true);
            Object wifiSsid = method.invoke(info);
            Log.d(TAG,"wifiSsid:"+wifiSsid.toString());
            if (wifiSsid == null) {
                return null;
            }
            method = wifiSsid.getClass().getMethod("getOctets");
            method.setAccessible(true);
            return (byte[]) method.invoke(wifiSsid);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void logout(View view) {
        sharedPreferences.edit().putBoolean("is_logged_in", false).apply();
        Intent intent = new Intent(CESMainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}