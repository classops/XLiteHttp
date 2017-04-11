package com.hanter.litehttp.http.exception;

import com.hanter.litehttp.NetworkResponse;

public class AuthFailureError extends LiteHttpError {

	private static final long serialVersionUID = 1L;

	public AuthFailureError() {
		super();
	}
	
	public AuthFailureError(NetworkResponse response) {
		super(response);
	}

}
