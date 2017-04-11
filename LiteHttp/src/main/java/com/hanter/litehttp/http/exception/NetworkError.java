package com.hanter.litehttp.http.exception;

import com.hanter.litehttp.NetworkResponse;

/**
 * 
 * 类名：NetworkError <br/>
 * 描述：网络异常基类 <br/>
 * 创建时间：2016年2月18日 上午11:54:16
 * @author wangmingshuo@ddsoucai.cn
 * @version 1.0
 */
public class NetworkError extends LiteHttpError {

	public NetworkError() {
		super();
	}
	
	public NetworkError(Throwable e) {
		super(e);
	}

	public NetworkError(String msg) {
		super(msg);
	}

	public NetworkError(NetworkResponse response) {
		super(response);
	}

	public NetworkError(String exceptionMessage, Throwable reason) {
		super(exceptionMessage, reason);
	}
}
