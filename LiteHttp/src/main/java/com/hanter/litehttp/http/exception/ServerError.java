package com.hanter.litehttp.http.exception;

import com.hanter.litehttp.NetworkResponse;

import java.nio.charset.Charset;

public class ServerError extends LiteHttpError {

	public ServerError() {
		super();
	}

	public ServerError(Throwable cause) {
		super(cause);
	}

	public ServerError(NetworkResponse response) {
		super(response);
	}

}
