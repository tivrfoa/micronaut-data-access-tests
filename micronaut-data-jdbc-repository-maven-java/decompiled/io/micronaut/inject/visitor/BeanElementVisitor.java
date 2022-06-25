package io.micronaut.inject.visitor;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.order.Ordered;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.inject.ast.beans.BeanElement;
import java.lang.annotation.Annotation;
import java.util.List;

public interface BeanElementVisitor<A extends Annotation> extends Ordered, Toggleable {
   List<BeanElementVisitor<?>> VISITORS = BeanElementVisitorLoader.load();

   @Nullable
   BeanElement visitBeanElement(@NonNull BeanElement beanElement, @NonNull VisitorContext visitorContext);

   default void start(VisitorContext visitorContext) {
   }

   default void finish(VisitorContext visitorContext) {
   }

   default boolean supports(@NonNull BeanElement beanElement) {
      if (beanElement == null) {
         return false;
      } else {
         Class<?> t = (Class)GenericTypeUtils.resolveInterfaceTypeArgument(this.getClass(), BeanElementVisitor.class).orElse(Annotation.class);
         return t == Annotation.class ? true : beanElement.hasAnnotation(t.getName());
      }
   }
}
