package com.hanter.litehttp;

/**
 * 类名：ResponseParser <br/>
 * 描述：请求相应解析 <br/>
 * 创建时间：2017/1/5 11:37
 *
 * @author wangmingshuo
 * @version 1.0
 */

public interface ResponseParser<T> {

    /**
     * 解析相应
     * @param response 响应
     * @return 返回格式化响应
     */
    Response<T> parseNetworkResponse(NetworkResponse response);
}
