package com.hanter.litehttp.http.exception;

/**
 * 类名：InvalidUrlError <br/>
 * 描述：Url无效错误
 * 创建时间：2017/01/15 12:46
 *
 * @author hanter
 * @version 1.0
 */
public class InvalidUrlError extends LiteHttpError {

    public InvalidUrlError(String exceptionMessage) {
        super(exceptionMessage);
    }

    public InvalidUrlError(String exceptionMessage, Throwable e) {
        super(exceptionMessage, e);
    }
}
