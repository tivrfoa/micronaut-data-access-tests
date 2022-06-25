package io.micronaut.data.intercept.async;

import io.micronaut.data.intercept.DataInterceptor;
import java.util.concurrent.CompletionStage;

public interface UpdateEntityAsyncInterceptor<T> extends DataInterceptor<T, CompletionStage<Object>> {
}
