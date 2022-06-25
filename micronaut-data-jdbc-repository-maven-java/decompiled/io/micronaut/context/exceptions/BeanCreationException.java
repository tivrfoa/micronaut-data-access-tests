package io.micronaut.context.exceptions;

import io.micronaut.context.BeanResolutionContext;
import io.micronaut.inject.BeanType;
import java.util.Optional;

public abstract class BeanCreationException extends BeanContextException {
   private final BeanType rootBeanType;

   protected BeanCreationException(String message, Throwable cause) {
      super(message, cause);
      this.rootBeanType = null;
   }

   protected BeanCreationException(String message) {
      super(message);
      this.rootBeanType = null;
   }

   protected BeanCreationException(BeanResolutionContext resolutionContext, String message) {
      super(message);
      this.rootBeanType = this.resolveRootBeanDefinition(resolutionContext);
   }

   protected BeanCreationException(BeanResolutionContext resolutionContext, String message, Throwable cause) {
      super(message, cause);
      this.rootBeanType = this.resolveRootBeanDefinition(resolutionContext);
   }

   protected <T> BeanCreationException(BeanType<T> beanDefinition, String message, Throwable cause) {
      super(message, cause);
      this.rootBeanType = beanDefinition;
   }

   protected <T> BeanCreationException(BeanType<T> beanDefinition, String message) {
      super(message);
      this.rootBeanType = beanDefinition;
   }

   private BeanType resolveRootBeanDefinition(BeanResolutionContext resolutionContext) {
      BeanType rootBeanType = null;
      if (resolutionContext != null) {
         BeanResolutionContext.Path path = resolutionContext.getPath();
         if (!path.isEmpty()) {
            BeanResolutionContext.Segment segment = (BeanResolutionContext.Segment)path.peek();
            rootBeanType = segment.getDeclaringType();
         } else {
            rootBeanType = resolutionContext.getRootDefinition();
         }
      }

      return rootBeanType;
   }

   public Optional<BeanType> getRootBeanType() {
      return Optional.ofNullable(this.rootBeanType);
   }
}
