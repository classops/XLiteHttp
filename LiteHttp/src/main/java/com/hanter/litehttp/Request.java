package com.hanter.litehttp;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.hanter.litehttp.cache.Cache;
import com.hanter.litehttp.http.FormBody;
import com.hanter.litehttp.http.HttpUrl;
import com.hanter.litehttp.http.MediaType;
import com.hanter.litehttp.http.MultipartBody;
import com.hanter.litehttp.http.RequestBody;
import com.hanter.litehttp.http.exception.LiteHttpError;
import com.hanter.litehttp.request.FormRequest;
import com.hanter.litehttp.request.LiteRequest;
import com.hanter.litehttp.request.MultipartRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类名：Request <br/>
 * 描述：请求基类 <br/>
 * 创建时间：2017/1/19 15:42
 *
 * @author wangmingshuo
 * @version 1.0
 */
public abstract class Request<T> implements Comparable<Request<?>> {

    public interface Method {
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
        int PATCH = 7;
    }

    public interface CacheMode {
        /** 仅从网络获取 */
        int ONLY_NETWORK = 0;
        /** 从缓存读取，如果过期或者需要软刷新则执行网络请求； 如果不存在缓存，则从网络请求*/
        int STANDARD_CACHE = 1;
        /** 从缓存读取，忽略过期；如果不存在缓存，则从网络请求 */
        int CACHE_IGNORE_EXPIRED = 2;
        /** 从缓存读取，并且从网络再请求一次；这样需求，往往是无法判断是否过期的，情况，所以这里忽略过期 */
        int CACHE_AND_NETWORK = 3;
        /** 仅从缓存读取；如果不存在，则报 NoCacheError 异常，这里也是忽略过期的 */
        int ONLY_CACHE = 4;
    }

    private final HttpUrl mUrl;

    private String mRedirectUrl;

    private final int mMethod;

    // 处理Headers
    private Map<String, List<String>> mHeaders;

    private Cache.Entry mCacheEntry;

    // 处理请求体
    protected RequestBody mRequestBody;

    private int mCacheMode;

    /**
     * 取消请求标志位
     */
    private volatile boolean mCanceled = false;

    /** 分发 */
    private volatile boolean mResponseDelivered = false;

    /** 回调 */
    protected RequestListener<T> mListener;

    /**
     * 重发策略
     */
    private RetryPolicy mRetryPolicy;

    private Thread mRequestThread;

    private Integer mSequence;

    /** 与之关联的请求队列 */
    private RequestQueue mRequestQueue;

    private Object mTag;

    private Priority mPriority = Priority.NORMAL;

    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }

    public Request(HttpUrl url, int method, RequestListener<T> listener) {
        mUrl = url;
        mMethod = method;
        mHeaders = new HashMap<>();
        mListener = listener;
        setRetryPolicy(new DefaultRetryPolicy());
    }

    protected abstract Response<T> parseNetworkResponse(NetworkResponse response);

    @MainThread
    public void deliverStart() {
        if (mListener != null) {
            mListener.onAddQueue();
        }
    }

    @MainThread
    public void deliverCancel() {
        if (mListener != null) {
            mListener.onCancel();
        }
    }

    @MainThread
    protected void deliverResponse(T response) {
        if (mListener != null) {
            try {
                mListener.onResponse(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 分发错误回调
     *
     * @param error 错误
     */
    @MainThread
    public void deliverError(LiteHttpError error) {
        if (mListener != null) {
            mListener.onError(error);
        }
    }

    /**
     * 请求完成
     */
    @MainThread
    public void deliverFinish() {
        if (mRequestQueue != null) {
            mRequestQueue.finish(this);
        }

        if (mListener != null) {
            mListener.onFinish();
        }
    }

    public String getOriginUrl() {
        return mUrl.toString();
    }

    public String getUrl() {
        return (mRedirectUrl != null) ? mRedirectUrl : mUrl.toString();
    }

    public String getRedirectUrl() {
        return mRedirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        mRedirectUrl = redirectUrl;
    }

    public synchronized void setRequestThread(Thread requestThread) {
        this.mRequestThread = requestThread;
    }

    public synchronized boolean isCanceled() {
        return mCanceled;
    }

    /**
     * 取消请求
     */
    public synchronized void cancel() {
        mCanceled = true;

        if (mRequestThread != null) {
            mRequestThread.interrupt();
            mRequestThread = null;
        }
    }

    public int getMethod() {
        return mMethod;
    }

    public Map<String, List<String>> getHeaders() {
        return mHeaders;
    }

    public void addHeader(String headerName, List<String> headerValues) {
        mHeaders.put(headerName, headerValues);
    }

    public void addHeader(String headerName, String headerValue) {
        List<String> headerValues = mHeaders.get(headerName);

        if (headerValues == null) {
            headerValues = new ArrayList<>();
        }

        headerValues.add(headerValue);
        mHeaders.put(headerName, headerValues);
    }

    public void setHeaders(Map<String, List<String>> headers) {
        mHeaders.clear();
        mHeaders.putAll(headers);
    }

    public final int getTimeoutMs() {
        return mRetryPolicy.getCurrentTimeout();
    }

    public RetryPolicy getRetryPolicy() {
        return this.mRetryPolicy;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        mRetryPolicy = retryPolicy;
    }

    public void markDelivered() {
        mResponseDelivered = true;
    }

    public boolean hasHadResponseDelivered() {
        return mResponseDelivered;
    }

    public RequestBody getBody() {
        return this.mRequestBody;
    }

    public String getCacheKey() {
        return mMethod + ":" + mUrl;
    }

    public int getCacheMode() {
        return mCacheMode;
    }

    /**
     * 设置请求缓存模式
     * @param cacheMode 缓存模式
     */
    public Request<?> setCacheMode(int cacheMode) {
        this.mCacheMode = cacheMode;
        return this;
    }

    public Cache.Entry getCacheEntry() {
        return mCacheEntry;
    }

    public void setCacheEntry(Cache.Entry cacheEntry) {
        this.mCacheEntry = cacheEntry;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public void setRequestQueue(RequestQueue requestQueue) {
        this.mRequestQueue = requestQueue;
    }

    public Object getTag() {
        return mTag;
    }

    public void setTag(Object tag) {
        this.mTag = tag;
    }

    public Priority getPriority() {
        return mPriority;
    }

    public void setPriority(Priority priority) {
        this.mPriority = priority;
    }

    public Integer getSequence() {
        return mSequence;
    }

    public void setSequence(Integer sequence) {
        this.mSequence = sequence;
    }

    @Override
    public int compareTo(@NonNull Request<?> t) {
        Priority left = this.getPriority();
        Priority right = t.getPriority();
        return left == right ? this.mSequence - t.mSequence : left.ordinal() - right.ordinal();
    }

    public interface Builder<V> {
        Builder method(int method);

        Builder url(String url);

        Builder url(@NonNull HttpUrl httpUrl);

        Builder queryParameter(String name, String value);

        Builder listener(RequestListener<V> listener);

        Builder retry(RetryPolicy retryPolicy);

        Builder cache(int cache);

        Builder header(String headerName, String headerValue);

        Builder headers(String headerName, List<String> headerValues);

        Request<?> create();
    }

    public static class LiteBuilder<V> implements Builder<V> {

        int method;
        ResponseParser<V> parser;
        Map<String, List<String>> headers;
        RetryPolicy retryPolicy;
        int cache;
        RequestListener<V> listener;
        HttpUrl.Builder urlBuilder;

        public LiteBuilder() {
            urlBuilder = new HttpUrl.Builder();
            headers = new HashMap<>();
        }

        @Override
        public LiteBuilder<V> method(int method) {
            this.method = method;
            return this;
        }

        @Override
        public LiteBuilder<V> url(@NonNull HttpUrl httpUrl) {
            urlBuilder = httpUrl.newBuilder();
            return this;
        }

        @Override
        public LiteBuilder<V> url(String url) {
            HttpUrl temp = HttpUrl.parse(url);

//            if (temp == null) {
//                throw new InvalidUrlError("invalid url.");
//            }

            if (temp != null)
                urlBuilder = temp.newBuilder();

            return this;
        }

        @Override
        public LiteBuilder<V> queryParameter(String name, String value) {
            urlBuilder.addQueryParameter(name, value);
            return this;
        }

        @Override
        public LiteBuilder<V> listener(RequestListener<V> listener) {
            this.listener = listener;
            return this;
        }

        public LiteBuilder<V> parser(ResponseParser<V> parser) {
            this.parser = parser;
            return this;
        }

        public LiteBuilder<V> retry(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        @Override
        public LiteBuilder<V> cache(int cache) {
            this.cache = cache;
            return this;
        }

        @Override
        public LiteBuilder<V> header(String headerName, String headerValue) {

            List<String> headerValues = headers.get(headerName);

            if (headerValues == null) {
                headerValues = new ArrayList<>();
            }

            headerValues.add(headerValue);
            headers.put(headerName, headerValues);

            return this;
        }

        @Override
        public LiteBuilder<V> headers(String headerName, List<String> headerValues) {
            headers.put(headerName, headerValues);
            return this;
        }

        @Override
        public Request<V> create() {
            LiteRequest<V> request = new LiteRequest<>(urlBuilder.build(), method, listener);
            request.setCacheMode(cache);
            request.setParser(parser);
            if (retryPolicy != null)
                request.setRetryPolicy(retryPolicy);
            return request;
        }
    }

    public static class FormBuilder<V> implements Builder<V> {

        int method;
        ResponseParser<V> parser;
        Map<String, List<String>> headers;
        FormBody.Builder bodyBuilder;
        RetryPolicy retryPolicy;
        int cache;
        RequestListener<V> listener;
        HttpUrl.Builder urlBuilder;

        public FormBuilder() {
            bodyBuilder = new FormBody.Builder();
            urlBuilder = new HttpUrl.Builder();
            headers = new HashMap<>();
        }

        @Override
        public FormBuilder<V> url(String url) {

            HttpUrl temp = HttpUrl.parse(url);

//            if (temp == null) {
//                throw new InvalidUrlError();
//            }

            urlBuilder = temp.newBuilder();
            return this;
        }

        @Override
        public FormBuilder<V> url(@NonNull HttpUrl httpUrl) {
            urlBuilder = httpUrl.newBuilder();
            return this;
        }

        @Override
        public FormBuilder<V> queryParameter(String name, String value) {
            urlBuilder.addQueryParameter(name, value);
            return this;
        }

        @Override
        public FormBuilder<V> listener(RequestListener<V> listener) {
            this.listener = listener;
            return this;
        }

        @Override
        public FormBuilder<V> method(int method) {
            this.method = method;
            return this;
        }

        public FormBuilder<V> form(String name, String value) {
            bodyBuilder.add(name, value);
            return this;
        }

        public FormBuilder<V> parser(ResponseParser<V> parser) {
            this.parser = parser;
            return this;
        }

        public FormBuilder<V> retry(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        @Override
        public FormBuilder<V> cache(int cache) {
            this.cache = cache;
            return this;
        }

        @Override
        public FormBuilder<V> header(String headerName, String headerValue) {

            List<String> headerValues = headers.get(headerName);

            if (headerValues == null) {
                headerValues = new ArrayList<>();
            }

            headerValues.add(headerValue);
            headers.put(headerName, headerValues);

            return this;
        }

        @Override
        public FormBuilder<V> headers(String headerName, List<String> headerValues) {
            headers.put(headerName, headerValues);
            return this;
        }

        @Override
        public Request<V> create() {
            FormRequest<V> request = new FormRequest<>(urlBuilder.build(), method, bodyBuilder.build(), listener);
            request.setCacheMode(cache);
            request.setParser(parser);
            if (retryPolicy != null)
                request.setRetryPolicy(retryPolicy);
            return request;
        }
    }

    public static class MultipartFormBuilder<V> implements Builder<V> {

        int method;
        Map<String, List<String>> headers;
        ResponseParser<V> parser;
        RetryPolicy retryPolicy;
        int cache;
        RequestListener<V> listener;
        HttpUrl.Builder urlBuilder;
        MultipartBody.Builder bodyBuilder;

        public MultipartFormBuilder() {
            bodyBuilder = new MultipartBody.Builder();
            urlBuilder = new HttpUrl.Builder();
            headers = new HashMap<>();
        }

        @Override
        public MultipartFormBuilder<V> url(String url) {

            HttpUrl temp = HttpUrl.parse(url);

//            if (temp == null) {
//                throw new InvalidUrlError();
//            }

            urlBuilder = temp.newBuilder();
            return this;
        }

        @Override
        public MultipartFormBuilder<V> url(@NonNull HttpUrl httpUrl) {
            urlBuilder = httpUrl.newBuilder();
            return this;
        }

        @Override
        public MultipartFormBuilder<V> queryParameter(String name, String value) {
            urlBuilder.addQueryParameter(name, value);
            return this;
        }

        @Override
        public MultipartFormBuilder<V> listener(RequestListener<V> listener) {
            this.listener = listener;
            return this;
        }

        @Override
        public MultipartFormBuilder<V> method(int method) {
            this.method = method;
            return this;
        }

        public MultipartFormBuilder<V> parser(ResponseParser<V> parser) {
            this.parser = parser;
            return this;
        }

        @Override
        public MultipartFormBuilder<V> header(String headerName, String headerValue) {

            List<String> headerValues = headers.get(headerName);

            if (headerValues == null) {
                headerValues = new ArrayList<>();
            }

            headerValues.add(headerValue);
            headers.put(headerName, headerValues);

            return this;
        }

        @Override
        public MultipartFormBuilder<V> headers(String headerName, List<String> headerValues) {
            headers.put(headerName, headerValues);
            return this;
        }

        public MultipartFormBuilder<V> retry(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        @Override
        public MultipartFormBuilder<V> cache(int cache) {
            this.cache = cache;
            return this;
        }



        public MultipartFormBuilder<V> part(String name, String value) {
            bodyBuilder.addFormDataPart(name, value);
            return this;
        }

        public MultipartFormBuilder<V> part(String name, String mediaType, File file) {
            bodyBuilder.addFormDataPart(name, file.getName(), RequestBody.create(MediaType.parse(mediaType), file));
            return this;
        }

        @Override
        public Request<V> create() {
            MultipartRequest<V> request = new MultipartRequest<>(urlBuilder.build(), method,
                    bodyBuilder.build(), listener);
            request.setCacheMode(cache);
            request.setParser(parser);
            if (retryPolicy != null)
                request.setRetryPolicy(retryPolicy);
            return request;
        }
    }

}
