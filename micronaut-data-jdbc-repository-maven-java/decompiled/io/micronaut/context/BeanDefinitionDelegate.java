package io.micronaut.context;

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.naming.NameResolver;
import io.micronaut.core.naming.Named;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.value.ValueResolver;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.DelegatingBeanDefinition;
import io.micronaut.inject.DisposableBeanDefinition;
import io.micronaut.inject.InitializingBeanDefinition;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.ParametrizedBeanFactory;
import io.micronaut.inject.ValidatedBeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Internal
class BeanDefinitionDelegate<T>
   extends AbstractBeanContextConditional
   implements DelegatingBeanDefinition<T>,
   BeanFactory<T>,
   NameResolver,
   ValueResolver<String> {
   static final String PRIMARY_ATTRIBUTE = Primary.class.getName();
   protected final BeanDefinition<T> definition;
   @Nullable
   protected Map<String, Object> attributes;
   @Nullable
   protected final Qualifier qualifier;

   private BeanDefinitionDelegate(BeanDefinition<T> definition) {
      this(definition, null);
   }

   private BeanDefinitionDelegate(BeanDefinition<T> definition, @Nullable Qualifier qualifier) {
      this.definition = definition;
      this.qualifier = qualifier;
   }

   @Nullable
   public Qualifier getQualifier() {
      return this.qualifier;
   }

   @Nullable
   public Map<String, Object> getAttributes() {
      return this.attributes;
   }

   @Nullable
   @Override
   public Qualifier<T> resolveDynamicQualifier() {
      if (this.qualifier != null) {
         return this.qualifier;
      } else if (this.attributes == null) {
         return null;
      } else {
         Object o = this.attributes.get(NAMED_ATTRIBUTE);
         return o instanceof CharSequence ? Qualifiers.byName(o.toString()) : null;
      }
   }

   BeanDefinition<T> getDelegate() {
      return this.definition;
   }

   @Override
   public boolean isProxy() {
      return this.definition.isProxy();
   }

   @Override
   public boolean isIterable() {
      return this.definition.isIterable();
   }

   @Override
   public boolean isPrimary() {
      return this.definition.isPrimary() || this.isPrimaryThroughAttribute();
   }

   private boolean isPrimaryThroughAttribute() {
      if (this.attributes == null) {
         return false;
      } else {
         Object o = this.attributes.get(PRIMARY_ATTRIBUTE);
         return o instanceof Boolean ? (Boolean)o : false;
      }
   }

   @Override
   public T build(BeanResolutionContext resolutionContext, BeanContext context, BeanDefinition<T> definition) throws BeanInstantiationException {
      LinkedHashMap<String, Object> oldAttributes = null;
      if (CollectionUtils.isNotEmpty(this.attributes)) {
         LinkedHashMap<String, Object> oldAttrs = new LinkedHashMap(this.attributes.size());
         this.attributes.forEach((keyx, value) -> {
            Object previous = resolutionContext.setAttribute(keyx, value);
            if (previous != null) {
               oldAttrs.put(keyx, previous);
            }

         });
         oldAttributes = oldAttrs;
      }

      Object key;
      try {
         if (!(this.definition instanceof ParametrizedBeanFactory)) {
            if (this.definition instanceof BeanFactory) {
               return ((BeanFactory)this.definition).build(resolutionContext, context, definition);
            }

            throw new IllegalStateException("Cannot construct a dynamically registered singleton");
         }

         ParametrizedBeanFactory<T> parametrizedBeanFactory = (ParametrizedBeanFactory)this.definition;
         Map<String, Object> fulfilled = this.getParametersValues(resolutionContext, (DefaultBeanContext)context, definition, parametrizedBeanFactory);
         key = parametrizedBeanFactory.build(resolutionContext, context, definition, fulfilled);
      } finally {
         if (this.attributes != null) {
            for(String key : this.attributes.keySet()) {
               resolutionContext.removeAttribute(key);
            }
         }

         if (oldAttributes != null) {
            oldAttributes.forEach(resolutionContext::setAttribute);
         }

      }

      return (T)key;
   }

   @Nullable
   private Map<String, Object> getParametersValues(
      BeanResolutionContext resolutionContext, DefaultBeanContext context, BeanDefinition<T> definition, ParametrizedBeanFactory<T> parametrizedBeanFactory
   ) {
      Argument<Object>[] requiredArguments = (Argument[])parametrizedBeanFactory.getRequiredArguments();
      Map<String, Object> fulfilled = new LinkedHashMap(requiredArguments.length);

      for(Argument<Object> argument : requiredArguments) {
         String argumentName = argument.getName();
         Object value = this.resolveValueAsName(argument);
         if (value == null) {
            Qualifier<Object> qualifier = this.resolveQualifier(argument);
            if (qualifier == null && !this.isPrimary()) {
               continue;
            }

            try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushConstructorResolve(definition, argument)) {
               value = context.findBean(resolutionContext, argument, qualifier).orElse(null);
            }
         }

         if (value != null) {
            fulfilled.put(argumentName, value);
         }
      }

      return fulfilled;
   }

   @Nullable
   private <K> Qualifier<K> resolveQualifier(Argument<K> argument) {
      Object qualifierMapValue = this.attributes == null ? Collections.emptyMap() : this.attributes.get("javax.inject.Qualifier");
      if (qualifierMapValue instanceof Map) {
         Qualifier<K> qualifier = (Qualifier)((Map)qualifierMapValue).get(argument);
         if (qualifier != null) {
            return qualifier;
         }
      }

      return this.resolveDynamicQualifier();
   }

   @Nullable
   private Object resolveValueAsName(Argument<?> argument) {
      Object named = this.attributes == null ? null : this.attributes.get(Named.class.getName());
      Object value = null;
      if (named != null) {
         value = ConversionService.SHARED.convert(named, argument).orElse(null);
      }

      if (value == null && this.isPrimary()) {
         value = ConversionService.SHARED.convert("Primary", argument).orElse(null);
      }

      return value;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         BeanDefinitionDelegate<?> that = (BeanDefinitionDelegate)o;
         return Objects.equals(this.definition, that.definition) && Objects.equals(this.resolveName().orElse(null), that.resolveName().orElse(null));
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.definition, this.resolveName().orElse(null)});
   }

   @Override
   public BeanDefinition<T> getTarget() {
      return this.definition;
   }

   @Override
   public Optional<String> resolveName() {
      return this.get(Named.class.getName(), String.class);
   }

   public void put(String name, Object value) {
      if (this.attributes == null) {
         this.attributes = new HashMap(2, 1.0F);
      }

      this.attributes.put(name, value);
   }

   public <K> Optional<K> get(String name, ArgumentConversionContext<K> conversionContext) {
      if (this.attributes == null) {
         return Optional.empty();
      } else {
         Object value = this.attributes.get(name);
         return value != null && conversionContext.getArgument().getType().isInstance(value) ? Optional.of(value) : Optional.empty();
      }
   }

   public String toString() {
      return this.definition.toString();
   }

   static <T> BeanDefinitionDelegate<T> create(BeanDefinition<T> definition) {
      return create(definition, null);
   }

   static <T> BeanDefinitionDelegate<T> create(BeanDefinition<T> definition, Qualifier qualifier) {
      if (!(definition instanceof InitializingBeanDefinition) && !(definition instanceof DisposableBeanDefinition)) {
         return (BeanDefinitionDelegate<T>)(definition instanceof ValidatedBeanDefinition
            ? new BeanDefinitionDelegate.ValidatingDelegate<>(definition, qualifier)
            : new BeanDefinitionDelegate<>(definition, qualifier));
      } else {
         return (BeanDefinitionDelegate<T>)(definition instanceof ValidatedBeanDefinition
            ? new BeanDefinitionDelegate.LifeCycleValidatingDelegate<>(definition, qualifier)
            : new BeanDefinitionDelegate.LifeCycleDelegate<>(definition, qualifier));
      }
   }

   @NonNull
   @Override
   public String getName() {
      return this.definition.getName();
   }

   private static final class LifeCycleDelegate<T>
      extends BeanDefinitionDelegate<T>
      implements BeanDefinitionDelegate.ProxyInitializingBeanDefinition<T>,
      BeanDefinitionDelegate.ProxyDisposableBeanDefinition<T> {
      private LifeCycleDelegate(BeanDefinition<T> definition, Qualifier qualifier) {
         super(definition, qualifier);
      }
   }

   private static final class LifeCycleValidatingDelegate<T>
      extends BeanDefinitionDelegate<T>
      implements BeanDefinitionDelegate.ProxyValidatingBeanDefinition<T>,
      BeanDefinitionDelegate.ProxyInitializingBeanDefinition<T>,
      BeanDefinitionDelegate.ProxyDisposableBeanDefinition<T> {
      private LifeCycleValidatingDelegate(BeanDefinition<T> definition, Qualifier qualifier) {
         super(definition, qualifier);
      }
   }

   interface ProxyDisposableBeanDefinition<T> extends DelegatingBeanDefinition<T>, DisposableBeanDefinition<T> {
      @Override
      default T dispose(BeanResolutionContext resolutionContext, BeanContext context, T bean) {
         BeanDefinition<T> definition = this.getTarget();
         return (T)(definition instanceof DisposableBeanDefinition ? ((DisposableBeanDefinition)definition).dispose(resolutionContext, context, bean) : bean);
      }
   }

   interface ProxyInitializingBeanDefinition<T> extends DelegatingBeanDefinition<T>, InitializingBeanDefinition<T> {
      @Override
      default T initialize(BeanResolutionContext resolutionContext, BeanContext context, T bean) {
         BeanDefinition<T> definition = this.getTarget();
         return (T)(definition instanceof InitializingBeanDefinition
            ? ((InitializingBeanDefinition)definition).initialize(resolutionContext, context, bean)
            : bean);
      }
   }

   interface ProxyValidatingBeanDefinition<T> extends DelegatingBeanDefinition<T>, ValidatedBeanDefinition<T> {
      @Override
      default T validate(BeanResolutionContext resolutionContext, T instance) {
         BeanDefinition<T> definition = this.getTarget();
         return (T)(definition instanceof ValidatedBeanDefinition ? ((ValidatedBeanDefinition)definition).validate(resolutionContext, instance) : instance);
      }

      @Override
      default <V> void validateBeanArgument(
         @NonNull BeanResolutionContext resolutionContext, @NonNull InjectionPoint injectionPoint, @NonNull Argument<V> argument, int index, @Nullable V value
      ) {
         BeanDefinition<T> definition = this.getTarget();
         if (definition instanceof ValidatedBeanDefinition) {
            ((ValidatedBeanDefinition)definition).validateBeanArgument(resolutionContext, injectionPoint, argument, index, value);
         }

      }
   }

   private static final class ValidatingDelegate<T> extends BeanDefinitionDelegate<T> implements BeanDefinitionDelegate.ProxyValidatingBeanDefinition<T> {
      private ValidatingDelegate(BeanDefinition<T> definition, Qualifier qualifier) {
         super(definition, qualifier);
      }
   }
}
