package io.micronaut.http.server;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.ContinuationArgumentBinder;
import io.micronaut.http.bind.binders.HttpCoroutineContextFactory;
import jakarta.inject.Singleton;
import java.util.List;
import kotlin.coroutines.CoroutineContext;
import reactor.util.context.ContextView;

@Internal
@Singleton
@Requires(
   classes = {CoroutineContext.class}
)
public final class CoroutineHelper {
   private final List<HttpCoroutineContextFactory<?>> coroutineContextFactories;

   CoroutineHelper(List<HttpCoroutineContextFactory<?>> coroutineContextFactories) {
      this.coroutineContextFactories = coroutineContextFactories;
   }

   public void setupCoroutineContext(HttpRequest<?> httpRequest, ContextView contextView) {
      ContinuationArgumentBinder.setupCoroutineContext(httpRequest, contextView, this.coroutineContextFactories);
   }
}
