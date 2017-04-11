package com.hanter.litehttp.http.exception;

import com.hanter.litehttp.NetworkResponse;

/**
 * 请求相应代码和信息
 */
public class LiteHttpError extends Exception {

	private static final long serialVersionUID = 1L;

	public final NetworkResponse networkResponse;
	private long networkTimeMs;

	public LiteHttpError() {
		networkResponse = null;
	}

	public LiteHttpError(NetworkResponse response) {
		networkResponse = response;
	}

	public LiteHttpError(String exceptionMessage) {
		super(exceptionMessage);
		networkResponse = null;
	}

	public LiteHttpError(String exceptionMessage, Throwable reason) {
		super(exceptionMessage, reason);
		networkResponse = null;
	}

	public LiteHttpError(Throwable cause) {
		super(cause);
		networkResponse = null;
	}

	/* package */ void setNetworkTimeMs(long networkTimeMs) {
		this.networkTimeMs = networkTimeMs;
	}

	public long getNetworkTimeMs() {
		return networkTimeMs;
	}

}
