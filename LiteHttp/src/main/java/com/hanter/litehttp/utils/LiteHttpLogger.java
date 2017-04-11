package com.hanter.litehttp.utils;

import android.util.Log;

/**
 * 类名：LiteHttpLogger <br/>
 * 描述：调试Log
 * 创建时间：2016/01/25 21:07
 *
 * @author hanter
 * @version 1.0
 */
public class LiteHttpLogger {

    public final static boolean DEBUG = LiteHttpConfig.DEBUG;

    public static void i(boolean debug, String tag, String msg) {
        if (debug) {
            Log.i(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        i(DEBUG, tag, msg);
    }

    public static void i(String msg) {
        i(DEBUG, getFunctionName(), msg);
    }

    public static void v(boolean debug, String tag, String msg) {
        if (debug) {
            Log.v(tag, msg);
        }
    }

    public static void v(String msg) {
        v(DEBUG, getFunctionName(), msg);
    }

    public static void v(String tag, String msg) {
        v(DEBUG, tag, msg);
    }

    public static void d(boolean debug, String tag, String msg) {
        if (debug) {
            Log.d(tag, msg);
        }
    }

    public static void d(String msg) {
        d(DEBUG, getFunctionName(), msg);
    }

    public static void d(String tag, String msg) {
        d(DEBUG, tag, msg);
    }

    public static void e(boolean debug, String tag, String msg) {
        if (debug) {
            Log.e(tag, msg);
        }
    }

    public static void e(String msg) {
        e(DEBUG, getFunctionName(), msg);
    }

    public static void e(String tag, String msg) {
        e(DEBUG, tag, msg);
    }

    public static void w(boolean debug, String tag, String msg) {
        if (debug) {
            Log.w(tag, msg);
        }
    }

    public static void w(String msg) {
        w(DEBUG, getFunctionName(), msg);
    }

    public static void w(String tag, String msg) {
        w(DEBUG, tag, msg);
    }

    private static String getFunctionName() {
        StackTraceElement caller = new Throwable().fillInStackTrace()
                .getStackTrace()[2];
        return caller.getFileName() + "." + caller.getMethodName() + "(" + caller.getLineNumber() + ")";
    }

}
