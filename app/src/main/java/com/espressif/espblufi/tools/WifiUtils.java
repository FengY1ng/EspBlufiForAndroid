package com.espressif.espblufi.tools;
import static android.content.Context.WIFI_SERVICE;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

public class WifiUtils {

    private static String TAG = "WifiUtils";

    public static WifiConfiguration getWifiConfiguration(Context context){
        WifiConfiguration mWifiConfig = null;
        try{
            WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
            Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
            method.setAccessible(true);
            mWifiConfig = (WifiConfiguration) method.invoke(wifiManager);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return mWifiConfig;
    }


    /**
     * @param flag 0: ssid
     *             1: pwd
     */
    public static void getApSSIDAndPwd(WifiManager wifiManager) {
        if (wifiManager != null) {
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();

            if (configuredNetworks != null) {
                for (WifiConfiguration config : configuredNetworks) {
                    Log.d("WifiInfo", "SSID: " + config.SSID);
                    if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
                        String ssid = config.SSID;
                        String password = config.preSharedKey;

                        Log.d("WifiInfo", "SSID: " + ssid);
                        Log.d("WifiInfo", "Password: " + password);
                    }
                }
            }
        } else {
            Log.e("WifiInfo", "WifiManager is null");
        }
    }





    public static String getCurrentSsid(WifiManager wifiManager) {
        String ssid = null;
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            ssid = wifiManager.getConnectionInfo().getSSID();
            Log.d(TAG ,"ssid:"+ssid );
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
                Log.d(TAG ,"ssid 2:"+ssid );
            }
        }
        return ssid;
    }

    public static String getCurrentWifiPassword(WifiManager wifiManager) {
        String ssid = getCurrentSsid(wifiManager);
        String password = null;
        if (ssid != null) {
            WifiConfiguration wifiConfig = findWifiConfiguration(wifiManager, ssid);
            if (wifiConfig != null) {
                password = wifiConfig.preSharedKey;
                Log.d(TAG ,"password:"+password );
            }
        }
        return password;
    }

    private static WifiConfiguration findWifiConfiguration(WifiManager wifiManager, String ssid) {

        if (wifiManager != null) {
            for (WifiConfiguration config : wifiManager.getConfiguredNetworks()) {
                if (config.SSID != null && config.SSID.equals("\"" + ssid + "\"")) {
                    return config;
                }
            }
        }
        return null;
    }

}