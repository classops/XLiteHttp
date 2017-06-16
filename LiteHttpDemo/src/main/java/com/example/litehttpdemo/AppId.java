package com.example.litehttpdemo;

import java.util.UUID;

public class AppId {

    /**
     * 生成 AppId
     * @return AppId
     */
    public String generateAppId() {
        return UUID.randomUUID().toString();
    }

    public String getDeviceId() {
//        Build.SERIAL


        return "";
//        TelephonyManager tm = ;
//        tm.getDeviceId()
    }
}
