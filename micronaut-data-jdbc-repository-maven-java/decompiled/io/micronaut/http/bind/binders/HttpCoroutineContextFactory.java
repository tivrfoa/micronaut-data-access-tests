package io.micronaut.http.bind.binders;

import kotlin.Metadata;
import kotlin.coroutines.CoroutineContext;
import org.jetbrains.annotations.NotNull;

@Metadata(
   mv = {1, 6, 0},
   k = 1,
   xi = 48,
   d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\bf\u0018\u0000*\n\b\u0000\u0010\u0001 \u0001*\u00020\u00022\u00020\u0003J\r\u0010\u0004\u001a\u00028\u0000H&¢\u0006\u0002\u0010\u0005¨\u0006\u0006"},
   d2 = {"Lio/micronaut/http/bind/binders/HttpCoroutineContextFactory;", "T", "Lkotlin/coroutines/CoroutineContext;", "", "create", "()Lkotlin/coroutines/CoroutineContext;", "http"}
)
public interface HttpCoroutineContextFactory<T extends CoroutineContext> {
   @NotNull
   T create();
}
