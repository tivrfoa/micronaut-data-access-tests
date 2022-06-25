package io.micronaut.inject.ast.beans;

import io.micronaut.context.annotation.Value;

public interface InjectableElement extends ConfigurableElement {
   default InjectableElement injectValue(String expression) {
      this.annotate(Value.class, builder -> builder.value(expression));
      return this;
   }
}
