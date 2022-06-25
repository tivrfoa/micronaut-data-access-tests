package reactor.util.function;

import kotlin.Deprecated;
import kotlin.Metadata;
import kotlin.ReplaceWith;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

@Metadata(
   mv = {1, 1, 18},
   bv = {1, 0, 3},
   k = 2,
   d1 = {"\u0000<\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\"\u0010\u0000\u001a\u0002H\u0001\"\u0004\b\u0000\u0010\u0001*\f\u0012\u0004\u0012\u0002H\u0001\u0012\u0002\b\u00030\u0002H\u0087\u0002¢\u0006\u0002\u0010\u0003\u001a\"\u0010\u0004\u001a\u0002H\u0001\"\u0004\b\u0000\u0010\u0001*\f\u0012\u0002\b\u0003\u0012\u0004\u0012\u0002H\u00010\u0002H\u0087\u0002¢\u0006\u0002\u0010\u0003\u001a&\u0010\u0005\u001a\u0002H\u0001\"\u0004\b\u0000\u0010\u0001*\u0010\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0004\u0012\u0002H\u00010\u0006H\u0087\u0002¢\u0006\u0002\u0010\u0007\u001a*\u0010\b\u001a\u0002H\u0001\"\u0004\b\u0000\u0010\u0001*\u0014\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0004\u0012\u0002H\u00010\tH\u0087\u0002¢\u0006\u0002\u0010\n\u001a.\u0010\u000b\u001a\u0002H\u0001\"\u0004\b\u0000\u0010\u0001*\u0018\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0004\u0012\u0002H\u00010\fH\u0087\u0002¢\u0006\u0002\u0010\r\u001a2\u0010\u000e\u001a\u0002H\u0001\"\u0004\b\u0000\u0010\u0001*\u001c\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0004\u0012\u0002H\u00010\u000fH\u0087\u0002¢\u0006\u0002\u0010\u0010\u001a6\u0010\u0011\u001a\u0002H\u0001\"\u0004\b\u0000\u0010\u0001* \u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0004\u0012\u0002H\u00010\u0012H\u0087\u0002¢\u0006\u0002\u0010\u0013\u001a:\u0010\u0014\u001a\u0002H\u0001\"\u0004\b\u0000\u0010\u0001*$\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0012\u0004\u0012\u0002H\u00010\u0015H\u0087\u0002¢\u0006\u0002\u0010\u0016¨\u0006\u0017"},
   d2 = {"component1", "T", "Lreactor/util/function/Tuple2;", "(Lreactor/util/function/Tuple2;)Ljava/lang/Object;", "component2", "component3", "Lreactor/util/function/Tuple3;", "(Lreactor/util/function/Tuple3;)Ljava/lang/Object;", "component4", "Lreactor/util/function/Tuple4;", "(Lreactor/util/function/Tuple4;)Ljava/lang/Object;", "component5", "Lreactor/util/function/Tuple5;", "(Lreactor/util/function/Tuple5;)Ljava/lang/Object;", "component6", "Lreactor/util/function/Tuple6;", "(Lreactor/util/function/Tuple6;)Ljava/lang/Object;", "component7", "Lreactor/util/function/Tuple7;", "(Lreactor/util/function/Tuple7;)Ljava/lang/Object;", "component8", "Lreactor/util/function/Tuple8;", "(Lreactor/util/function/Tuple8;)Ljava/lang/Object;", "reactor-core"}
)
public final class TupleExtensionsKt {
   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions and import reactor.kotlin.core.util.function.component1",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.util.function.component1"},
   expression = "component1()"
)
   )
   public static final <T> T component1(@NotNull Tuple2<T, ?> $this$component1) {
      Intrinsics.checkParameterIsNotNull($this$component1, "$this$component1");
      return (T)$this$component1.t1;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions and import reactor.kotlin.core.util.function.component2",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.util.function.component2"},
   expression = "component2()"
)
   )
   public static final <T> T component2(@NotNull Tuple2<?, T> $this$component2) {
      Intrinsics.checkParameterIsNotNull($this$component2, "$this$component2");
      return (T)$this$component2.t2;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions and import reactor.kotlin.core.util.function.component3",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.util.function.component3"},
   expression = "component3()"
)
   )
   public static final <T> T component3(@NotNull Tuple3<?, ?, T> $this$component3) {
      Intrinsics.checkParameterIsNotNull($this$component3, "$this$component3");
      return (T)$this$component3.t3;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions and import reactor.kotlin.core.util.function.component4",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.util.function.component4"},
   expression = "component4()"
)
   )
   public static final <T> T component4(@NotNull Tuple4<?, ?, ?, T> $this$component4) {
      Intrinsics.checkParameterIsNotNull($this$component4, "$this$component4");
      return (T)$this$component4.t4;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions and import reactor.kotlin.core.util.function.component5",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.util.function.component5"},
   expression = "component5()"
)
   )
   public static final <T> T component5(@NotNull Tuple5<?, ?, ?, ?, T> $this$component5) {
      Intrinsics.checkParameterIsNotNull($this$component5, "$this$component5");
      return (T)$this$component5.t5;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions and import reactor.kotlin.core.util.function.component6",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.util.function.component6"},
   expression = "component6()"
)
   )
   public static final <T> T component6(@NotNull Tuple6<?, ?, ?, ?, ?, T> $this$component6) {
      Intrinsics.checkParameterIsNotNull($this$component6, "$this$component6");
      return (T)$this$component6.t6;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions and import reactor.kotlin.core.util.function.component7",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.util.function.component7"},
   expression = "component7()"
)
   )
   public static final <T> T component7(@NotNull Tuple7<?, ?, ?, ?, ?, ?, T> $this$component7) {
      Intrinsics.checkParameterIsNotNull($this$component7, "$this$component7");
      return (T)$this$component7.t7;
   }

   /** @deprecated */
   @Deprecated(
      message = "To be removed in 3.3.0.RELEASE, replaced by module reactor-kotlin-extensions and import reactor.kotlin.core.util.function.component8",
      replaceWith = @ReplaceWith(
   imports = {"reactor.kotlin.core.util.function.component8"},
   expression = "component8()"
)
   )
   public static final <T> T component8(@NotNull Tuple8<?, ?, ?, ?, ?, ?, ?, T> $this$component8) {
      Intrinsics.checkParameterIsNotNull($this$component8, "$this$component8");
      return (T)$this$component8.t8;
   }
}
