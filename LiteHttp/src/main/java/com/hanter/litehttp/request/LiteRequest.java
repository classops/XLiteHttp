package com.hanter.litehttp.request;

import com.hanter.litehttp.NetworkResponse;
import com.hanter.litehttp.Request;
import com.hanter.litehttp.RequestListener;
import com.hanter.litehttp.Response;
import com.hanter.litehttp.ResponseParser;
import com.hanter.litehttp.http.HttpUrl;

public class LiteRequest<T> extends Request<T> {
    private ResponseParser<T> mParser;

    public LiteRequest(HttpUrl url, int method, RequestListener<T> listener) {
        super(url, method, listener);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        return mParser.parseNetworkResponse(response);
    }

    public void setParser(ResponseParser<T> parser) {
        this.mParser = parser;
    }
}
