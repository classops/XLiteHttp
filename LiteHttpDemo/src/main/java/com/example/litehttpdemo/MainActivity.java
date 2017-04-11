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

public class MainActivity extends Activity {
	
	RequestQueue mRequestQueue;
	
	private Button btnTest;
	private Button btnStop;

	Request request;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		getWindow().getWindowStyle().


//		obtainStyledAttributes(R.style)

//		getWindow().getWindowStyle().getString()

//		try {
//			mRequestQueue = new RequestQueue(this, new OkhttpStack(RequestQueue.createSSLSocketFactory(this, R.raw.baidu, "wms123")));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
//		mRequestQueue = new RequestQueue(this, new OkhttpStack());
		
        mRequestQueue = new RequestQueue(this, new HurlStack(null, HttpsUtils.createSSLSocketFactory(this, R.raw.baidu, "wms123")));

//		mRequestQueue = new RequestQueue(this);


				
		btnTest = (Button) findViewById(R.id.btn_main_test);
		btnStop = (Button) findViewById(R.id.btn_main_stop);
		
		btnTest.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//                testOkHttp();

				mRequestQueue.start();

				for (int i = 0; i < 1; i++) {

//					HttpUrl httpUrl = HttpUrl.parse("http://api.jinqiangxinxi.cn/open/selectNewsContentIndex.do");

//					HttpUrl httpUrl = HttpUrl.parse("http://www.sogou.com");

					newRequest();


					/*
					HttpUrl httpUrl = HttpUrl.parse("http://192.168.1.68:8080/ilocallife/login");

					TestRequest test = new TestRequest(httpUrl.toString(), Method.POST, new RequestListener<String>() {
					
//					TestRequest test = new TestRequest("http://www.baidu.com", Method.GET, new RequestListener<String>() {
//					TestRequest test = new TestRequest("https://service.ddsoucai.com/ddsc/v1/30098.html", Method.GET, new RequestListener<String>() {
						
						@Override
						public void onResponse(String response) {												
							Toast.makeText(MainActivity.this, "回调成功" + response, Toast.LENGTH_SHORT).show();
						}
					});
					*/


					/*
					okhttp3.HttpUrl url = new okhttp3.HttpUrl.Builder()
							.scheme("http")
							.host("192.168.1.68").port(8080)
							.addPathSegment("ilocallife")
							.addPathSegment("login")
							.addQueryParameter("phone", "15057140903")
							.addQueryParameter("password", "123456")
							.build();

					Toast.makeText(MainActivity.this, url.toString(), Toast.LENGTH_SHORT).show();
					*/

					/*
					request = new Request.FormBuilder<String>()
							.method(Method.POST)
							.url(httpUrl.toString())
//							.form("pageIndex", "1")
//							.form("sign", "1")
//							.form("phone", "15057140903")
//							.form("password", "123456")
							.queryParameter("phone", "明硕")
							.queryParameter("password", "123456")
							.cache(true)
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
					*/

//					ExecutorService exec = Executors.newFixedThreadPool(10);
//					test.setRetryPolicy(new DefaultRetryPolicy(10000, 2, 1));

//					mRequestQueue.add(test);

//					mRequestQueue.add(request);
					
					
					/*
					UploadFileRequest fileRequest = new UploadFileRequest(url, Method.GET, new RequestListener<String>() {

						@Override
						public void onResponse(String response) {

							Toast.makeText(MainActivity.this, "回调成功" + response, Toast.LENGTH_SHORT).show();
						}
						
					});
					
					fileRequest.addPart("test", "test");
					
					mRequestQueue.add(fileRequest);
					*/
					
					/*
					FormRequest formRequest = new FormRequest(url, Method.POST, new RequestListener<String>() {

						@Override
						public void onResponse(String response) {

							Toast.makeText(MainActivity.this, "回调成功" + response, Toast.LENGTH_SHORT).show();
						}
						
					});
					
					formRequest.addForm("test2", "测试值2");
					formRequest.addEncoded("test1", "测试值1");
					
					mRequestQueue.add(formRequest);
					*/
				}
												
			}

		});
		
		btnStop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mRequestQueue.cancelTag(MainActivity.this);
//				mRequestQueue.stop();
//				request.cancel();
			}
		});
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mRequestQueue.stop();
	}

	public void newRequest() {

//        "https://www.jd.com"

//        HttpUrl httpUrl = HttpUrl.parse("https://www.zhihu.com/");

//        HttpUrl httpUrl = HttpUrl.parse("https://baidu.com");

//		HttpUrl httpUrl = HttpUrl.parse("https://www.greenjsq.me");

//		HttpUrl httpUrl = HttpUrl.parse("https://www.baidu.com");

//        HttpUrl httpUrl = HttpUrl.parse("https://www.jd.hk");

		HttpUrl httpUrl = HttpUrl.parse("https://kyfw.12306.cn/otn/");

//		HttpUrl httpUrl = HttpUrl.parse("https://www.hao123.com");

//        HttpUrl httpUrl = HttpUrl.parse("https://www.baidu.com");

//		HttpUrl httpUrl = HttpUrl.parse("http://api.jinqiangxinxi.cn/app/loadFile.do");

//		HttpUrl httpUrl = HttpUrl.parse("http://192.168.1.68:8080/ilocallife/login");
//		HttpUrl httpUrl = HttpUrl.parse("http://api.jinqiangxinxi.cn/open/selectNewsContentIndex.do");

		request = new Request.LiteBuilder<String>()
				.method(Method.GET)
				.url(httpUrl.toString())
//				.queryParameter("phone", "明硕")
//				.queryParameter("password", "123456")
//                .part("image", "image/*", new File("/sdcard/news_article/0f26b1770f9311fcf7b0e955ef44fc9d.jpg"))
				.cache(Request.CacheMode.ONLY_CACHE)
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
		request.setTag(MainActivity.this);
		mRequestQueue.add(request);
	}

	void testOkHttp() {
        /*
        okhttp3.Request request = new okhttp3.Request.Builder().url("http://www.baidu.com").build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue();
        */
    }
}
