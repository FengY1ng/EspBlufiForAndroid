package com.espressif.espblufi.design;


/**
 * 观察者
 */
public interface MyUAObServer {
     /**
      * 实时获取CES下位机数据方法
      * @return
      */
     boolean getCES_State_Info();

     /**
      * 实时获取服务器变更信息信息
      * @return
      */
     boolean getCES_Service_Info();

     /**
      * 刷新信息
      * @param dataType
      * @param messagedata
      */
     void update(int dataType, String messagedata);
}
