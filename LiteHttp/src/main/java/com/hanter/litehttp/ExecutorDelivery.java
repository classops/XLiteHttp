package com.hanter.litehttp;

import java.util.concurrent.Executor;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.hanter.litehttp.http.exception.CancelError;
import com.hanter.litehttp.http.exception.LiteHttpError;

/**
 * 类名：ExecutorDelivery <br/>
 * 描述：对请求结果的分发 <br/>
 * 创建时间：2016年2月16日 下午3:02:10
 * @author wangmingshuo@ddsoucai.cn
 * @version 1.0
 */
public class ExecutorDelivery implements ResponseDelivery {
	
	private final Executor mExecutor;
	
	public ExecutorDelivery() {
		mExecutor = new Executor() {
			
			private final Handler mHandler = new Handler(Looper.getMainLooper());
			
			@Override
			public void execute(@NonNull Runnable command) {
				mHandler.post(command);
			}
		};
	}

	@Override
	public void postStart(final Request<?> request) {
		mExecutor.execute(new Runnable() {
			@Override
			public void run() {
				request.deliverStart();
			}
		});
	}

	@Override
	public void postResponse(Request<?> request, Response<?> response, Runnable runnable) {
		mExecutor.execute(new ResponseDeliveryRunnable(request, response, runnable));
	}

	@Override
	public void postResponse(Request<?> request, Response<?> response) {
		mExecutor.execute(new ResponseDeliveryRunnable(request, response));
	}

	@Override
	public void postError(Request<?> request, LiteHttpError error, Runnable runnable) {
		Response<?> response = Response.error(error);
		mExecutor.execute(new ResponseDeliveryRunnable(request, response, runnable));
	}

	@Override
	public void postError(Request<?> request, LiteHttpError error) {
		Response<?> response = Response.error(error);
		mExecutor.execute(new ResponseDeliveryRunnable(request, response));
	}

	@Override
	public void postCancel(final Request<?> request, LiteHttpError error, final Runnable runnable) {
		mExecutor.execute(new ResponseDeliveryRunnable(request, Response.error(error), runnable));
	}

	@Override
	public void postCancel(final Request<?> request, final CancelError error) {
		postCancel(request, error, null);
	}

	/**
	 * 统一处理错误还是正确请求
	 */
	@SuppressWarnings("rawtypes")
	private static class ResponseDeliveryRunnable implements Runnable {
		private final Request mRequest;
		private final Response mResponse;
		private final Runnable mRunnable;

		public ResponseDeliveryRunnable(Request<?> request, Response<?> response, Runnable runnable) {
			mRequest = request;
			mResponse = response;
			mRunnable = runnable;
		}

		public ResponseDeliveryRunnable(Request<?> request, Response<?> response) {
			this(request, response, null);
		}

		@SuppressWarnings("unchecked")
		public void run() {

			if (mResponse.isSuccess() && !mRequest.isCanceled()) {
				mRequest.deliverResponse(mResponse.result);
			} else {
				if (mResponse.error instanceof CancelError
						|| mRequest.isCanceled()) {
					mRequest.deliverCancel();
				} else {
					mRequest.deliverError(mResponse.error);
				}
			}

			if (!mResponse.intermediate) {
				mRequest.deliverFinish();
			}

			// 缓存需要刷新时，先取缓存，然后进行刷新请求
			if (mRunnable != null) {
				mRunnable.run();
			}
		}
	}

}
