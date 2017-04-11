package com.hanter.litehttp;

import com.hanter.litehttp.http.exception.CancelError;
import com.hanter.litehttp.http.exception.LiteHttpError;

public interface ResponseDelivery {
    void postStart(Request<?> request);

    void postResponse(Request<?> request, Response<?> response, Runnable runnable);

    void postResponse(Request<?> request, Response<?> response);

    void postError(Request<?> request, LiteHttpError error, Runnable runnable);

    void postError(Request<?> request, LiteHttpError error);

    void postCancel(Request<?> request, LiteHttpError error, Runnable runnable);

    void postCancel(Request<?> request, CancelError error);

//    void postFinish(Request<?> request);
}
