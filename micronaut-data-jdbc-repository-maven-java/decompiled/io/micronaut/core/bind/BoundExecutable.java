package io.micronaut.core.bind;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.Executable;
import java.util.Collections;
import java.util.List;

public interface BoundExecutable<T, R> extends Executable<T, R> {
   Executable<T, R> getTarget();

   R invoke(T instance);

   Object[] getBoundArguments();

   @Override
   default Class<T> getDeclaringType() {
      return this.getTarget().getDeclaringType();
   }

   default List<Argument<?>> getUnboundArguments() {
      return Collections.emptyList();
   }

   @Override
   default R invoke(T instance, Object... arguments) {
      return this.getTarget().invoke(instance, arguments);
   }

   @Override
   default Argument[] getArguments() {
      return this.getTarget().getArguments();
   }

   @Override
   default AnnotationMetadata getAnnotationMetadata() {
      return this.getTarget().getAnnotationMetadata();
   }
}
