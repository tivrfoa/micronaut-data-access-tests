package io.micronaut.inject.provider;

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.Any;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.context.exceptions.DisabledBeanException;
import io.micronaut.context.exceptions.NoSuchBeanException;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Indexes;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.Named;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ArgumentCoercible;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanDefinitionReference;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.annotation.MutableAnnotationMetadata;
import io.micronaut.inject.qualifiers.AnyQualifier;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class AbstractProviderDefinition<T> implements BeanDefinition<T>, BeanFactory<T>, BeanDefinitionReference<T> {
   private static final Argument<Object> TYPE_VARIABLE = Argument.ofTypeVariable(Object.class, "T");
   private final AnnotationMetadata annotationMetadata;

   public AbstractProviderDefinition() {
      MutableAnnotationMetadata metadata = new MutableAnnotationMetadata();
      metadata.addDeclaredAnnotation(Any.class.getName(), Collections.emptyMap());
      metadata.addDeclaredStereotype(Collections.singletonList(Any.class.getName()), "javax.inject.Qualifier", Collections.emptyMap());
      metadata.addDeclaredAnnotation(BootstrapContextCompatible.class.getName(), Collections.emptyMap());

      try {
         metadata.addDeclaredAnnotation(Indexes.class.getName(), Collections.singletonMap("value", this.getBeanType()));
      } catch (NoClassDefFoundError var3) {
      }

      this.annotationMetadata = metadata;
   }

   @Override
   public boolean isContainerType() {
      return false;
   }

   @Override
   public boolean isEnabled(@NonNull BeanContext context, @Nullable BeanResolutionContext resolutionContext) {
      return this.isPresent();
   }

   @Override
   public String getBeanDefinitionName() {
      return this.getClass().getName();
   }

   @Override
   public BeanDefinition<T> load() {
      return this;
   }

   @Override
   public boolean isPresent() {
      return false;
   }

   @NonNull
   protected abstract T buildProvider(
      @NonNull BeanResolutionContext resolutionContext,
      @NonNull BeanContext context,
      @NonNull Argument<Object> argument,
      @Nullable Qualifier<Object> qualifier,
      boolean singleton
   );

   @Override
   public T build(BeanResolutionContext resolutionContext, BeanContext context, BeanDefinition<T> definition) throws BeanInstantiationException {
      BeanResolutionContext.Segment<?> segment = (BeanResolutionContext.Segment)resolutionContext.getPath().currentSegment().orElse(null);
      if (segment != null) {
         InjectionPoint<?> injectionPoint = segment.getInjectionPoint();
         if (injectionPoint instanceof ArgumentCoercible) {
            Argument<?> injectionPointArgument = ((ArgumentCoercible)injectionPoint).asArgument();
            Argument<?> resolveArgument = injectionPointArgument;
            if (injectionPointArgument.isOptional()) {
               resolveArgument = (Argument)injectionPointArgument.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
            }

            Argument<Object> argument = (Argument)resolveArgument.getFirstTypeVariable().orElse(null);
            if (argument != null) {
               Qualifier<Object> qualifier = resolutionContext.getCurrentQualifier();
               if (qualifier == null && segment.getDeclaringType().isIterable()) {
                  Object n = resolutionContext.getAttribute(Named.class.getName());
                  if (n != null) {
                     qualifier = Qualifiers.byName(n.toString());
                  }
               }

               boolean hasBean = context.containsBean(argument, qualifier);
               if (hasBean) {
                  return this.buildProvider(resolutionContext, context, argument, qualifier, definition.isSingleton());
               }

               if (injectionPointArgument.isOptional()) {
                  return (T)Optional.empty();
               }

               if (injectionPointArgument.isNullable()) {
                  throw new DisabledBeanException("Nullable bean doesn't exist");
               }

               if (!(qualifier instanceof AnyQualifier) && !this.isAllowEmptyProviders(context)) {
                  throw new NoSuchBeanException(argument, qualifier);
               }

               return this.buildProvider(resolutionContext, context, argument, qualifier, definition.isSingleton());
            }
         }
      }

      throw new UnsupportedOperationException("Cannot inject provider for Object type");
   }

   protected boolean isAllowEmptyProviders(BeanContext context) {
      return context.getContextConfiguration().isAllowEmptyProviders();
   }

   @Override
   public final boolean isAbstract() {
      return false;
   }

   @Override
   public final boolean isSingleton() {
      return false;
   }

   @NonNull
   @Override
   public final List<Argument<?>> getTypeArguments(Class<?> type) {
      return type == this.getBeanType() ? this.getTypeArguments() : Collections.emptyList();
   }

   @NonNull
   @Override
   public final List<Argument<?>> getTypeArguments() {
      return Collections.singletonList(TYPE_VARIABLE);
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @Override
   public Qualifier<T> getDeclaredQualifier() {
      return null;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o != null && this.getClass() == o.getClass();
      }
   }

   public int hashCode() {
      return this.getClass().hashCode();
   }
}
