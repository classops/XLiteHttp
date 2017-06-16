package com.example.litehttpdemo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import org.json.JSONObject;

import java.util.Locale;


public class Utils {

    private static final String PALTFORM_ANDROID = "Android";

    public static String getApiUserAgent(Context context) {
        String userAgent = "";

        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            StringBuilder sb = new StringBuilder();
            sb.append(context.getString(R.string.app_name));
            sb.append("/");
            sb.append(pi.versionName);
            sb.append(" (");
            sb.append("Android; ");
            sb.append("U; ");
            sb.append("Android ");
            sb.append(Build.VERSION.RELEASE);
            sb.append("; ");
            sb.append(getSystemLanguage());
            sb.append("; ");

            sb.append(Build.MODEL);
            sb.append(" Build/");
            sb.append(Build.ID);

            sb.append(")");

            userAgent = sb.toString();


            Log.d("test", "User - " + Build.USER + ", " + "Time - " + Build.TIME + ", TYPE - " + Build.TYPE);


            //

            // 一般有问题厂商，不更新系统，使用软件信息，另外可以通过 process 来获取系统信息，可以排除 6.0 以下机器。

            //

            Log.d("test", "Host - " + Build.HOST + ", " + "Id - " + Build.ID);

            Log.d("test", "Build - " + Build.class.hashCode());

            Log.d("test", userAgent);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return userAgent;
    }

    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static String getApiExtraInfo(Context context) {
        JSONObject json = new JSONObject();
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);

            // TODO 添加一些额外信息，设备ID

            json.put("platform", PALTFORM_ANDROID);
            json.put("phone", Build.MODEL);
//            json.put("terminalid", DeviceInfoUtils.getDeviceId(context));
            json.put("appversioncode", info.versionCode);
            json.put("appversion", info.versionName);
            json.put("osversion", Build.VERSION.RELEASE);
            json.put("apilevel", Build.VERSION.SDK_INT); // API
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e("UserAgent", json.toString());

        return json.toString();
    }
}
