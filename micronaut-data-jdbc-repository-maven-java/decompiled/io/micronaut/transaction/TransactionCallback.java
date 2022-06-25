package io.micronaut.transaction;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.function.Function;

@FunctionalInterface
public interface TransactionCallback<T, R> extends Function<TransactionStatus<T>, R> {
   default R apply(TransactionStatus<T> status) {
      try {
         return this.call(status);
      } catch (Exception var3) {
         throw new UndeclaredThrowableException(var3);
      }
   }

   @Nullable
   R call(@NonNull TransactionStatus<T> status) throws Exception;
}
