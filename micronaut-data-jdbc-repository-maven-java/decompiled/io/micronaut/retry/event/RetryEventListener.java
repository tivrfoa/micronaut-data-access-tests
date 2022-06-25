package io.micronaut.retry.event;

import io.micronaut.context.event.ApplicationEventListener;

public interface RetryEventListener extends ApplicationEventListener<RetryEvent> {
}
