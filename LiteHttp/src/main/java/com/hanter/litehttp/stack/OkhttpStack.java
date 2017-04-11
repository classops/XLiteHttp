package com.hanter.litehttp.stack;

import com.hanter.litehttp.NetworkResponse;
import com.hanter.litehttp.Request;
import com.hanter.litehttp.Request.Method;
import com.hanter.litehttp.http.ProxyHelper;
import com.hanter.litehttp.http.exception.AuthFailureError;
import com.hanter.litehttp.utils.LiteHttpLogger;

import java.io.IOException;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;

import okhttp3.OkHttpClient;

public class OkhttpStack implements HttpStack {

    static final boolean DEBUG = false;

    static final String TAG = "OkhttpStack";

    private static final OkHttpClient sClient = new OkHttpClient();

    private final SSLSocketFactory mSslSocketFactory;

    public OkhttpStack() {
        this.mSslSocketFactory = null;
    }

    public OkhttpStack(SSLSocketFactory sslSocketFactory) {
        this.mSslSocketFactory = sslSocketFactory;
    }

    @Override
    public NetworkResponse performRequest(Request<?> request,
                                          Map<String, List<String>> additionalHeaders) throws IOException,
            AuthFailureError {

        okhttp3.Request okhttpRequest;
        if (request.getMethod() == Method.GET || request.getMethod() == Method.HEAD) {
            okhttpRequest = new okhttp3.Request.Builder()
                    .url(request.getUrl())
                    .get()
                    .build();
        } else {
            okhttpRequest = new okhttp3.Request.Builder()
                    .url(request.getUrl())
                    .method(getRequestMethod(request.getMethod()),
                            OkhttpRequestBodyHelper.convertToRequestBody(request.getBody()))
                    .build();
        }

        // 设置请求参数：超时、方法、请求体
        int timeout = request.getRetryPolicy().getCurrentTimeout();

        // 这里新增了代理设置
        Proxy proxy = ProxyHelper.getProxy();

        OkHttpClient client;
        if (mSslSocketFactory != null) {
            client = sClient
                    .newBuilder()
                    .readTimeout(timeout, TimeUnit.MILLISECONDS)
                    .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                    .connectTimeout(timeout, TimeUnit.MICROSECONDS)
                    .sslSocketFactory(mSslSocketFactory)
                    .proxy(proxy)
                    .build();
        } else {
            client = sClient
                    .newBuilder()
                    .readTimeout(timeout, TimeUnit.MILLISECONDS)
                    .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                    .connectTimeout(timeout, TimeUnit.MICROSECONDS)
                    .build();
        }

        LiteHttpLogger.d(DEBUG, TAG, "startRequest");

        okhttp3.Response response = client.newCall(okhttpRequest).execute();

        LiteHttpLogger.d(DEBUG, TAG, "getResponseCode");

        int statusCode = response.code();

        NetworkResponse networkResponse;

        LiteHttpLogger.d(DEBUG, TAG, "readData");

        networkResponse = new NetworkResponse(statusCode, response.body().bytes(),
                response.headers().toMultimap(), false);

        return networkResponse;
    }

    private String getRequestMethod(int method) {

        String methodName;

        switch (method) {
            case Method.GET:
                methodName = "GET";
                break;
            case Method.DELETE:
                methodName = "DELETE";
                break;
            case Method.POST:
                methodName = "POST";
                break;
            case Method.PUT:
                methodName = "PUT";
                break;
            case Method.HEAD:
                methodName = "HEAD";
                break;
            case Method.OPTIONS:
                methodName = "OPTIONS";
                break;
            case Method.TRACE:
                methodName = "TRACE";
                break;
            case Method.PATCH:
                methodName = "PATCH";
                break;
            default:
                throw new IllegalStateException("Unknown method type.");

        }

        return methodName;
    }

}
