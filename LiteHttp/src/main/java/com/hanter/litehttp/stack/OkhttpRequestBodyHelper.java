package com.hanter.litehttp.stack;

import com.hanter.litehttp.http.RequestBody;

import java.io.IOException;

import okio.BufferedSink;

public class OkhttpRequestBodyHelper {

	public static okhttp3.RequestBody convertToRequestBody(final RequestBody requestBody) {
		return new okhttp3.RequestBody() {

			@Override
			public okhttp3.MediaType contentType() {
				return okhttp3.MediaType.parse(requestBody.contentType().toString());
			}
					
			@Override
			public long contentLength() throws IOException {
				return requestBody.contentLength();
			}

			@Override
			public void writeTo(BufferedSink sink) throws IOException {
				requestBody.writeTo(sink);
			}
			
		};
	}

}
