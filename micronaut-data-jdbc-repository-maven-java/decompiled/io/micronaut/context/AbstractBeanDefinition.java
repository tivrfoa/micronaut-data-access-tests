package io.micronaut.context;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.ConfigurationReader;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Provided;
import io.micronaut.context.annotation.Type;
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
import io.micronaut.core.naming.Named;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.DefaultArgument;
import io.micronaut.core.type.TypeInformation;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.value.PropertyResolver;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ConstructorInjectionPoint;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.FieldInjectionPoint;
import io.micronaut.inject.MethodInjectionPoint;
import io.micronaut.inject.ValidatedBeanDefinition;
import io.micronaut.inject.annotation.AbstractEnvironmentAnnotationMetadata;
import io.micronaut.inject.qualifiers.InterceptorBindingQualifier;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.inject.qualifiers.TypeAnnotationQualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public class AbstractBeanDefinition<T> extends AbstractBeanContextConditional implements BeanDefinition<T>, EnvironmentConfigurable {
   private static final Logger LOG = LoggerFactory.getLogger(AbstractBeanDefinition.class);
   private static final String NAMED_ATTRIBUTE = Named.class.getName();
   protected final List<MethodInjectionPoint<T, ?>> methodInjectionPoints = new ArrayList(3);
   protected final List<FieldInjectionPoint<T, ?>> fieldInjectionPoints = new ArrayList(3);
   protected List<MethodInjectionPoint<T, ?>> postConstructMethods;
   protected List<MethodInjectionPoint<T, ?>> preDestroyMethods;
   protected Map<AbstractBeanDefinition<T>.MethodKey, ExecutableMethod<T, ?>> executableMethodMap;
   private final Class<T> type;
   private final boolean isAbstract;
   private final boolean isConfigurationProperties;
   private final Class<?> declaringType;
   private final ConstructorInjectionPoint<T> constructor;
   private final Collection<Class<?>> requiredComponents = new HashSet(3);
   private AnnotationMetadata beanAnnotationMetadata;
   private Environment environment;
   private Set<Class<?>> exposedTypes;
   private Argument<?> containerElement;

   @Internal
   protected AbstractBeanDefinition(Class<T> producedType, Class<?> declaringType, String fieldName, AnnotationMetadata fieldMetadata, boolean isFinal) {
      this.type = producedType;
      this.isAbstract = false;
      this.declaringType = declaringType;
      this.constructor = new DefaultFieldConstructorInjectionPoint<>(this, declaringType, producedType, fieldName, fieldMetadata);
      this.isConfigurationProperties = this.hasStereotype(ConfigurationReader.class) || this.isIterable();
      this.initContainerElement();
   }

   @Internal
   protected AbstractBeanDefinition(
      Class<T> producedType, Class<?> declaringType, String methodName, AnnotationMetadata methodMetadata, boolean requiresReflection, Argument<?>... arguments
   ) {
      this.type = producedType;
      this.isAbstract = false;
      this.declaringType = declaringType;
      if (requiresReflection) {
         this.constructor = new ReflectionMethodConstructorInjectionPoint(this, declaringType, methodName, arguments, methodMetadata);
      } else {
         this.constructor = new DefaultMethodConstructorInjectionPoint<>(this, declaringType, methodName, arguments, methodMetadata);
      }

      this.isConfigurationProperties = this.hasStereotype(ConfigurationReader.class) || this.isIterable();
      this.addRequiredComponents(arguments);
      this.initContainerElement();
   }

   @Internal
   protected AbstractBeanDefinition(Class<T> type, AnnotationMetadata constructorAnnotationMetadata, boolean requiresReflection, Argument... arguments) {
      this.type = type;
      this.isAbstract = Modifier.isAbstract(this.type.getModifiers());
      this.declaringType = type;
      if (requiresReflection) {
         this.constructor = new ReflectionConstructorInjectionPoint<>(this, type, constructorAnnotationMetadata, arguments);
      } else {
         this.constructor = new DefaultConstructorInjectionPoint<>(this, type, constructorAnnotationMetadata, arguments);
      }

      this.isConfigurationProperties = this.hasStereotype(ConfigurationReader.class) || this.isIterable();
      this.addRequiredComponents(arguments);
      this.initContainerElement();
   }

   private void initContainerElement() {
      if (this.isContainerType()) {
         List<Argument<?>> iterableArguments = this.getTypeArguments(Iterable.class);
         if (!iterableArguments.isEmpty()) {
            this.containerElement = (Argument)iterableArguments.iterator().next();
         }
      }

   }

   @Override
   public Optional<Argument<?>> getContainerElement() {
      return Optional.ofNullable(this.containerElement);
   }

   @Override
   public final boolean hasPropertyExpressions() {
      return this.getAnnotationMetadata().hasPropertyExpressions();
   }

   @NonNull
   @Override
   public List<Argument<?>> getTypeArguments(String type) {
      if (type == null) {
         return Collections.emptyList();
      } else {
         Map<String, Argument<?>[]> typeArguments = this.getTypeArgumentsMap();
         Argument<?>[] arguments = (Argument[])typeArguments.get(type);
         return arguments != null ? Arrays.asList(arguments) : Collections.emptyList();
      }
   }

   @NonNull
   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      if (this.beanAnnotationMetadata == null) {
         this.beanAnnotationMetadata = this.initializeAnnotationMetadata();
      }

      return this.beanAnnotationMetadata;
   }

   @Override
   public boolean isAbstract() {
      return this.isAbstract;
   }

   @Override
   public boolean isIterable() {
      return this.hasDeclaredStereotype(EachProperty.class) || this.hasDeclaredStereotype(EachBean.class);
   }

   @Override
   public boolean isPrimary() {
      return this.hasDeclaredStereotype(Primary.class);
   }

   @Override
   public <R> Optional<ExecutableMethod<T, R>> findMethod(String name, Class<?>... argumentTypes) {
      if (this.executableMethodMap != null) {
         AbstractBeanDefinition<T>.MethodKey methodKey = new AbstractBeanDefinition.MethodKey(name, argumentTypes);
         ExecutableMethod<T, R> invocableMethod = (ExecutableMethod)this.executableMethodMap.get(methodKey);
         if (invocableMethod != null) {
            return Optional.of(invocableMethod);
         }
      }

      return Optional.empty();
   }

   @Override
   public Stream<ExecutableMethod<T, ?>> findPossibleMethods(String name) {
      return this.executableMethodMap != null && this.executableMethodMap.keySet().stream().anyMatch(methodKey -> methodKey.name.equals(name))
         ? this.executableMethodMap.values().stream().filter(method -> method.getMethodName().equals(name))
         : Stream.empty();
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
      return "Definition: " + this.declaringType.getName();
   }

   @Override
   public boolean isProvided() {
      return this.getAnnotationMetadata().hasDeclaredStereotype(Provided.class);
   }

   @Override
   public Optional<Class<? extends Annotation>> getScope() {
      return this.getAnnotationMetadata().getAnnotationTypeByStereotype("javax.inject.Scope");
   }

   @Override
   public Optional<String> getScopeName() {
      return this.getAnnotationMetadata().getAnnotationNameByStereotype("javax.inject.Scope");
   }

   @Override
   public final Class<T> getBeanType() {
      return this.type;
   }

   @NonNull
   @Override
   public final Set<Class<?>> getExposedTypes() {
      if (this.exposedTypes == null) {
         this.exposedTypes = BeanDefinition.super.getExposedTypes();
      }

      return this.exposedTypes;
   }

   @Override
   public final Optional<Class<?>> getDeclaringType() {
      return Optional.ofNullable(this.declaringType);
   }

   @Override
   public final ConstructorInjectionPoint<T> getConstructor() {
      return this.constructor;
   }

   @Override
   public Collection<Class<?>> getRequiredComponents() {
      return Collections.unmodifiableCollection(this.requiredComponents);
   }

   @Override
   public final Collection<MethodInjectionPoint<T, ?>> getInjectedMethods() {
      return Collections.unmodifiableCollection(this.methodInjectionPoints);
   }

   @Override
   public final Collection<FieldInjectionPoint<T, ?>> getInjectedFields() {
      return Collections.unmodifiableCollection(this.fieldInjectionPoints);
   }

   @Override
   public final Collection<MethodInjectionPoint<T, ?>> getPostConstructMethods() {
      return (Collection<MethodInjectionPoint<T, ?>>)(this.postConstructMethods != null
         ? Collections.unmodifiableCollection(this.postConstructMethods)
         : Collections.emptyList());
   }

   @Override
   public final Collection<MethodInjectionPoint<T, ?>> getPreDestroyMethods() {
      return (Collection<MethodInjectionPoint<T, ?>>)(this.preDestroyMethods != null
         ? Collections.unmodifiableCollection(this.preDestroyMethods)
         : Collections.emptyList());
   }

   @NonNull
   @Override
   public String getName() {
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
   public Collection<ExecutableMethod<T, ?>> getExecutableMethods() {
      return (Collection<ExecutableMethod<T, ?>>)(this.executableMethodMap != null
         ? Collections.unmodifiableCollection(this.executableMethodMap.values())
         : Collections.emptyList());
   }

   @Internal
   @Override
   public final void configure(Environment environment) {
      if (environment != null) {
         this.environment = environment;
         if (this.constructor instanceof EnvironmentConfigurable) {
            ((EnvironmentConfigurable)this.constructor).configure(environment);
         }

         for(MethodInjectionPoint<T, ?> methodInjectionPoint : this.methodInjectionPoints) {
            if (methodInjectionPoint instanceof EnvironmentConfigurable) {
               ((EnvironmentConfigurable)methodInjectionPoint).configure(environment);
            }
         }

         if (this.executableMethodMap != null) {
            for(ExecutableMethod<T, ?> executableMethod : this.executableMethodMap.values()) {
               if (executableMethod instanceof EnvironmentConfigurable) {
                  ((EnvironmentConfigurable)executableMethod).configure(environment);
               }
            }
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

   @Internal
   protected final AbstractBeanDefinition<T> addExecutableMethod(ExecutableMethod<T, ?> executableMethod) {
      AbstractBeanDefinition<T>.MethodKey key = new AbstractBeanDefinition.MethodKey(executableMethod.getMethodName(), executableMethod.getArgumentTypes());
      if (this.executableMethodMap == null) {
         this.executableMethodMap = new LinkedHashMap(3);
      }

      this.executableMethodMap.put(key, executableMethod);
      return this;
   }

   @Internal
   protected final AbstractBeanDefinition addInjectionPoint(
      Class declaringType,
      Class fieldType,
      String field,
      @Nullable AnnotationMetadata annotationMetadata,
      @Nullable Argument[] typeArguments,
      boolean requiresReflection
   ) {
      FieldInjectionPoint injectionPoint;
      if (requiresReflection) {
         injectionPoint = new ReflectionFieldInjectionPoint(this, declaringType, fieldType, field, annotationMetadata, typeArguments);
      } else {
         injectionPoint = new DefaultFieldInjectionPoint(this, declaringType, fieldType, field, annotationMetadata, typeArguments);
      }

      if (annotationMetadata != null && annotationMetadata.hasDeclaredAnnotation("javax.inject.Inject")) {
         this.addRequiredComponents(injectionPoint.asArgument());
      }

      this.fieldInjectionPoints.add(injectionPoint);
      return this;
   }

   @Internal
   protected final AbstractBeanDefinition addInjectionPoint(
      Class declaringType, String method, @Nullable Argument[] arguments, @Nullable AnnotationMetadata annotationMetadata, boolean requiresReflection
   ) {
      return this.addInjectionPointInternal(declaringType, method, arguments, annotationMetadata, requiresReflection, this.methodInjectionPoints);
   }

   @Internal
   protected final AbstractBeanDefinition addPostConstruct(
      Class declaringType, String method, @Nullable Argument[] arguments, @Nullable AnnotationMetadata annotationMetadata, boolean requiresReflection
   ) {
      if (this.postConstructMethods == null) {
         this.postConstructMethods = new ArrayList(1);
      }

      return this.addInjectionPointInternal(declaringType, method, arguments, annotationMetadata, requiresReflection, this.postConstructMethods);
   }

   @Internal
   protected final AbstractBeanDefinition addPreDestroy(
      Class declaringType, String method, Argument[] arguments, AnnotationMetadata annotationMetadata, boolean requiresReflection
   ) {
      if (this.preDestroyMethods == null) {
         this.preDestroyMethods = new ArrayList(1);
      }

      return this.addInjectionPointInternal(declaringType, method, arguments, annotationMetadata, requiresReflection, this.preDestroyMethods);
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
         DefaultBeanContext defaultContext = (DefaultBeanContext)context;
         return defaultContext.inject(resolutionContext, this, bean);
      }
   }

   @Internal
   protected Object postConstruct(BeanResolutionContext resolutionContext, BeanContext context, Object bean) {
      boolean addInCreationHandling = this.isSingleton() && !CollectionUtils.isNotEmpty(this.postConstructMethods);
      DefaultBeanContext.BeanKey key = null;
      if (addInCreationHandling) {
         key = new DefaultBeanContext.BeanKey<>(this, resolutionContext.getCurrentQualifier());
         resolutionContext.addInFlightBean(key, new BeanRegistration<>(key, this, (T)bean));
      }

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

      DefaultBeanContext defaultContext = (DefaultBeanContext)context;

      for(int i = 0; i < this.methodInjectionPoints.size(); ++i) {
         MethodInjectionPoint methodInjectionPoint = (MethodInjectionPoint)this.methodInjectionPoints.get(i);
         if (methodInjectionPoint.isPostConstructMethod() && methodInjectionPoint.requiresReflection()) {
            this.injectBeanMethod(resolutionContext, defaultContext, i, bean);
         }
      }

      if (bean instanceof LifeCycle) {
         bean = ((LifeCycle)bean).start();
      }

      Object var16;
      try {
         var16 = bean;
      } finally {
         if (addInCreationHandling) {
            resolutionContext.removeInFlightBean(key);
         }

      }

      return var16;
   }

   protected Object preDestroy(BeanResolutionContext resolutionContext, BeanContext context, Object bean) {
      DefaultBeanContext defaultContext = (DefaultBeanContext)context;

      for(int i = 0; i < this.methodInjectionPoints.size(); ++i) {
         MethodInjectionPoint methodInjectionPoint = (MethodInjectionPoint)this.methodInjectionPoints.get(i);
         if (methodInjectionPoint.isPreDestroyMethod() && methodInjectionPoint.requiresReflection()) {
            this.injectBeanMethod(resolutionContext, defaultContext, i, bean);
         }
      }

      if (bean instanceof LifeCycle) {
         bean = ((LifeCycle)bean).stop();
      }

      return bean;
   }

   @Internal
   protected void injectBeanMethod(BeanResolutionContext resolutionContext, DefaultBeanContext context, int methodIndex, Object bean) {
      MethodInjectionPoint methodInjectionPoint = (MethodInjectionPoint)this.methodInjectionPoints.get(methodIndex);
      Argument[] methodArgumentTypes = methodInjectionPoint.getArguments();
      Object[] methodArgs = new Object[methodArgumentTypes.length];

      for(int i = 0; i < methodArgumentTypes.length; ++i) {
         methodArgs[i] = this.getBeanForMethodArgument(resolutionContext, context, methodIndex, i);
      }

      try {
         methodInjectionPoint.invoke(bean, methodArgs);
      } catch (Throwable var9) {
         throw new BeanInstantiationException(this, var9);
      }
   }

   @Internal
   protected final void injectBeanField(BeanResolutionContext resolutionContext, DefaultBeanContext context, int index, Object bean) {
      FieldInjectionPoint fieldInjectionPoint = (FieldInjectionPoint)this.fieldInjectionPoints.get(index);
      boolean isInject = fieldInjectionPoint.getAnnotationMetadata().hasDeclaredAnnotation("javax.inject.Inject");

      try {
         Object value;
         if (isInject) {
            this.instrumentAnnotationMetadata(context, fieldInjectionPoint);
            value = this.getBeanForField(resolutionContext, context, fieldInjectionPoint);
         } else {
            value = this.getValueForField(resolutionContext, context, index);
         }

         if (value != null) {
            fieldInjectionPoint.set(bean, value);
         }

      } catch (Throwable var8) {
         if (var8 instanceof BeanContextException) {
            throw (BeanContextException)var8;
         } else {
            throw new DependencyInjectionException(resolutionContext, fieldInjectionPoint, "Error setting field value: " + var8.getMessage(), var8);
         }
      }
   }

   @Internal
   protected final Object getValueForMethodArgument(BeanResolutionContext resolutionContext, BeanContext context, int methodIndex, int argIndex) {
      MethodInjectionPoint injectionPoint = (MethodInjectionPoint)this.methodInjectionPoints.get(methodIndex);
      Argument argument = injectionPoint.getArguments()[argIndex];
      BeanResolutionContext.Path path = resolutionContext.getPath();
      path.pushMethodArgumentResolve(this, injectionPoint, argument);
      if (context instanceof ApplicationContext) {
         Object conversionContext;
         try {
            String valueAnnStr = (String)argument.getAnnotationMetadata().stringValue(Value.class).orElse(null);
            boolean isCollection = false;
            Argument<?> argumentType;
            if (Collection.class.isAssignableFrom(argument.getType())) {
               argumentType = (Argument)argument.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
               isCollection = true;
            } else {
               argumentType = argument;
            }

            if (!this.isInnerConfiguration(argumentType, context)) {
               String valString = this.resolvePropertyValueName(resolutionContext, injectionPoint.getAnnotationMetadata(), argument, valueAnnStr);
               ApplicationContext applicationContext = (ApplicationContext)context;
               ArgumentConversionContext conversionContext = ConversionContext.of(argument);
               Optional value = this.resolveValue(applicationContext, conversionContext, valueAnnStr != null, valString);
               if (argumentType.isOptional()) {
                  return this.resolveOptionalObject(value);
               }

               if (value.isPresent()) {
                  return value.get();
               }

               if (argument.isDeclaredNullable()) {
                  return null;
               }

               throw new DependencyInjectionException(resolutionContext, injectionPoint, conversionContext, valString);
            }

            Qualifier qualifier = this.resolveQualifier(resolutionContext, argument, true);
            if (!isCollection) {
               return ((DefaultBeanContext)context).getBean(resolutionContext, argumentType, qualifier);
            }

            Collection beans = ((DefaultBeanContext)context).getBeansOfType(resolutionContext, argumentType, qualifier);
            conversionContext = this.coerceCollectionToCorrectType(argument.getType(), beans);
         } finally {
            path.pop();
         }

         return conversionContext;
      } else {
         path.pop();
         throw new DependencyInjectionException(resolutionContext, argument, "BeanContext must support property resolution");
      }
   }

   @Internal
   protected final boolean containsValueForMethodArgument(BeanResolutionContext resolutionContext, BeanContext context, int methodIndex, int argIndex) {
      if (!(context instanceof ApplicationContext)) {
         return false;
      } else {
         MethodInjectionPoint injectionPoint = (MethodInjectionPoint)this.methodInjectionPoints.get(methodIndex);
         Argument argument = injectionPoint.getArguments()[argIndex];
         String valueAnnStr = (String)argument.getAnnotationMetadata().stringValue(Value.class).orElse(null);
         String valString = this.resolvePropertyValueName(resolutionContext, injectionPoint.getAnnotationMetadata(), argument, valueAnnStr);
         ApplicationContext applicationContext = (ApplicationContext)context;
         Class type = argument.getType();
         boolean isConfigProps = type.isAnnotationPresent(ConfigurationProperties.class);
         boolean result = !isConfigProps && !Map.class.isAssignableFrom(type) && !Collection.class.isAssignableFrom(type)
            ? applicationContext.containsProperty(valString)
            : applicationContext.containsProperties(valString);
         if (!result && this.isConfigurationProperties()) {
            String cliOption = this.resolveCliOption(argument.getName());
            if (cliOption != null) {
               result = applicationContext.containsProperty(cliOption);
            }
         }

         if (result && injectionPoint instanceof MissingMethodInjectionPoint) {
            if (LOG.isWarnEnabled()) {
               LOG.warn(
                  "Bean definition for type [{}] is compiled against an older version and value [{}] can no longer be set for missing method: {}",
                  this.getBeanType(),
                  valString,
                  injectionPoint.getName()
               );
            }

            result = false;
         }

         return result;
      }
   }

   @Internal
   protected final Object getBeanForMethodArgument(BeanResolutionContext resolutionContext, BeanContext context, int methodIndex, int argIndex) {
      MethodInjectionPoint injectionPoint = (MethodInjectionPoint)this.methodInjectionPoints.get(methodIndex);
      Argument argument = this.resolveArgument(context, argIndex, injectionPoint.getArguments());
      return this.getBeanForMethodArgument(resolutionContext, context, injectionPoint, argument);
   }

   @Internal
   protected final Collection getBeansOfTypeForMethodArgument(
      BeanResolutionContext resolutionContext, BeanContext context, MethodInjectionPoint injectionPoint, Argument argument
   ) {
      return this.resolveBeanWithGenericsFromMethodArgument(
         resolutionContext,
         injectionPoint,
         argument,
         (beanType, qualifier) -> {
            boolean hasNoGenerics = !argument.getType().isArray() && argument.getTypeVariables().isEmpty();
            return hasNoGenerics
               ? ((DefaultBeanContext)context).getBean(resolutionContext, beanType, qualifier)
               : ((DefaultBeanContext)context).getBeansOfType(resolutionContext, beanType, qualifier);
         }
      );
   }

   @Internal
   protected final Optional findBeanForMethodArgument(
      BeanResolutionContext resolutionContext, BeanContext context, MethodInjectionPoint injectionPoint, Argument argument
   ) {
      return this.resolveBeanWithGenericsFromMethodArgument(
         resolutionContext, injectionPoint, argument, (beanType, qualifier) -> ((DefaultBeanContext)context).findBean(resolutionContext, beanType, qualifier)
      );
   }

   @Internal
   protected final Stream streamOfTypeForMethodArgument(
      BeanResolutionContext resolutionContext, BeanContext context, MethodInjectionPoint injectionPoint, Argument argument
   ) {
      return this.resolveBeanWithGenericsFromMethodArgument(
         resolutionContext,
         injectionPoint,
         argument,
         (beanType, qualifier) -> ((DefaultBeanContext)context).streamOfType(resolutionContext, beanType, qualifier)
      );
   }

   @Internal
   protected final Object getBeanForConstructorArgument(BeanResolutionContext resolutionContext, BeanContext context, int argIndex) {
      ConstructorInjectionPoint<T> constructorInjectionPoint = this.getConstructor();
      Argument<?> argument = this.getArgument(context, constructorInjectionPoint.getArguments(), argIndex);
      Class<?> beanType = argument.getType();
      if (beanType == BeanResolutionContext.class) {
         return resolutionContext;
      } else if (argument.isArray()) {
         Collection beansOfType = this.getBeansOfTypeForConstructorArgument(resolutionContext, context, constructorInjectionPoint, argument);
         return beansOfType.toArray(Array.newInstance(beanType.getComponentType(), beansOfType.size()));
      } else if (Collection.class.isAssignableFrom(beanType)) {
         Collection beansOfType = this.getBeansOfTypeForConstructorArgument(resolutionContext, context, constructorInjectionPoint, argument);
         return this.coerceCollectionToCorrectType(beanType, beansOfType);
      } else if (Stream.class.isAssignableFrom(beanType)) {
         return this.streamOfTypeForConstructorArgument(resolutionContext, context, constructorInjectionPoint, argument);
      } else if (argument.isOptional()) {
         return this.findBeanForConstructorArgument(resolutionContext, context, constructorInjectionPoint, argument);
      } else {
         BeanResolutionContext.Path path = resolutionContext.getPath();
         BeanResolutionContext.Segment current = (BeanResolutionContext.Segment)path.peek();
         boolean isNullable = argument.isDeclaredNullable();
         if (isNullable && current != null && current.getArgument().equals(argument)) {
            return null;
         } else {
            path.pushConstructorResolve(this, argument);

            try {
               Qualifier qualifier = this.resolveQualifier(resolutionContext, argument, this.isInnerConfiguration(argument, context));
               Object bean;
               if (Qualifier.class.isAssignableFrom(beanType)) {
                  bean = qualifier;
               } else {
                  Object previous = !argument.isAnnotationPresent(Parameter.class) ? resolutionContext.removeAttribute(NAMED_ATTRIBUTE) : null;

                  try {
                     bean = ((DefaultBeanContext)context).getBean(resolutionContext, argument, qualifier);
                  } finally {
                     if (previous != null) {
                        resolutionContext.setAttribute(NAMED_ATTRIBUTE, previous);
                     }

                  }
               }

               path.pop();
               return bean;
            } catch (DisabledBeanException var18) {
               if (AbstractBeanContextConditional.LOG.isDebugEnabled()) {
                  AbstractBeanContextConditional.LOG.debug("Bean of type [{}] disabled for reason: {}", argument.getTypeName(), var18.getMessage());
               }

               if (this.isIterable() && this.getAnnotationMetadata().hasDeclaredAnnotation(EachBean.class)) {
                  throw new DisabledBeanException("Bean [" + this.getBeanType().getSimpleName() + "] disabled by parent: " + var18.getMessage());
               } else if (isNullable) {
                  path.pop();
                  return null;
               } else {
                  throw new DependencyInjectionException(resolutionContext, argument, var18);
               }
            } catch (NoSuchBeanException var19) {
               if (isNullable) {
                  path.pop();
                  return null;
               } else {
                  throw new DependencyInjectionException(resolutionContext, argument, var19);
               }
            }
         }
      }
   }

   private Argument<?> getArgument(BeanContext context, Argument[] arguments, int argIndex) {
      return this.resolveArgument(context, argIndex, arguments);
   }

   @Internal
   protected final Object getValueForConstructorArgument(BeanResolutionContext resolutionContext, BeanContext context, int argIndex) {
      ConstructorInjectionPoint<T> constructorInjectionPoint = this.getConstructor();
      BeanResolutionContext.Path path = resolutionContext.getPath();
      Argument<?> argument = constructorInjectionPoint.getArguments()[argIndex];
      path.pushConstructorResolve(this, argument);

      Object var14;
      try {
         if (!(context instanceof ApplicationContext)) {
            throw new DependencyInjectionException(resolutionContext, argument, "BeanContext must support property resolution");
         }

         ApplicationContext propertyResolver = (ApplicationContext)context;
         AnnotationMetadata argMetadata = argument.getAnnotationMetadata();
         Optional<String> valAnn = argMetadata.stringValue(Value.class);
         String prop = this.resolvePropertyValueName(resolutionContext, argMetadata, argument, (String)valAnn.orElse(null));
         ArgumentConversionContext<?> conversionContext = ConversionContext.of(argument);
         Optional<?> value = this.resolveValue(propertyResolver, conversionContext, valAnn.isPresent(), prop);
         if (argument.getType() != Optional.class) {
            Object result;
            if (value.isPresent()) {
               result = value.get();
            } else if (argument.isDeclaredNullable()) {
               result = null;
            } else {
               result = argMetadata.getValue(Bindable.class, "defaultValue", argument)
                  .orElseThrow(() -> new DependencyInjectionException(resolutionContext, conversionContext, prop));
            }

            if (this instanceof ValidatedBeanDefinition) {
               ((ValidatedBeanDefinition)this).validateBeanArgument(resolutionContext, constructorInjectionPoint, argument, argIndex, result);
            }

            return result;
         }

         var14 = this.resolveOptionalObject(value);
      } catch (BeanInstantiationException | NoSuchBeanException var18) {
         throw new DependencyInjectionException(resolutionContext, argument, var18);
      } finally {
         path.pop();
      }

      return var14;
   }

   @Internal
   protected final Collection getBeansOfTypeForConstructorArgument(
      BeanResolutionContext resolutionContext, BeanContext context, ConstructorInjectionPoint<T> constructorInjectionPoint, Argument argument
   ) {
      return this.resolveBeanWithGenericsFromConstructorArgument(
         resolutionContext,
         argument,
         (beanType, qualifier) -> {
            boolean hasNoGenerics = !argument.getType().isArray() && argument.getTypeVariables().isEmpty();
            return hasNoGenerics
               ? ((DefaultBeanContext)context).getBean(resolutionContext, beanType, qualifier)
               : ((DefaultBeanContext)context).getBeansOfType(resolutionContext, beanType, qualifier);
         }
      );
   }

   @Internal
   protected final Object getBeansOfTypeForConstructorArgument(BeanResolutionContext resolutionContext, BeanContext context, int argumentIndex) {
      ConstructorInjectionPoint<T> constructorInjectionPoint = this.getConstructor();
      Argument<?> argument = this.getArgument(context, constructorInjectionPoint.getArguments(), argumentIndex);
      Class<?> argumentType = argument.getType();
      Argument<?> genericType = this.resolveGenericType(
         argument,
         (Supplier<DependencyInjectionException>)(() -> new DependencyInjectionException(
               resolutionContext, argument, "Type " + argumentType + " has no generic argument"
            ))
      );
      Qualifier qualifier = this.resolveQualifier(resolutionContext, argument);
      BeanResolutionContext.Path path = resolutionContext.getPath();
      path.pushConstructorResolve(this, argument);
      return this.doGetBeansOfType(resolutionContext, (DefaultBeanContext)context, argumentType, genericType, qualifier, path);
   }

   @Internal
   protected final Object getBeansOfTypeForMethodArgument(BeanResolutionContext resolutionContext, BeanContext context, int methodIndex, int argumentIndex) {
      MethodInjectionPoint<?, ?> methodInjectionPoint = (MethodInjectionPoint)this.methodInjectionPoints.get(methodIndex);
      Argument<?> argument = this.getArgument(context, methodInjectionPoint.getArguments(), argumentIndex);
      Class<?> argumentType = argument.getType();
      Argument<?> genericType = this.resolveGenericType(
         argument,
         (Supplier<DependencyInjectionException>)(() -> new DependencyInjectionException(
               resolutionContext, methodInjectionPoint, argument, "Type " + argumentType + " has no generic argument"
            ))
      );
      Qualifier qualifier = this.resolveQualifier(resolutionContext, argument);
      BeanResolutionContext.Path path = resolutionContext.getPath();
      path.pushMethodArgumentResolve(this, methodInjectionPoint, argument);
      return this.doGetBeansOfType(resolutionContext, (DefaultBeanContext)context, argumentType, genericType, qualifier, path);
   }

   @Internal
   protected final Object getBeansOfTypeForField(BeanResolutionContext resolutionContext, BeanContext context, int fieldIndex) {
      FieldInjectionPoint<?, ?> fieldInjectionPoint = (FieldInjectionPoint)this.fieldInjectionPoints.get(fieldIndex);
      Argument<?> argument = fieldInjectionPoint.asArgument();
      Class<?> argumentType = argument.getType();
      Argument<?> genericType = this.resolveGenericType(
         argument,
         (Supplier<DependencyInjectionException>)(() -> new DependencyInjectionException(
               resolutionContext, fieldInjectionPoint, "Type " + argumentType + " has no generic argument"
            ))
      );
      Qualifier qualifier = this.resolveQualifier(resolutionContext, argument);
      BeanResolutionContext.Path path = resolutionContext.getPath();
      path.pushFieldResolve(this, fieldInjectionPoint);
      return this.doGetBeansOfType(resolutionContext, (DefaultBeanContext)context, argumentType, genericType, qualifier, path);
   }

   private Object doGetBeansOfType(
      BeanResolutionContext resolutionContext,
      DefaultBeanContext context,
      Class<?> argumentType,
      Argument<?> genericType,
      Qualifier qualifier,
      BeanResolutionContext.Path path
   ) {
      Object[] var8;
      try {
         Collection<?> beansOfType = context.getBeansOfType(resolutionContext, genericType, qualifier);
         if (!argumentType.isArray()) {
            return this.coerceCollectionToCorrectType(argumentType, beansOfType);
         }

         var8 = beansOfType.toArray(Array.newInstance(genericType.getType(), beansOfType.size()));
      } finally {
         path.pop();
      }

      return var8;
   }

   private Argument<?> resolveGenericType(Argument<?> argument, Supplier<DependencyInjectionException> exceptionSupplier) {
      Argument<?> genericType;
      if (argument.isArray()) {
         genericType = Argument.of(argument.getType().getComponentType());
      } else {
         genericType = (Argument)argument.getFirstTypeVariable().orElseThrow(exceptionSupplier);
      }

      return genericType;
   }

   @Internal
   protected final Object getBeanRegistrationsForConstructorArgument(BeanResolutionContext resolutionContext, BeanContext context, int argumentIndex) {
      Argument<?> argument = this.getArgument(context, this.getConstructor().getArguments(), argumentIndex);
      BeanResolutionContext.Path path = resolutionContext.getPath();
      path.pushConstructorResolve(this, argument);
      return this.doResolveBeanRegistrations(resolutionContext, (DefaultBeanContext)context, argument, path);
   }

   @Internal
   protected final BeanRegistration<?> getBeanRegistrationForConstructorArgument(BeanResolutionContext resolutionContext, BeanContext context, int argIndex) {
      Argument<?> argument = this.getArgument(context, this.getConstructor().getArguments(), argIndex);
      BeanResolutionContext.Path path = resolutionContext.getPath();
      path.pushConstructorResolve(this, argument);
      return this.resolveBeanRegistrationWithGenericsFromArgument(
         resolutionContext, argument, path, (beanType, qualifier) -> ((DefaultBeanContext)context).getBeanRegistration(resolutionContext, beanType, qualifier)
      );
   }

   @Internal
   protected final Object getBeanRegistrationsForField(BeanResolutionContext resolutionContext, BeanContext context, int fieldIndex) {
      FieldInjectionPoint<?, ?> field = (FieldInjectionPoint)this.fieldInjectionPoints.get(fieldIndex);
      this.instrumentAnnotationMetadata(context, field);
      BeanResolutionContext.Path path = resolutionContext.getPath();
      path.pushFieldResolve(this, field);
      return this.doResolveBeanRegistrations(resolutionContext, (DefaultBeanContext)context, field.asArgument(), path);
   }

   @Internal
   protected final BeanRegistration<?> getBeanRegistrationForField(BeanResolutionContext resolutionContext, BeanContext context, int fieldIndex) {
      FieldInjectionPoint<?, ?> field = (FieldInjectionPoint)this.fieldInjectionPoints.get(fieldIndex);
      this.instrumentAnnotationMetadata(context, field);
      BeanResolutionContext.Path path = resolutionContext.getPath();
      path.pushFieldResolve(this, field);
      return this.resolveBeanRegistrationWithGenericsFromArgument(
         resolutionContext,
         field.asArgument(),
         path,
         (beanType, qualifier) -> ((DefaultBeanContext)context).getBeanRegistration(resolutionContext, beanType, qualifier)
      );
   }

   @Internal
   protected final Object getBeanRegistrationsForMethodArgument(BeanResolutionContext resolutionContext, BeanContext context, int methodIndex, int argIndex) {
      MethodInjectionPoint<?, ?> methodInjectionPoint = (MethodInjectionPoint)this.methodInjectionPoints.get(methodIndex);
      Argument<?> argument = this.resolveArgument(context, argIndex, methodInjectionPoint.getArguments());
      BeanResolutionContext.Path path = resolutionContext.getPath();
      path.pushMethodArgumentResolve(this, methodInjectionPoint, argument);
      return this.doResolveBeanRegistrations(resolutionContext, (DefaultBeanContext)context, argument, path);
   }

   @Internal
   protected final BeanRegistration<?> getBeanRegistrationForMethodArgument(
      BeanResolutionContext resolutionContext, BeanContext context, int methodIndex, int argIndex
   ) {
      MethodInjectionPoint<?, ?> methodInjectionPoint = (MethodInjectionPoint)this.methodInjectionPoints.get(methodIndex);
      Argument<?> argument = this.resolveArgument(context, argIndex, methodInjectionPoint.getArguments());
      BeanResolutionContext.Path path = resolutionContext.getPath();
      path.pushMethodArgumentResolve(this, methodInjectionPoint, argument);
      return this.resolveBeanRegistrationWithGenericsFromArgument(
         resolutionContext, argument, path, (beanType, qualifier) -> ((DefaultBeanContext)context).getBeanRegistration(resolutionContext, beanType, qualifier)
      );
   }

   @Internal
   protected final Stream streamOfTypeForConstructorArgument(
      BeanResolutionContext resolutionContext, BeanContext context, ConstructorInjectionPoint<T> constructorInjectionPoint, Argument argument
   ) {
      return this.resolveBeanWithGenericsFromConstructorArgument(
         resolutionContext, argument, (beanType, qualifier) -> ((DefaultBeanContext)context).streamOfType(resolutionContext, beanType, qualifier)
      );
   }

   @Internal
   protected final Optional findBeanForConstructorArgument(
      BeanResolutionContext resolutionContext, BeanContext context, ConstructorInjectionPoint<T> constructorInjectionPoint, Argument argument
   ) {
      return this.resolveBeanWithGenericsFromConstructorArgument(
         resolutionContext, argument, (beanType, qualifier) -> ((DefaultBeanContext)context).findBean(resolutionContext, beanType, qualifier)
      );
   }

   @Internal
   protected final Object getBeanForField(BeanResolutionContext resolutionContext, BeanContext context, int fieldIndex) {
      FieldInjectionPoint injectionPoint = (FieldInjectionPoint)this.fieldInjectionPoints.get(fieldIndex);
      this.instrumentAnnotationMetadata(context, injectionPoint);
      return this.getBeanForField(resolutionContext, context, injectionPoint);
   }

   @Internal
   protected final Object getValueForField(BeanResolutionContext resolutionContext, BeanContext context, int fieldIndex) {
      FieldInjectionPoint injectionPoint = (FieldInjectionPoint)this.fieldInjectionPoints.get(fieldIndex);
      BeanResolutionContext.Path path = resolutionContext.getPath();
      path.pushFieldResolve(this, injectionPoint);

      Object value;
      try {
         if (!(context instanceof PropertyResolver)) {
            throw new DependencyInjectionException(resolutionContext, injectionPoint, "@Value requires a BeanContext that implements PropertyResolver");
         }

         AnnotationMetadata annotationMetadata = injectionPoint.getAnnotationMetadata();
         String valueAnnVal = (String)annotationMetadata.stringValue(Value.class).orElse(null);
         Argument<?> fieldArgument = injectionPoint.asArgument();
         boolean isCollection = false;
         Argument<?> argumentType;
         if (Collection.class.isAssignableFrom(injectionPoint.getType())) {
            argumentType = (Argument)fieldArgument.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
            isCollection = true;
         } else {
            argumentType = fieldArgument;
         }

         if (!this.isInnerConfiguration(argumentType, context)) {
            String valString = this.resolvePropertyValueName(resolutionContext, injectionPoint, valueAnnVal, annotationMetadata);
            ArgumentConversionContext conversionContext = ConversionContext.of(fieldArgument);
            Optional value = this.resolveValue((ApplicationContext)context, conversionContext, valueAnnVal != null, valString);
            if (argumentType.isOptional()) {
               return this.resolveOptionalObject(value);
            }

            if (value.isPresent()) {
               return value.get();
            }

            if (fieldArgument.isDeclaredNullable()) {
               return null;
            }

            throw new DependencyInjectionException(
               resolutionContext, injectionPoint, "Error resolving field value [" + valString + "]. Property doesn't exist or cannot be converted"
            );
         }

         Qualifier qualifier = this.resolveQualifier(resolutionContext, fieldArgument, true);
         if (!isCollection) {
            return ((DefaultBeanContext)context).getBean(resolutionContext, argumentType, qualifier);
         }

         Collection beans = ((DefaultBeanContext)context).getBeansOfType(resolutionContext, argumentType, qualifier);
         value = this.coerceCollectionToCorrectType(fieldArgument.getType(), beans);
      } finally {
         path.pop();
      }

      return value;
   }

   @Internal
   protected final <T1> Optional<T1> getValueForPath(
      BeanResolutionContext resolutionContext, BeanContext context, Argument<T1> propertyType, String... propertyPath
   ) {
      if (context instanceof PropertyResolver) {
         PropertyResolver propertyResolver = (PropertyResolver)context;
         String pathString = propertyPath.length > 1 ? String.join(".", propertyPath) : propertyPath[0];
         String valString = this.resolvePropertyPath(resolutionContext, pathString);
         return propertyResolver.getProperty(valString, ConversionContext.of(propertyType));
      } else {
         return Optional.empty();
      }
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
   protected final boolean containsValueForField(BeanResolutionContext resolutionContext, BeanContext context, int fieldIndex) {
      if (!(context instanceof ApplicationContext)) {
         return false;
      } else {
         FieldInjectionPoint injectionPoint = (FieldInjectionPoint)this.fieldInjectionPoints.get(fieldIndex);
         AnnotationMetadata annotationMetadata = injectionPoint.getAnnotationMetadata();
         String valueAnnVal = (String)annotationMetadata.stringValue(Value.class).orElse(null);
         String valString = this.resolvePropertyValueName(resolutionContext, injectionPoint, valueAnnVal, annotationMetadata);
         ApplicationContext applicationContext = (ApplicationContext)context;
         Class fieldType = injectionPoint.getType();
         boolean isConfigProps = fieldType.isAnnotationPresent(ConfigurationProperties.class);
         boolean result = !isConfigProps && !Map.class.isAssignableFrom(fieldType) && !Collection.class.isAssignableFrom(fieldType)
            ? applicationContext.containsProperty(valString)
            : applicationContext.containsProperties(valString);
         if (!result && this.isConfigurationProperties()) {
            String cliOption = this.resolveCliOption(injectionPoint.getName());
            if (cliOption != null) {
               return applicationContext.containsProperty(cliOption);
            }
         }

         return result;
      }
   }

   @Internal
   protected final boolean containsProperties(BeanResolutionContext resolutionContext, BeanContext context) {
      return this.containsProperties(resolutionContext, context, null);
   }

   @Internal
   protected final boolean containsProperties(BeanResolutionContext resolutionContext, BeanContext context, String subProperty) {
      boolean isSubProperty = StringUtils.isNotEmpty(subProperty);
      if (!isSubProperty && !this.requiredComponents.isEmpty()) {
         return true;
      } else if (this.isConfigurationProperties && context instanceof ApplicationContext) {
         AnnotationMetadata annotationMetadata = this.getAnnotationMetadata();
         ApplicationContext appCtx = (ApplicationContext)context;
         if (annotationMetadata.getValue(ConfigurationProperties.class, "cliPrefix").isPresent()) {
            return true;
         } else {
            String path = this.getConfigurationPropertiesPath(resolutionContext);
            return appCtx.containsProperties(path);
         }
      } else {
         return false;
      }
   }

   @Internal
   protected final Object getBeanForField(BeanResolutionContext resolutionContext, BeanContext context, FieldInjectionPoint injectionPoint) {
      Class beanClass = injectionPoint.getType();
      if (beanClass.isArray()) {
         Collection beansOfType = this.getBeansOfTypeForField(resolutionContext, context, injectionPoint);
         return beansOfType.toArray(Array.newInstance(beanClass.getComponentType(), beansOfType.size()));
      } else if (Collection.class.isAssignableFrom(beanClass)) {
         Collection beansOfType = this.getBeansOfTypeForField(resolutionContext, context, injectionPoint);
         return beanClass.isInstance(beansOfType) ? beansOfType : CollectionUtils.convertCollection(beanClass, beansOfType).orElse(null);
      } else if (Stream.class.isAssignableFrom(beanClass)) {
         return this.getStreamOfTypeForField(resolutionContext, context, injectionPoint);
      } else if (Optional.class.isAssignableFrom(beanClass)) {
         return this.findBeanForField(resolutionContext, context, injectionPoint);
      } else {
         BeanResolutionContext.Path path = resolutionContext.getPath();
         path.pushFieldResolve(this, injectionPoint);
         Argument argument = injectionPoint.asArgument();

         try {
            Qualifier qualifier = this.resolveQualifier(resolutionContext, argument);
            Object bean = ((DefaultBeanContext)context).getBean(resolutionContext, argument, qualifier);
            path.pop();
            return bean;
         } catch (DisabledBeanException var9) {
            if (AbstractBeanContextConditional.LOG.isDebugEnabled()) {
               AbstractBeanContextConditional.LOG.debug("Bean of type [{}] disabled for reason: {}", argument.getTypeName(), var9.getMessage());
            }

            if (this.isIterable() && this.getAnnotationMetadata().hasDeclaredAnnotation(EachBean.class)) {
               throw new DisabledBeanException("Bean [" + this.getBeanType().getSimpleName() + "] disabled by parent: " + var9.getMessage());
            } else if (injectionPoint.isDeclaredNullable()) {
               path.pop();
               return null;
            } else {
               throw new DependencyInjectionException(resolutionContext, injectionPoint, var9);
            }
         } catch (NoSuchBeanException var10) {
            if (injectionPoint.isDeclaredNullable()) {
               path.pop();
               return null;
            } else {
               throw new DependencyInjectionException(resolutionContext, injectionPoint, var10);
            }
         }
      }
   }

   @Internal
   protected final Optional findBeanForField(BeanResolutionContext resolutionContext, BeanContext context, FieldInjectionPoint injectionPoint) {
      return this.resolveBeanWithGenericsForField(
         resolutionContext, injectionPoint, (beanType, qualifier) -> ((DefaultBeanContext)context).findBean(resolutionContext, beanType, qualifier)
      );
   }

   @Internal
   protected final Collection getBeansOfTypeForField(BeanResolutionContext resolutionContext, BeanContext context, FieldInjectionPoint injectionPoint) {
      return this.resolveBeanWithGenericsForField(
         resolutionContext,
         injectionPoint,
         (beanType, qualifier) -> {
            boolean hasNoGenerics = !injectionPoint.getType().isArray() && injectionPoint.asArgument().getTypeVariables().isEmpty();
            return hasNoGenerics
               ? ((DefaultBeanContext)context).getBean(resolutionContext, beanType, qualifier)
               : ((DefaultBeanContext)context).getBeansOfType(resolutionContext, beanType, qualifier);
         }
      );
   }

   @Internal
   protected final Stream getStreamOfTypeForField(BeanResolutionContext resolutionContext, BeanContext context, FieldInjectionPoint injectionPoint) {
      return this.resolveBeanWithGenericsForField(
         resolutionContext, injectionPoint, (beanType, qualifier) -> ((DefaultBeanContext)context).streamOfType(resolutionContext, beanType, qualifier)
      );
   }

   @Internal
   protected Map<String, Argument<?>[]> getTypeArgumentsMap() {
      return Collections.emptyMap();
   }

   protected AnnotationMetadata resolveAnnotationMetadata() {
      return AnnotationMetadata.EMPTY_METADATA;
   }

   private AnnotationMetadata initializeAnnotationMetadata() {
      AnnotationMetadata annotationMetadata = this.resolveAnnotationMetadata();
      if (annotationMetadata != AnnotationMetadata.EMPTY_METADATA) {
         return (AnnotationMetadata)(annotationMetadata.hasPropertyExpressions()
            ? new AbstractBeanDefinition.BeanAnnotationMetadata(annotationMetadata)
            : annotationMetadata);
      } else {
         return AnnotationMetadata.EMPTY_METADATA;
      }
   }

   private AbstractBeanDefinition addInjectionPointInternal(
      Class declaringType,
      String method,
      @Nullable Argument[] arguments,
      @Nullable AnnotationMetadata annotationMetadata,
      boolean requiresReflection,
      List<MethodInjectionPoint<T, ?>> targetInjectionPoints
   ) {
      boolean isPreDestroy = targetInjectionPoints == this.preDestroyMethods;
      boolean isPostConstruct = targetInjectionPoints == this.postConstructMethods;
      MethodInjectionPoint injectionPoint;
      if (requiresReflection) {
         injectionPoint = new ReflectionMethodInjectionPoint(this, declaringType, method, arguments, annotationMetadata);
      } else {
         injectionPoint = new DefaultMethodInjectionPoint<>(this, declaringType, method, arguments, annotationMetadata);
      }

      targetInjectionPoints.add(injectionPoint);
      if (isPostConstruct || isPreDestroy) {
         this.methodInjectionPoints.add(injectionPoint);
      }

      this.addRequiredComponents(arguments);
      return this;
   }

   private Object getBeanForMethodArgument(BeanResolutionContext resolutionContext, BeanContext context, MethodInjectionPoint injectionPoint, Argument argument) {
      Class argumentType = argument.getType();
      if (argumentType.isArray()) {
         Collection beansOfType = this.getBeansOfTypeForMethodArgument(resolutionContext, context, injectionPoint, argument);
         return beansOfType.toArray(Array.newInstance(argumentType.getComponentType(), beansOfType.size()));
      } else if (Collection.class.isAssignableFrom(argumentType)) {
         Collection beansOfType = this.getBeansOfTypeForMethodArgument(resolutionContext, context, injectionPoint, argument);
         return this.coerceCollectionToCorrectType(argumentType, beansOfType);
      } else if (Stream.class.isAssignableFrom(argumentType)) {
         return this.streamOfTypeForMethodArgument(resolutionContext, context, injectionPoint, argument);
      } else if (Optional.class.isAssignableFrom(argumentType)) {
         return this.findBeanForMethodArgument(resolutionContext, context, injectionPoint, argument);
      } else {
         BeanResolutionContext.Path path = resolutionContext.getPath();
         path.pushMethodArgumentResolve(this, injectionPoint, argument);

         try {
            Qualifier qualifier = this.resolveQualifier(resolutionContext, argument);
            Object bean = ((DefaultBeanContext)context).getBean(resolutionContext, argument, qualifier);
            path.pop();
            return bean;
         } catch (DisabledBeanException var9) {
            if (AbstractBeanContextConditional.LOG.isDebugEnabled()) {
               AbstractBeanContextConditional.LOG.debug("Bean of type [{}] disabled for reason: {}", argumentType.getSimpleName(), var9.getMessage());
            }

            if (this.isIterable() && this.getAnnotationMetadata().hasDeclaredAnnotation(EachBean.class)) {
               throw new DisabledBeanException("Bean [" + this.getBeanType().getSimpleName() + "] disabled by parent: " + var9.getMessage());
            } else if (argument.isDeclaredNullable()) {
               path.pop();
               return null;
            } else {
               throw new DependencyInjectionException(resolutionContext, argument, var9);
            }
         } catch (NoSuchBeanException var10) {
            if (argument.isDeclaredNullable()) {
               path.pop();
               return null;
            } else {
               throw new DependencyInjectionException(resolutionContext, argument, var10);
            }
         }
      }
   }

   private Optional resolveValue(ApplicationContext context, ArgumentConversionContext<?> argument, boolean hasValueAnnotation, String valString) {
      if (hasValueAnnotation) {
         return context.resolvePlaceholders(valString).flatMap(v -> context.getConversionService().convert(v, argument));
      } else {
         Optional<?> value = context.getProperty(valString, argument);
         if (!value.isPresent() && this.isConfigurationProperties()) {
            String cliOption = this.resolveCliOption(argument.getArgument().getName());
            if (cliOption != null) {
               return context.getProperty(cliOption, argument);
            }
         }

         return value;
      }
   }

   private String resolvePropertyValueName(
      BeanResolutionContext resolutionContext, AnnotationMetadata annotationMetadata, Argument argument, String valueAnnStr
   ) {
      String valString;
      if (valueAnnStr != null) {
         valString = valueAnnStr;
      } else {
         valString = (String)annotationMetadata.stringValue(Property.class, "name")
            .orElseGet(
               () -> (String)argument.getAnnotationMetadata()
                     .stringValue(Property.class, "name")
                     .orElseThrow(
                        () -> new DependencyInjectionException(resolutionContext, argument, "Value resolution attempted but @Value annotation is missing")
                     )
            );
         valString = this.substituteWildCards(resolutionContext, valString);
      }

      return valString;
   }

   private String resolvePropertyValueName(
      BeanResolutionContext resolutionContext, FieldInjectionPoint injectionPoint, String valueAnn, AnnotationMetadata annotationMetadata
   ) {
      String valString;
      if (valueAnn != null) {
         valString = valueAnn;
      } else {
         valString = (String)annotationMetadata.stringValue(Property.class, "name")
            .orElseThrow(
               () -> new DependencyInjectionException(resolutionContext, injectionPoint, "Value resolution attempted but @Value annotation is missing")
            );
         valString = this.substituteWildCards(resolutionContext, valString);
      }

      return valString;
   }

   private String resolvePropertyPath(BeanResolutionContext resolutionContext, String path) {
      String valString = this.getConfigurationPropertiesPath(resolutionContext);
      return valString + "." + path;
   }

   private String getConfigurationPropertiesPath(BeanResolutionContext resolutionContext) {
      String valString = (String)this.getAnnotationMetadata()
         .stringValue(ConfigurationReader.class, "prefix")
         .orElseThrow(() -> new IllegalStateException("Resolve property path called for non @ConfigurationProperties bean"));
      return this.substituteWildCards(resolutionContext, valString);
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

   private boolean isInnerConfiguration(Argument<?> argumentType, BeanContext beanContext) {
      Class<?> type = argumentType.getType();
      return this.isConfigurationProperties
         && type.getName().indexOf(36) > -1
         && !type.isEnum()
         && !type.isPrimitive()
         && Modifier.isPublic(type.getModifiers())
         && Modifier.isStatic(type.getModifiers())
         && this.isInnerOfAnySuperclass(type)
         && beanContext.findBeanDefinition(argumentType).map(bd -> bd.hasStereotype(ConfigurationReader.class) || bd.isIterable()).isPresent();
   }

   private boolean isInnerOfAnySuperclass(Class argumentType) {
      for(Class beanType = this.getBeanType(); beanType != null; beanType = beanType.getSuperclass()) {
         if ((beanType.getName() + "$" + argumentType.getSimpleName()).equals(argumentType.getName())) {
            return true;
         }
      }

      return false;
   }

   private <B, X extends RuntimeException> B resolveBeanWithGenericsFromMethodArgument(
      BeanResolutionContext resolutionContext, MethodInjectionPoint injectionPoint, Argument argument, AbstractBeanDefinition.BeanResolver<B> beanResolver
   ) throws X {
      BeanResolutionContext.Path path = resolutionContext.getPath();
      path.pushMethodArgumentResolve(this, injectionPoint, argument);

      try {
         Qualifier qualifier = this.resolveQualifier(resolutionContext, argument);
         Class argumentType = argument.getType();
         Argument genericType = this.resolveGenericType(argument, argumentType);
         B bean = beanResolver.resolveBean(genericType != null ? genericType : argument, qualifier);
         path.pop();
         return bean;
      } catch (NoSuchBeanException var10) {
         throw new DependencyInjectionException(resolutionContext, injectionPoint, argument, var10);
      }
   }

   private Argument resolveGenericType(Argument argument, Class argumentType) {
      return argument.isArray() ? Argument.of(argumentType.getComponentType()) : (Argument)argument.getFirstTypeVariable().orElse(null);
   }

   private <B> B resolveBeanWithGenericsFromConstructorArgument(
      BeanResolutionContext resolutionContext, Argument argument, AbstractBeanDefinition.BeanResolver<B> beanResolver
   ) {
      BeanResolutionContext.Path path = resolutionContext.getPath();
      path.pushConstructorResolve(this, argument);

      try {
         Class argumentType = argument.getType();
         Argument genericType = this.resolveGenericType(argument, argumentType);
         Qualifier qualifier = this.resolveQualifier(resolutionContext, argument);
         B bean = beanResolver.resolveBean(genericType != null ? genericType : argument, qualifier);
         path.pop();
         return bean;
      } catch (NoSuchBeanException var9) {
         if (argument.isNullable()) {
            path.pop();
            return null;
         } else {
            throw new DependencyInjectionException(resolutionContext, argument, var9);
         }
      }
   }

   private <B> Collection<BeanRegistration<B>> resolveBeanRegistrationsWithGenericsFromArgument(
      BeanResolutionContext resolutionContext,
      Argument<?> argument,
      BeanResolutionContext.Path path,
      BiFunction<Argument<B>, Qualifier<B>, Collection<BeanRegistration<B>>> beanResolver
   ) {
      try {
         Supplier<DependencyInjectionException> errorSupplier = () -> new DependencyInjectionException(
               resolutionContext, argument, "Cannot resolve bean registrations. Argument [" + argument + "] missing generic type information."
            );
         Argument<?> genericType = (Argument)argument.getFirstTypeVariable().orElseThrow(errorSupplier);
         Argument beanType = argument.isArray() ? genericType : (Argument)genericType.getFirstTypeVariable().orElseThrow(errorSupplier);
         Qualifier qualifier = this.resolveQualifier(resolutionContext, argument);
         Collection result = (Collection)beanResolver.apply(beanType, qualifier);
         path.pop();
         return result;
      } catch (NoSuchBeanException var10) {
         if (argument.isNullable()) {
            path.pop();
            return null;
         } else {
            throw new DependencyInjectionException(resolutionContext, argument, var10);
         }
      }
   }

   private Argument<?> resolveArgument(BeanContext context, int argIndex, Argument<?>[] arguments) {
      Argument<?> argument = arguments[argIndex];
      if (argument instanceof DefaultArgument && argument.getAnnotationMetadata().hasPropertyExpressions()) {
         argument = new EnvironmentAwareArgument<>((DefaultArgument<?>)argument);
         this.instrumentAnnotationMetadata(context, argument);
      }

      return argument;
   }

   private <B> BeanRegistration<B> resolveBeanRegistrationWithGenericsFromArgument(
      BeanResolutionContext resolutionContext,
      Argument<?> argument,
      BeanResolutionContext.Path path,
      BiFunction<Argument<B>, Qualifier<B>, BeanRegistration<B>> beanResolver
   ) {
      try {
         Supplier<DependencyInjectionException> errorSupplier = () -> new DependencyInjectionException(
               resolutionContext, argument, "Cannot resolve bean registration. Argument [" + argument + "] missing generic type information."
            );
         Argument genericType = (Argument)argument.getFirstTypeVariable().orElseThrow(errorSupplier);
         Qualifier qualifier = this.resolveQualifier(resolutionContext, argument);
         BeanRegistration result = (BeanRegistration)beanResolver.apply(genericType, qualifier);
         path.pop();
         return result;
      } catch (NoSuchBeanException var9) {
         if (argument.isNullable()) {
            path.pop();
            return null;
         } else {
            throw new DependencyInjectionException(resolutionContext, argument, var9);
         }
      }
   }

   private Object doResolveBeanRegistrations(
      BeanResolutionContext resolutionContext, DefaultBeanContext context, Argument<?> argument, BeanResolutionContext.Path path
   ) {
      Collection<BeanRegistration<Object>> beanRegistrations = this.resolveBeanRegistrationsWithGenericsFromArgument(
         resolutionContext, argument, path, (beanType, qualifier) -> context.getBeanRegistrations(resolutionContext, beanType, qualifier)
      );
      if (CollectionUtils.isNotEmpty(beanRegistrations)) {
         return argument.isArray()
            ? beanRegistrations.toArray(new BeanRegistration[beanRegistrations.size()])
            : this.coerceCollectionToCorrectType(argument.getType(), beanRegistrations);
      } else {
         return argument.isArray() ? Array.newInstance(argument.getType(), 0) : this.coerceCollectionToCorrectType(argument.getType(), Collections.emptySet());
      }
   }

   private <B> B resolveBeanWithGenericsForField(
      BeanResolutionContext resolutionContext, FieldInjectionPoint injectionPoint, AbstractBeanDefinition.BeanResolver<B> beanResolver
   ) {
      BeanResolutionContext.Path path = resolutionContext.getPath();
      path.pushFieldResolve(this, injectionPoint);
      Argument argument = injectionPoint.asArgument();

      try {
         Argument genericType = argument.isArray()
            ? Argument.of(argument.getType().getComponentType())
            : (Argument)argument.getFirstTypeVariable().orElse(argument);
         Qualifier qualifier = this.resolveQualifier(resolutionContext, argument);
         B bean = beanResolver.resolveBean(genericType, qualifier);
         path.pop();
         return bean;
      } catch (NoSuchBeanException var9) {
         if (argument.isNullable()) {
            path.pop();
            return null;
         } else {
            throw new DependencyInjectionException(resolutionContext, injectionPoint, var9);
         }
      }
   }

   private boolean isConfigurationProperties() {
      return this.isConfigurationProperties;
   }

   private Qualifier resolveQualifier(BeanResolutionContext resolutionContext, Argument argument) {
      return this.resolveQualifier(resolutionContext, argument, false);
   }

   private Qualifier resolveQualifier(BeanResolutionContext resolutionContext, Argument argument, boolean innerConfiguration) {
      Qualifier<Object> argumentQualifier = Qualifiers.forArgument(argument);
      if (argumentQualifier != null) {
         return argumentQualifier;
      } else {
         AnnotationMetadata annotationMetadata = argument.getAnnotationMetadata();
         boolean hasMetadata = annotationMetadata != AnnotationMetadata.EMPTY_METADATA;
         if (hasMetadata && annotationMetadata.hasAnnotation("io.micronaut.inject.qualifiers.InterceptorBindingQualifier")) {
            return Qualifiers.byInterceptorBinding(annotationMetadata);
         } else {
            Class<?>[] byType = hasMetadata ? (annotationMetadata.hasDeclaredAnnotation(Type.class) ? annotationMetadata.classValues(Type.class) : null) : null;
            if (byType != null) {
               return Qualifiers.byType(byType);
            } else {
               Qualifier qualifier = null;
               boolean isIterable = this.isIterable()
                  || resolutionContext.get(EachProperty.class.getName(), Class.class).map(this.getBeanType()::equals).orElse(false);
               if (isIterable) {
                  Optional<Qualifier> optional = resolutionContext.get("javax.inject.Qualifier", Map.class).map(map -> (Qualifier)map.get(argument));
                  qualifier = (Qualifier)optional.orElse(null);
               }

               if (qualifier == null
                  && (hasMetadata && argument.isAnnotationPresent(Parameter.class) || innerConfiguration && isIterable || Qualifier.class == argument.getType())
                  )
                {
                  Qualifier<?> currentQualifier = resolutionContext.getCurrentQualifier();
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
         }
      }
   }

   private Object resolveOptionalObject(Optional value) {
      if (!value.isPresent()) {
         return value;
      } else {
         Object convertedOptional = value.get();
         return convertedOptional instanceof Optional ? convertedOptional : value;
      }
   }

   private Object coerceCollectionToCorrectType(Class collectionType, Collection beansOfType) {
      return collectionType.isInstance(beansOfType) ? beansOfType : CollectionUtils.convertCollection(collectionType, beansOfType).orElse(null);
   }

   private void addRequiredComponents(Argument... arguments) {
      if (arguments != null) {
         for(Argument argument : arguments) {
            if (!argument.isContainerType() && !argument.isProvider()) {
               this.requiredComponents.add(argument.getType());
            } else {
               argument.getFirstTypeVariable().map(TypeInformation::getType).ifPresent(this.requiredComponents::add);
            }
         }
      }

   }

   private void instrumentAnnotationMetadata(BeanContext context, Object object) {
      if (object instanceof EnvironmentConfigurable && context instanceof ApplicationContext) {
         EnvironmentConfigurable ec = (EnvironmentConfigurable)object;
         if (ec.hasPropertyExpressions()) {
            ec.configure(((ApplicationContext)context).getEnvironment());
         }
      }

   }

   private final class BeanAnnotationMetadata extends AbstractEnvironmentAnnotationMetadata {
      BeanAnnotationMetadata(AnnotationMetadata targetMetadata) {
         super(targetMetadata);
      }

      @Nullable
      @Override
      protected Environment getEnvironment() {
         return AbstractBeanDefinition.this.environment;
      }
   }

   private interface BeanResolver<T> {
      T resolveBean(Argument<T> beanType, Qualifier<T> qualifier);
   }

   private final class MethodKey {
      final String name;
      final Class[] argumentTypes;

      MethodKey(String name, Class[] argumentTypes) {
         this.name = name;
         this.argumentTypes = argumentTypes;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            AbstractBeanDefinition<T>.MethodKey methodKey = (AbstractBeanDefinition.MethodKey)o;
            return !this.name.equals(methodKey.name) ? false : Arrays.equals(this.argumentTypes, methodKey.argumentTypes);
         } else {
            return false;
         }
      }

      public int hashCode() {
         int result = this.name.hashCode();
         return 31 * result + Arrays.hashCode(this.argumentTypes);
      }
   }
}
