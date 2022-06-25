package io.micronaut.data.runtime.intercept.async;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.operations.async.AsyncCapableRepository;
import io.micronaut.data.operations.async.AsyncRepositoryOperations;
import io.micronaut.data.runtime.intercept.AbstractQueryInterceptor;
import java.util.List;
import java.util.concurrent.CompletionStage;

public abstract class AbstractAsyncInterceptor<T, R> extends AbstractQueryInterceptor<T, CompletionStage<R>> {
   protected static final Argument<List<Object>> LIST_OF_OBJECTS = Argument.listOf(Object.class);
   @NonNull
   protected final AsyncRepositoryOperations asyncDatastoreOperations;

   protected AbstractAsyncInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
      if (datastore instanceof AsyncCapableRepository) {
         this.asyncDatastoreOperations = ((AsyncCapableRepository)datastore).async();
      } else {
         throw new DataAccessException("Datastore of type [" + datastore.getClass() + "] does not support asynchronous operations");
      }
   }

   @Override
   protected final Argument<?> getReturnType(MethodInvocationContext<?, ?> context) {
      return this.findReturnType(context, Argument.OBJECT_ARGUMENT);
   }

   protected final Argument<?> findReturnType(MethodInvocationContext<?, ?> context, Argument<?> defaultArg) {
      return context.isSuspend()
         ? context.getReturnType().asArgument()
         : (Argument)context.getReturnType().asArgument().getFirstTypeVariable().orElse(defaultArg);
   }

   @Nullable
   protected Number convertNumberToReturnType(MethodInvocationContext<?, ?> context, Number number) {
      Argument<?> firstTypeVar = this.findReturnType(context, Argument.LONG);
      Class<?> type = firstTypeVar.getType();
      if (type != Object.class && type != Void.class) {
         if (number == null) {
            number = 0;
         }

         return !type.isInstance(number)
            ? (Number)this.operations
               .getConversionService()
               .convert(number, firstTypeVar)
               .orElseThrow(() -> new IllegalStateException("Unsupported number type for return type: " + firstTypeVar))
            : number;
      } else {
         return null;
      }
   }
}
