package io.micronaut.data.intercept.async;

import io.micronaut.data.intercept.DataInterceptor;
import java.util.concurrent.CompletionStage;

public interface DeleteOneAsyncInterceptor<T, R> extends DataInterceptor<T, CompletionStage<R>> {
}
