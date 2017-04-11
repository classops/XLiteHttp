package com.hanter.litehttp.http.exception;

/**
 * 类名：NoCacheError <br/>
 * 描述：无缓存错误
 * 创建时间：2017/04/02 01:18
 *
 * @author hanter
 * @version 1.0
 */
public class NoCacheError extends LiteHttpError {

    public NoCacheError() {
        super("no cache error!");
    }

    public NoCacheError(String exceptionMessage) {
        super(exceptionMessage);
    }
}
