package com.hanter.litehttp;

import android.os.Process;

import com.hanter.litehttp.cache.Cache;
import com.hanter.litehttp.http.exception.CancelError;
import com.hanter.litehttp.task.RequestThreadFactory;
import com.hanter.litehttp.utils.LiteHttpLogger;
import com.hanter.litehttp.utils.RuntimeParametersUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkDispatcher extends Thread {
	private final static String TAG = "NetworkDispatcher";

	private final BlockingQueue<Request<?>> mQueue;
	private final Network mNetwork;
	private final Cache mCache;
	private final ExecutorDelivery mDelivery;

	private final ThreadPoolExecutor mExecutor;
	
	private final AtomicBoolean mQuit;
	
	public NetworkDispatcher(BlockingQueue<Request<?>> queue, Network network, Cache cache, ExecutorDelivery delivery) {
		mQueue = queue;
		mNetwork = network;
		mCache = cache;
		mDelivery = delivery;

		mQuit = new AtomicBoolean(false);
		mExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(
				RuntimeParametersUtils.getAutofitThreadCount(),
				new RequestThreadFactory());
	}
	
	public void quit() {
		LiteHttpLogger.d(TAG, "quit");
		mQuit.set(true);
		interrupt();
	}

	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

		while (true) {
			try {
				final Request<?> request = mQueue.take();

				if (request.isCanceled()) {
					mDelivery.postCancel(request, new CancelError());
				} else {
					mExecutor.execute(new RequestTaskRunnable(request, mNetwork, mCache, mDelivery));
				}

			} catch (InterruptedException e) {
				if (mQuit.get()) {
					LiteHttpLogger.d(TAG, "network dispatcher is quit");

					/*
					List<Runnable> list = mExecutor.shutdownNow();
					for (Runnable task : list) {
						RequestTaskRunnable requestTask = (RequestTaskRunnable) task;

						// 这里不需要任何处理，因为 请求队列停止，无需做其他操作
						// 取消请求，回调Cancel，判断是否已经取消。
						// 如果取消，不做任何处理
						// 没有取消，设置取消，并进行回调
					}
					*/

					return;
				}
			}
			
		}
	}
	
}
