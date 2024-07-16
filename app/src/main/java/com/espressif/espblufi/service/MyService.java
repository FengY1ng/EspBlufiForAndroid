package com.espressif.espblufi.service;

import android.app.Service;


import com.espressif.espblufi.data.DataType;
import com.espressif.espblufi.design.MyUAObServer;
import com.espressif.espblufi.design.MyUASubject;

import java.util.ArrayList;


public abstract class MyService extends Service implements MyUASubject {


    private ArrayList<MyUAObServer> myUaObservers;

    public MyService() {
        myUaObservers = new ArrayList<>();
    }


    /**
     * 观察者模式 注册
     **/
    @Override
    public void registerObserver(MyUAObServer o) {
        if (myUaObservers != null && o != null) {
            myUaObservers.add(o);
        }
    }

    /**
     * 观察者模式 移除
     **/
    @Override
    public void removeObserver(MyUAObServer o) {
        if (o != null) {
            int i = myUaObservers.indexOf(o);
            myUaObservers.remove(i);
        }

    }

    /**
     * 观察者模式 通知观察者
     *
     * @param dataType    数据类型
     * @param messagedata 数据信息
     */
    @Override
    public void notifyObserver(int dataType, String messagedata) {
        for (int i = 0; i < myUaObservers.size(); i++) {
            MyUAObServer observer = (MyUAObServer) myUaObservers.get(i);
            if (observer.getCES_Service_Info() && (dataType == DataType.WIFI_COMMAND_STATE)) {
                observer.update(dataType, messagedata);
            }
            if (observer.getCES_State_Info() && dataType == DataType.WIFI_DATA_OP) {
                observer.update(dataType, messagedata);
            }
        }

    }
}