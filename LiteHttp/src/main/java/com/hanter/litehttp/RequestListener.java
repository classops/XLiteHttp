package com.hanter.litehttp;

import com.hanter.litehttp.http.exception.LiteHttpError;

/**
 * 类名：RequestListener <br/>
 * 描述：请求 <br/>
 * 创建时间：2016年2月16日 下午3:01:58
 * @author wangmingshuo@ddsoucai.cn
 * @version 1.0
 */
public interface RequestListener<T> {
	void onAddQueue();

	void onResponse(T response);

	void onError(LiteHttpError error);

	void onCancel();

	void onFinish();
}
