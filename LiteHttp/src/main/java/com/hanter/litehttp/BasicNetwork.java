package com.hanter.litehttp;

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.SystemClock;

import com.hanter.litehttp.cache.Cache;
import com.hanter.litehttp.http.exception.AuthFailureError;
import com.hanter.litehttp.http.exception.InvalidUrlError;
import com.hanter.litehttp.http.exception.LiteHttpError;
import com.hanter.litehttp.http.exception.CancelError;
import com.hanter.litehttp.http.exception.NetworkError;
import com.hanter.litehttp.http.exception.NoConnectionError;
import com.hanter.litehttp.http.exception.ServerError;
import com.hanter.litehttp.http.exception.TimeoutError;
import com.hanter.litehttp.stack.HttpStack;
import com.hanter.litehttp.utils.DateUtils;
import com.hanter.litehttp.utils.LiteHttpConfig;
import com.hanter.litehttp.utils.LiteHttpLogger;

/**
 * A network performing Volley requests over an {@link HttpStack}.
 */
public class BasicNetwork implements Network {

    public static final String TAG = "BasicNetwork";

    protected static final boolean DEBUG = LiteHttpLogger.DEBUG;

    private static final int SLOW_REQUEST_THRESHOLD_MS = 3000;

    protected final HttpStack mHttpStack;

    /*
     * @param httpStack HTTP stack to be used
     * @param pool a buffer pool that improves GC performance in copy operations
     */
    public BasicNetwork(HttpStack httpStack) {
        mHttpStack = httpStack;
    }

    @Override
    public NetworkResponse performRequest(Request<?> request) throws LiteHttpError {
        long requestStart = SystemClock.elapsedRealtime();
        while (true) {
        	
            NetworkResponse httpResponse = null;
            byte[] responseContents = null;
            Map<String, List<String>> responseHeaders = Collections.emptyMap();
            try {
                // Gather headers.
                Map<String, List<String>> headers = new HashMap<>();
                               
                addCacheHeaders(headers, request.getCacheEntry());
                
                httpResponse = mHttpStack.performRequest(request, headers);
                                                      
                int statusCode = httpResponse.statusCode;
                         
                responseHeaders = httpResponse.headers;

                logHeaders(responseHeaders);

                
                // Handle cache validation.
                if (statusCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                    Cache.Entry entry = request.getCacheEntry();
                    if (entry == null) {
                        return new NetworkResponse(HttpURLConnection.HTTP_NOT_MODIFIED, null,
                                responseHeaders, true,
                                SystemClock.elapsedRealtime() - requestStart);
                    }

                    // A HTTP 304 response does not have all header fields. We
                    // have to use the header fields from the cache entry plus
                    // the new ones from the response.
                    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.5
                    entry.responseHeaders.putAll(responseHeaders);
                    
                    return new NetworkResponse(HttpURLConnection.HTTP_NOT_MODIFIED, entry.data,
                            entry.responseHeaders, true,
                            SystemClock.elapsedRealtime() - requestStart);
                }

                // handle moved resources
                if (statusCode == HttpURLConnection.HTTP_MOVED_TEMP
                        || statusCode == HttpURLConnection.HTTP_MOVED_PERM) {
                    String newUrl = responseHeaders.get("Location").get(0);
                    request.setRedirectUrl(newUrl);
                    LiteHttpLogger.d(TAG, "redirect：" + newUrl);
                }               

                // Some responses such as 204s do not have content.  We must check.
                if (httpResponse.data != null) {
                	responseContents = httpResponse.data;
                } else {
                	// Add 0 byte response as a way of honestly representing a
                	// no-content request.
                	responseContents = new byte[0];
                }

                // if the request is slow, log it.
                long requestLifetime = SystemClock.elapsedRealtime() - requestStart;
                logSlowRequests(requestLifetime, request, responseContents, statusCode);

                if (statusCode < HttpURLConnection.HTTP_OK || statusCode > 299) {
                    throw new IOException();
                }
                return new NetworkResponse(statusCode, responseContents, responseHeaders, false,
                        SystemClock.elapsedRealtime() - requestStart);
            } catch (SocketTimeoutException e) {
                attemptRetryOnException("socket & connection", request, new TimeoutError(e));
            } catch (MalformedURLException e) {
                throw new InvalidUrlError("Bad URL " + request.getUrl(), e);
            } catch (IOException e) {

                if (e.getMessage() != null)
                    LiteHttpLogger.w(TAG, e.getMessage());

                int statusCode;
                NetworkResponse networkResponse;
                if (httpResponse != null) {
                    statusCode = httpResponse.statusCode;
                } else if (request.isCanceled()
                        || Thread.currentThread().isInterrupted()
                        || e instanceof InterruptedIOException) {
                    throw new CancelError(e);
                } else {
                    throw new NoConnectionError(e);
                }

                if (responseContents != null) {
                    networkResponse = new NetworkResponse(statusCode, responseContents,
                            responseHeaders, false, SystemClock.elapsedRealtime() - requestStart);
                    if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED
                            || statusCode == HttpURLConnection.HTTP_FORBIDDEN) {
                        attemptRetryOnException("auth",
                                request, new AuthFailureError(networkResponse));
                    } else if (statusCode == HttpURLConnection.HTTP_MOVED_TEMP
                            || statusCode == HttpURLConnection.HTTP_MOVED_PERM) {
                        attemptRetryOnException("redirect",
                                request, new AuthFailureError(networkResponse));
                    } else {
                        // Only throw ServerError for 5xx status codes.
                        throw new ServerError(networkResponse);
                    }
                } else {
                    throw new NetworkError(e);
                }
            }
        }
    }

    private void logHeaders(Map<String, List<String>> responseHeaders) {

        if (LiteHttpConfig.DEBUG) return;

        // log headers
        for (Map.Entry<String, List<String>> set : responseHeaders.entrySet()) {

            for (String value : set.getValue()) {
                LiteHttpLogger.i(set.getKey(), value);
            }
        }
    }
    
    /**
     * Logs requests that took over SLOW_REQUEST_THRESHOLD_MS to complete.
     */
    private void logSlowRequests(long requestLifetime, Request<?> request,
            byte[] responseContents, int statusCode) {
        if (DEBUG || requestLifetime > SLOW_REQUEST_THRESHOLD_MS) {
//            LiteHttpLogger.d("HTTP response for request=<%s> [lifetime=%d], [size=%s], " +
//                    "[rc=%d], [retryCount=%s]", request, requestLifetime,
//                    responseContents != null ? responseContents.length : "null",
//                    		statusCode);
        }    	
    	
//        if (DEBUG || requestLifetime > SLOW_REQUEST_THRESHOLD_MS) {
//            LiteHttpLogger.d("HTTP response for request=<%s> [lifetime=%d], [size=%s], " +
//                    "[rc=%d], [retryCount=%s]", request, requestLifetime,
//                    responseContents != null ? responseContents.length : "null",
//                    		statusCode, request.getRetryPolicy().getCurrentRetryCount());
//        }
    }

    /**
     * Attempts to prepare the request for a retry. If there are no more attempts remaining in the
     * request's retry policy, a timeout exception is thrown.
     * @param request The request to use.
     */
    private static void attemptRetryOnException(String logPrefix, Request<?> request,
            LiteHttpError exception) throws LiteHttpError {
        RetryPolicy retryPolicy = request.getRetryPolicy();
//        int oldTimeout = request.getTimeoutMs();

        try {
            retryPolicy.retry(exception);
        } catch (LiteHttpError e) {
//            request.addMarker(
//                    String.format("%s-timeout-giveup [timeout=%s]", logPrefix, oldTimeout));
            throw e;
        }
//        request.addMarker(String.format("%s-retry [timeout=%s]", logPrefix, oldTimeout));        
    }

    private void addCacheHeaders(Map<String, List<String>> headers, Cache.Entry entry) {
        // If there's no cache entry, we're done.
        if (entry == null) {
            return;
        }

        if (entry.etag != null) {
            List<String> headerNoneMatch = new ArrayList<>();
            headerNoneMatch.add(entry.etag);
            headers.put("If-None-Match", headerNoneMatch);
        }

        if (entry.lastModified > 0) {
            List<String> headerModifiedSince = new ArrayList<>();
            Date refTime = new Date(entry.lastModified);
            headerModifiedSince.add(DateUtils.formatDate(refTime));
            headers.put("If-Modified-Since", headerModifiedSince);
        }
    }

    protected void logError(String what, String url, long start) {
        long now = SystemClock.elapsedRealtime();
//        VolleyLog.v("HTTP ERROR(%s) %d ms to fetch %s", what, (now - start), url);
    }
}

