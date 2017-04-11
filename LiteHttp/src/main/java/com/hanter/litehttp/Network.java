package com.hanter.litehttp;

import com.hanter.litehttp.http.exception.LiteHttpError;

/**
 * 类名：Network <br/>
 * 描述：完成网络请求接口 <br/>
 * 创建时间：2016年2月17日 上午9:49:15
 * @author wangmingshuo@ddsoucai.cn
 * @version 1.0
 */
public interface Network {
	NetworkResponse performRequest(Request<?> request) throws LiteHttpError, InterruptedException;
}
