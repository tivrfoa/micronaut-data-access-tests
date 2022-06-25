package reactor.core.publisher;

import java.util.List;
import java.util.function.Function;
import kotlin.Deprecated;
import kotlin.Metadata;
import kotlin.ReplaceWith;
import kotlin.TypeCastException;
import kotlin.collections.ArraysKt;
import kotlin.jvm.JvmName;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

@Metadata(
   mv = {1, 1, 18},
   bv = {1, 0, 3},
   k = 2,
   d1 = {"\u0000*\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u001c\n\u0000\n\u0002\u0010 \n\u0000\u001a/\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u00012\u001a\u0010\u0003\u001a\u000e\u0012\n\b\u0001\u0012\u0006\u0012\u0002\b\u00030\u00050\u0004\"\u0006\u0012\u0002\b\u00030\u0005H\u0007¢\u0006\u0002\u0010\u0006\u001aM\u0010\u0007\u001a\b\u0012\u0004\u0012\u0002H\b0\u0001\"\u0004\b\u0000\u0010\b2\u001a\u0010\t\u001a\u000e\u0012\n\b\u0001\u0012\u0006\u0012\u0002\b\u00030\u00010\u0004\"\u0006\u0012\u0002\b\u00030\u00012\u0016\u0010\n\u001a\u0012\u0012\b\u0012\u0006\u0012\u0002\b\u00030\u0004\u0012\u0004\u0012\u0002H\b0\u000bH\u0007¢\u0006\u0002\u0010\f\u001a\u001c\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001*\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\u00050\rH\u0007\u001aG\u0010\u0007\u001a\b\u0012\u0004\u0012\u0002H\b0\u0001\"\u0004\b\u0000\u0010\u000e\"\u0004\b\u0001\u0010\b*\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u000e0\u00010\r2\u001a\b\u0004\u0010\n\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u000e0\u000f\u0012\u0004\u0012\u0002H\b0\u000bH\u0087\b¨\u0006\u0010"},
   d2 = {"whenComplete", "Lreactor/core/publisher/Mono;", "Ljava/lang/Void;", "sources", "", "Lorg/reactivestreams/Publisher;", "([Lorg/reactivestreams/Publisher;)Lreactor/core/publisher/Mono;", "zip", "R", "monos", "combinator", "Lkotlin/Function1;", "([Lreactor/core/publisher/Mono;Lkotlin/jvm/functions/Function1;)Lreactor/core/publisher/Mono;", "", "T", "", "reactor-core"}
)
@JvmName(
   name = "MonoWhenFunctionsKt"
)
public final class MonoWhenFunctionsKt {
   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.whenComplete"},
   expression = "whenComplete()"
)
   )
   @NotNull
   public static final Mono<Void> whenComplete(@NotNull Iterable<? extends Publisher<?>> $this$whenComplete) {
      Intrinsics.checkParameterIsNotNull($this$whenComplete, "$this$whenComplete");
      Mono var10000 = Mono.when($this$whenComplete);
      Intrinsics.checkExpressionValueIsNotNull(var10000, "Mono.`when`(this)");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.zip"},
   expression = "zip(combinator)"
)
   )
   @NotNull
   public static final <T, R> Mono<R> zip(
      @NotNull Iterable<? extends Mono<T>> $this$zip, @NotNull final Function1<? super List<? extends T>, ? extends R> combinator
   ) {
      int $i$f$zip = 0;
      Intrinsics.checkParameterIsNotNull($this$zip, "$this$zip");
      Intrinsics.checkParameterIsNotNull(combinator, "combinator");
      Mono var10000 = Mono.zip($this$zip, new Function() {
         public final R apply(Object[] it) {
            Function1 var10000 = combinator;
            Intrinsics.checkExpressionValueIsNotNull(it, "it");
            List var10001 = ArraysKt.asList(it);
            if (var10001 == null) {
               throw new TypeCastException("null cannot be cast to non-null type kotlin.collections.List<T>");
            } else {
               return (R)var10000.invoke(var10001);
            }
         }
      });
      Intrinsics.checkExpressionValueIsNotNull(var10000, "Mono.zip(this) { combina…it.asList() as List<T>) }");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.whenComplete"},
   expression = "whenComplete(*sources)"
)
   )
   @NotNull
   public static final Mono<Void> whenComplete(@NotNull Publisher<?>... sources) {
      Intrinsics.checkParameterIsNotNull(sources, "sources");
      Mono var10000 = MonoBridges.when(sources);
      Intrinsics.checkExpressionValueIsNotNull(var10000, "MonoBridges.`when`(sources)");
      return var10000;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.publisher.zip"},
   expression = "zip(*monos, combinator)"
)
   )
   @NotNull
   public static final <R> Mono<R> zip(@NotNull Mono<?>[] monos, @NotNull final Function1<? super Object[], ? extends R> combinator) {
      Intrinsics.checkParameterIsNotNull(monos, "monos");
      Intrinsics.checkParameterIsNotNull(combinator, "combinator");
      Mono var10000 = MonoBridges.zip(new Function() {
      }, monos);
      Intrinsics.checkExpressionValueIsNotNull(var10000, "MonoBridges.zip(combinator, monos)");
      return var10000;
   }
}
