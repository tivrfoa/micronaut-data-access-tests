package io.micronaut.inject.provider;

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.exceptions.NoSuchBeanException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.AnyQualifier;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

@Internal
public final class BeanProviderDefinition extends AbstractProviderDefinition<BeanProvider<Object>> {
   @Override
   public boolean isEnabled(BeanContext context, BeanResolutionContext resolutionContext) {
      return true;
   }

   @Override
   public Class<BeanProvider<Object>> getBeanType() {
      return BeanProvider.class;
   }

   @Override
   public boolean isPresent() {
      return true;
   }

   protected BeanProvider<Object> buildProvider(
      @NonNull BeanResolutionContext resolutionContext,
      @NonNull BeanContext context,
      @NonNull Argument<Object> argument,
      @Nullable Qualifier<Object> qualifier,
      boolean singleton
   ) {
      return new BeanProvider<Object>() {
         private final Qualifier<Object> finalQualifier = qualifier instanceof AnyQualifier ? null : qualifier;

         private Qualifier<Object> qualify(Qualifier<Object> qualifier) {
            if (this.finalQualifier == null) {
               return qualifier;
            } else {
               return qualifier == null ? this.finalQualifier : Qualifiers.byQualifiers(this.finalQualifier, qualifier);
            }
         }

         @Override
         public Object get() {
            return ((DefaultBeanContext)context).getBean(resolutionContext.copy(), argument, this.finalQualifier);
         }

         @Override
         public Optional<Object> find(Qualifier<Object> qualifier) {
            return ((DefaultBeanContext)context).findBean(resolutionContext.copy(), argument, this.qualify(qualifier));
         }

         @Override
         public BeanDefinition<Object> getDefinition() {
            return context.getBeanDefinition(argument, this.finalQualifier);
         }

         @Override
         public Object get(Qualifier<Object> qualifier) {
            return ((DefaultBeanContext)context).getBean(resolutionContext.copy(), argument, this.qualify(qualifier));
         }

         @Override
         public boolean isUnique() {
            try {
               return context.getBeanDefinitions(argument, this.finalQualifier).size() == 1;
            } catch (NoSuchBeanException var2) {
               return false;
            }
         }

         @Override
         public boolean isPresent() {
            return context.containsBean(argument, this.finalQualifier);
         }

         @NonNull
         @Override
         public Iterator<Object> iterator() {
            return ((DefaultBeanContext)context).getBeansOfType(resolutionContext.copy(), argument, this.finalQualifier).iterator();
         }

         @Override
         public Stream<Object> stream() {
            return ((DefaultBeanContext)context).streamOfType(resolutionContext.copy(), argument, this.finalQualifier);
         }
      };
   }

   @Override
   protected boolean isAllowEmptyProviders(BeanContext context) {
      return true;
   }
}
