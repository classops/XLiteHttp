package com.hanter.litehttp;

import com.hanter.litehttp.cache.Cache;
import com.hanter.litehttp.http.exception.LiteHttpError;

/**
 * 响应
 * @param <T> 返回数据类型
 */
public class Response<T> {

	/** Returns a successful response containing the parsed result. */
	public static <T> Response<T> success(T result, Cache.Entry cacheEntry) {
		return new Response<>(result, cacheEntry);
	}

	/**
	 * Returns a failed response containing the given error code and an optional
	 * localized message displayed to the user.
	 */
	public static <T> Response<T> error(LiteHttpError error) {
		return new Response<>(error);
	}

	/** Parsed response, or null in the case of error. */
	public final T result;

	/** Cache metadata for this response, or null in the case of error. */
	public final Cache.Entry cacheEntry;

	/** Detailed error information if <code>errorCode != OK</code>. */
	public final LiteHttpError error;

	/** True if this response was a soft-expired one and a second one MAY be coming. */
	public boolean intermediate = false;

	/**
	 * Returns whether this response is considered successful.
	 */
	public boolean isSuccess() {
		return error == null;
	}


	private Response(T result, Cache.Entry cacheEntry) {
		this.result = result;
		this.cacheEntry = cacheEntry;
		this.error = null;
	}

	private Response(LiteHttpError error) {
		this.result = null;
		this.cacheEntry = null;
		this.error = error;
	}

}
