package io.micronaut.data.intercept.async;

import io.micronaut.data.intercept.DataInterceptor;
import io.micronaut.data.model.Page;
import java.util.concurrent.CompletionStage;

public interface FindPageAsyncInterceptor<T> extends DataInterceptor<T, CompletionStage<Page<Object>>> {
}
