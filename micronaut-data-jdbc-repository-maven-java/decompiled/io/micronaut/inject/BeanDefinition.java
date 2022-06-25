package io.micronaut.inject;

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.DefaultScope;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Provided;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataDelegate;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.Named;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ArgumentCoercible;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface BeanDefinition<T> extends AnnotationMetadataDelegate, Named, BeanType<T>, ArgumentCoercible<T> {
   String NAMED_ATTRIBUTE = Named.class.getName();

   default Optional<Class<? extends Annotation>> getScope() {
      return Optional.empty();
   }

   default Optional<String> getScopeName() {
      return Optional.empty();
   }

   default boolean isSingleton() {
      String scopeName = (String)this.getScopeName().orElse(null);
      return scopeName != null && scopeName.equals("javax.inject.Singleton")
         ? true
         : this.getAnnotationMetadata()
            .stringValue(DefaultScope.class)
            .map(t -> t.equals(Singleton.class.getName()) || t.equals("javax.inject.Singleton"))
            .orElse(false);
   }

   default Optional<Argument<?>> getContainerElement() {
      return Optional.empty();
   }

   @Override
   default boolean isCandidateBean(@Nullable Argument<?> beanType) {
      if (beanType == null) {
         return false;
      } else if (!BeanType.super.isCandidateBean(beanType)) {
         return false;
      } else {
         Argument<?>[] typeArguments = beanType.getTypeParameters();
         int len = typeArguments.length;
         Class<?> beanClass = beanType.getType();
         if (len == 0) {
            if (!this.isContainerType()) {
               return true;
            } else if (this.getBeanType().isAssignableFrom(beanClass)) {
               return true;
            } else {
               Optional<Argument<?>> containerElement = this.getContainerElement();
               if (!containerElement.isPresent()) {
                  return false;
               } else {
                  Class<?> t = ((Argument)containerElement.get()).getType();
                  return beanType.isAssignableFrom(t) || beanClass == t;
               }
            }
         } else {
            Argument<?>[] beanTypeParameters;
            if (!Iterable.class.isAssignableFrom(beanClass)) {
               Optional<Argument<?>> containerElement = this.getContainerElement();
               if (containerElement.isPresent()) {
                  beanTypeParameters = ((Argument)containerElement.get()).getTypeParameters();
               } else {
                  beanTypeParameters = (Argument[])this.getTypeArguments(beanClass).toArray(Argument.ZERO_ARGUMENTS);
               }
            } else {
               beanTypeParameters = (Argument[])this.getTypeArguments(beanClass).toArray(Argument.ZERO_ARGUMENTS);
            }

            if (len != beanTypeParameters.length) {
               return false;
            } else {
               for(int i = 0; i < beanTypeParameters.length; ++i) {
                  Argument<?> candidateParameter = beanTypeParameters[i];
                  Argument<?> requestedParameter = typeArguments[i];
                  if (!requestedParameter.isAssignableFrom(candidateParameter.getType())
                     && (!candidateParameter.isTypeVariable() || !candidateParameter.isAssignableFrom(requestedParameter.getType()))) {
                     return false;
                  }
               }

               return true;
            }
         }
      }
   }

   @Deprecated
   default boolean isProvided() {
      return this.getAnnotationMetadata().hasDeclaredStereotype(Provided.class);
   }

   default boolean isIterable() {
      return this.hasDeclaredStereotype(EachProperty.class) || this.hasDeclaredStereotype(EachBean.class);
   }

   @Override
   Class<T> getBeanType();

   default Optional<Class<?>> getDeclaringType() {
      return Optional.empty();
   }

   default ConstructorInjectionPoint<T> getConstructor() {
      return new ConstructorInjectionPoint<T>() {
         @Override
         public T invoke(Object... args) {
            throw new UnsupportedOperationException("Cannot be instantiated directly");
         }

         @Override
         public Argument<?>[] getArguments() {
            return Argument.ZERO_ARGUMENTS;
         }

         @Override
         public BeanDefinition<T> getDeclaringBean() {
            return BeanDefinition.this;
         }

         @Override
         public boolean requiresReflection() {
            return false;
         }
      };
   }

   default Collection<Class<?>> getRequiredComponents() {
      return Collections.emptyList();
   }

   default Collection<MethodInjectionPoint<T, ?>> getInjectedMethods() {
      return Collections.emptyList();
   }

   default Collection<FieldInjectionPoint<T, ?>> getInjectedFields() {
      return Collections.emptyList();
   }

   default Collection<MethodInjectionPoint<T, ?>> getPostConstructMethods() {
      return Collections.emptyList();
   }

   default Collection<MethodInjectionPoint<T, ?>> getPreDestroyMethods() {
      return Collections.emptyList();
   }

   @NonNull
   @Override
   default String getName() {
      return this.getBeanType().getName();
   }

   default <R> Optional<ExecutableMethod<T, R>> findMethod(String name, Class<?>... argumentTypes) {
      return Optional.empty();
   }

   default <R> Stream<ExecutableMethod<T, R>> findPossibleMethods(String name) {
      return Stream.empty();
   }

   default T inject(BeanContext context, T bean) {
      return bean;
   }

   default T inject(BeanResolutionContext resolutionContext, BeanContext context, T bean) {
      return bean;
   }

   default Collection<ExecutableMethod<T, ?>> getExecutableMethods() {
      return Collections.emptyList();
   }

   @NonNull
   @Override
   default Argument<T> asArgument() {
      return Argument.of(this.getBeanType(), this.getTypeParameters());
   }

   default boolean isProxy() {
      return this instanceof ProxyBeanDefinition;
   }

   @NonNull
   default List<Argument<?>> getTypeArguments() {
      return this.getTypeArguments(this.getBeanType());
   }

   @NonNull
   default List<Argument<?>> getTypeArguments(Class<?> type) {
      return type == null ? Collections.emptyList() : this.getTypeArguments(type.getName());
   }

   @NonNull
   default Class<?>[] getTypeParameters(@Nullable Class<?> type) {
      if (type == null) {
         return ReflectionUtils.EMPTY_CLASS_ARRAY;
      } else {
         List<Argument<?>> typeArguments = this.getTypeArguments(type);
         if (typeArguments.isEmpty()) {
            return ReflectionUtils.EMPTY_CLASS_ARRAY;
         } else {
            Class[] params = new Class[typeArguments.size()];
            int i = 0;

            for(Argument<?> argument : typeArguments) {
               params[i++] = argument.getType();
            }

            return params;
         }
      }
   }

   @NonNull
   default Class<?>[] getTypeParameters() {
      return this.getTypeParameters(this.getBeanType());
   }

   @NonNull
   default List<Argument<?>> getTypeArguments(String type) {
      return Collections.emptyList();
   }

   default <R> ExecutableMethod<T, R> getRequiredMethod(String name, Class<?>... argumentTypes) {
      return (ExecutableMethod<T, R>)this.findMethod(name, argumentTypes)
         .orElseThrow(() -> ReflectionUtils.newNoSuchMethodError(this.getBeanType(), name, argumentTypes));
   }

   default boolean isAbstract() {
      return Modifier.isAbstract(this.getBeanType().getModifiers());
   }

   @Nullable
   default Qualifier<T> getDeclaredQualifier() {
      AnnotationMetadata annotationMetadata = this.getAnnotationMetadata();
      if (annotationMetadata instanceof AnnotationMetadataHierarchy) {
         annotationMetadata = annotationMetadata.getDeclaredMetadata();
      }

      List<AnnotationValue<Annotation>> annotations = annotationMetadata.getAnnotationValuesByStereotype("javax.inject.Qualifier");
      if (annotations.isEmpty()) {
         Qualifier<T> qualifier = this.resolveDynamicQualifier();
         if (qualifier == null) {
            String name = (String)annotationMetadata.stringValue("javax.inject.Named").orElse(null);
            qualifier = name != null ? Qualifiers.byAnnotation(annotationMetadata, name) : null;
         }

         return qualifier;
      } else if (annotations.size() == 1) {
         AnnotationValue<Annotation> annotationValue = (AnnotationValue)annotations.iterator().next();
         return annotationValue.getAnnotationName().equals(Qualifier.PRIMARY) ? null : Qualifiers.byAnnotation(annotationMetadata, annotationValue);
      } else {
         Qualifier<T>[] qualifiers = new Qualifier[annotations.size()];
         int i = 0;

         for(AnnotationValue<Annotation> annotationValue : annotations) {
            qualifiers[i++] = Qualifiers.byAnnotation(annotationMetadata, annotationValue);
         }

         return Qualifiers.byQualifiers(qualifiers);
      }
   }

   @Nullable
   default Qualifier<T> resolveDynamicQualifier() {
      return null;
   }
}
