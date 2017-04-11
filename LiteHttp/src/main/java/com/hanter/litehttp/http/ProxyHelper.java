package com.hanter.litehttp.http;

import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;

import com.hanter.litehttp.utils.LiteHttpConfig;
import com.hanter.litehttp.utils.LiteHttpLogger;

import java.net.InetSocketAddress;

public class ProxyHelper {
	
	@SuppressWarnings("deprecation")
	public static java.net.Proxy getProxy() {
		
		String host;
		String port;
		
		// API11+采用JVM参数，参考：http://developer.android.com/reference/android/net/Proxy.html
		if (Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
			host = System.getProperty("http.proxyHost");
			port = System.getProperty("http.proxyPort");
			
	    // API10及以下使用这个
		} else {
			host = android.net.Proxy.getDefaultHost();
			port = Integer.toString(android.net.Proxy.getDefaultPort());
		}

		try {
			if (TextUtils.isEmpty(host)) {
				return null;
			} else {
				LiteHttpLogger.d(LiteHttpConfig.PROXY_DEBUG, "Proxy", host+":"+port);
				return new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(host, parsePort(port)));
			}
		} catch (Exception e) {
			return null;
		}
		
	}
	
	private static int parsePort(String port) {
		int portNumber;
		try {
			portNumber = Integer.valueOf(port);
		} catch (Exception e) {
			portNumber = 0;
		}
		
		return portNumber;
	}
}
