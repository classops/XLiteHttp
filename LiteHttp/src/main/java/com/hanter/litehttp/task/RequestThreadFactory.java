package com.hanter.litehttp.task;

import android.support.annotation.NonNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 类名：RequestThreadFactory <br/>
 * 描述：ThreadPoolExecutor执行的线程工厂，方便中断执行 <br/>
 * 创建时间：2017/1/5 14:05
 *
 * @author wangmingshuo
 * @version 1.0
 */

public class RequestThreadFactory implements ThreadFactory {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public RequestThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" +
                poolNumber.getAndIncrement() +
                "-thread-";
    }

    public Thread newThread(@NonNull Runnable r) {
        Thread t = new BlockingTaskThread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);

        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }

}
