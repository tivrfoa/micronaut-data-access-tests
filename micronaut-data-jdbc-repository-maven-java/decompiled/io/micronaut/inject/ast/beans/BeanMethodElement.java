package io.micronaut.inject.ast.beans;

import io.micronaut.context.annotation.Executable;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.ast.MethodElement;
import java.util.Objects;
import java.util.function.Consumer;

public interface BeanMethodElement extends MethodElement {
   @NonNull
   default BeanMethodElement intercept(AnnotationValue<?>... annotationValue) {
      if (annotationValue != null) {
         for(AnnotationValue<?> value : annotationValue) {
            this.annotate(value);
         }
      }

      return this;
   }

   @NonNull
   default BeanMethodElement executable() {
      this.annotate(Executable.class);
      return this;
   }

   @NonNull
   default BeanMethodElement executable(boolean processOnStartup) {
      this.annotate(Executable.class, builder -> builder.member("processOnStartup", processOnStartup));
      return this;
   }

   @NonNull
   default BeanMethodElement inject() {
      if (this.hasAnnotation("javax.annotation.PreDestroy")) {
         throw new IllegalStateException("Cannot inject a method annotated with @PreDestroy");
      } else if (this.hasAnnotation("javax.annotation.PostConstruct")) {
         throw new IllegalStateException("Cannot inject a method annotated with @PostConstruct");
      } else {
         this.annotate("javax.inject.Inject");
         return this;
      }
   }

   @NonNull
   default BeanMethodElement preDestroy() {
      if (this.hasAnnotation("javax.inject.Inject")) {
         throw new IllegalStateException("Cannot make a method annotated with @Inject a @PreDestroy handler");
      } else if (this.hasAnnotation("javax.annotation.PostConstruct")) {
         throw new IllegalStateException("Cannot make a method annotated with @PostConstruct a @PreDestroy handler");
      } else {
         this.annotate("javax.annotation.PreDestroy");
         return this;
      }
   }

   @NonNull
   default BeanMethodElement postConstruct() {
      if (this.hasAnnotation("javax.inject.Inject")) {
         throw new IllegalStateException("Cannot make a method annotated with @Inject a @PostConstruct handler");
      } else if (this.hasAnnotation("javax.annotation.PreDestroy")) {
         throw new IllegalStateException("Cannot make a method annotated with @PreDestroy a @PostConstruct handler");
      } else {
         this.annotate("javax.annotation.PostConstruct");
         return this;
      }
   }

   @NonNull
   default BeanMethodElement withParameters(@NonNull Consumer<BeanParameterElement[]> parameterConsumer) {
      Objects.requireNonNull(parameterConsumer, "The parameter consumer cannot be null");
      parameterConsumer.accept(this.getParameters());
      return this;
   }

   @NonNull
   BeanParameterElement[] getParameters();
}
