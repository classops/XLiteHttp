package com.hanter.litehttp;

import android.content.Context;

import com.hanter.litehttp.cache.Cache;
import com.hanter.litehttp.cache.DiskBasedCache;
import com.hanter.litehttp.stack.HttpStack;
import com.hanter.litehttp.stack.HurlStack;
import com.hanter.litehttp.utils.LiteHttpLogger;

import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * 类名：RequestQueue <br/>
 * 描述：请求队列 完成缓存任务分发 <br/>
 * 创建时间：2016年2月16日 下午2:45:05
 *
 * @author wangmingshuo@ddsoucai.cn
 * @version 1.0
 */
public class RequestQueue {

	private final static String TAG = "RequestQueue";

	/**
	 * 所有请求集合，方便管理
	 */
	private final Set<Request<?>> mCurrentRequests = new HashSet<>();

	/** 缓存请求队列 */
	private final HashMap<String, Queue<Request<?>>> mWaitingRequests = new HashMap<>();

	/**
	 * 缓存请求队列
	 */
	private final PriorityBlockingQueue<Request<?>> mCacheQueue;
	/**
	 * 网络请求队列
	 */
	private final PriorityBlockingQueue<Request<?>> mNetworkQueue;
	private NetworkDispatcher mNetworkDispatcher;
	private CacheDispatcher mCacheDispatcher;

	private final ExecutorDelivery mDelivery;

	private Network mNetwork;

	/**
	 * 磁盘缓存实现
	 */
	private Cache mCache;

	private AtomicInteger mSequenceGenerator = new AtomicInteger();

	/**
	 * A simple predicate or filter interface for Requests, for use by
	 * {@link RequestQueue#cancelAll(RequestFilter)}.
	 */
	public interface RequestFilter {
		boolean apply(Request<?> request);
	}

	public RequestQueue(Context context) {
		this(context, null);
	}

	public RequestQueue(Context context, HttpStack stack) {
		mCacheQueue = new PriorityBlockingQueue<>();
		mNetworkQueue = new PriorityBlockingQueue<>();
//		mCache = new DiskBasedCache(context.getCacheDir());
		mCache = new DiskBasedCache(new File("/sdcard/litehttp"));
		mDelivery = new ExecutorDelivery();

		HttpStack httpStack;
		try {
			if (stack != null) {
				httpStack = stack;
			} else {
                httpStack = new HurlStack(null);
			}

            mNetwork = new BasicNetwork(httpStack);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 启动队列，请求和文件下载
	 */
	public void start() {
		mCacheDispatcher = new CacheDispatcher(mCacheQueue, mNetworkQueue, mCache, mDelivery);
		mCacheDispatcher.start();

		mNetworkDispatcher = new NetworkDispatcher(mNetworkQueue, mNetwork, mCache, mDelivery);
		mNetworkDispatcher.start();
	}

	/**
	 * 停止队列，中断所有请求
	 */
	public void stop() {
		LiteHttpLogger.d(TAG, "request queue is quit.");

		mNetworkDispatcher.quit();
		mCacheDispatcher.quit();
	}

	public Integer getSequenceNumber() {
		return mSequenceGenerator.incrementAndGet();
	}

	public void add(Request<?> request) {

		request.setRequestQueue(this);

		synchronized (mCurrentRequests) {
			mCurrentRequests.add(request);
		}

		// 设置序列号，比较优先级
		request.setSequence(getSequenceNumber());

		// 请求开始回调
		mDelivery.postStart(request);

		// 分发任务 网络请求还是缓存
		if (request.getCacheMode() == Request.CacheMode.ONLY_NETWORK) {
			mNetworkQueue.add(request);
			return;
		}

		synchronized (mWaitingRequests) {
			String cacheKey = request.getCacheKey();

			if (mWaitingRequests.containsKey(cacheKey)) {
				Queue<Request<?>> queue = mWaitingRequests.get(request.getCacheKey());

				if (queue == null)
					queue = new LinkedList<>();

				queue.add(request);
				mWaitingRequests.put(cacheKey, queue);
			} else {
				mWaitingRequests.put(cacheKey, null); // 表明有一个请求正在进行中
				mCacheQueue.add(request);
			}
		}
	}


	/**
	 * 结束请求，并从队列中移除
	 *
	 * @param request 结束的请求
	 */
	public void finish(Request<?> request) {
		LiteHttpLogger.i(TAG, "finish");

		synchronized (mCurrentRequests) {
			mCurrentRequests.remove(request);
		}

		// 缓存队列请求
		synchronized (mWaitingRequests) {
			String cacheKey = request.getCacheKey();

			if (request.getCacheMode() > Request.CacheMode.ONLY_NETWORK) {

				Queue<Request<?>> queue = mWaitingRequests.remove(cacheKey);

				if (queue != null) {

					LiteHttpLogger.d("RequestQueue", "staged request read cache.");

					mCacheQueue.addAll(queue);
				}
			}
		}
	}

	/**
	 * Cancels all requests in this queue for which the given filter applies.
	 *
	 * @param filter The filtering function to use
	 */
	public void cancelAll(RequestFilter filter) {
		synchronized (mCurrentRequests) {
			for (Request<?> request : mCurrentRequests) {
				if (filter.apply(request)) {
					request.cancel();
				}
			}
		}
	}

	public void cancelTag(final Object tag) {
		cancelAll(new RequestFilter() {
			@Override
			public boolean apply(Request<?> request) {
				return request.getTag() == tag;
			}
		});
	}
}
