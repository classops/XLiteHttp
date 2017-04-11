package com.hanter.litehttp.request;

import com.hanter.litehttp.NetworkResponse;
import com.hanter.litehttp.Request;
import com.hanter.litehttp.RequestListener;
import com.hanter.litehttp.Response;
import com.hanter.litehttp.ResponseParser;
import com.hanter.litehttp.http.HttpUrl;
import com.hanter.litehttp.http.MultipartBody;

/**
 * 类名：MultipartRequest <br/>
 * 描述：Multipart请求 <br/>
 * 创建时间：2017/1/5 11:10
 *
 * @author wangmingshuo
 * @version 1.0
 */

public class MultipartRequest<T> extends Request<T> {

    private ResponseParser<T> mParser;

    public MultipartRequest(HttpUrl url, int method, MultipartBody body, RequestListener<T> listener) {
        super(url, method, listener);
        this.mRequestBody = body;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        return mParser.parseNetworkResponse(response);
    }

    public void setParser(ResponseParser<T> parser) {
        this.mParser = parser;
    }
}
