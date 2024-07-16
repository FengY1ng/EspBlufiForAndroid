package com.espressif.espblufi.data;

import blufi.espressif.params.BlufiParameter;

public class DataType {

    public static final int TYPE_CMD = 0 ;
    public static final int TYPE_PRM = 1 ;


    public static final int CMD_START = 0 ;
    public static final int CMD_PAUSE = 1 ;
    public static final int CMD_END = 2 ;

    /**
     * 服务与活动之间的数据通讯类型
     */
    public static int WIFI_COMMAND_STATE = 1; //下位机实时控制与读取
    public static int WIFI_DATA_OP = 2;    //服务器数据通讯
    public static int WIFI_MUSIC_OP = 3;    //音乐播放

    /**
     * WIFI组网角色类型与安全类型
     */
    public static final int OP_MODE_POS_STA = 0;
    public static final int OP_MODE_POS_SOFTAP = 1;
    public static final int OP_MODE_POS_STASOFTAP = 2;

    public static final int[] OP_MODE_VALUES = {
            BlufiParameter.OP_MODE_STA,
            BlufiParameter.OP_MODE_SOFTAP,
            BlufiParameter.OP_MODE_STASOFTAP
    };
    public static final int[] SOFTAP_SECURITY_VALUES = {
            BlufiParameter.SOFTAP_SECURITY_OPEN,
    //      BlufiParameter.SOFTAP_SECURITY_WEP,
            BlufiParameter.SOFTAP_SECURITY_WPA,
            BlufiParameter.SOFTAP_SECURITY_WPA2,
            BlufiParameter.SOFTAP_SECURITY_WPA_WPA2
    };

}
