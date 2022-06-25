package io.micronaut.data.intercept.async;

import io.micronaut.data.intercept.DataInterceptor;
import io.micronaut.data.model.Slice;
import java.util.concurrent.CompletionStage;

public interface FindSliceAsyncInterceptor<T> extends DataInterceptor<T, CompletionStage<Slice<Object>>> {
}
