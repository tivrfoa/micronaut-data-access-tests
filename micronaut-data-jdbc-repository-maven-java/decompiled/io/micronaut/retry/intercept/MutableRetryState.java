package io.micronaut.retry.intercept;

import io.micronaut.core.annotation.Internal;
import io.micronaut.retry.RetryState;

@Internal
interface MutableRetryState extends RetryState {
   long nextDelay();
}
