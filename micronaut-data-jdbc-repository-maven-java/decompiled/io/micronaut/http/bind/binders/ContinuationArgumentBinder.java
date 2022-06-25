package io.micronaut.http.bind.binders;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import kotlin.Deprecated;
import kotlin.Metadata;
import kotlin.ReplaceWith;
import kotlin.collections.CollectionsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.reactor.ReactorContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.util.context.ContextView;

@Internal
@Metadata(
   mv = {1, 6, 0},
   k = 1,
   xi = 48,
   d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u0000 \f2\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\u00020\u0001:\u0001\fB\u0005¢\u0006\u0002\u0010\u0003J\u0012\u0010\u0004\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\u00020\u0005H\u0016J2\u0010\u0006\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\u00020\u00072\u0012\u0010\b\u001a\u000e\u0012\b\u0012\u0006\u0012\u0002\b\u00030\u0002\u0018\u00010\t2\n\u0010\n\u001a\u0006\u0012\u0002\b\u00030\u000bH\u0016¨\u0006\r"},
   d2 = {"Lio/micronaut/http/bind/binders/ContinuationArgumentBinder;", "Lio/micronaut/http/bind/binders/TypedRequestArgumentBinder;", "Lkotlin/coroutines/Continuation;", "()V", "argumentType", "Lio/micronaut/core/type/Argument;", "bind", "Lio/micronaut/core/bind/ArgumentBinder$BindingResult;", "context", "Lio/micronaut/core/convert/ArgumentConversionContext;", "source", "Lio/micronaut/http/HttpRequest;", "Companion", "http"}
)
public final class ContinuationArgumentBinder implements TypedRequestArgumentBinder<Continuation<?>> {
   @NotNull
   public static final ContinuationArgumentBinder.Companion Companion = new ContinuationArgumentBinder.Companion(null);
   private static final boolean reactorContextPresent = ClassUtils.isPresent("kotlinx.coroutines.reactor.ReactorContext", null);
   @NotNull
   private static final String CONTINUATION_ARGUMENT_ATTRIBUTE_KEY = "__continuation__";

   @NotNull
   public ArgumentBinder.BindingResult<Continuation<?>> bind(@Nullable ArgumentConversionContext<Continuation<?>> context, @NotNull HttpRequest<?> source) {
      Intrinsics.checkNotNullParameter(source, "source");
      CustomContinuation cc = new CustomContinuation();
      source.setAttribute((CharSequence)"__continuation__", cc);
      return ContinuationArgumentBinder::bind$lambda-0;
   }

   @NotNull
   @Override
   public Argument<Continuation<?>> argumentType() {
      Argument var1 = Argument.of(Continuation.class);
      Intrinsics.checkNotNullExpressionValue(var1, "of(Continuation::class.java)");
      return var1;
   }

   private static final Optional bind$lambda_0/* $FF was: bind$lambda-0*/(CustomContinuation $cc) {
      Intrinsics.checkNotNullParameter($cc, "$cc");
      return Optional.of($cc);
   }

   /** @deprecated */
   @JvmStatic
   @Deprecated(
      message = "Use the new method that takes a collection of coroutine context factories",
      replaceWith = @ReplaceWith(
   expression = "setupCoroutineContext(source, contextView, emptyList())",
   imports = {"io.micronaut.http.bind.binders.ContinuationArgumentBinder.Companion.setupCoroutineContext"}
)
   )
   public static final void setupCoroutineContext(@NotNull HttpRequest<?> source, @NotNull ContextView contextView) {
      Companion.setupCoroutineContext(source, contextView);
   }

   @JvmStatic
   public static final void setupCoroutineContext(
      @NotNull HttpRequest<?> source,
      @NotNull ContextView contextView,
      @NotNull Collection<? extends HttpCoroutineContextFactory<?>> continuationArgumentBinderCoroutineContextFactories
   ) {
      Companion.setupCoroutineContext(source, contextView, continuationArgumentBinderCoroutineContextFactories);
   }

   @JvmStatic
   @Nullable
   public static final Supplier<CompletableFuture<?>> extractContinuationCompletableFutureSupplier(@NotNull HttpRequest<?> source) {
      return Companion.extractContinuationCompletableFutureSupplier(source);
   }

   @Metadata(
      mv = {1, 6, 0},
      k = 1,
      xi = 48,
      d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u001e\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J \u0010\u0007\u001a\u000e\u0012\b\u0012\u0006\u0012\u0002\b\u00030\t\u0018\u00010\b2\n\u0010\n\u001a\u0006\u0012\u0002\b\u00030\u000bH\u0007J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0002J\u001c\u0010\u0010\u001a\u00020\u00112\n\u0010\n\u001a\u0006\u0012\u0002\b\u00030\u000b2\u0006\u0010\u000e\u001a\u00020\u000fH\u0007J.\u0010\u0010\u001a\u00020\u00112\n\u0010\n\u001a\u0006\u0012\u0002\b\u00030\u000b2\u0006\u0010\u000e\u001a\u00020\u000f2\u0010\u0010\u0012\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\u00140\u0013H\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0015"},
      d2 = {"Lio/micronaut/http/bind/binders/ContinuationArgumentBinder$Companion;", "", "()V", "CONTINUATION_ARGUMENT_ATTRIBUTE_KEY", "", "reactorContextPresent", "", "extractContinuationCompletableFutureSupplier", "Ljava/util/function/Supplier;", "Ljava/util/concurrent/CompletableFuture;", "source", "Lio/micronaut/http/HttpRequest;", "propagateReactorContext", "Lkotlin/coroutines/CoroutineContext;", "contextView", "Lreactor/util/context/ContextView;", "setupCoroutineContext", "", "continuationArgumentBinderCoroutineContextFactories", "", "Lio/micronaut/http/bind/binders/HttpCoroutineContextFactory;", "http"}
   )
   public static final class Companion {
      private Companion() {
      }

      /** @deprecated */
      @JvmStatic
      @Deprecated(
         message = "Use the new method that takes a collection of coroutine context factories",
         replaceWith = @ReplaceWith(
   expression = "setupCoroutineContext(source, contextView, emptyList())",
   imports = {"io.micronaut.http.bind.binders.ContinuationArgumentBinder.Companion.setupCoroutineContext"}
)
      )
      public final void setupCoroutineContext(@NotNull HttpRequest<?> source, @NotNull ContextView contextView) {
         Intrinsics.checkNotNullParameter(source, "source");
         Intrinsics.checkNotNullParameter(contextView, "contextView");
         this.setupCoroutineContext(source, contextView, (Collection<? extends HttpCoroutineContextFactory<?>>)CollectionsKt.emptyList());
      }

      @JvmStatic
      public final void setupCoroutineContext(
         @NotNull HttpRequest<?> source,
         @NotNull ContextView contextView,
         @NotNull Collection<? extends HttpCoroutineContextFactory<?>> continuationArgumentBinderCoroutineContextFactories
      ) {
         Intrinsics.checkNotNullParameter(source, "source");
         Intrinsics.checkNotNullParameter(contextView, "contextView");
         Intrinsics.checkNotNullParameter(continuationArgumentBinderCoroutineContextFactories, "continuationArgumentBinderCoroutineContextFactories");
         CustomContinuation customContinuation = (CustomContinuation)source.getAttribute((CharSequence)"__continuation__", CustomContinuation.class)
            .orElse(null);
         if (customContinuation != null) {
            Object coroutineContext = null;
            Object var12 = Dispatchers.getDefault().plus((CoroutineContext)(new ServerRequestScopeHandler(source)));
            if (ContinuationArgumentBinder.reactorContextPresent) {
               var12 = var12.plus(this.propagateReactorContext(contextView));
            }

            Iterable $this$forEach$iv = (Iterable)continuationArgumentBinderCoroutineContextFactories;
            int $i$f$forEach = 0;

            for(Object element$iv : $this$forEach$iv) {
               HttpCoroutineContextFactory it = (HttpCoroutineContextFactory)element$iv;
               int $i$a$-forEach-ContinuationArgumentBinder$Companion$setupCoroutineContext$1 = 0;
               var12 = var12.plus(it.create());
            }

            customContinuation.getContext().setDelegatingCoroutineContext(var12);
         }

      }

      private final CoroutineContext propagateReactorContext(ContextView contextView) {
         return contextView.isEmpty() ? (CoroutineContext)EmptyCoroutineContext.INSTANCE : (CoroutineContext)(new ReactorContext(contextView));
      }

      @JvmStatic
      @Nullable
      public final Supplier<CompletableFuture<?>> extractContinuationCompletableFutureSupplier(@NotNull HttpRequest<?> source) {
         Intrinsics.checkNotNullParameter(source, "source");
         return (Supplier<CompletableFuture<?>>)source.getAttribute((CharSequence)"__continuation__", CustomContinuation.class).orElse(null);
      }
   }
}
