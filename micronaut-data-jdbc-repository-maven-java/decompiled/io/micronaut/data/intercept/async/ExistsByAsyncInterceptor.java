package io.micronaut.data.intercept.async;

import io.micronaut.data.intercept.DataInterceptor;
import java.util.concurrent.CompletionStage;

public interface ExistsByAsyncInterceptor<T> extends DataInterceptor<T, CompletionStage<Boolean>> {
}
