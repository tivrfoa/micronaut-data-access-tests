package io.micronaut.context;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.BeanInitializedEventListener;
import io.micronaut.context.event.BeanInitializingEvent;
import io.micronaut.context.exceptions.BeanContextException;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.context.exceptions.DependencyInjectionException;
import io.micronaut.context.exceptions.DisabledBeanException;
import io.micronaut.context.exceptions.NoSuchBeanException;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.naming.Named;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.DefaultArgument;
import io.micronaut.core.type.TypeInformation;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.value.PropertyResolver;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ConstructorInjectionPoint;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.ExecutableMethodsDefinition;
import io.micronaut.inject.FieldInjectionPoint;
import io.micronaut.inject.MethodInjectionPoint;
import io.micronaut.inject.ValidatedBeanDefinition;
import io.micronaut.inject.annotation.AbstractEnvironmentAnnotationMetadata;
import io.micronaut.inject.qualifiers.InterceptorBindingQualifier;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.inject.qualifiers.TypeAnnotationQualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public class AbstractInitializableBeanDefinition<T> extends AbstractBeanContextConditional implements BeanDefinition<T>, EnvironmentConfigurable {
   private static final Logger LOG = LoggerFactory.getLogger(AbstractInitializableBeanDefinition.class);
   private static final String NAMED_ATTRIBUTE = Named.class.getName();
   private final Class<T> type;
   private final AnnotationMetadata annotationMetadata;
   private final Optional<String> scope;
   private final boolean isProvided;
   private final boolean isIterable;
   private final boolean isSingleton;
   private final boolean isPrimary;
   private final boolean isAbstract;
   private final boolean isConfigurationProperties;
   private final boolean isContainerType;
   private final boolean requiresMethodProcessing;
   @Nullable
   private final AbstractInitializableBeanDefinition.MethodOrFieldReference constructor;
   @Nullable
   private final AbstractInitializableBeanDefinition.MethodReference[] methodInjection;
   @Nullable
   private final AbstractInitializableBeanDefinition.FieldReference[] fieldInjection;
   @Nullable
   private final ExecutableMethodsDefinition<T> executableMethodsDefinition;
   @Nullable
   private final Map<String, Argument<?>[]> typeArgumentsMap;
   @Nullable
   private AbstractInitializableBeanDefinition.AnnotationReference[] annotationInjection;
   @Nullable
   private Environment environment;
   @Nullable
   private Optional<Argument<?>> containerElement;
   @Nullable
   private ConstructorInjectionPoint<T> constructorInjectionPoint;
   @Nullable
   private List<MethodInjectionPoint<T, ?>> methodInjectionPoints;
   @Nullable
   private List<FieldInjectionPoint<T, ?>> fieldInjectionPoints;
   @Nullable
   private List<MethodInjectionPoint<T, ?>> postConstructMethods;
   @Nullable
   private List<MethodInjectionPoint<T, ?>> preDestroyMethods;
   @Nullable
   private Collection<Class<?>> requiredComponents;
   @Nullable
   private Argument<?>[] requiredParametrizedArguments;
   private Qualifier<T> declaredQualifier;

   @Internal
   protected AbstractInitializableBeanDefinition(
      Class<T> beanType,
      @Nullable AbstractInitializableBeanDefinition.MethodOrFieldReference constructor,
      @Nullable AnnotationMetadata annotationMetadata,
      @Nullable AbstractInitializableBeanDefinition.MethodReference[] methodInjection,
      @Nullable AbstractInitializableBeanDefinition.FieldReference[] fieldInjection,
      @Nullable ExecutableMethodsDefinition<T> executableMethodsDefinition,
      @Nullable Map<String, Argument<?>[]> typeArgumentsMap,
      Optional<String> scope,
      boolean isAbstract,
      boolean isProvided,
      boolean isIterable,
      boolean isSingleton,
      boolean isPrimary,
      boolean isConfigurationProperties,
      boolean isContainerType,
      boolean requiresMethodProcessing
   ) {
      this.scope = scope;
      this.type = beanType;
      if (annotationMetadata == null || annotationMetadata == AnnotationMetadata.EMPTY_METADATA) {
         this.annotationMetadata = AnnotationMetadata.EMPTY_METADATA;
      } else if (annotationMetadata.hasPropertyExpressions()) {
         this.annotationMetadata = new AbstractInitializableBeanDefinition.BeanAnnotationMetadata(annotationMetadata);
      } else {
         this.annotationMetadata = annotationMetadata;
      }

      this.isProvided = isProvided;
      this.isIterable = isIterable;
      this.isSingleton = isSingleton;
      this.isPrimary = isPrimary;
      this.isAbstract = isAbstract;
      this.constructor = constructor;
      this.methodInjection = methodInjection;
      this.fieldInjection = fieldInjection;
      this.executableMethodsDefinition = executableMethodsDefinition;
      this.typeArgumentsMap = typeArgumentsMap;
      this.isConfigurationProperties = isConfigurationProperties;
      this.isContainerType = isContainerType;
      this.requiresMethodProcessing = requiresMethodProcessing;
   }

   @Internal
   protected AbstractInitializableBeanDefinition(
      Class<T> beanType,
      @Nullable AbstractInitializableBeanDefinition.MethodOrFieldReference constructor,
      @Nullable AnnotationMetadata annotationMetadata,
      @Nullable AbstractInitializableBeanDefinition.MethodReference[] methodInjection,
      @Nullable AbstractInitializableBeanDefinition.FieldReference[] fieldInjection,
      @Nullable AbstractInitializableBeanDefinition.AnnotationReference[] annotationInjection,
      @Nullable ExecutableMethodsDefinition<T> executableMethodsDefinition,
      @Nullable Map<String, Argument<?>[]> typeArgumentsMap,
      Optional<String> scope,
      boolean isAbstract,
      boolean isProvided,
      boolean isIterable,
      boolean isSingleton,
      boolean isPrimary,
      boolean isConfigurationProperties,
      boolean isContainerType,
      boolean requiresMethodProcessing
   ) {
      this(
         beanType,
         constructor,
         annotationMetadata,
         methodInjection,
         fieldInjection,
         executableMethodsDefinition,
         typeArgumentsMap,
         scope,
         isAbstract,
         isProvided,
         isIterable,
         isSingleton,
         isPrimary,
         isConfigurationProperties,
         isContainerType,
         requiresMethodProcessing
      );
      this.annotationInjection = annotationInjection;
   }

   @Override
   public Qualifier<T> getDeclaredQualifier() {
      if (this.declaredQualifier == null) {
         this.declaredQualifier = BeanDefinition.super.getDeclaredQualifier();
      }

      return this.declaredQualifier;
   }

   @Override
   public final boolean isContainerType() {
      return this.isContainerType;
   }

   @Override
   public final Optional<Argument<?>> getContainerElement() {
      if (this.isContainerType) {
         if (this.containerElement != null) {
            return this.containerElement;
         } else {
            if (this.getBeanType().isArray()) {
               this.containerElement = Optional.of(Argument.of(this.getBeanType().getComponentType()));
            } else {
               List<Argument<?>> iterableArguments = this.getTypeArguments(Iterable.class);
               if (!iterableArguments.isEmpty()) {
                  this.containerElement = Optional.of(iterableArguments.iterator().next());
               }
            }

            return this.containerElement;
         }
      } else {
         return Optional.empty();
      }
   }

   @Override
   public final boolean hasPropertyExpressions() {
      return this.getAnnotationMetadata().hasPropertyExpressions();
   }

   @NonNull
   @Override
   public final List<Argument<?>> getTypeArguments(String type) {
      if (type != null && this.typeArgumentsMap != null) {
         Argument<?>[] arguments = (Argument[])this.typeArgumentsMap.get(type);
         return arguments != null ? Arrays.asList(arguments) : Collections.emptyList();
      } else {
         return Collections.emptyList();
      }
   }

   @NonNull
   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @Override
   public boolean isAbstract() {
      return this.isAbstract;
   }

   @Override
   public boolean isIterable() {
      return this.isIterable;
   }

   @Override
   public boolean isPrimary() {
      return this.isPrimary;
   }

   @Override
   public boolean isProvided() {
      return this.isProvided;
   }

   @Override
   public boolean requiresMethodProcessing() {
      return this.requiresMethodProcessing;
   }

   @Override
   public final <R> Optional<ExecutableMethod<T, R>> findMethod(String name, Class<?>... argumentTypes) {
      return this.executableMethodsDefinition == null ? Optional.empty() : this.executableMethodsDefinition.findMethod(name, argumentTypes);
   }

   @Override
   public final <R> Stream<ExecutableMethod<T, R>> findPossibleMethods(String name) {
      return this.executableMethodsDefinition == null ? Stream.empty() : this.executableMethodsDefinition.findPossibleMethods(name);
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

   public String toString() {
      Class declaringType = this.constructor == null ? this.type : this.constructor.declaringType;
      return "Definition: " + declaringType.getName();
   }

   @Override
   public boolean isSingleton() {
      return this.isSingleton;
   }

   @Override
   public final Optional<Class<? extends Annotation>> getScope() {
      return this.scope.flatMap(scopeClassName -> ClassUtils.forName(scopeClassName, this.getClass().getClassLoader()));
   }

   @Override
   public final Optional<String> getScopeName() {
      return this.scope;
   }

   @Override
   public final Class<T> getBeanType() {
      return this.type;
   }

   @NonNull
   @Override
   public Set<Class<?>> getExposedTypes() {
      return Collections.EMPTY_SET;
   }

   @Override
   public final Optional<Class<?>> getDeclaringType() {
      return this.constructor == null ? Optional.of(this.type) : Optional.of(this.constructor.declaringType);
   }

   @Override
   public final ConstructorInjectionPoint<T> getConstructor() {
      if (this.constructor == null) {
         this.constructorInjectionPoint = null;
      } else {
         if (this.constructor instanceof AbstractInitializableBeanDefinition.MethodReference) {
            AbstractInitializableBeanDefinition.MethodReference methodConstructor = (AbstractInitializableBeanDefinition.MethodReference)this.constructor;
            if ("<init>".equals(methodConstructor.methodName)) {
               if (methodConstructor.requiresReflection) {
                  this.constructorInjectionPoint = new ReflectionConstructorInjectionPoint<>(
                     this, methodConstructor.declaringType, methodConstructor.annotationMetadata, methodConstructor.arguments
                  );
               } else {
                  this.constructorInjectionPoint = new DefaultConstructorInjectionPoint<>(
                     this, methodConstructor.declaringType, methodConstructor.annotationMetadata, methodConstructor.arguments
                  );
               }
            } else if (methodConstructor.requiresReflection) {
               this.constructorInjectionPoint = new ReflectionMethodConstructorInjectionPoint(
                  this, methodConstructor.declaringType, methodConstructor.methodName, methodConstructor.arguments, methodConstructor.annotationMetadata
               );
            } else {
               this.constructorInjectionPoint = new DefaultMethodConstructorInjectionPoint<>(
                  this, methodConstructor.declaringType, methodConstructor.methodName, methodConstructor.arguments, methodConstructor.annotationMetadata
               );
            }
         } else if (this.constructor instanceof AbstractInitializableBeanDefinition.FieldReference) {
            AbstractInitializableBeanDefinition.FieldReference fieldConstructor = (AbstractInitializableBeanDefinition.FieldReference)this.constructor;
            this.constructorInjectionPoint = new DefaultFieldConstructorInjectionPoint<>(
               this, fieldConstructor.declaringType, this.type, fieldConstructor.argument.getName(), fieldConstructor.argument.getAnnotationMetadata()
            );
         }

         if (this.environment != null && this.constructorInjectionPoint instanceof EnvironmentConfigurable) {
            ((EnvironmentConfigurable)this.constructorInjectionPoint).configure(this.environment);
         }
      }

      return this.constructorInjectionPoint;
   }

   @Override
   public final Collection<Class<?>> getRequiredComponents() {
      if (this.requiredComponents != null) {
         return this.requiredComponents;
      } else {
         Set<Class<?>> requiredComponents = new HashSet();
         Consumer<Argument> argumentConsumer = argumentx -> {
            if (!argumentx.isContainerType() && !argumentx.isProvider()) {
               requiredComponents.add(argumentx.getType());
            } else {
               argumentx.getFirstTypeVariable().map(TypeInformation::getType).ifPresent(requiredComponents::add);
            }

         };
         if (this.constructor != null && this.constructor instanceof AbstractInitializableBeanDefinition.MethodReference) {
            AbstractInitializableBeanDefinition.MethodReference methodConstructor = (AbstractInitializableBeanDefinition.MethodReference)this.constructor;
            if (methodConstructor.arguments != null && methodConstructor.arguments.length > 0) {
               for(Argument<?> argument : methodConstructor.arguments) {
                  argumentConsumer.accept(argument);
               }
            }
         }

         if (this.methodInjection != null) {
            for(AbstractInitializableBeanDefinition.MethodReference methodReference : this.methodInjection) {
               if (methodReference.arguments != null && methodReference.arguments.length > 0) {
                  for(Argument<?> argument : methodReference.arguments) {
                     argumentConsumer.accept(argument);
                  }
               }
            }
         }

         if (this.fieldInjection != null) {
            for(AbstractInitializableBeanDefinition.FieldReference fieldReference : this.fieldInjection) {
               if (this.annotationMetadata != null && this.annotationMetadata.hasDeclaredAnnotation("javax.inject.Inject")) {
                  argumentConsumer.accept(fieldReference.argument);
               }
            }
         }

         if (this.annotationInjection != null) {
            for(AbstractInitializableBeanDefinition.AnnotationReference annotationReference : this.annotationInjection) {
               if (annotationReference.argument != null) {
                  argumentConsumer.accept(annotationReference.argument);
               }
            }
         }

         this.requiredComponents = Collections.unmodifiableSet(requiredComponents);
         return this.requiredComponents;
      }
   }

   public final List<MethodInjectionPoint<T, ?>> getInjectedMethods() {
      if (this.methodInjection == null) {
         return Collections.emptyList();
      } else if (this.methodInjectionPoints != null) {
         return this.methodInjectionPoints;
      } else {
         List<MethodInjectionPoint<T, ?>> methodInjectionPoints = new ArrayList(this.methodInjection.length);

         for(AbstractInitializableBeanDefinition.MethodReference methodReference : this.methodInjection) {
            MethodInjectionPoint<T, ?> methodInjectionPoint;
            if (methodReference.requiresReflection) {
               methodInjectionPoint = new ReflectionMethodInjectionPoint(
                  this, methodReference.declaringType, methodReference.methodName, methodReference.arguments, methodReference.annotationMetadata
               );
            } else {
               methodInjectionPoint = new DefaultMethodInjectionPoint<>(
                  this, methodReference.declaringType, methodReference.methodName, methodReference.arguments, methodReference.annotationMetadata
               );
            }

            methodInjectionPoints.add(methodInjectionPoint);
            if (this.environment != null) {
               ((EnvironmentConfigurable)methodInjectionPoint).configure(this.environment);
            }
         }

         this.methodInjectionPoints = Collections.unmodifiableList(methodInjectionPoints);
         return this.methodInjectionPoints;
      }
   }

   public final List<FieldInjectionPoint<T, ?>> getInjectedFields() {
      if (this.fieldInjection == null) {
         return Collections.emptyList();
      } else if (this.fieldInjectionPoints != null) {
         return this.fieldInjectionPoints;
      } else {
         List<FieldInjectionPoint<T, ?>> fieldInjectionPoints = new ArrayList(this.fieldInjection.length);

         for(AbstractInitializableBeanDefinition.FieldReference fieldReference : this.fieldInjection) {
            FieldInjectionPoint<T, ?> fieldInjectionPoint;
            if (fieldReference.requiresReflection) {
               fieldInjectionPoint = new ReflectionFieldInjectionPoint<>(
                  this,
                  fieldReference.declaringType,
                  fieldReference.argument.getType(),
                  fieldReference.argument.getName(),
                  fieldReference.argument.getAnnotationMetadata(),
                  fieldReference.argument.getTypeParameters()
               );
            } else {
               fieldInjectionPoint = new DefaultFieldInjectionPoint<>(
                  this,
                  fieldReference.declaringType,
                  fieldReference.argument.getType(),
                  fieldReference.argument.getName(),
                  fieldReference.argument.getAnnotationMetadata(),
                  fieldReference.argument.getTypeParameters()
               );
            }

            if (this.environment != null) {
               ((EnvironmentConfigurable)fieldInjectionPoint).configure(this.environment);
            }

            fieldInjectionPoints.add(fieldInjectionPoint);
         }

         this.fieldInjectionPoints = Collections.unmodifiableList(fieldInjectionPoints);
         return this.fieldInjectionPoints;
      }
   }

   public final List<MethodInjectionPoint<T, ?>> getPostConstructMethods() {
      if (this.methodInjection == null) {
         return Collections.emptyList();
      } else if (this.postConstructMethods != null) {
         return this.postConstructMethods;
      } else {
         List<MethodInjectionPoint<T, ?>> postConstructMethods = new ArrayList(1);

         for(MethodInjectionPoint<T, ?> methodInjectionPoint : this.getInjectedMethods()) {
            if (methodInjectionPoint.isPostConstructMethod()) {
               postConstructMethods.add(methodInjectionPoint);
            }
         }

         this.postConstructMethods = Collections.unmodifiableList(postConstructMethods);
         return this.postConstructMethods;
      }
   }

   public final List<MethodInjectionPoint<T, ?>> getPreDestroyMethods() {
      if (this.methodInjection == null) {
         return Collections.emptyList();
      } else if (this.preDestroyMethods != null) {
         return this.preDestroyMethods;
      } else {
         List<MethodInjectionPoint<T, ?>> preDestroyMethods = new ArrayList(1);

         for(MethodInjectionPoint<T, ?> methodInjectionPoint : this.getInjectedMethods()) {
            if (methodInjectionPoint.isPreDestroyMethod()) {
               preDestroyMethods.add(methodInjectionPoint);
            }
         }

         this.preDestroyMethods = Collections.unmodifiableList(preDestroyMethods);
         return this.preDestroyMethods;
      }
   }

   @NonNull
   @Override
   public final String getName() {
      return this.getBeanType().getName();
   }

   @Override
   public T inject(BeanContext context, T bean) {
      return (T)this.injectBean(new DefaultBeanResolutionContext(context, this), context, bean);
   }

   @Override
   public T inject(BeanResolutionContext resolutionContext, BeanContext context, T bean) {
      return (T)this.injectBean(resolutionContext, context, bean);
   }

   @Override
   public final Collection<ExecutableMethod<T, ?>> getExecutableMethods() {
      return (Collection<ExecutableMethod<T, ?>>)(this.executableMethodsDefinition == null
         ? Collections.emptyList()
         : this.executableMethodsDefinition.getExecutableMethods());
   }

   @Internal
   @Override
   public final void configure(Environment environment) {
      if (environment != null) {
         this.environment = environment;
         if (this.constructorInjectionPoint instanceof EnvironmentConfigurable) {
            ((EnvironmentConfigurable)this.constructorInjectionPoint).configure(environment);
         }

         if (this.methodInjectionPoints != null) {
            for(MethodInjectionPoint<T, ?> methodInjectionPoint : this.methodInjectionPoints) {
               if (methodInjectionPoint instanceof EnvironmentConfigurable) {
                  ((EnvironmentConfigurable)methodInjectionPoint).configure(environment);
               }
            }
         }

         if (this.fieldInjectionPoints != null) {
            for(FieldInjectionPoint<T, ?> fieldInjectionPoint : this.fieldInjectionPoints) {
               if (fieldInjectionPoint instanceof EnvironmentConfigurable) {
                  ((EnvironmentConfigurable)fieldInjectionPoint).configure(environment);
               }
            }
         }

         if (this.executableMethodsDefinition instanceof EnvironmentConfigurable) {
            ((EnvironmentConfigurable)this.executableMethodsDefinition).configure(environment);
         }
      }

   }

   @Internal
   protected final void warn(String message) {
      if (LOG.isWarnEnabled()) {
         LOG.warn(message);
      }

   }

   @Internal
   protected final void warnMissingProperty(Class type, String method, String property) {
      if (LOG.isWarnEnabled()) {
         LOG.warn(
            "Configuration property [{}] could not be set as the underlying method [{}] does not exist on builder [{}]. This usually indicates the configuration option was deprecated and has been removed by the builder implementation (potentially a third-party library).",
            property,
            method,
            type
         );
      }

   }

   @Internal
   protected final Object getProxiedBean(BeanContext beanContext) {
      DefaultBeanContext defaultBeanContext = (DefaultBeanContext)beanContext;
      Optional<String> qualifier = this.getAnnotationMetadata().getAnnotationNameByStereotype("javax.inject.Qualifier");
      return defaultBeanContext.getProxyTargetBean(
         this.getBeanType(), (Qualifier<T>)qualifier.map(q -> Qualifiers.byAnnotation(this.getAnnotationMetadata(), q)).orElse(null)
      );
   }

   public final Argument<?>[] getRequiredArguments() {
      if (this.requiredParametrizedArguments != null) {
         return this.requiredParametrizedArguments;
      } else {
         this.requiredParametrizedArguments = (Argument[])Arrays.stream(this.getConstructor().getArguments()).filter(arg -> {
            Optional<String> qualifierType = arg.getAnnotationMetadata().getAnnotationNameByStereotype("javax.inject.Qualifier");
            return qualifierType.isPresent() && ((String)qualifierType.get()).equals(Parameter.class.getName());
         }).toArray(x$0 -> new Argument[x$0]);
         return this.requiredParametrizedArguments;
      }
   }

   public final T build(BeanResolutionContext resolutionContext, BeanContext context, BeanDefinition<T> definition, Map<String, Object> requiredArgumentValues) throws BeanInstantiationException {
      requiredArgumentValues = (Map<String, Object>)(requiredArgumentValues != null ? new LinkedHashMap(requiredArgumentValues) : Collections.emptyMap());
      Optional<Class> eachBeanType = null;

      for(Argument<?> requiredArgument : this.getRequiredArguments()) {
         try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushConstructorResolve(this, requiredArgument)) {
            String argumentName = requiredArgument.getName();
            Object value = requiredArgumentValues.get(argumentName);
            if (value == null && !requiredArgument.isNullable()) {
               if (eachBeanType == null) {
                  eachBeanType = definition.classValue(EachBean.class);
               }

               if (eachBeanType.filter(type -> type == requiredArgument.getType()).isPresent()) {
                  throw new DisabledBeanException("@EachBean parameter disabled for argument: " + requiredArgument.getName());
               }

               throw new BeanInstantiationException(resolutionContext, "Missing bean argument value: " + argumentName);
            }

            boolean requiresConversion = value != null && !requiredArgument.getType().isInstance(value);
            if (requiresConversion) {
               Optional<?> converted = ConversionService.SHARED.convert(value, requiredArgument.getType(), ConversionContext.of(requiredArgument));
               Object finalValue = value;
               value = converted.orElseThrow(
                  () -> new BeanInstantiationException(resolutionContext, "Invalid value [" + finalValue + "] for argument: " + argumentName)
               );
               requiredArgumentValues.put(argumentName, value);
            }
         }
      }

      return this.doBuild(resolutionContext, context, definition, requiredArgumentValues);
   }

   @Internal
   protected T doBuild(BeanResolutionContext resolutionContext, BeanContext context, BeanDefinition<T> definition, Map<String, Object> requiredArgumentValues) {
      throw new IllegalStateException("Method must be implemented for 'ParametrizedBeanFactory' instance!");
   }

   @Internal
   protected Object injectBean(BeanResolutionContext resolutionContext, BeanContext context, Object bean) {
      return bean;
   }

   @Internal
   protected Object injectAnother(BeanResolutionContext resolutionContext, BeanContext context, Object bean) {
      if (bean == null) {
         throw new BeanInstantiationException(resolutionContext, "Bean factory returned null");
      } else {
         return resolutionContext.inject(this, bean);
      }
   }

   @Internal
   protected Object postConstruct(BeanResolutionContext resolutionContext, BeanContext context, Object bean) {
      Set<Entry<Class<?>, List<BeanInitializedEventListener>>> beanInitializedEventListeners = ((DefaultBeanContext)context).beanInitializedEventListeners;
      if (CollectionUtils.isNotEmpty(beanInitializedEventListeners)) {
         for(Entry<Class<?>, List<BeanInitializedEventListener>> entry : beanInitializedEventListeners) {
            if (((Class)entry.getKey()).isAssignableFrom(this.getBeanType())) {
               for(BeanInitializedEventListener listener : (List)entry.getValue()) {
                  bean = listener.onInitialized(new BeanInitializingEvent<>(context, this, (T)bean));
                  if (bean == null) {
                     throw new BeanInstantiationException(resolutionContext, "Listener [" + listener + "] returned null from onInitialized event");
                  }
               }
            }
         }
      }

      if (bean instanceof LifeCycle) {
         bean = ((LifeCycle)bean).start();
      }

      return bean;
   }

   @Internal
   protected Object preDestroy(BeanResolutionContext resolutionContext, BeanContext context, Object bean) {
      if (bean instanceof LifeCycle) {
         bean = ((LifeCycle)bean).stop();
      }

      return bean;
   }

   @Internal
   protected boolean isInnerConfiguration(Class<?> clazz) {
      return false;
   }

   @Internal
   protected void checkIfShouldLoad(BeanResolutionContext resolutionContext, BeanContext context) {
   }

   @Internal
   protected final void checkInjectedBeanPropertyValue(
      String injectedBeanPropertyName, @Nullable Object beanPropertyValue, @Nullable String requiredValue, @Nullable String notEqualsValue
   ) {
      if (beanPropertyValue instanceof Optional) {
         beanPropertyValue = ((Optional)beanPropertyValue).orElse(null);
      }

      String convertedValue = (String)ConversionService.SHARED.convert(beanPropertyValue, String.class).orElse(null);
      if (convertedValue == null && notEqualsValue == null) {
         throw new DisabledBeanException(
            "Bean [" + this.getBeanType() + "] is disabled since required bean property [" + injectedBeanPropertyName + "] id not set"
         );
      } else {
         if (convertedValue != null) {
            if (requiredValue != null && !convertedValue.equals(requiredValue)) {
               throw new DisabledBeanException(
                  "Bean ["
                     + this.getBeanType()
                     + "] is disabled since bean property ["
                     + injectedBeanPropertyName
                     + "] value is not equal to ["
                     + requiredValue
                     + "]"
               );
            }

            if (requiredValue == null && convertedValue.equals(notEqualsValue)) {
               throw new DisabledBeanException(
                  "Bean ["
                     + this.getBeanType()
                     + "] is disabled since bean property ["
                     + injectedBeanPropertyName
                     + "] value is equal to ["
                     + notEqualsValue
                     + "]"
               );
            }
         }

      }
   }

   @Internal
   protected final void invokeMethodWithReflection(
      BeanResolutionContext resolutionContext, BeanContext context, int methodIndex, Object bean, Object[] methodArgs
   ) {
      AbstractInitializableBeanDefinition.MethodReference methodRef = this.methodInjection[methodIndex];
      Argument[] methodArgumentTypes = methodRef.arguments == null ? Argument.ZERO_ARGUMENTS : methodRef.arguments;
      if (ClassUtils.REFLECTION_LOGGER.isDebugEnabled()) {
         ClassUtils.REFLECTION_LOGGER.debug("Bean of type [" + this.getBeanType() + "] uses reflection to inject method: '" + methodRef.methodName + "'");
      }

      try {
         Method method = (Method)ReflectionUtils.getMethod(methodRef.declaringType, methodRef.methodName, Argument.toClassArray(methodArgumentTypes))
            .orElseThrow(() -> ReflectionUtils.newNoSuchMethodError(methodRef.declaringType, methodRef.methodName, Argument.toClassArray(methodArgumentTypes)));
         method.setAccessible(true);
         ReflectionUtils.invokeMethod(bean, method, methodArgs);
      } catch (Throwable var9) {
         if (var9 instanceof BeanContextException) {
            throw (BeanContextException)var9;
         } else {
            throw new DependencyInjectionException(resolutionContext, "Error invoking method: " + methodRef.methodName, var9);
         }
      }
   }

   @Internal
   protected final void setFieldWithReflection(BeanResolutionContext resolutionContext, BeanContext context, int index, Object object, Object value) {
      AbstractInitializableBeanDefinition.FieldReference fieldRef = this.fieldInjection[index];

      try {
         if (ClassUtils.REFLECTION_LOGGER.isDebugEnabled()) {
            ClassUtils.REFLECTION_LOGGER
               .debug("Bean of type [" + this.getBeanType() + "] uses reflection to inject field: '" + fieldRef.argument.getName() + "'");
         }

         Field field = ReflectionUtils.getRequiredField(fieldRef.declaringType, fieldRef.argument.getName());
         field.setAccessible(true);
         field.set(object, value);
      } catch (Throwable var8) {
         if (var8 instanceof BeanContextException) {
            throw (BeanContextException)var8;
         } else {
            throw new DependencyInjectionException(resolutionContext, "Error setting field value: " + var8.getMessage(), var8);
         }
      }
   }

   @Internal
   @Deprecated
   protected final Object getValueForMethodArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int methodIndex, int argIndex, Qualifier qualifier
   ) {
      AbstractInitializableBeanDefinition.MethodReference methodRef = this.methodInjection[methodIndex];
      Argument argument = methodRef.arguments[argIndex];

      Object var10;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath()
            .pushMethodArgumentResolve(this, methodRef.methodName, argument, methodRef.arguments, methodRef.requiresReflection)) {
         var10 = this.resolveValue(resolutionContext, context, methodRef.annotationMetadata, argument, qualifier);
      }

      return var10;
   }

   @Internal
   protected final Object getPropertyValueForMethodArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int methodIndex, int argIndex, String propertyValue, String cliProperty
   ) {
      AbstractInitializableBeanDefinition.MethodReference methodRef = this.methodInjection[methodIndex];
      Argument argument = methodRef.arguments[argIndex];

      Object var11;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath()
            .pushMethodArgumentResolve(this, methodRef.methodName, argument, methodRef.arguments, methodRef.requiresReflection)) {
         var11 = this.resolvePropertyValue(resolutionContext, context, argument, propertyValue, cliProperty, false);
      }

      return var11;
   }

   @Internal
   protected final Object getPropertyPlaceholderValueForMethodArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int methodIndex, int argIndex, String value
   ) {
      AbstractInitializableBeanDefinition.MethodReference methodRef = this.methodInjection[methodIndex];
      Argument argument = methodRef.arguments[argIndex];

      Object var10;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath()
            .pushMethodArgumentResolve(this, methodRef.methodName, argument, methodRef.arguments, methodRef.requiresReflection)) {
         var10 = this.resolvePropertyValue(resolutionContext, context, argument, value, null, true);
      }

      return var10;
   }

   @Internal
   protected final Object getPropertyValueForSetter(
      BeanResolutionContext resolutionContext, BeanContext context, String setterName, Argument<?> argument, String propertyValue, String cliProperty
   ) {
      Object var9;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath()
            .pushMethodArgumentResolve(this, setterName, argument, new Argument[]{argument}, false)) {
         var9 = this.resolvePropertyValue(resolutionContext, context, argument, propertyValue, cliProperty, false);
      }

      return var9;
   }

   @Internal
   protected final Object getPropertyPlaceholderValueForSetter(
      BeanResolutionContext resolutionContext, BeanContext context, String setterName, Argument<?> argument, String value
   ) {
      Object var8;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath()
            .pushMethodArgumentResolve(this, setterName, argument, new Argument[]{argument}, false)) {
         var8 = this.resolvePropertyValue(resolutionContext, context, argument, value, null, true);
      }

      return var8;
   }

   @Internal
   @Deprecated
   protected final boolean containsValueForMethodArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int methodIndex, int argIndex, boolean isValuePrefix
   ) {
      AbstractInitializableBeanDefinition.MethodReference methodRef = this.methodInjection[methodIndex];
      AnnotationMetadata parentAnnotationMetadata = methodRef.annotationMetadata;
      Argument argument = methodRef.arguments[argIndex];
      return this.resolveContainsValue(resolutionContext, context, parentAnnotationMetadata, argument, isValuePrefix);
   }

   @Internal
   protected final <K> K getBeanForMethodArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int methodIndex, int argIndex, Qualifier<K> qualifier
   ) {
      AbstractInitializableBeanDefinition.MethodReference methodRef = this.methodInjection[methodIndex];
      Argument<K> argument = this.resolveArgument(context, argIndex, methodRef.arguments);

      Object var10;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath()
            .pushMethodArgumentResolve(this, methodRef.methodName, argument, methodRef.arguments, methodRef.requiresReflection)) {
         var10 = this.<K>resolveBean(resolutionContext, argument, qualifier, true);
      }

      return (K)var10;
   }

   @Internal
   protected final <K, R extends Collection<K>> R getBeansOfTypeForMethodArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int methodIndex, int argumentIndex, Argument<K> genericType, Qualifier<K> qualifier
   ) {
      AbstractInitializableBeanDefinition.MethodReference methodRef = this.methodInjection[methodIndex];
      Argument<R> argument = this.resolveArgument(context, argumentIndex, methodRef.arguments);

      Collection var11;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath()
            .pushMethodArgumentResolve(this, methodRef.methodName, argument, methodRef.arguments, methodRef.requiresReflection)) {
         var11 = this.resolveBeansOfType(resolutionContext, context, argument, this.resolveEnvironmentArgument(context, genericType), qualifier);
      }

      return (R)var11;
   }

   @Internal
   protected final Object getBeanForSetter(
      BeanResolutionContext resolutionContext, BeanContext context, String setterName, Argument argument, Qualifier qualifier
   ) {
      Object var8;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath()
            .pushMethodArgumentResolve(this, setterName, argument, new Argument[]{argument}, false)) {
         var8 = this.resolveBean(resolutionContext, argument, qualifier, true);
      }

      return var8;
   }

   @Internal
   protected final Collection<Object> getBeansOfTypeForSetter(
      BeanResolutionContext resolutionContext, BeanContext context, String setterName, Argument argument, Argument genericType, Qualifier qualifier
   ) {
      Collection var9;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath()
            .pushMethodArgumentResolve(this, setterName, argument, new Argument[]{argument}, false)) {
         var9 = this.resolveBeansOfType(resolutionContext, context, argument, this.resolveEnvironmentArgument(context, genericType), qualifier);
      }

      return var9;
   }

   @Internal
   protected final <K> Optional<K> findBeanForMethodArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int methodIndex, int argIndex, Argument<K> genericType, Qualifier<K> qualifier
   ) {
      AbstractInitializableBeanDefinition.MethodReference methodRef = this.methodInjection[methodIndex];
      Argument<K> argument = this.resolveArgument(context, argIndex, methodRef.arguments);

      Optional var11;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath()
            .pushMethodArgumentResolve(this, methodRef.methodName, argument, methodRef.arguments, methodRef.requiresReflection)) {
         var11 = this.resolveOptionalBean(resolutionContext, argument, this.resolveEnvironmentArgument(context, genericType), qualifier);
      }

      return var11;
   }

   @Internal
   protected final Stream<?> getStreamOfTypeForMethodArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int methodIndex, int argIndex, Argument genericType, Qualifier qualifier
   ) {
      AbstractInitializableBeanDefinition.MethodReference methodRef = this.methodInjection[methodIndex];
      Argument<?> argument = this.resolveArgument(context, argIndex, methodRef.arguments);

      Stream var11;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath()
            .pushMethodArgumentResolve(this, methodRef.methodName, argument, methodRef.arguments, methodRef.requiresReflection)) {
         var11 = this.resolveStreamOfType(resolutionContext, argument, this.resolveEnvironmentArgument(context, genericType), qualifier);
      }

      return var11;
   }

   @Internal
   protected final Object getBeanForConstructorArgument(BeanResolutionContext resolutionContext, BeanContext context, int argIndex, Qualifier qualifier) {
      AbstractInitializableBeanDefinition.MethodReference constructorMethodRef = (AbstractInitializableBeanDefinition.MethodReference)this.constructor;
      Argument<?> argument = this.resolveArgument(context, argIndex, constructorMethodRef.arguments);
      if (argument.isDeclaredNullable()) {
         BeanResolutionContext.Segment current = (BeanResolutionContext.Segment)resolutionContext.getPath().peek();
         if (current != null && current.getArgument().equals(argument)) {
            return null;
         }
      }

      Object var9;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushConstructorResolve(this, argument)) {
         var9 = this.resolveBean(resolutionContext, argument, qualifier, true);
      }

      return var9;
   }

   @Internal
   @Deprecated
   protected final Object getValueForConstructorArgument(BeanResolutionContext resolutionContext, BeanContext context, int argIndex, Qualifier qualifier) {
      AbstractInitializableBeanDefinition.MethodReference constructorRef = (AbstractInitializableBeanDefinition.MethodReference)this.constructor;
      Argument<?> argument = constructorRef.arguments[argIndex];

      Object var10;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushConstructorResolve(this, argument)) {
         try {
            Object result = this.resolveValue(resolutionContext, context, constructorRef.annotationMetadata, argument, qualifier);
            if (this instanceof ValidatedBeanDefinition) {
               ((ValidatedBeanDefinition)this).validateBeanArgument(resolutionContext, this.getConstructor(), argument, argIndex, result);
            }

            var10 = result;
         } catch (BeanInstantiationException | NoSuchBeanException var20) {
            throw new DependencyInjectionException(resolutionContext, argument, var20);
         }
      }

      return var10;
   }

   @Internal
   protected final Object getPropertyValueForConstructorArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int argIndex, String propertyValue, String cliProperty
   ) {
      AbstractInitializableBeanDefinition.MethodReference constructorRef = (AbstractInitializableBeanDefinition.MethodReference)this.constructor;
      Argument<?> argument = constructorRef.arguments[argIndex];

      Object var11;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushConstructorResolve(this, argument)) {
         try {
            Object result = this.resolvePropertyValue(resolutionContext, context, argument, propertyValue, cliProperty, false);
            if (this instanceof ValidatedBeanDefinition) {
               ((ValidatedBeanDefinition)this).validateBeanArgument(resolutionContext, this.getConstructor(), argument, argIndex, result);
            }

            var11 = result;
         } catch (BeanInstantiationException | NoSuchBeanException var21) {
            throw new DependencyInjectionException(resolutionContext, argument, var21);
         }
      }

      return var11;
   }

   @Internal
   protected final Object getPropertyPlaceholderValueForConstructorArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int argIndex, String propertyValue
   ) {
      AbstractInitializableBeanDefinition.MethodReference constructorRef = (AbstractInitializableBeanDefinition.MethodReference)this.constructor;
      Argument<?> argument = constructorRef.arguments[argIndex];

      Object var10;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushConstructorResolve(this, argument)) {
         try {
            Object result = this.resolvePropertyValue(resolutionContext, context, argument, propertyValue, null, true);
            if (this instanceof ValidatedBeanDefinition) {
               ((ValidatedBeanDefinition)this).validateBeanArgument(resolutionContext, this.getConstructor(), argument, argIndex, result);
            }

            var10 = result;
         } catch (BeanInstantiationException | NoSuchBeanException var20) {
            throw new DependencyInjectionException(resolutionContext, argument, var20);
         }
      }

      return var10;
   }

   @Internal
   protected final Collection<Object> getBeansOfTypeForConstructorArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int argumentIndex, Argument genericType, Qualifier qualifier
   ) {
      AbstractInitializableBeanDefinition.MethodReference constructorMethodRef = (AbstractInitializableBeanDefinition.MethodReference)this.constructor;
      Argument argument = this.resolveArgument(context, argumentIndex, constructorMethodRef.arguments);

      Collection var10;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushConstructorResolve(this, argument)) {
         var10 = this.resolveBeansOfType(resolutionContext, context, argument, this.resolveEnvironmentArgument(context, genericType), qualifier);
      }

      return var10;
   }

   @Internal
   protected final <K, R extends Collection<BeanRegistration<K>>> R getBeanRegistrationsForConstructorArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int argumentIndex, Argument<K> genericType, Qualifier<K> qualifier
   ) {
      AbstractInitializableBeanDefinition.MethodReference constructorMethodRef = (AbstractInitializableBeanDefinition.MethodReference)this.constructor;
      Argument<R> argument = this.resolveArgument(context, argumentIndex, constructorMethodRef.arguments);

      Collection var10;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushConstructorResolve(this, argument)) {
         var10 = this.resolveBeanRegistrations(resolutionContext, argument, this.resolveEnvironmentArgument(context, genericType), qualifier);
      }

      return (R)var10;
   }

   @Internal
   protected final <K> BeanRegistration<K> getBeanRegistrationForConstructorArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int argumentIndex, Argument<K> genericType, Qualifier<K> qualifier
   ) {
      AbstractInitializableBeanDefinition.MethodReference constructorMethodRef = (AbstractInitializableBeanDefinition.MethodReference)this.constructor;
      Argument<K> argument = this.resolveArgument(context, argumentIndex, constructorMethodRef.arguments);

      BeanRegistration var10;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushConstructorResolve(this, argument)) {
         var10 = this.resolveBeanRegistration(resolutionContext, context, argument, this.resolveEnvironmentArgument(context, genericType), qualifier);
      }

      return var10;
   }

   @Internal
   protected final <K, R extends Collection<BeanRegistration<K>>> R getBeanRegistrationsForMethodArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int methodIndex, int argIndex, Argument<K> genericType, Qualifier<K> qualifier
   ) {
      AbstractInitializableBeanDefinition.MethodReference methodReference = this.methodInjection[methodIndex];
      Argument<R> argument = this.resolveArgument(context, argIndex, methodReference.arguments);

      Collection var11;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath()
            .pushMethodArgumentResolve(this, methodReference.methodName, argument, methodReference.arguments, methodReference.requiresReflection)) {
         var11 = this.resolveBeanRegistrations(resolutionContext, argument, this.resolveEnvironmentArgument(context, genericType), qualifier);
      }

      return (R)var11;
   }

   @Internal
   protected final <K> BeanRegistration<K> getBeanRegistrationForMethodArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int methodIndex, int argIndex, Argument<K> genericType, Qualifier<K> qualifier
   ) {
      AbstractInitializableBeanDefinition.MethodReference methodRef = this.methodInjection[methodIndex];
      Argument<K> argument = this.resolveArgument(context, argIndex, methodRef.arguments);

      BeanRegistration var11;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath()
            .pushMethodArgumentResolve(this, methodRef.methodName, argument, methodRef.arguments, methodRef.requiresReflection)) {
         var11 = this.resolveBeanRegistration(resolutionContext, context, argument, this.resolveEnvironmentArgument(context, genericType), qualifier);
      }

      return var11;
   }

   @Internal
   protected final <K> Stream<K> getStreamOfTypeForConstructorArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int argIndex, Argument<K> genericType, Qualifier<K> qualifier
   ) {
      AbstractInitializableBeanDefinition.MethodReference constructorMethodRef = (AbstractInitializableBeanDefinition.MethodReference)this.constructor;
      Argument<K> argument = this.resolveArgument(context, argIndex, constructorMethodRef.arguments);

      Stream var10;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushConstructorResolve(this, argument)) {
         var10 = this.resolveStreamOfType(resolutionContext, argument, this.resolveEnvironmentArgument(context, genericType), qualifier);
      }

      return var10;
   }

   @Internal
   protected final <K> Optional<K> findBeanForConstructorArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int argIndex, Argument<K> genericType, Qualifier<K> qualifier
   ) {
      AbstractInitializableBeanDefinition.MethodReference constructorMethodRef = (AbstractInitializableBeanDefinition.MethodReference)this.constructor;
      Argument<K> argument = this.resolveArgument(context, argIndex, constructorMethodRef.arguments);

      Optional var10;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushConstructorResolve(this, argument)) {
         var10 = this.resolveOptionalBean(resolutionContext, argument, this.resolveEnvironmentArgument(context, genericType), qualifier);
      }

      return var10;
   }

   @Internal
   protected final <K> K getBeanForField(BeanResolutionContext resolutionContext, BeanContext context, int fieldIndex, Qualifier<K> qualifier) {
      Argument<K> argument = this.resolveEnvironmentArgument(context, this.fieldInjection[fieldIndex].argument);

      Object var8;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath()
            .pushFieldResolve(this, argument, this.fieldInjection[fieldIndex].requiresReflection)) {
         var8 = this.<K>resolveBean(resolutionContext, argument, qualifier, true);
      }

      return (K)var8;
   }

   @Internal
   protected final <K> K getBeanForAnnotation(BeanResolutionContext resolutionContext, BeanContext context, int annotationBeanIndex, Qualifier<K> qualifier) {
      Argument<K> argument = this.resolveEnvironmentArgument(context, this.annotationInjection[annotationBeanIndex].argument);

      Object var8;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushAnnotationResolve(this, argument)) {
         var8 = this.<K>resolveBean(resolutionContext, argument, qualifier);
      }

      return (K)var8;
   }

   @Internal
   @Deprecated
   protected final Object getValueForField(BeanResolutionContext resolutionContext, BeanContext context, int fieldIndex, Qualifier qualifier) {
      AbstractInitializableBeanDefinition.FieldReference fieldRef = this.fieldInjection[fieldIndex];

      Object var8;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushFieldResolve(this, fieldRef.argument, fieldRef.requiresReflection)) {
         var8 = this.resolveValue(resolutionContext, context, fieldRef.argument.getAnnotationMetadata(), fieldRef.argument, qualifier);
      }

      return var8;
   }

   @Internal
   @Deprecated
   protected final Object getPropertyValueForField(
      BeanResolutionContext resolutionContext, BeanContext context, Argument argument, String propertyValue, String cliProperty
   ) {
      Object var8;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushFieldResolve(this, argument, false)) {
         var8 = this.resolvePropertyValue(resolutionContext, context, argument, propertyValue, cliProperty, false);
      }

      return var8;
   }

   @Internal
   @Deprecated
   protected final Object getPropertyPlaceholderValueForField(
      BeanResolutionContext resolutionContext, BeanContext context, Argument argument, String placeholder
   ) {
      Object var7;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushFieldResolve(this, argument, false)) {
         var7 = this.resolvePropertyValue(resolutionContext, context, argument, placeholder, null, true);
      }

      return var7;
   }

   @Internal
   protected final <T1> Optional<T1> getValueForPath(
      BeanResolutionContext resolutionContext, BeanContext context, Argument<T1> propertyType, String propertyPath
   ) {
      if (context instanceof PropertyResolver) {
         PropertyResolver propertyResolver = (PropertyResolver)context;
         String valString = this.substituteWildCards(resolutionContext, propertyPath);
         return propertyResolver.getProperty(valString, ConversionContext.of(propertyType));
      } else {
         return Optional.empty();
      }
   }

   @Internal
   @Deprecated
   protected final boolean containsValueForField(BeanResolutionContext resolutionContext, BeanContext context, int fieldIndex, boolean isValuePrefix) {
      AbstractInitializableBeanDefinition.FieldReference fieldRef = this.fieldInjection[fieldIndex];
      return this.resolveContainsValue(resolutionContext, context, fieldRef.argument.getAnnotationMetadata(), fieldRef.argument, isValuePrefix);
   }

   @Internal
   protected final boolean containsProperties(BeanResolutionContext resolutionContext, BeanContext context) {
      return this.containsProperties(resolutionContext, context, null);
   }

   @Internal
   protected final boolean containsProperties(BeanResolutionContext resolutionContext, BeanContext context, String subProperty) {
      return this.isConfigurationProperties;
   }

   @Internal
   protected final <K, R extends Collection<K>> Object getBeansOfTypeForField(
      BeanResolutionContext resolutionContext, BeanContext context, int fieldIndex, Argument<K> genericType, Qualifier<K> qualifier
   ) {
      AbstractInitializableBeanDefinition.FieldReference fieldRef = this.fieldInjection[fieldIndex];
      Argument<R> argument = this.resolveEnvironmentArgument(context, fieldRef.argument);

      Collection var10;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushFieldResolve(this, argument, fieldRef.requiresReflection)) {
         var10 = this.resolveBeansOfType(resolutionContext, context, argument, this.resolveEnvironmentArgument(context, genericType), qualifier);
      }

      return var10;
   }

   @Internal
   protected final <K, R extends Collection<BeanRegistration<K>>> R getBeanRegistrationsForField(
      BeanResolutionContext resolutionContext, BeanContext context, int fieldIndex, Argument<K> genericType, Qualifier<K> qualifier
   ) {
      AbstractInitializableBeanDefinition.FieldReference fieldRef = this.fieldInjection[fieldIndex];
      Argument<R> argument = this.resolveEnvironmentArgument(context, fieldRef.argument);

      Collection var10;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushFieldResolve(this, argument, fieldRef.requiresReflection)) {
         var10 = this.resolveBeanRegistrations(resolutionContext, argument, this.resolveEnvironmentArgument(context, genericType), qualifier);
      }

      return (R)var10;
   }

   @Internal
   protected final <K> BeanRegistration<K> getBeanRegistrationForField(
      BeanResolutionContext resolutionContext, BeanContext context, int fieldIndex, Argument<K> genericType, Qualifier<K> qualifier
   ) {
      AbstractInitializableBeanDefinition.FieldReference fieldRef = this.fieldInjection[fieldIndex];
      Argument<K> argument = this.resolveEnvironmentArgument(context, fieldRef.argument);

      BeanRegistration var10;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushFieldResolve(this, argument, fieldRef.requiresReflection)) {
         var10 = this.resolveBeanRegistration(resolutionContext, context, argument, this.resolveEnvironmentArgument(context, genericType), qualifier);
      }

      return var10;
   }

   @Internal
   protected final <K> Optional<K> findBeanForField(
      BeanResolutionContext resolutionContext, BeanContext context, int fieldIndex, Argument<K> genericType, Qualifier<K> qualifier
   ) {
      AbstractInitializableBeanDefinition.FieldReference fieldRef = this.fieldInjection[fieldIndex];
      Argument<K> argument = this.resolveEnvironmentArgument(context, fieldRef.argument);

      Optional var10;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushFieldResolve(this, argument, fieldRef.requiresReflection)) {
         var10 = this.resolveOptionalBean(resolutionContext, argument, this.resolveEnvironmentArgument(context, genericType), qualifier);
      }

      return var10;
   }

   @Internal
   protected final <K> Stream<K> getStreamOfTypeForField(
      BeanResolutionContext resolutionContext, BeanContext context, int fieldIndex, Argument<K> genericType, Qualifier<K> qualifier
   ) {
      AbstractInitializableBeanDefinition.FieldReference fieldRef = this.fieldInjection[fieldIndex];
      Argument<K> argument = this.resolveEnvironmentArgument(context, fieldRef.argument);

      Stream var10;
      try (BeanResolutionContext.Path ignored = resolutionContext.getPath().pushFieldResolve(this, argument, fieldRef.requiresReflection)) {
         var10 = this.resolveStreamOfType(resolutionContext, argument, this.resolveEnvironmentArgument(context, genericType), qualifier);
      }

      return var10;
   }

   @Internal
   protected final boolean containsPropertiesValue(BeanResolutionContext resolutionContext, BeanContext context, String value) {
      if (!(context instanceof ApplicationContext)) {
         return false;
      } else {
         value = this.substituteWildCards(resolutionContext, value);
         ApplicationContext applicationContext = (ApplicationContext)context;
         return applicationContext.containsProperties(value);
      }
   }

   @Internal
   protected final boolean containsPropertyValue(BeanResolutionContext resolutionContext, BeanContext context, String value) {
      if (!(context instanceof ApplicationContext)) {
         return false;
      } else {
         value = this.substituteWildCards(resolutionContext, value);
         ApplicationContext applicationContext = (ApplicationContext)context;
         return applicationContext.containsProperty(value);
      }
   }

   private boolean resolveContainsValue(
      BeanResolutionContext resolutionContext, BeanContext context, AnnotationMetadata parentAnnotationMetadata, Argument argument, boolean isValuePrefix
   ) {
      if (!(context instanceof ApplicationContext)) {
         return false;
      } else {
         ApplicationContext applicationContext = (ApplicationContext)context;
         String valueAnnStr = (String)argument.getAnnotationMetadata().stringValue(Value.class).orElse(null);
         String valString = this.resolvePropertyValueName(resolutionContext, parentAnnotationMetadata, argument, valueAnnStr);
         boolean result = isValuePrefix ? applicationContext.containsProperties(valString) : applicationContext.containsProperty(valString);
         if (!result && this.isConfigurationProperties) {
            String cliOption = this.resolveCliOption(argument.getName());
            if (cliOption != null) {
               result = applicationContext.containsProperty(cliOption);
            }
         }

         return result;
      }
   }

   private Object resolveValue(
      BeanResolutionContext resolutionContext, BeanContext context, AnnotationMetadata parentAnnotationMetadata, Argument<?> argument, Qualifier qualifier
   ) {
      if (!(context instanceof PropertyResolver)) {
         throw new DependencyInjectionException(resolutionContext, "@Value requires a BeanContext that implements PropertyResolver");
      } else {
         String valueAnnVal = (String)argument.getAnnotationMetadata().stringValue(Value.class).orElse(null);
         boolean isCollection = false;
         boolean wrapperType = argument.isWrapperType();
         Class<?> argumentJavaType = argument.getType();
         Argument<?> argumentType;
         if (Collection.class.isAssignableFrom(argumentJavaType)) {
            argumentType = (Argument)argument.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
            isCollection = true;
         } else if (wrapperType) {
            argumentType = argument.getWrappedType();
         } else {
            argumentType = argument;
         }

         if (this.isInnerConfiguration(argumentType.getType())) {
            qualifier = qualifier == null ? this.resolveQualifierWithInnerConfiguration(resolutionContext, argument, true) : qualifier;
            if (isCollection) {
               Collection<?> beans = resolutionContext.getBeansOfType(argumentType, qualifier);
               return this.coerceCollectionToCorrectType(argumentJavaType, beans, resolutionContext, argument);
            } else {
               return resolutionContext.getBean(argumentType, qualifier);
            }
         } else {
            String valString = this.resolvePropertyValueName(resolutionContext, parentAnnotationMetadata, argument.getAnnotationMetadata(), valueAnnVal);
            ArgumentConversionContext conversionContext = wrapperType ? ConversionContext.of(argumentType) : ConversionContext.of(argument);
            Optional value = this.resolveValue((ApplicationContext)context, conversionContext, valueAnnVal != null, valString);
            if (argument.isOptional()) {
               if (!value.isPresent()) {
                  return value;
               } else {
                  Object convertedOptional = value.get();
                  return convertedOptional instanceof Optional ? convertedOptional : value;
               }
            } else {
               if (wrapperType) {
                  Object v = value.orElse(null);
                  if (OptionalInt.class == argumentJavaType) {
                     return v instanceof Integer ? OptionalInt.of((Integer)v) : OptionalInt.empty();
                  }

                  if (OptionalLong.class == argumentJavaType) {
                     return v instanceof Long ? OptionalLong.of((Long)v) : OptionalLong.empty();
                  }

                  if (OptionalDouble.class == argumentJavaType) {
                     return v instanceof Double ? OptionalDouble.of((Double)v) : OptionalDouble.empty();
                  }
               }

               if (value.isPresent()) {
                  return value.get();
               } else {
                  return argument.isDeclaredNullable()
                     ? null
                     : argument.getAnnotationMetadata()
                        .getValue(Bindable.class, "defaultValue", argument)
                        .orElseThrow(() -> DependencyInjectionException.missingProperty(resolutionContext, conversionContext, valString));
               }
            }
         }
      }
   }

   private Object resolvePropertyValue(
      BeanResolutionContext resolutionContext, BeanContext context, Argument<?> argument, String stringValue, String cliProperty, boolean isPlaceholder
   ) {
      if (!(context instanceof PropertyResolver)) {
         throw new DependencyInjectionException(resolutionContext, "@Value requires a BeanContext that implements PropertyResolver");
      } else {
         ApplicationContext applicationContext = (ApplicationContext)context;
         Argument<?> argumentType = argument;
         Class<?> wrapperType = null;
         Class<?> type = argument.getType();
         if (type == Optional.class) {
            wrapperType = Optional.class;
            argumentType = (Argument)argument.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
         } else if (type == OptionalInt.class) {
            wrapperType = OptionalInt.class;
            argumentType = Argument.INT;
         } else if (type == OptionalLong.class) {
            wrapperType = OptionalLong.class;
            argumentType = Argument.LONG;
         } else if (type == OptionalDouble.class) {
            wrapperType = OptionalDouble.class;
            argumentType = Argument.DOUBLE;
         }

         ArgumentConversionContext<?> conversionContext = wrapperType != null ? ConversionContext.of(argumentType) : ConversionContext.of(argument);
         Optional<?> value;
         if (isPlaceholder) {
            value = applicationContext.resolvePlaceholders(stringValue).flatMap(v -> applicationContext.getConversionService().convert(v, conversionContext));
         } else {
            stringValue = this.substituteWildCards(resolutionContext, stringValue);
            value = applicationContext.getProperty(stringValue, conversionContext);
            if (!value.isPresent() && cliProperty != null) {
               value = applicationContext.getProperty(cliProperty, conversionContext);
            }
         }

         if (argument.isOptional()) {
            if (!value.isPresent()) {
               return value;
            } else {
               Object convertedOptional = value.get();
               return convertedOptional instanceof Optional ? convertedOptional : value;
            }
         } else {
            if (wrapperType != null) {
               Object v = value.orElse(null);
               if (OptionalInt.class == wrapperType) {
                  return v instanceof Integer ? OptionalInt.of((Integer)v) : OptionalInt.empty();
               }

               if (OptionalLong.class == wrapperType) {
                  return v instanceof Long ? OptionalLong.of((Long)v) : OptionalLong.empty();
               }

               if (OptionalDouble.class == wrapperType) {
                  return v instanceof Double ? OptionalDouble.of((Double)v) : OptionalDouble.empty();
               }
            }

            if (value.isPresent()) {
               return value.get();
            } else if (argument.isDeclaredNullable()) {
               return null;
            } else {
               String finalStringValue = stringValue;
               return argument.getAnnotationMetadata()
                  .getValue(Bindable.class, "defaultValue", argument)
                  .orElseThrow(() -> DependencyInjectionException.missingProperty(resolutionContext, conversionContext, finalStringValue));
            }
         }
      }
   }

   private <K> K resolveBean(BeanResolutionContext resolutionContext, Argument<K> argument, @Nullable Qualifier<K> qualifier) {
      return this.resolveBean(resolutionContext, argument, qualifier, false);
   }

   private <K> K resolveBean(
      BeanResolutionContext resolutionContext, Argument<K> argument, @Nullable Qualifier<K> qualifier, boolean resolveIsInnerConfiguration
   ) {
      qualifier = qualifier == null ? this.resolveQualifier(resolutionContext, argument, resolveIsInnerConfiguration) : qualifier;
      if (Qualifier.class.isAssignableFrom(argument.getType())) {
         return (K)qualifier;
      } else {
         try {
            Object previous = !argument.isAnnotationPresent(Parameter.class) ? resolutionContext.removeAttribute(NAMED_ATTRIBUTE) : null;

            Object var6;
            try {
               var6 = resolutionContext.<K>getBean(argument, qualifier);
            } finally {
               if (previous != null) {
                  resolutionContext.setAttribute(NAMED_ATTRIBUTE, previous);
               }

            }

            return (K)var6;
         } catch (DisabledBeanException var12) {
            if (AbstractBeanContextConditional.LOG.isDebugEnabled()) {
               AbstractBeanContextConditional.LOG.debug("Bean of type [{}] disabled for reason: {}", argument.getTypeName(), var12.getMessage());
            }

            if (this.isIterable() && this.getAnnotationMetadata().hasDeclaredAnnotation(EachBean.class)) {
               throw new DisabledBeanException("Bean [" + this.getBeanType().getSimpleName() + "] disabled by parent: " + var12.getMessage());
            } else if (argument.isDeclaredNullable()) {
               return null;
            } else {
               throw new DependencyInjectionException(resolutionContext, var12);
            }
         } catch (NoSuchBeanException var13) {
            if (argument.isDeclaredNullable()) {
               return null;
            } else {
               throw new DependencyInjectionException(resolutionContext, var13);
            }
         }
      }
   }

   private <K> Optional<K> resolveValue(ApplicationContext context, ArgumentConversionContext<K> argument, boolean hasValueAnnotation, String valString) {
      if (hasValueAnnotation) {
         return context.resolvePlaceholders(valString).flatMap(v -> context.getConversionService().convert(v, argument));
      } else {
         Optional<K> value = context.getProperty(valString, argument);
         if (!value.isPresent() && this.isConfigurationProperties) {
            String cliOption = this.resolveCliOption(argument.getArgument().getName());
            if (cliOption != null) {
               return context.getProperty(cliOption, argument);
            }
         }

         return value;
      }
   }

   private String resolvePropertyValueName(
      BeanResolutionContext resolutionContext, AnnotationMetadata parentAnnotationMetadata, Argument argument, String valueAnnStr
   ) {
      return this.resolvePropertyValueName(resolutionContext, parentAnnotationMetadata, argument.getAnnotationMetadata(), valueAnnStr);
   }

   private String resolvePropertyValueName(
      BeanResolutionContext resolutionContext, AnnotationMetadata parentAnnotationMetadata, AnnotationMetadata annotationMetadata, String valueAnnStr
   ) {
      if (valueAnnStr != null) {
         return valueAnnStr;
      } else {
         String valString = this.getProperty(resolutionContext, parentAnnotationMetadata, annotationMetadata);
         return this.substituteWildCards(resolutionContext, valString);
      }
   }

   private String getProperty(BeanResolutionContext resolutionContext, AnnotationMetadata parentAnnotationMetadata, AnnotationMetadata annotationMetadata) {
      Optional<String> property = parentAnnotationMetadata.stringValue(Property.class, "name");
      if (property.isPresent()) {
         return (String)property.get();
      } else {
         if (parentAnnotationMetadata != annotationMetadata) {
            property = annotationMetadata.stringValue(Property.class, "name");
            if (property.isPresent()) {
               return (String)property.get();
            }
         }

         throw new DependencyInjectionException(resolutionContext, "Value resolution attempted but @Value annotation is missing");
      }
   }

   private String substituteWildCards(BeanResolutionContext resolutionContext, String valString) {
      if (valString.indexOf(42) > -1) {
         Optional<String> namedBean = resolutionContext.get(Named.class.getName(), ConversionContext.STRING);
         if (namedBean.isPresent()) {
            valString = valString.replace("*", (CharSequence)namedBean.get());
         }
      }

      return valString;
   }

   private String resolveCliOption(String name) {
      String attr = "cliPrefix";
      AnnotationMetadata annotationMetadata = this.getAnnotationMetadata();
      return annotationMetadata.isPresent(ConfigurationProperties.class, attr)
         ? (String)annotationMetadata.stringValue(ConfigurationProperties.class, attr).map(val -> val + name).orElse(null)
         : null;
   }

   private <K, R extends Collection<K>> R resolveBeansOfType(
      BeanResolutionContext resolutionContext, BeanContext context, Argument<R> argument, Argument<K> resultGenericType, Qualifier<K> qualifier
   ) {
      if (resultGenericType == null) {
         throw new DependencyInjectionException(resolutionContext, "Type " + argument.getType() + " has no generic argument");
      } else {
         qualifier = qualifier == null ? this.resolveQualifier(resolutionContext, argument, true) : qualifier;
         Collection<K> beansOfType = resolutionContext.getBeansOfType(this.resolveEnvironmentArgument(context, resultGenericType), qualifier);
         return this.coerceCollectionToCorrectType(argument.getType(), beansOfType, resolutionContext, argument);
      }
   }

   private <K> Stream<K> resolveStreamOfType(
      BeanResolutionContext resolutionContext, Argument<K> argument, Argument<K> resultGenericType, Qualifier<K> qualifier
   ) {
      if (resultGenericType == null) {
         throw new DependencyInjectionException(resolutionContext, "Type " + argument.getType() + " has no generic argument");
      } else {
         qualifier = qualifier == null ? this.resolveQualifier(resolutionContext, argument) : qualifier;
         return resolutionContext.streamOfType(resultGenericType, qualifier);
      }
   }

   private <K> Optional<K> resolveOptionalBean(
      BeanResolutionContext resolutionContext, Argument<K> argument, Argument<K> resultGenericType, Qualifier<K> qualifier
   ) {
      if (resultGenericType == null) {
         throw new DependencyInjectionException(resolutionContext, "Type " + argument.getType() + " has no generic argument");
      } else {
         qualifier = qualifier == null ? this.resolveQualifier(resolutionContext, argument) : qualifier;
         return resolutionContext.findBean(resultGenericType, qualifier);
      }
   }

   private <I, K extends Collection<BeanRegistration<I>>> K resolveBeanRegistrations(
      BeanResolutionContext resolutionContext, Argument<K> argument, Argument<I> genericArgument, Qualifier<I> qualifier
   ) {
      try {
         if (genericArgument == null) {
            throw new DependencyInjectionException(
               resolutionContext, "Cannot resolve bean registrations. Argument [" + argument + "] missing generic type information."
            );
         } else {
            qualifier = qualifier == null ? this.resolveQualifier(resolutionContext, argument) : qualifier;
            Collection<BeanRegistration<I>> beanRegistrations = resolutionContext.getBeanRegistrations(genericArgument, qualifier);
            return this.coerceCollectionToCorrectType(argument.getType(), beanRegistrations, resolutionContext, argument);
         }
      } catch (NoSuchBeanException var6) {
         if (argument.isNullable()) {
            return null;
         } else {
            throw new DependencyInjectionException(resolutionContext, var6);
         }
      }
   }

   private <K> Argument<K> resolveArgument(BeanContext context, int argIndex, Argument<?>[] arguments) {
      return arguments == null ? null : this.resolveEnvironmentArgument(context, arguments[argIndex]);
   }

   private <K> Argument<K> resolveEnvironmentArgument(BeanContext context, Argument<K> argument) {
      if (argument instanceof DefaultArgument && argument.getAnnotationMetadata().hasPropertyExpressions()) {
         argument = new EnvironmentAwareArgument<>((DefaultArgument<K>)argument);
         this.instrumentAnnotationMetadata(context, argument);
      }

      return argument;
   }

   private <B> BeanRegistration<B> resolveBeanRegistration(
      BeanResolutionContext resolutionContext, BeanContext context, Argument<B> argument, Argument<B> genericArgument, Qualifier<B> qualifier
   ) {
      try {
         if (genericArgument == null) {
            throw new DependencyInjectionException(
               resolutionContext, "Cannot resolve bean registration. Argument [" + argument + "] missing generic type information."
            );
         } else {
            qualifier = qualifier == null ? this.resolveQualifier(resolutionContext, argument) : qualifier;
            return context.getBeanRegistration(genericArgument, qualifier);
         }
      } catch (NoSuchBeanException var7) {
         if (argument.isNullable()) {
            return null;
         } else {
            throw new DependencyInjectionException(resolutionContext, argument, var7);
         }
      }
   }

   private <K> Qualifier<K> resolveQualifier(BeanResolutionContext resolutionContext, Argument<K> argument) {
      return this.resolveQualifier(resolutionContext, argument, false);
   }

   private <K> Qualifier<K> resolveQualifier(BeanResolutionContext resolutionContext, Argument<K> argument, boolean resolveIsInnerConfiguration) {
      boolean innerConfiguration = resolveIsInnerConfiguration && this.isInnerConfiguration(argument.getType());
      return this.resolveQualifierWithInnerConfiguration(resolutionContext, argument, innerConfiguration);
   }

   private <K> Qualifier<K> resolveQualifierWithInnerConfiguration(BeanResolutionContext resolutionContext, Argument<K> argument, boolean innerConfiguration) {
      boolean hasMetadata = argument.getAnnotationMetadata() != AnnotationMetadata.EMPTY_METADATA;
      Qualifier<K> qualifier = null;
      boolean isIterable = this.isIterable() || resolutionContext.get(EachProperty.class.getName(), Class.class).map(this.getBeanType()::equals).orElse(false);
      if (isIterable) {
         qualifier = (Qualifier)resolutionContext.get("javax.inject.Qualifier", Map.class).map(map -> (Qualifier)map.get(argument)).orElse(null);
      }

      if (qualifier == null
         && (hasMetadata && argument.isAnnotationPresent(Parameter.class) || innerConfiguration && isIterable || Qualifier.class == argument.getType())) {
         Qualifier<K> currentQualifier = resolutionContext.getCurrentQualifier();
         if (currentQualifier != null
            && currentQualifier.getClass() != InterceptorBindingQualifier.class
            && currentQualifier.getClass() != TypeAnnotationQualifier.class) {
            qualifier = currentQualifier;
         } else {
            Optional<String> n = resolutionContext.get(NAMED_ATTRIBUTE, ConversionContext.STRING);
            qualifier = (Qualifier)n.map(Qualifiers::byName).orElse(null);
         }
      }

      return qualifier;
   }

   private <I, K extends Collection<I>> K coerceCollectionToCorrectType(
      Class<K> collectionType, Collection<I> beansOfType, BeanResolutionContext resolutionContext, Argument<?> argument
   ) {
      return (K)(!argument.isArray() && !collectionType.isInstance(beansOfType)
         ? CollectionUtils.convertCollection(collectionType, beansOfType)
            .orElseThrow(() -> new DependencyInjectionException(resolutionContext, "Cannot create a collection of type: " + collectionType.getName()))
         : beansOfType);
   }

   private void instrumentAnnotationMetadata(BeanContext context, Object object) {
      if (object instanceof EnvironmentConfigurable && context instanceof ApplicationContext) {
         EnvironmentConfigurable ec = (EnvironmentConfigurable)object;
         if (ec.hasPropertyExpressions()) {
            ec.configure(((ApplicationContext)context).getEnvironment());
         }
      }

   }

   @Internal
   public static final class AnnotationReference {
      public final Argument argument;

      public AnnotationReference(Argument argument) {
         this.argument = argument;
      }
   }

   private final class BeanAnnotationMetadata extends AbstractEnvironmentAnnotationMetadata {
      BeanAnnotationMetadata(AnnotationMetadata targetMetadata) {
         super(targetMetadata);
      }

      @Nullable
      @Override
      protected Environment getEnvironment() {
         return AbstractInitializableBeanDefinition.this.environment;
      }
   }

   @Internal
   public static final class FieldReference extends AbstractInitializableBeanDefinition.MethodOrFieldReference {
      public final Argument argument;

      public FieldReference(Class declaringType, Argument argument, boolean requiresReflection) {
         super(declaringType, requiresReflection);
         this.argument = argument;
      }
   }

   @Internal
   public abstract static class MethodOrFieldReference {
      final Class declaringType;
      final boolean requiresReflection;

      public MethodOrFieldReference(Class declaringType, boolean requiresReflection) {
         this.declaringType = declaringType;
         this.requiresReflection = requiresReflection;
      }
   }

   @Internal
   public static final class MethodReference extends AbstractInitializableBeanDefinition.MethodOrFieldReference {
      public final String methodName;
      public final Argument[] arguments;
      public final AnnotationMetadata annotationMetadata;
      public final boolean isPreDestroyMethod;
      public final boolean isPostConstructMethod;

      public MethodReference(
         Class declaringType, String methodName, Argument[] arguments, @Nullable AnnotationMetadata annotationMetadata, boolean requiresReflection
      ) {
         this(declaringType, methodName, arguments, annotationMetadata, requiresReflection, false, false);
      }

      public MethodReference(
         Class declaringType,
         String methodName,
         Argument[] arguments,
         @Nullable AnnotationMetadata annotationMetadata,
         boolean requiresReflection,
         boolean isPostConstructMethod,
         boolean isPreDestroyMethod
      ) {
         super(declaringType, requiresReflection);
         this.methodName = methodName;
         this.arguments = arguments;
         this.annotationMetadata = annotationMetadata == null ? AnnotationMetadata.EMPTY_METADATA : annotationMetadata;
         this.isPostConstructMethod = isPostConstructMethod;
         this.isPreDestroyMethod = isPreDestroyMethod;
      }
   }
}
