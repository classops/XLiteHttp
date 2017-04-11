package com.hanter.litehttp.stack;

import android.support.annotation.WorkerThread;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.hanter.litehttp.NetworkResponse;
import com.hanter.litehttp.Request;
import com.hanter.litehttp.http.exception.AuthFailureError;
import com.hanter.litehttp.http.exception.CancelError;


/**
 * An HTTP stack abstraction.
 */
public interface HttpStack {
    /**
     * Performs an HTTP request with the given parameters.
     *
     * <p>A GET request is sent if request.getPostBody() == null. A POST request is sent otherwise,
     * and the Content-Type header is set to request.getPostBodyContentType().</p>
     *
     * @param request the request to perform
     * @param additionalHeaders additional headers to be sent together with
     *         {@link Request#getHeaders()}
     * @return the HTTP response
     */
    @WorkerThread
    NetworkResponse performRequest(Request<?> request, Map<String, List<String>> additionalHeaders)
            throws IOException, AuthFailureError, CancelError;

}
