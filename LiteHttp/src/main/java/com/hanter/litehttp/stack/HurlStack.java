package com.hanter.litehttp.stack;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import okio.BufferedSink;
import okio.Okio;

import com.hanter.litehttp.NetworkResponse;
import com.hanter.litehttp.Request;
import com.hanter.litehttp.Request.Method;
import com.hanter.litehttp.http.ByteArrayPool;
import com.hanter.litehttp.http.PoolingByteArrayOutputStream;
import com.hanter.litehttp.http.ProxyHelper;
import com.hanter.litehttp.http.RequestBody;
import com.hanter.litehttp.http.exception.AuthFailureError;
import com.hanter.litehttp.http.exception.CancelError;
import com.hanter.litehttp.http.exception.ServerError;
import com.hanter.litehttp.utils.LiteHttpLogger;

/**
 * An {@link HttpStack} based on {@link HttpURLConnection}.
 */
public class HurlStack implements HttpStack {

    private static final boolean DEBUG = true;

    private static final String TAG = "HurlStack";

    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * An interface for transforming URLs before use.
     */
    public interface UrlRewriter {
        /**
         * Returns a URL to use instead of the provided one, or null to indicate
         * this URL should not be used at all.
         */
        String rewriteUrl(String originalUrl);
    }
    
    private final static int DEFAULT_POOL_SIZE = 4096;
    
    protected final ByteArrayPool mPool;

    private final UrlRewriter mUrlRewriter;
    private final SSLSocketFactory mSslSocketFactory;

    public HurlStack() {
        this(null);
    }

    /**
     * @param urlRewriter Rewriter to use for request URLs
     */
    public HurlStack(UrlRewriter urlRewriter) {
        this(urlRewriter, null);
    }

    /**
     * @param urlRewriter Rewriter to use for request URLs
     * @param sslSocketFactory SSL factory to use for HTTPS connections
     */
    public HurlStack(UrlRewriter urlRewriter, SSLSocketFactory sslSocketFactory) {
        mUrlRewriter = urlRewriter;
        mSslSocketFactory = sslSocketFactory;
        
        mPool = new ByteArrayPool(DEFAULT_POOL_SIZE);
    }

    @Override
    public NetworkResponse performRequest(Request<?> request, Map<String, List<String>> additionalHeaders)
            throws IOException, AuthFailureError, CancelError {
        String url = request.getUrl();
        HashMap<String, List<String>> map = new HashMap<>();
        // 添加header
        map.putAll(request.getHeaders());
        map.putAll(additionalHeaders);
        if (mUrlRewriter != null) {
            String rewritten = mUrlRewriter.rewriteUrl(url);
            if (rewritten == null) {
                throw new IOException("URL blocked by rewriter: " + url);
            }
            url = rewritten;
        }
        URL parsedUrl = new URL(url);

        LiteHttpLogger.d(DEBUG, TAG, "openConnection");

        if (request.isCanceled() || Thread.currentThread().isInterrupted()) {
            throw new CancelError();
        }

        HttpURLConnection connection = openConnection(parsedUrl, request);

        LiteHttpLogger.d(DEBUG, TAG, "setHeaderValues");

        for (String headerName : map.keySet()) {        	
        	List<String> headerValues = map.get(headerName);        	
        	for (String headerValue : headerValues) {
        		connection.addRequestProperty(headerName, headerValue);
        	}        	           
        }

        LiteHttpLogger.d(DEBUG, TAG, "setRequestParameters");

        setConnectionParametersForRequest(connection, request);

        LiteHttpLogger.d(TAG, "getResponseCode");

        connection.connect();

        if (request.isCanceled() || Thread.currentThread().isInterrupted()) {
            throw new CancelError();
        }

        // Initialize HttpResponse with data from the HttpURLConnection.
        int responseCode = connection.getResponseCode();
        if (responseCode == -1) {
            // -1 is returned by getResponseCode() if the response code could not be retrieved.
            // Signal to the caller that something was wrong with the connection.
            throw new IOException("Could not retrieve response code from HttpUrlConnection.");
        }

        if (request.isCanceled() || Thread.currentThread().isInterrupted()) {
            throw new CancelError();
        }

        NetworkResponse response = null;

        // Header有时接收为空？？
        Map<String, List<String>> headers = connection.getHeaderFields();

        if (headers != null)
            LiteHttpLogger.d(DEBUG, TAG, headers.toString());

		try {
			response = new NetworkResponse(responseCode, readData(request, connection),
                    headers, false);
		} catch (ServerError e) {
			e.printStackTrace();
		}

        String msg = connection.getResponseMessage();

        if (!TextUtils.isEmpty(msg)) {
            LiteHttpLogger.d(TAG, "HTTP 1.1 " + connection.getResponseCode() + " " + msg);
        } else {
            LiteHttpLogger.d(TAG, "responseMsg: null");
        }

        return response;
    }
    
    private byte[] readData(Request<?> request, HttpURLConnection connection) throws IOException, ServerError, CancelError {
        PoolingByteArrayOutputStream bytes =
                new PoolingByteArrayOutputStream(mPool, connection.getContentLength());
        byte[] buffer = null;
        
        InputStream in = null;

        try {
            LiteHttpLogger.d(TAG, "getInputStream");

            try {
                in = connection.getInputStream();

                if (in == null) {
                    throw new ServerError();
                }
            } catch (IOException ioe) {
                in = connection.getErrorStream();

                if (in == null) {
                    throw new ServerError(ioe);
                }
            }

            if (request.isCanceled() || Thread.currentThread().isInterrupted()) {
                throw new CancelError();
            }

            LiteHttpLogger.d(TAG, "readBuffer");

            buffer = mPool.getBuf(1024);
            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);

                if (request.isCanceled() || Thread.currentThread().isInterrupted()) {
                    throw new CancelError();
                }
            }

            return bytes.toByteArray();
        } finally {
            try {
                // Close the InputStream and release the resources by "consuming the content".
                if (in != null) {
                	in.close();
                }

                mPool.returnBuf(buffer);

                bytes.close();
            } catch (IOException e) {
                // This can happen if there was an exception above that left the entity in
                // an invalid state.
//                VolleyLog.v("Error occured when calling consumingContent");
            }
        }	
    }

    /**
     * Create an {@link HttpURLConnection} for the specified {@code url}.
     */
    protected HttpURLConnection createConnection(URL url) throws IOException {
    	
    	// 实时代理设置
    	
    	Proxy proxy = ProxyHelper.getProxy();
    	
    	HttpURLConnection connection;
    	if (proxy != null) {
    		connection = (HttpURLConnection) url.openConnection(proxy);
    	} else {
    		connection = (HttpURLConnection) url.openConnection();
    	}   
        
        // Workaround for the M release HttpURLConnection not observing the
        // HttpURLConnection.setFollowRedirects() property.
        // https://code.google.com/p/android/issues/detail?id=194495
        connection.setInstanceFollowRedirects(HttpURLConnection.getFollowRedirects());        
        
        return connection;
    }

    /**
     * Opens an {@link HttpURLConnection} with parameters.
     * @param url url
     * @return an open connection
     * @throws IOException
     */
    private HttpURLConnection openConnection(URL url, Request<?> request) throws IOException {
        HttpURLConnection connection = createConnection(url);

        // 设置超时
        int timeoutMs = request.getTimeoutMs();
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setUseCaches(false);
        connection.setDoInput(true);            
               
        // use caller-provided custom SslSocketFactory, if any, for HTTPS
        if ("https".equals(url.getProtocol())) {

            if (mSslSocketFactory != null) {
                ((HttpsURLConnection)connection).setSSLSocketFactory(mSslSocketFactory);
            } else {
                ((HttpsURLConnection)connection).setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
            }

            ((HttpsURLConnection)connection).setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session);
                }
            });
            connection.setInstanceFollowRedirects(true);
        }

        return connection;
    }

    static void setConnectionParametersForRequest(HttpURLConnection connection,
            Request<?> request) throws IOException, AuthFailureError {
        switch (request.getMethod()) {
            case Method.GET:
                connection.setRequestMethod("GET");
                break;
            case Method.DELETE:
                connection.setRequestMethod("DELETE");
                break;
            case Method.POST:
                connection.setRequestMethod("POST");
                addBodyIfExists(connection, request);
                break;
            case Method.PUT:
                connection.setRequestMethod("PUT");
                addBodyIfExists(connection, request);
                break;
            case Method.HEAD:
                connection.setRequestMethod("HEAD");
                break;
            case Method.OPTIONS:
                connection.setRequestMethod("OPTIONS");
                break;
            case Method.TRACE:
                connection.setRequestMethod("TRACE");
                break;
            case Method.PATCH:
                connection.setRequestMethod("PATCH");
                addBodyIfExists(connection, request);
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    // 对POST等请求，添加请求体   
    private static void addBodyIfExists(HttpURLConnection connection, Request<?> request)
            throws IOException, AuthFailureError {
        RequestBody body = request.getBody();
        if (body != null) {
            connection.setDoOutput(true);                                         
            connection.addRequestProperty(HEADER_CONTENT_TYPE, body.contentType().toString());
            
            // 添加请求体，利用Okio可以缓冲写数据，避免了OOM                 
            BufferedSink bufferedSink = null;  
            try {
            	bufferedSink = Okio.buffer(Okio.sink(connection.getOutputStream()));
            	body.writeTo(bufferedSink);
            } finally {
            	if (bufferedSink != null) {
            		try {
            			bufferedSink.close();
            		} catch (Exception e) {
            			//            		
            		}
            	}                
            }
         
        }
    }   
   
}
