package io.micronaut.inject.ast.beans;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.ast.ConstructorElement;
import java.util.Objects;
import java.util.function.Consumer;

public interface BeanConstructorElement extends ConstructorElement {
   @NonNull
   default BeanConstructorElement withParameters(@NonNull Consumer<BeanParameterElement[]> parameterConsumer) {
      Objects.requireNonNull(parameterConsumer, "The parameter consumer cannot be null");
      parameterConsumer.accept(this.getParameters());
      return this;
   }

   @NonNull
   BeanParameterElement[] getParameters();
}
