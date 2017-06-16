package com.example.litehttpdemo;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.hanter.litehttp.DefaultRetryPolicy;
import com.hanter.litehttp.Request;
import com.hanter.litehttp.Request.Method;
import com.hanter.litehttp.RequestListener;
import com.hanter.litehttp.RequestQueue;
import com.hanter.litehttp.http.HttpUrl;
import com.hanter.litehttp.http.exception.LiteHttpError;
import com.hanter.litehttp.parser.StringParser;
import com.hanter.litehttp.stack.HurlStack;
import com.hanter.litehttp.utils.HttpsUtils;

import org.json.JSONObject;

import java.io.File;

public class MainActivity extends Activity implements OnClickListener {
	
	RequestQueue mRequestQueue;

    Request request;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		try {
//			mRequestQueue = new RequestQueue(this, new OkhttpStack(RequestQueue.createSSLSocketFactory(this, R.raw.baidu, "wms123")));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
//		mRequestQueue = new RequestQueue(this, new OkhttpStack());
//		mRequestQueue = new RequestQueue(this);
        mRequestQueue = new RequestQueue(this, new HurlStack(null, HttpsUtils.createSSLSocketFactory(this, R.raw.baidu, "wms123")));
        mRequestQueue.start();

        Button btnPost = (Button) findViewById(R.id.btn_main_post);
        Button btnPostFile = (Button) findViewById(R.id.btn_main_upload_file);

        Button btnGet = (Button) findViewById(R.id.btn_main_get);
        Button btnStop = (Button) findViewById(R.id.btn_main_stop);
		
		btnGet.setOnClickListener(this);
        btnPost.setOnClickListener(this);
        btnPostFile.setOnClickListener(this);
        btnStop.setOnClickListener(this);


        Utils.getApiUserAgent(this);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mRequestQueue.stop();
	}

	public void newRequest() {
        HttpUrl httpUrl = HttpUrl.parse("https://kyfw.12306.cn/otn/");

        request = new Request.FormBuilder<String>()
                .method(Method.POST)
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
                        Toast.makeText(MainActivity.this, "响应：" + response, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(LiteHttpError error) {

                        if (error != null && error.getMessage() != null) {
                            Toast.makeText(MainActivity.this, "错误：" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
                        }


                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onFinish() {

                    }
                }).create();

        request.addHeader("appExtraInfo", Utils.getApiExtraInfo(this));
        request.addHeader(HttpConstants.HEADER_AUTH, HttpConstants.AUTH_TYPE_BEARER + " " + "token");
        request.setTag(MainActivity.this);
        mRequestQueue.add(request);
	}

	private void newPostRequest() {
        HttpUrl httpUrl = HttpUrl.parse("http://api.jinqiangxinxi.cn/app/loadFile.do");
        request = new Request.FormBuilder<String>()
                .method(Method.POST)
                .url(httpUrl)
                .form("mytoken", "ae6bc7d91f1444a683b8d582e9248bb21495432924774")
                .form("moduleCode", "9")
                .form("test", "9")
                .form("test", "92")
                .cache(Request.CacheMode.STANDARD_CACHE)
                .parser(new StringParser())
                .retry(new DefaultRetryPolicy(10000, 3, 1))
                .listener(new RequestListener<String>() {
                    @Override
                    public void onAddQueue() {
                        Log.d("MainActivity", "onStart");
                    }

                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this, "响应：" + response, Toast.LENGTH_SHORT).show();

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

        request.addHeader("appExtraInfo", Utils.getApiExtraInfo(this));
        request.addHeader(HttpConstants.HEADER_AUTH, HttpConstants.AUTH_TYPE_BEARER + " " + "token");
        request.setTag(MainActivity.this);
        mRequestQueue.add(request);
    }

    private void newPostFileRequest() {
//		HttpUrl httpUrl = HttpUrl.parse("https://www.greenjsq.me");

//		HttpUrl httpUrl = HttpUrl.parse("https://www.baidu.com");

//        HttpUrl httpUrl = HttpUrl.parse("https://www.jd.hk");

//		HttpUrl httpUrl = HttpUrl.parse("https://kyfw.12306.cn/otn/");


        HttpUrl httpUrl = HttpUrl.parse("http://api.jinqiangxinxi.cn/app/loadFile.do");


//		HttpUrl httpUrl = HttpUrl.parse("https://www.hao123.com");

//        HttpUrl httpUrl = HttpUrl.parse("https://www.baidu.com");

//		HttpUrl httpUrl = HttpUrl.parse("http://api.jinqiangxinxi.cn/app/loadFile.do");

//		HttpUrl httpUrl = HttpUrl.parse("http://192.168.1.68:8080/ilocallife/login");


        request = new Request.MultipartFormBuilder<String>()
                .method(Method.POST)
                .url(httpUrl)
                .part("mytoken", "ae6bc7d91f1444a683b8d582e9248bb21495432924774")
                .part("moduleCode", "9")
                .part("test", "image/*", new File("/sdcard/news_article/98a3c22cf91ebb36b91eb6ceab1c0d36.gif"))
                .cache(Request.CacheMode.STANDARD_CACHE)
                .parser(new StringParser())
                .retry(new DefaultRetryPolicy(10000, 3, 1))
                .listener(new RequestListener<String>() {
                    @Override
                    public void onAddQueue() {
                        Log.d("MainActivity", "onStart");
                    }

                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this, "响应：" + response, Toast.LENGTH_SHORT).show();

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

//		request.addHeader("Cache-Control", "no-cache");

        request.addHeader("User-Agent", "");

        request.setTag(MainActivity.this);
        mRequestQueue.add(request);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_main_get:
                newRequest();
                break;

            case R.id.btn_main_post:
                newPostRequest();
                break;

            case R.id.btn_main_upload_file:
                newPostFileRequest();
                break;

            case R.id.btn_main_stop:
                mRequestQueue.cancelTag(MainActivity.this);
                break;
        }
    }



}
