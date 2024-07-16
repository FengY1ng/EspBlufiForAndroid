package com.espressif.espblufi.design;


/**
 *
 */
public interface MyUASubject {
     /**
      * 注册观察者
      * @param o
      */
     void registerObserver(MyUAObServer o);

     /**
      * 移除观察者
      * @param o
      */
     void removeObserver(MyUAObServer o);

     /**
      * 通知刷新
      * @param dataType
      * @param messagedata
      */
     void notifyObserver(int dataType, String messagedata);
}
