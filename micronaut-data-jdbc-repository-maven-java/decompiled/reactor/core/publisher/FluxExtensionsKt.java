package reactor.core.publisher;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import kotlin.Deprecated;
import kotlin.Metadata;
import kotlin.ReplaceWith;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.reflect.KClass;
import kotlin.sequences.Sequence;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

@Metadata(
   mv = {1, 1, 18},
   bv = {1, 0, 3},
   k = 2,
   d1 = {"\u0000\u0091\u0001\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0003\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u001c\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\u000b\n\u0002\u0010\u0018\n\u0002\u0010\u0005\n\u0002\u0010\u0012\n\u0002\u0010\u0006\n\u0002\u0010\u0013\n\u0002\u0010\u0007\n\u0002\u0010\u0014\n\u0002\u0010\b\n\u0002\u0010\u0015\n\u0002\u0010\t\n\u0002\u0010\u0016\n\u0002\u0010\n\n\u0002\u0010\u0017\n\u0002\u0010(\n\u0002\u0018\u0002\n\u0000\n\u0002\b\u0003*\u0001/\u001a#\u0010\u0000\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\n\b\u0000\u0010\u0002\u0018\u0001*\u00020\u0003*\u0006\u0012\u0002\b\u00030\u0001H\u0087\b\u001aJ\u0010\u0004\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\b\b\u0001\u0010\u0005*\u00020\u0006*\b\u0012\u0004\u0012\u0002H\u00020\u00012\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u0002H\u00050\b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u0002H\u0005\u0012\u0004\u0012\u00020\u000b0\nH\u0007\u001a#\u0010\f\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\n\b\u0000\u0010\u0002\u0018\u0001*\u00020\u0003*\u0006\u0012\u0002\b\u00030\u0001H\u0087\b\u001aJ\u0010\r\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\b\b\u0001\u0010\u0005*\u00020\u0006*\b\u0012\u0004\u0012\u0002H\u00020\u00012\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u0002H\u00050\b2\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u0002H\u0005\u0012\u0004\u0012\u00020\u00060\nH\u0007\u001aT\u0010\u000f\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\b\b\u0000\u0010\u0002*\u00020\u0003\"\b\b\u0001\u0010\u0005*\u00020\u0006*\b\u0012\u0004\u0012\u0002H\u00020\u00012\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u0002H\u00050\b2\u0018\u0010\u0010\u001a\u0014\u0012\u0004\u0012\u0002H\u0005\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00020\u00110\nH\u0007\u001aG\u0010\u0012\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\b\b\u0000\u0010\u0002*\u00020\u0003\"\b\b\u0001\u0010\u0005*\u00020\u0006*\b\u0012\u0004\u0012\u0002H\u00020\u00012\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u0002H\u00050\b2\u0006\u0010\u0013\u001a\u0002H\u0002H\u0007¢\u0006\u0002\u0010\u0014\u001a*\u0010\u0015\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\b\b\u0000\u0010\u0002*\u00020\u0003*\u0010\u0012\f\b\u0001\u0012\b\u0012\u0004\u0012\u0002H\u00020\u00160\u0001H\u0007\u001a2\u0010\u0017\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u00012\u0012\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00020\u00110\u0019H\u0007\u001a\"\u0010\u001a\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\b\b\u0000\u0010\u0002*\u00020\u0003*\b\u0012\u0004\u0012\u0002H\u00020\u001bH\u0007\u001a%\u0010\u001a\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\n\u0012\u0006\b\u0001\u0012\u0002H\u00020\u001cH\u0007¢\u0006\u0002\u0010\u001d\u001a\u0012\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001e0\u0001*\u00020\u001fH\u0007\u001a\u0012\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020 0\u0001*\u00020!H\u0007\u001a\u0012\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\"0\u0001*\u00020#H\u0007\u001a\u0012\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020$0\u0001*\u00020%H\u0007\u001a\u0012\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020&0\u0001*\u00020'H\u0007\u001a\u0012\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020(0\u0001*\u00020)H\u0007\u001a\u0012\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020*0\u0001*\u00020+H\u0007\u001a\u0018\u0010\u001a\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\u00020\u0006H\u0007\u001a\"\u0010\u001a\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\b\b\u0000\u0010\u0002*\u00020\u0003*\b\u0012\u0004\u0012\u0002H\u00020\u0016H\u0007\u001a\"\u0010\u001a\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\b\b\u0000\u0010\u0002*\u00020\u0003*\b\u0012\u0004\u0012\u0002H\u00020,H\u0007\u001a\"\u0010\u001a\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\b\b\u0000\u0010\u0002*\u00020\u0003*\b\u0012\u0004\u0012\u0002H\u00020-H\u0007\u001a\"\u0010\u001a\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\b\b\u0000\u0010\u0002*\u00020\u0003*\b\u0012\u0004\u0012\u0002H\u00020\u0011H\u0007\u001a#\u0010.\u001a\b\u0012\u0004\u0012\u0002H\u00020/\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020,H\u0002¢\u0006\u0002\u00100¨\u00061"},
   d2 = {"cast", "Lreactor/core/publisher/Flux;", "T", "", "doOnError", "E", "", "exceptionType", "Lkotlin/reflect/KClass;", "onError", "Lkotlin/Function1;", "", "ofType", "onErrorMap", "mapper", "onErrorResume", "fallback", "Lorg/reactivestreams/Publisher;", "onErrorReturn", "value", "(Lreactor/core/publisher/Flux;Lkotlin/reflect/KClass;Ljava/lang/Object;)Lreactor/core/publisher/Flux;", "split", "", "switchIfEmpty", "s", "Lkotlin/Function0;", "toFlux", "Ljava/util/stream/Stream;", "", "([Ljava/lang/Object;)Lreactor/core/publisher/Flux;", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "Lkotlin/sequences/Sequence;", "toIterable", "reactor/core/publisher/FluxExtensionsKt$toIterable$1", "(Ljava/util/Iterator;)Lreactor/core/publisher/FluxExtensionsKt$toIterable$1;", "reactor-core"}
)
public final class FluxExtensionsKt {
   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toFlux"},
   expression = "toFlux()"
)
   )
   @NotNull
   public static final <T> Flux<T> toFlux(@NotNull Publisher<T> $this$toFlux) {
      Intrinsics.checkParameterIsNotNull($this$toFlux, "$this$toFlux");
      Flux var10000 = Flux.from($this$toFlux);
      Intrinsics.checkExpressionValueIsNotNull(var10000, "Flux.from(this)");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toFlux"},
   expression = "toFlux()"
)
   )
   @NotNull
   public static final <T> Flux<T> toFlux(@NotNull Iterator<? extends T> $this$toFlux) {
      Intrinsics.checkParameterIsNotNull($this$toFlux, "$this$toFlux");
      return toFlux(toIterable($this$toFlux));
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toFlux"},
   expression = "toFlux()"
)
   )
   @NotNull
   public static final <T> Flux<T> toFlux(@NotNull Iterable<? extends T> $this$toFlux) {
      Intrinsics.checkParameterIsNotNull($this$toFlux, "$this$toFlux");
      Flux var10000 = Flux.fromIterable($this$toFlux);
      Intrinsics.checkExpressionValueIsNotNull(var10000, "Flux.fromIterable(this)");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toFlux"},
   expression = "toFlux()"
)
   )
   @NotNull
   public static final <T> Flux<T> toFlux(@NotNull final Sequence<? extends T> $this$toFlux) {
      Intrinsics.checkParameterIsNotNull($this$toFlux, "$this$toFlux");
      Flux var10000 = Flux.fromIterable(new Iterable<T>() {
         @NotNull
         public Iterator<T> iterator() {
            return $this$toFlux.iterator();
         }
      });
      Intrinsics.checkExpressionValueIsNotNull(var10000, "Flux.fromIterable(object…this@toFlux.iterator()\n})");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toFlux"},
   expression = "toFlux()"
)
   )
   @NotNull
   public static final <T> Flux<T> toFlux(@NotNull Stream<T> $this$toFlux) {
      Intrinsics.checkParameterIsNotNull($this$toFlux, "$this$toFlux");
      Flux var10000 = Flux.fromStream($this$toFlux);
      Intrinsics.checkExpressionValueIsNotNull(var10000, "Flux.fromStream(this)");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toFlux"},
   expression = "toFlux()"
)
   )
   @NotNull
   public static final Flux<Boolean> toFlux(@NotNull boolean[] $this$toFlux) {
      Intrinsics.checkParameterIsNotNull($this$toFlux, "$this$toFlux");
      return toFlux((Iterable)ArraysKt.toList($this$toFlux));
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toFlux"},
   expression = "toFlux()"
)
   )
   @NotNull
   public static final Flux<Byte> toFlux(@NotNull byte[] $this$toFlux) {
      Intrinsics.checkParameterIsNotNull($this$toFlux, "$this$toFlux");
      return toFlux((Iterable)ArraysKt.toList($this$toFlux));
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toFlux"},
   expression = "toFlux()"
)
   )
   @NotNull
   public static final Flux<Short> toFlux(@NotNull short[] $this$toFlux) {
      Intrinsics.checkParameterIsNotNull($this$toFlux, "$this$toFlux");
      return toFlux((Iterable)ArraysKt.toList($this$toFlux));
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toFlux"},
   expression = "toFlux()"
)
   )
   @NotNull
   public static final Flux<Integer> toFlux(@NotNull int[] $this$toFlux) {
      Intrinsics.checkParameterIsNotNull($this$toFlux, "$this$toFlux");
      return toFlux((Iterable)ArraysKt.toList($this$toFlux));
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toFlux"},
   expression = "toFlux()"
)
   )
   @NotNull
   public static final Flux<Long> toFlux(@NotNull long[] $this$toFlux) {
      Intrinsics.checkParameterIsNotNull($this$toFlux, "$this$toFlux");
      return toFlux((Iterable)ArraysKt.toList($this$toFlux));
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toFlux"},
   expression = "toFlux()"
)
   )
   @NotNull
   public static final Flux<Float> toFlux(@NotNull float[] $this$toFlux) {
      Intrinsics.checkParameterIsNotNull($this$toFlux, "$this$toFlux");
      return toFlux((Iterable)ArraysKt.toList($this$toFlux));
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toFlux"},
   expression = "toFlux()"
)
   )
   @NotNull
   public static final Flux<Double> toFlux(@NotNull double[] $this$toFlux) {
      Intrinsics.checkParameterIsNotNull($this$toFlux, "$this$toFlux");
      return toFlux((Iterable)ArraysKt.toList($this$toFlux));
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toFlux"},
   expression = "toFlux()"
)
   )
   @NotNull
   public static final <T> Flux<T> toFlux(@NotNull T[] $this$toFlux) {
      Intrinsics.checkParameterIsNotNull($this$toFlux, "$this$toFlux");
      Flux var10000 = Flux.fromArray($this$toFlux);
      Intrinsics.checkExpressionValueIsNotNull(var10000, "Flux.fromArray(this)");
      return var10000;
   }

   private static final <T> <undefinedtype> toIterable(final Iterator<? extends T> $this$toIterable) {
      return new Iterable<T>() {
         @NotNull
         public Iterator<T> iterator() {
            return $this$toIterable;
         }
      };
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.toFlux"},
   expression = "toFlux<T>()"
)
   )
   @NotNull
   public static final <T> Flux<T> toFlux(@NotNull Throwable $this$toFlux) {
      Intrinsics.checkParameterIsNotNull($this$toFlux, "$this$toFlux");
      Flux var10000 = Flux.error($this$toFlux);
      Intrinsics.checkExpressionValueIsNotNull(var10000, "Flux.error(this)");
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
   public static final <T, E extends Throwable> Flux<T> doOnError(
      @NotNull Flux<T> $this$doOnError, @NotNull KClass<E> exceptionType, @NotNull final Function1<? super E, Unit> onError
   ) {
      Intrinsics.checkParameterIsNotNull($this$doOnError, "$this$doOnError");
      Intrinsics.checkParameterIsNotNull(exceptionType, "exceptionType");
      Intrinsics.checkParameterIsNotNull(onError, "onError");
      Flux var10000 = $this$doOnError.doOnError(JvmClassMappingKt.getJavaClass(exceptionType), new Consumer() {
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
   public static final <T, E extends Throwable> Flux<T> onErrorMap(
      @NotNull Flux<T> $this$onErrorMap, @NotNull KClass<E> exceptionType, @NotNull final Function1<? super E, ? extends Throwable> mapper
   ) {
      Intrinsics.checkParameterIsNotNull($this$onErrorMap, "$this$onErrorMap");
      Intrinsics.checkParameterIsNotNull(exceptionType, "exceptionType");
      Intrinsics.checkParameterIsNotNull(mapper, "mapper");
      Flux var10000 = $this$onErrorMap.onErrorMap(JvmClassMappingKt.getJavaClass(exceptionType), new Function() {
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
   public static final <T, E extends Throwable> Flux<T> onErrorResume(
      @NotNull Flux<T> $this$onErrorResume, @NotNull KClass<E> exceptionType, @NotNull final Function1<? super E, ? extends Publisher<T>> fallback
   ) {
      Intrinsics.checkParameterIsNotNull($this$onErrorResume, "$this$onErrorResume");
      Intrinsics.checkParameterIsNotNull(exceptionType, "exceptionType");
      Intrinsics.checkParameterIsNotNull(fallback, "fallback");
      Flux var10000 = $this$onErrorResume.onErrorResume(JvmClassMappingKt.getJavaClass(exceptionType), new Function() {
         @NotNull
         public final Publisher<T> apply(E it) {
            Function1 var10000 = fallback;
            Intrinsics.checkExpressionValueIsNotNull(it, "it");
            return (Publisher<T>)var10000.invoke(it);
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
   public static final <T, E extends Throwable> Flux<T> onErrorReturn(@NotNull Flux<T> $this$onErrorReturn, @NotNull KClass<E> exceptionType, @NotNull T value) {
      Intrinsics.checkParameterIsNotNull($this$onErrorReturn, "$this$onErrorReturn");
      Intrinsics.checkParameterIsNotNull(exceptionType, "exceptionType");
      Intrinsics.checkParameterIsNotNull(value, "value");
      Flux var10000 = $this$onErrorReturn.onErrorReturn(JvmClassMappingKt.getJavaClass(exceptionType), value);
      Intrinsics.checkExpressionValueIsNotNull(var10000, "onErrorReturn(exceptionType.java, value)");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.split"},
   expression = "split()"
)
   )
   @NotNull
   public static final <T> Flux<T> split(@NotNull Flux<? extends Iterable<? extends T>> $this$split) {
      Intrinsics.checkParameterIsNotNull($this$split, "$this$split");
      Flux var10000 = $this$split.flatMapIterable(null.INSTANCE);
      Intrinsics.checkExpressionValueIsNotNull(var10000, "this.flatMapIterable { it }");
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
   public static final <T> Flux<T> switchIfEmpty(@NotNull Flux<T> $this$switchIfEmpty, @NotNull final Function0<? extends Publisher<T>> s) {
      Intrinsics.checkParameterIsNotNull($this$switchIfEmpty, "$this$switchIfEmpty");
      Intrinsics.checkParameterIsNotNull(s, "s");
      Flux var10000 = $this$switchIfEmpty.switchIfEmpty(Flux.defer(new Supplier() {
         @NotNull
         public final Publisher<T> get() {
            return (Publisher<T>)s.invoke();
         }
      }));
      Intrinsics.checkExpressionValueIsNotNull(var10000, "this.switchIfEmpty(Flux.defer { s() })");
      return var10000;
   }
}
