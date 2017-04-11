package com.hanter.litehttp.http.exception;

/**
 * 
 * 类名：NoConnectionError <br/>
 * 描述：连接异常 <br/>
 * 创建时间：2016年2月18日 上午11:54:45
 * @author wangmingshuo@ddsoucai.cn
 * @version 1.0
 */
public class NoConnectionError extends NetworkError {

	public NoConnectionError(Throwable e) {
		super("request network connection is error", e);
	}

}
