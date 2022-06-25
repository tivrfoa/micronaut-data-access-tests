package io.micronaut.context;

import io.micronaut.context.annotation.InjectScope;
import io.micronaut.context.scope.BeanCreationContext;
import io.micronaut.context.scope.CreatedBean;
import io.micronaut.context.scope.CustomScope;
import io.micronaut.context.scope.CustomScopeRegistry;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.BeanIdentifier;
import io.micronaut.inject.BeanType;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultCustomScopeRegistry implements CustomScopeRegistry {
   static final CustomScope<InjectScope> INJECT_SCOPE = new DefaultCustomScopeRegistry.InjectScopeImpl();
   private final BeanLocator beanLocator;
   private final Map<String, Optional<CustomScope<?>>> scopes = new ConcurrentHashMap(2);

   protected DefaultCustomScopeRegistry(BeanLocator beanLocator) {
      this.beanLocator = beanLocator;
      this.scopes.put(InjectScope.class.getName(), Optional.of(INJECT_SCOPE));
   }

   @Override
   public <T> Optional<BeanRegistration<T>> findBeanRegistration(T bean) {
      for(Optional<CustomScope<?>> value : this.scopes.values()) {
         if (value.isPresent()) {
            CustomScope<?> customScope = (CustomScope)value.get();
            Optional<BeanRegistration<T>> beanRegistration = customScope.findBeanRegistration(bean);
            if (beanRegistration.isPresent()) {
               return beanRegistration;
            }
         }
      }

      return Optional.empty();
   }

   @Override
   public Optional<CustomScope<?>> findDeclaredScope(@NonNull Argument<?> argument) {
      AnnotationMetadata annotationMetadata = argument.getAnnotationMetadata();
      return annotationMetadata.hasStereotype("javax.inject.Scope")
         ? annotationMetadata.getAnnotationNameByStereotype("javax.inject.Scope").flatMap(this::findScope)
         : Optional.empty();
   }

   @Override
   public Optional<CustomScope<?>> findDeclaredScope(@NonNull BeanType<?> beanType) {
      if (beanType.getAnnotationMetadata().hasStereotype("javax.inject.Scope")) {
         List<String> scopeHierarchy = beanType.getAnnotationMetadata().getAnnotationNamesByStereotype("javax.inject.Scope");
         if (CollectionUtils.isNotEmpty(scopeHierarchy)) {
            Optional<CustomScope<?>> registeredScope = Optional.empty();

            for(String scope : scopeHierarchy) {
               registeredScope = this.findScope(scope);
               if (registeredScope.isPresent()) {
                  break;
               }
            }

            return registeredScope;
         }
      }

      return Optional.empty();
   }

   @Override
   public Optional<CustomScope<?>> findScope(Class<? extends Annotation> scopeAnnotation) {
      return (Optional<CustomScope<?>>)this.scopes.computeIfAbsent(scopeAnnotation.getName(), s -> {
         Qualifier qualifier = Qualifiers.byTypeArguments(scopeAnnotation);
         return this.beanLocator.findBean(CustomScope.class, qualifier);
      });
   }

   @Override
   public Optional<CustomScope<?>> findScope(String scopeAnnotation) {
      return (Optional<CustomScope<?>>)this.scopes.computeIfAbsent(scopeAnnotation, type -> {
         Qualifier qualifier = Qualifiers.byExactTypeArgumentName(scopeAnnotation);
         return this.beanLocator.findBean(CustomScope.class, qualifier);
      });
   }

   private static final class InjectScopeImpl implements CustomScope<InjectScope>, LifeCycle<DefaultCustomScopeRegistry.InjectScopeImpl> {
      private final List<CreatedBean<?>> currentCreatedBeans = new ArrayList(2);

      private InjectScopeImpl() {
      }

      @Override
      public Class<InjectScope> annotationType() {
         return InjectScope.class;
      }

      @Override
      public <T> T getOrCreate(BeanCreationContext<T> creationContext) {
         CreatedBean<T> createdBean = creationContext.create();
         this.currentCreatedBeans.add(createdBean);
         return createdBean.bean();
      }

      @Override
      public <T> Optional<T> remove(BeanIdentifier identifier) {
         return Optional.empty();
      }

      @Override
      public boolean isRunning() {
         return true;
      }

      public DefaultCustomScopeRegistry.InjectScopeImpl stop() {
         for(CreatedBean<?> currentCreatedBean : this.currentCreatedBeans) {
            currentCreatedBean.close();
         }

         this.currentCreatedBeans.clear();
         return this;
      }
   }
}
