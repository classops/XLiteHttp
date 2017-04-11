package com.hanter.litehttp;

import android.os.Process;

import com.hanter.litehttp.cache.Cache;
import com.hanter.litehttp.http.exception.CancelError;
import com.hanter.litehttp.http.exception.NoCacheError;
import com.hanter.litehttp.utils.LiteHttpLogger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 类名：CacheDispatcher <br/>
 * 描述：缓存队列机制 <br/>
 * 创建时间：2017/1/6 11:18
 *
 * @author wangmingshuo
 * @version 1.0
 */

public class CacheDispatcher extends Thread {

    private final static String TAG = "CacheDispatcher";

    private final BlockingQueue<Request<?>> mCacheQueue;
    private final BlockingQueue<Request<?>> mNetworkQueue;
    private final Cache mCache;
    private final ExecutorDelivery mDelivery;

    private final AtomicBoolean mQuit;

    public CacheDispatcher(BlockingQueue<Request<?>> cacheQueue,
                           BlockingQueue<Request<?>> networkQueue,
                           Cache cache,
                           ExecutorDelivery delivery) {
        this.mCacheQueue = cacheQueue;
        this.mNetworkQueue = networkQueue;
        this.mCache = cache;
        this.mDelivery = delivery;

        this.mQuit = new AtomicBoolean(false);
    }

    public void quit() {
        LiteHttpLogger.d(TAG, "quit");
        mQuit.set(true);
        interrupt();
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        mCache.initialize();

        while (true) {
            try {
                // Take a request from the queue.
                final Request<?> request = mCacheQueue.take();

                if (request.isCanceled()) {
                    mDelivery.postCancel(request, new CancelError());
                    continue;
                }

                Cache.Entry entry = mCache.get(request.getCacheKey());

                if (entry == null) {
                    if (request.getCacheMode() == Request.CacheMode.ONLY_CACHE) {
                        mDelivery.postError(request, new NoCacheError());
                    } else {
                        mNetworkQueue.put(request);
                    }
                    continue;
                }

                if (entry.isExpired()
                        && request.getCacheMode() == Request.CacheMode.STANDARD_CACHE) { // 缓存过期处理
                    request.setCacheEntry(entry);
                    mNetworkQueue.put(request);
                    continue;
                }

                NetworkResponse networkResponse = new NetworkResponse(entry.data, entry.responseHeaders);

                Response<?> response = request.parseNetworkResponse(networkResponse);

                LiteHttpLogger.d(TAG, "read response from cache");

                // 需要刷新缓存时，还有CACHE_AND_NETWORK模式，进行 磁盘和网络
                if ((entry.refreshNeeded() && request.getCacheMode() == Request.CacheMode.STANDARD_CACHE)
                        || request.getCacheMode() == Request.CacheMode.CACHE_AND_NETWORK) { // 刷新缓存
                    request.setCacheEntry(entry);
                    response.intermediate = true;

                    mDelivery.postResponse(request, response, new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mNetworkQueue.put(request);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    mDelivery.postResponse(request, response);
                }

            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit.get()) {
                    LiteHttpLogger.d(TAG, "cache dispatcher is quit.");
                    return;
                }
            }

        }
    }
}
