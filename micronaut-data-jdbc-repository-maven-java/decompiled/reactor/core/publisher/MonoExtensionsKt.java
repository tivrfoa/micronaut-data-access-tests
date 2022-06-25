package reactor.core.publisher;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import kotlin.Deprecated;
import kotlin.Metadata;
import kotlin.ReplaceWith;
import kotlin.Unit;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KClass;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;

@Metadata(
   mv = {1, 1, 18},
   bv = {1, 0, 3},
   k = 2,
   d1 = {"\u0000>\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0003\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\u001a#\u0010\u0000\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\n\b\u0000\u0010\u0002\u0018\u0001*\u00020\u0003*\u0006\u0012\u0002\b\u00030\u0001H\u0087\b\u001aJ\u0010\u0004\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\b\b\u0001\u0010\u0005*\u00020\u0006*\b\u0012\u0004\u0012\u0002H\u00020\u00012\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u0002H\u00050\b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u0002H\u0005\u0012\u0004\u0012\u00020\u000b0\nH\u0007\u001a#\u0010\f\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\n\b\u0000\u0010\u0002\u0018\u0001*\u00020\u0003*\u0006\u0012\u0002\b\u00030\u0001H\u0087\b\u001aJ\u0010\r\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\b\b\u0001\u0010\u0005*\u00020\u0006*\b\u0012\u0004\u0012\u0002H\u00020\u00012\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u0002H\u00050\b2\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u0002H\u0005\u0012\u0004\u0012\u00020\u00060\nH\u0007\u001aT\u0010\u000f\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\b\b\u0000\u0010\u0002*\u00020\u0003\"\b\b\u0001\u0010\u0005*\u00020\u0006*\b\u0012\u0004\u0012\u0002H\u00020\u00012\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u0002H\u00050\b2\u0018\u0010\u0010\u001a\u0014\u0012\u0004\u0012\u0002H\u0005\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00020\u00010\nH\u0007\u001aG\u0010\u0011\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\b\b\u0000\u0010\u0002*\u00020\u0003\"\b\b\u0001\u0010\u0005*\u00020\u0006*\b\u0012\u0004\u0012\u0002H\u00020\u00012\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u0002H\u00050\b2\u0006\u0010\u0012\u001a\u0002H\u0002H\u0007¢\u0006\u0002\u0010\u0013\u001a2\u0010\u0014\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u00012\u0012\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00020\u00010\u0016H\u0007\u001a \u0010\u0017\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\n\u0012\u0006\u0012\u0004\u0018\u0001H\u00020\u0016H\u0007\u001a!\u0010\u0017\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\b\b\u0000\u0010\u0002*\u00020\u0003*\u0002H\u0002H\u0007¢\u0006\u0002\u0010\u0018\u001a \u0010\u0017\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\n\u0012\u0006\u0012\u0004\u0018\u0001H\u00020\u0019H\u0007\u001a\"\u0010\u0017\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\f\u0012\b\b\u0001\u0012\u0004\u0018\u0001H\u00020\u001aH\u0007\u001a\u0018\u0010\u0017\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\u00020\u0006H\u0007\u001a\u001e\u0010\u0017\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u001bH\u0007¨\u0006\u001c"},
   d2 = {"cast", "Lreactor/core/publisher/Mono;", "T", "", "doOnError", "E", "", "exceptionType", "Lkotlin/reflect/KClass;", "onError", "Lkotlin/Function1;", "", "ofType", "onErrorMap", "mapper", "onErrorResume", "fallback", "onErrorReturn", "value", "(Lreactor/core/publisher/Mono;Lkotlin/reflect/KClass;Ljava/lang/Object;)Lreactor/core/publisher/Mono;", "switchIfEmpty", "s", "Lkotlin/Function0;", "toMono", "(Ljava/lang/Object;)Lreactor/core/publisher/Mono;", "Ljava/util/concurrent/Callable;", "Ljava/util/concurrent/CompletableFuture;", "Lorg/reactivestreams/Publisher;", "reactor-core"}
)
public final class MonoExtensionsKt {
   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toMono"},
   expression = "toMono()"
)
   )
   @NotNull
   public static final <T> Mono<T> toMono(@NotNull Publisher<T> $this$toMono) {
      Intrinsics.checkParameterIsNotNull($this$toMono, "$this$toMono");
      Mono var10000 = Mono.from($this$toMono);
      Intrinsics.checkExpressionValueIsNotNull(var10000, "Mono.from(this)");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toMono"},
   expression = "toMono()"
)
   )
   @NotNull
   public static final <T> Mono<T> toMono(@NotNull final Function0<? extends T> $this$toMono) {
      Intrinsics.checkParameterIsNotNull($this$toMono, "$this$toMono");
      Mono var10000 = Mono.fromSupplier(new Supplier() {
      });
      Intrinsics.checkExpressionValueIsNotNull(var10000, "Mono.fromSupplier(this)");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toMono"},
   expression = "toMono()"
)
   )
   @NotNull
   public static final <T> Mono<T> toMono(@NotNull T $this$toMono) {
      Intrinsics.checkParameterIsNotNull($this$toMono, "$this$toMono");
      Mono var10000 = Mono.just($this$toMono);
      Intrinsics.checkExpressionValueIsNotNull(var10000, "Mono.just(this)");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toMono"},
   expression = "toMono()"
)
   )
   @NotNull
   public static final <T> Mono<T> toMono(@NotNull CompletableFuture<? extends T> $this$toMono) {
      Intrinsics.checkParameterIsNotNull($this$toMono, "$this$toMono");
      Mono var10000 = Mono.fromFuture($this$toMono);
      Intrinsics.checkExpressionValueIsNotNull(var10000, "Mono.fromFuture(this)");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toMono"},
   expression = "toMono()"
)
   )
   @NotNull
   public static final <T> Mono<T> toMono(@NotNull Callable<T> $this$toMono) {
      Intrinsics.checkParameterIsNotNull($this$toMono, "$this$toMono");
      final Function0 var1 = (Function0)(new Function0<T>($this$toMono) {
         @Nullable
         public final T invoke() {
            return (T)((Callable)this.receiver).call();
         }

         public final KDeclarationContainer getOwner() {
            return Reflection.getOrCreateKotlinClass(Callable.class);
         }

         public final String getName() {
            return "call";
         }

         public final String getSignature() {
            return "call()Ljava/lang/Object;";
         }
      });
      Mono var10000 = Mono.fromCallable(new Callable() {
      });
      Intrinsics.checkExpressionValueIsNotNull(var10000, "Mono.fromCallable(this::call)");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toMono"},
   expression = "toMono<T>()"
)
   )
   @NotNull
   public static final <T> Mono<T> toMono(@NotNull Throwable $this$toMono) {
      Intrinsics.checkParameterIsNotNull($this$toMono, "$this$toMono");
      Mono var10000 = Mono.error($this$toMono);
      Intrinsics.checkExpressionValueIsNotNull(var10000, "Mono.error(this)");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.doOnError"},
   expression = "doOnError(exceptionType, onError)"
)
   )
   @NotNull
   public static final <T, E extends Throwable> Mono<T> doOnError(
      @NotNull Mono<T> $this$doOnError, @NotNull KClass<E> exceptionType, @NotNull final Function1<? super E, Unit> onError
   ) {
      Intrinsics.checkParameterIsNotNull($this$doOnError, "$this$doOnError");
      Intrinsics.checkParameterIsNotNull(exceptionType, "exceptionType");
      Intrinsics.checkParameterIsNotNull(onError, "onError");
      Mono var10000 = $this$doOnError.doOnError(JvmClassMappingKt.getJavaClass(exceptionType), new Consumer() {
         public final void accept(E it) {
            Function1 var10000 = onError;
            Intrinsics.checkExpressionValueIsNotNull(it, "it");
            var10000.invoke(it);
         }
      });
      Intrinsics.checkExpressionValueIsNotNull(var10000, "doOnError(exceptionType.java) { onError(it) }");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.onErrorMap"},
   expression = "onErrorMap(exceptionType, mapper)"
)
   )
   @NotNull
   public static final <T, E extends Throwable> Mono<T> onErrorMap(
      @NotNull Mono<T> $this$onErrorMap, @NotNull KClass<E> exceptionType, @NotNull final Function1<? super E, ? extends Throwable> mapper
   ) {
      Intrinsics.checkParameterIsNotNull($this$onErrorMap, "$this$onErrorMap");
      Intrinsics.checkParameterIsNotNull(exceptionType, "exceptionType");
      Intrinsics.checkParameterIsNotNull(mapper, "mapper");
      Mono var10000 = $this$onErrorMap.onErrorMap(JvmClassMappingKt.getJavaClass(exceptionType), new Function() {
         @NotNull
         public final Throwable apply(E it) {
            Function1 var10000 = mapper;
            Intrinsics.checkExpressionValueIsNotNull(it, "it");
            return (Throwable)var10000.invoke(it);
         }
      });
      Intrinsics.checkExpressionValueIsNotNull(var10000, "onErrorMap(exceptionType.java) { mapper(it) }");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.onErrorResume"},
   expression = "onErrorResume(exceptionType, fallback)"
)
   )
   @NotNull
   public static final <T, E extends Throwable> Mono<T> onErrorResume(
      @NotNull Mono<T> $this$onErrorResume, @NotNull KClass<E> exceptionType, @NotNull final Function1<? super E, ? extends Mono<T>> fallback
   ) {
      Intrinsics.checkParameterIsNotNull($this$onErrorResume, "$this$onErrorResume");
      Intrinsics.checkParameterIsNotNull(exceptionType, "exceptionType");
      Intrinsics.checkParameterIsNotNull(fallback, "fallback");
      Mono var10000 = $this$onErrorResume.onErrorResume(JvmClassMappingKt.getJavaClass(exceptionType), new Function() {
         @NotNull
         public final Mono<T> apply(E it) {
            Function1 var10000 = fallback;
            Intrinsics.checkExpressionValueIsNotNull(it, "it");
            return (Mono<T>)var10000.invoke(it);
         }
      });
      Intrinsics.checkExpressionValueIsNotNull(var10000, "onErrorResume(exceptionType.java) { fallback(it) }");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.onErrorReturn"},
   expression = "onErrorReturn(exceptionType, value)"
)
   )
   @NotNull
   public static final <T, E extends Throwable> Mono<T> onErrorReturn(@NotNull Mono<T> $this$onErrorReturn, @NotNull KClass<E> exceptionType, @NotNull T value) {
      Intrinsics.checkParameterIsNotNull($this$onErrorReturn, "$this$onErrorReturn");
      Intrinsics.checkParameterIsNotNull(exceptionType, "exceptionType");
      Intrinsics.checkParameterIsNotNull(value, "value");
      Mono var10000 = $this$onErrorReturn.onErrorReturn(JvmClassMappingKt.getJavaClass(exceptionType), value);
      Intrinsics.checkExpressionValueIsNotNull(var10000, "onErrorReturn(exceptionType.java, value)");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.switchIfEmpty"},
   expression = "switchIfEmpty(s)"
)
   )
   @NotNull
   public static final <T> Mono<T> switchIfEmpty(@NotNull Mono<T> $this$switchIfEmpty, @NotNull final Function0<? extends Mono<T>> s) {
      Intrinsics.checkParameterIsNotNull($this$switchIfEmpty, "$this$switchIfEmpty");
      Intrinsics.checkParameterIsNotNull(s, "s");
      Mono var10000 = $this$switchIfEmpty.switchIfEmpty(Mono.defer(new Supplier() {
         @NotNull
         public final Mono<T> get() {
            return (Mono<T>)s.invoke();
         }
      }));
      Intrinsics.checkExpressionValueIsNotNull(var10000, "this.switchIfEmpty(Mono.defer { s() })");
      return var10000;
   }
}
