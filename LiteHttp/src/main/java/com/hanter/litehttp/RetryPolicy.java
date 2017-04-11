package com.hanter.litehttp;

import com.hanter.litehttp.http.exception.LiteHttpError;

/**
 * Retry policy for a request.
 */
public interface RetryPolicy {

    /**
     * Returns the current timeout (used for logging).
     */
    int getCurrentTimeout();

    /**
     * Returns the current retry count (used for logging).
     */
    int getCurrentRetryCount();

    /**
     * Prepares for the next retry by applying a backoff to the timeout.
     * @param error The error code of the last attempt.
     * @throws LiteHttpError In the event that the retry could not be performed (for example if we
     * ran out of attempts), the passed in error is thrown.
     */
    void retry(LiteHttpError error) throws LiteHttpError;
}