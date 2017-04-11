package com.hanter.litehttp.http.exception;

/**
 * 类名：CancelError <br/>
 * 描述：请求取消异常 <br/>
 * 创建时间：2017/1/5 14:57
 *
 * @author wangmingshuo
 * @version 1.0
 */

public class CancelError extends LiteHttpError {

    public CancelError() {
        super("this request is canceled.");
    }

    public CancelError(Throwable cause) {
        super("this request is canceled.", cause);
    }
}
