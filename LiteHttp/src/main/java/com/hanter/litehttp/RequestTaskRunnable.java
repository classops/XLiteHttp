package com.hanter.litehttp;

import com.hanter.litehttp.cache.Cache;
import com.hanter.litehttp.http.exception.CancelError;
import com.hanter.litehttp.http.exception.LiteHttpError;
import com.hanter.litehttp.utils.LiteHttpLogger;

import android.os.Process;

import static com.hanter.litehttp.utils.LiteHttpConfig.TASK_THREAD_DEBUG;

public class RequestTaskRunnable implements Runnable {

	private static final String TAG = "RequestTaskRunnable";

	private final Request<?> mRequest;
	private final Network mNetwork;
	private final Cache mCache;
	private final ExecutorDelivery mDelivery;

	RequestTaskRunnable(Request<?> request, Network network, Cache cache, ExecutorDelivery delivery) {
		mRequest = request;
		mNetwork = network;
		mCache = cache;
		mDelivery = delivery;
	}

	/**
	 * 中断，不能中断的堵塞方法
	 */
	public void interrupt() {
		LiteHttpLogger.d(TASK_THREAD_DEBUG, TAG, "interrupt：thread - " + Thread.currentThread().getName());
	}

	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

		LiteHttpLogger.d(TASK_THREAD_DEBUG, TAG,
                "start request "
				+ mRequest.getSequence()
				+ "thread name：" + Thread.currentThread().getName());

		mRequest.setRequestThread(Thread.currentThread());

		NetworkResponse networkResponse;
		try {

			// 开始任务前，判断是否取消，避免做无用功
			if (mRequest.isCanceled())
				throw new InterruptedException();

			networkResponse = mNetwork.performRequest(mRequest);

			// 即使完成请求，也没必要响应
			if (mRequest.isCanceled())
				throw new InterruptedException();

			Response<?> response = mRequest.parseNetworkResponse(networkResponse);

			if (mRequest.getCacheMode() > Request.CacheMode.ONLY_NETWORK && response.cacheEntry != null) {
				mCache.put(mRequest.getCacheKey(), response.cacheEntry);
			}

			mDelivery.postResponse(mRequest, response);
		} catch (LiteHttpError e) {
			if (e instanceof CancelError) { // 是否取消，还是其他请求错误
				mDelivery.postCancel(mRequest, new CancelError(e));
			} else {
				mDelivery.postError(mRequest, e);
			}
		} catch (InterruptedException e) {
			LiteHttpLogger.d(TASK_THREAD_DEBUG, TAG, "interrupted：" + Thread.currentThread().getName());
			mDelivery.postCancel(mRequest, new CancelError(e));
		} catch (Exception e) {

			LiteHttpLogger.e(TAG, e.getMessage());

			LiteHttpError error = new LiteHttpError(e);
			mDelivery.postError(mRequest, error);
		} finally {
			mRequest.setRequestThread(null);
			LiteHttpLogger.d(TASK_THREAD_DEBUG, TAG, "finish request thread：" + Thread.currentThread().getName());
		}
	}

}
