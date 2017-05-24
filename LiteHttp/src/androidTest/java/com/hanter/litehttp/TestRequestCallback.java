package com.hanter.litehttp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.hanter.litehttp.http.HttpUrl;
import com.hanter.litehttp.http.exception.LiteHttpError;
import com.hanter.litehttp.parser.StringParser;
import com.hanter.litehttp.stack.OkhttpStack;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TestRequestCallback {

    @Test
    public void testRequestCallback() {
        Context appContext = InstrumentationRegistry.getTargetContext();
//        RequestQueue queue = new RequestQueue(appContext, new OkhttpStack(mSslSocketFactory));

        RequestQueue queue = new RequestQueue(appContext, new OkhttpStack());

        queue.start();
        newRequest(appContext, queue);
    }

    private void newRequest(Context context, RequestQueue queue) {
        HttpUrl httpUrl = HttpUrl.parse("http://192.168.1.68:8080/ilocallife/login");
//		HttpUrl httpUrl = HttpUrl.parse("http://api.jinqiangxinxi.cn/open/selectNewsContentIndex.do");

        Request request = new Request.FormBuilder<String>()
                .method(Request.Method.POST)
                .url(httpUrl.toString())
                .queryParameter("phone", "明硕")
                .queryParameter("password", "123456")
                .cache(Request.CacheMode.STANDARD_CACHE)
                .parser(new StringParser())
                .retry(new DefaultRetryPolicy(10000, 3, 1))
                .listener(new RequestListener<String>() {
                    @Override
                    public void onAddQueue() {

                    }

                    @Override
                    public void onResponse(String response) {
                        Log.d("MainActivity", "响应：" + response);
                    }

                    @Override
                    public void onError(LiteHttpError error) {

                        if (error != null && error.getMessage() != null) {
//							Toast.makeText(MainActivity.this, "错误：" + error.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("MainActivity", "错误：" + error.getMessage());
                        } else {
//							Toast.makeText(MainActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
                            Log.d("MainActivity", "未知错误");
                        }

                    }

                    @Override
                    public void onCancel() {
                        Log.d("MainActivity", "onCancel");
                    }

                    @Override
                    public void onFinish() {
                        Log.d("MainActivity", "onFinish");
                    }
                }).create();

        request.addHeader("Cache-Control", "no-cache");
        queue.add(request);
    }

}
