package com.hanter.litehttp.request;

import com.hanter.litehttp.NetworkResponse;
import com.hanter.litehttp.Request;
import com.hanter.litehttp.RequestListener;
import com.hanter.litehttp.Response;
import com.hanter.litehttp.ResponseParser;
import com.hanter.litehttp.http.FormBody;
import com.hanter.litehttp.http.HttpUrl;

/**
 * 类名：FormRequest <br/>
 * 描述：Form请求 <br/>
 * 创建时间：2017/1/5 11:10
 *
 * @author wangmingshuo
 * @version 1.0
 */

public class FormRequest<T> extends Request<T> {

    private ResponseParser<T> mParser;

    public FormRequest(HttpUrl url, int method, FormBody body, RequestListener<T> listener) {
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
