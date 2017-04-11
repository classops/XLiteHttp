package com.hanter.litehttp.parser;

import com.hanter.litehttp.NetworkResponse;
import com.hanter.litehttp.Response;
import com.hanter.litehttp.ResponseParser;
import com.hanter.litehttp.utils.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * 类名：StringParser <br/>
 * 描述：字符串解析 <br/>
 * 创建时间：2017/1/5 11:52
 *
 * @author wangmingshuo
 * @version 1.0
 */

public class StringParser implements ResponseParser<String> {
    @Override
    public Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed; // 根据所给编码生成字符串
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }
}
