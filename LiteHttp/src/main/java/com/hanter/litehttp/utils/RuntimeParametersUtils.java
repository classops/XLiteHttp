package com.hanter.litehttp.utils;

/**
 * 类名：RuntimeParametersUtils <br/>
 * 描述：获取运行时内存、CPU参数 <br/>
 * 创建时间：2017/1/6 11:05
 *
 * @author wangmingshuo
 * @version 1.0
 */

public class RuntimeParametersUtils {

    /** 内存缓存大小 */
    private static final long MEMORY_CACHE_MAX_SIZE = 60 * 1024 * 1024;

    public static int getAutofitThreadCount() {
        return Runtime.getRuntime().availableProcessors() * 2;
    }

    public static long getAutofitMemory() {
        long maxMemory = Runtime.getRuntime().maxMemory();

        long cacheSize = maxMemory / 6;
        if (cacheSize > MEMORY_CACHE_MAX_SIZE) {
            cacheSize = MEMORY_CACHE_MAX_SIZE;
        }

        return cacheSize;
    }

}
