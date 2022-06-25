package io.micronaut.context;

import io.micronaut.context.annotation.ConfigurationReader;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.context.annotation.Executable;
import io.micronaut.context.annotation.Infrastructure;
import io.micronaut.context.annotation.Parallel;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.context.env.PropertyPlaceholderResolver;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.context.event.BeanDestroyedEvent;
import io.micronaut.context.event.BeanDestroyedEventListener;
import io.micronaut.context.event.BeanInitializedEventListener;
import io.micronaut.context.event.BeanPreDestroyEvent;
import io.micronaut.context.event.BeanPreDestroyEventListener;
import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.context.exceptions.BeanContextException;
import io.micronaut.context.exceptions.BeanCreationException;
import io.micronaut.context.exceptions.BeanDestructionException;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.context.exceptions.DependencyInjectionException;
import io.micronaut.context.exceptions.DisabledBeanException;
import io.micronaut.context.exceptions.NoSuchBeanException;
import io.micronaut.context.exceptions.NonUniqueBeanException;
import io.micronaut.context.processor.AnnotationProcessor;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.context.scope.BeanCreationContext;
import io.micronaut.context.scope.CreatedBean;
import io.micronaut.context.scope.CustomScope;
import io.micronaut.context.scope.CustomScopeRegistry;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.AnnotationMetadataResolver;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Indexes;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.Order;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.TypeConverter;
import io.micronaut.core.convert.TypeConverterRegistrar;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.core.io.service.SoftServiceLoader;
import io.micronaut.core.naming.Named;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.core.order.Ordered;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.type.TypeInformation;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StreamUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.clhm.ConcurrentLinkedHashMap;
import io.micronaut.core.value.PropertyResolver;
import io.micronaut.core.value.ValueResolver;
import io.micronaut.inject.AdvisedBeanType;
import io.micronaut.inject.BeanConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanDefinitionMethodReference;
import io.micronaut.inject.BeanDefinitionReference;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.BeanIdentifier;
import io.micronaut.inject.BeanType;
import io.micronaut.inject.ConstructorInjectionPoint;
import io.micronaut.inject.DisposableBeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.InitializingBeanDefinition;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.MethodExecutionHandle;
import io.micronaut.inject.ParametrizedBeanFactory;
import io.micronaut.inject.ProxyBeanDefinition;
import io.micronaut.inject.ValidatedBeanDefinition;
import io.micronaut.inject.proxy.InterceptedBeanProxy;
import io.micronaut.inject.qualifiers.AnyQualifier;
import io.micronaut.inject.qualifiers.Qualified;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.inject.validation.BeanDefinitionValidator;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultBeanContext implements InitializableBeanContext {
   protected static final Logger LOG = LoggerFactory.getLogger(DefaultBeanContext.class);
   protected static final Logger LOG_LIFECYCLE = LoggerFactory.getLogger(DefaultBeanContext.class.getPackage().getName() + ".lifecycle");
   private static final Qualifier PROXY_TARGET_QUALIFIER = new Qualifier<Object>() {
      @Override
      public <BT extends BeanType<Object>> Stream<BT> reduce(Class<Object> beanType, Stream<BT> candidates) {
         return candidates.filter(bt -> {
            if (bt instanceof BeanDefinitionDelegate) {
               return !(((BeanDefinitionDelegate)bt).getDelegate() instanceof ProxyBeanDefinition);
            } else {
               return !(bt instanceof ProxyBeanDefinition);
            }
         });
      }
   };
   private static final String SCOPED_PROXY_ANN = "io.micronaut.runtime.context.scope.ScopedProxy";
   private static final String INTRODUCTION_TYPE = "io.micronaut.aop.Introduction";
   private static final String ADAPTER_TYPE = "io.micronaut.aop.Adapter";
   private static final String NAMED_MEMBER = "named";
   private static final String QUALIFIER_MEMBER = "qualifier";
   private static final String PARALLEL_TYPE = Parallel.class.getName();
   private static final String INDEXES_TYPE = Indexes.class.getName();
   private static final String REPLACES_ANN = Replaces.class.getName();
   private static final Comparator<BeanRegistration<?>> BEAN_REGISTRATION_COMPARATOR = (o1, o2) -> {
      int order1 = OrderUtil.getOrder(o1.getBeanDefinition(), o1.getBean());
      int order2 = OrderUtil.getOrder(o2.getBeanDefinition(), o2.getBean());
      return Integer.compare(order1, order2);
   };
   protected final AtomicBoolean running = new AtomicBoolean(false);
   protected final AtomicBoolean initializing = new AtomicBoolean(false);
   protected final AtomicBoolean terminating = new AtomicBoolean(false);
   final Map<BeanIdentifier, BeanRegistration<?>> singlesInCreation = new ConcurrentHashMap(5);
   Set<Entry<Class<?>, List<BeanInitializedEventListener>>> beanInitializedEventListeners;
   private final SingletonScope singletonScope = new SingletonScope();
   private final BeanContextConfiguration beanContextConfiguration;
   private final Collection<BeanDefinitionReference> beanDefinitionsClasses = new ConcurrentLinkedQueue();
   private final Map<String, BeanConfiguration> beanConfigurations = new HashMap(10);
   private final Map<DefaultBeanContext.BeanKey, Boolean> containsBeanCache = new ConcurrentHashMap(30);
   private final Map<CharSequence, Object> attributes = Collections.synchronizedMap(new HashMap(5));
   private final Map<DefaultBeanContext.BeanKey, DefaultBeanContext.CollectionHolder> singletonBeanRegistrations = new ConcurrentHashMap(50);
   private final Map<DefaultBeanContext.BeanCandidateKey, Optional<BeanDefinition>> beanConcreteCandidateCache = new ConcurrentLinkedHashMap.Builder()
      .maximumWeightedCapacity(30L)
      .build();
   private final Map<Argument, Collection<BeanDefinition>> beanCandidateCache = new ConcurrentLinkedHashMap.Builder().maximumWeightedCapacity(30L).build();
   private final Map<Class, Collection<BeanDefinitionReference>> beanIndex = new ConcurrentHashMap(12);
   private final ClassLoader classLoader;
   private final Set<Class> thisInterfaces = CollectionUtils.setOf(
      BeanDefinitionRegistry.class,
      BeanContext.class,
      AnnotationMetadataResolver.class,
      BeanLocator.class,
      ExecutionHandleLocator.class,
      ApplicationContext.class,
      PropertyResolver.class,
      ValueResolver.class,
      PropertyPlaceholderResolver.class
   );
   private final Set<Class> indexedTypes = CollectionUtils.setOf(
      ResourceLoader.class,
      TypeConverter.class,
      TypeConverterRegistrar.class,
      ApplicationEventListener.class,
      BeanCreatedEventListener.class,
      BeanInitializedEventListener.class
   );
   private final CustomScopeRegistry customScopeRegistry;
   private final String[] eagerInitStereotypes;
   private final boolean eagerInitStereotypesPresent;
   private final boolean eagerInitSingletons;
   private BeanDefinitionValidator beanValidator;
   private List<BeanDefinitionReference> beanDefinitionReferences;
   private List<BeanConfiguration> beanConfigurationsList;
   private Set<Entry<Class<?>, List<BeanCreatedEventListener<?>>>> beanCreationEventListeners;
   private Set<Entry<Class<?>, List<BeanPreDestroyEventListener>>> beanPreDestroyEventListeners;
   private Set<Entry<Class<?>, List<BeanDestroyedEventListener>>> beanDestroyedEventListeners;

   public DefaultBeanContext() {
      this(BeanContext.class.getClassLoader());
   }

   public DefaultBeanContext(@NonNull ClassLoader classLoader) {
      this(new BeanContextConfiguration() {
         @NonNull
         @Override
         public ClassLoader getClassLoader() {
            ArgumentUtils.requireNonNull("classLoader", classLoader);
            return classLoader;
         }
      });
   }

   public DefaultBeanContext(@NonNull ClassPathResourceLoader resourceLoader) {
      this(new BeanContextConfiguration() {
         @NonNull
         @Override
         public ClassLoader getClassLoader() {
            ArgumentUtils.requireNonNull("resourceLoader", resourceLoader);
            return resourceLoader.getClassLoader();
         }
      });
   }

   public DefaultBeanContext(@NonNull BeanContextConfiguration contextConfiguration) {
      ArgumentUtils.requireNonNull("contextConfiguration", contextConfiguration);
      System.setProperty("micronaut.classloader.logging", "true");
      this.classLoader = contextConfiguration.getClassLoader();
      this.customScopeRegistry = (CustomScopeRegistry)Objects.requireNonNull(this.createCustomScopeRegistry(), "Scope registry cannot be null");
      Set<Class<? extends Annotation>> eagerInitAnnotated = contextConfiguration.getEagerInitAnnotated();
      List<String> eagerInitStereotypes = new ArrayList(eagerInitAnnotated.size());

      for(Class<? extends Annotation> ann : eagerInitAnnotated) {
         eagerInitStereotypes.add(ann.getName());
      }

      this.eagerInitStereotypes = (String[])eagerInitStereotypes.toArray(new String[0]);
      this.eagerInitStereotypesPresent = !eagerInitStereotypes.isEmpty();
      this.eagerInitSingletons = this.eagerInitStereotypesPresent
         && (eagerInitStereotypes.contains("javax.inject.Singleton") || eagerInitStereotypes.contains(Singleton.class.getName()));
      this.beanContextConfiguration = contextConfiguration;
   }

   @NonNull
   protected CustomScopeRegistry createCustomScopeRegistry() {
      return new DefaultCustomScopeRegistry(this);
   }

   @Internal
   @NonNull
   CustomScopeRegistry getCustomScopeRegistry() {
      return this.customScopeRegistry;
   }

   @Override
   public boolean isRunning() {
      return this.running.get() && !this.initializing.get();
   }

   public synchronized BeanContext start() {
      if (!this.isRunning()) {
         if (this.initializing.compareAndSet(false, true)) {
            if (LOG.isDebugEnabled()) {
               LOG.debug("Starting BeanContext");
            }

            this.finalizeConfiguration();
            if (LOG.isDebugEnabled()) {
               String activeConfigurations = (String)this.beanConfigurations
                  .values()
                  .stream()
                  .filter(config -> config.isEnabled(this))
                  .map(BeanConfiguration::getName)
                  .collect(Collectors.joining(","));
               if (StringUtils.isNotEmpty(activeConfigurations)) {
                  LOG.debug("Loaded active configurations: {}", activeConfigurations);
               }
            }

            if (LOG.isDebugEnabled()) {
               LOG.debug("BeanContext Started.");
            }

            this.publishEvent(new StartupEvent(this));
         }

         this.running.set(true);
         this.initializing.set(false);
      }

      return this;
   }

   public synchronized BeanContext stop() {
      if (this.terminating.compareAndSet(false, true)) {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Stopping BeanContext");
         }

         this.publishEvent(new ShutdownEvent(this));
         this.attributes.clear();
         List<BeanRegistration> objects = this.topologicalSort(this.singletonScope.getBeanRegistrations());
         Map<Boolean, List<BeanRegistration>> result = (Map)objects.stream()
            .collect(
               Collectors.groupingBy(br -> br.bean != null && (br.bean instanceof BeanPreDestroyEventListener || br.bean instanceof BeanDestroyedEventListener))
            );
         List<BeanRegistration> listeners = (List)result.get(true);
         if (listeners != null) {
            objects.clear();
            objects.addAll((Collection)result.get(false));
            objects.addAll(listeners);
         }

         Set<Integer> processed = new HashSet();

         for(BeanRegistration beanRegistration : objects) {
            Object bean = beanRegistration.bean;
            int sysId = System.identityHashCode(bean);
            if (!processed.contains(sysId)) {
               if (LOG_LIFECYCLE.isDebugEnabled()) {
                  LOG_LIFECYCLE.debug("Destroying bean [{}] with identifier [{}]", bean, beanRegistration.identifier);
               }

               processed.add(sysId);

               try {
                  this.destroyBean(beanRegistration);
               } catch (BeanDestructionException var10) {
                  if (LOG.isErrorEnabled()) {
                     LOG.error(var10.getMessage(), var10);
                  }
               }
            }
         }

         this.singletonBeanRegistrations.clear();
         this.beanConcreteCandidateCache.clear();
         this.beanCandidateCache.clear();
         this.containsBeanCache.clear();
         this.beanConfigurations.clear();
         this.singletonScope.clear();
         this.beanInitializedEventListeners = null;
         this.beanCreationEventListeners = null;
         this.beanPreDestroyEventListeners = null;
         this.beanDestroyedEventListeners = null;
         this.terminating.set(false);
         this.running.set(false);
      }

      return this;
   }

   @NonNull
   @Override
   public AnnotationMetadata resolveMetadata(Class<?> type) {
      return type == null
         ? AnnotationMetadata.EMPTY_METADATA
         : (AnnotationMetadata)this.findBeanDefinition(Argument.of(type), null, false)
            .map(AnnotationMetadataProvider::getAnnotationMetadata)
            .orElse(AnnotationMetadata.EMPTY_METADATA);
   }

   @Override
   public <T> Optional<T> refreshBean(@Nullable BeanIdentifier identifier) {
      if (identifier == null) {
         return Optional.empty();
      } else {
         BeanRegistration<T> beanRegistration = this.singletonScope.findBeanRegistration(identifier);
         if (beanRegistration != null) {
            this.refreshBean(beanRegistration);
            return Optional.of(beanRegistration.bean);
         } else {
            return Optional.empty();
         }
      }
   }

   @Override
   public <T> void refreshBean(@NonNull BeanRegistration<T> beanRegistration) {
      Objects.requireNonNull(beanRegistration, "BeanRegistration cannot be null");
      if (beanRegistration.bean != null) {
         beanRegistration.definition().inject(this, beanRegistration.bean);
      }

   }

   @Override
   public Collection<BeanRegistration<?>> getActiveBeanRegistrations(Qualifier<?> qualifier) {
      return (Collection<BeanRegistration<?>>)(qualifier == null ? Collections.emptyList() : this.singletonScope.getBeanRegistrations(qualifier));
   }

   @Override
   public <T> Collection<BeanRegistration<T>> getActiveBeanRegistrations(Class<T> beanType) {
      return (Collection<BeanRegistration<T>>)(beanType == null ? Collections.emptyList() : this.singletonScope.getBeanRegistrations(beanType));
   }

   @Override
   public <T> Collection<BeanRegistration<T>> getBeanRegistrations(Class<T> beanType) {
      return (Collection<BeanRegistration<T>>)(beanType == null ? Collections.emptyList() : this.getBeanRegistrations(null, Argument.of(beanType), null));
   }

   @Override
   public <T> BeanRegistration<T> getBeanRegistration(Class<T> beanType, Qualifier<T> qualifier) {
      return this.getBeanRegistration(null, Argument.of(beanType), qualifier);
   }

   @Override
   public <T> Collection<BeanRegistration<T>> getBeanRegistrations(Class<T> beanType, Qualifier<T> qualifier) {
      return (Collection<BeanRegistration<T>>)(beanType == null ? Collections.emptyList() : this.getBeanRegistrations(null, Argument.of(beanType), null));
   }

   @Override
   public <T> Collection<BeanRegistration<T>> getBeanRegistrations(Argument<T> beanType, Qualifier<T> qualifier) {
      return this.getBeanRegistrations(null, (Argument<T>)Objects.requireNonNull(beanType, "Bean type cannot be null"), qualifier);
   }

   @Override
   public <T> BeanRegistration<T> getBeanRegistration(Argument<T> beanType, Qualifier<T> qualifier) {
      return this.getBeanRegistration(null, (Argument<T>)Objects.requireNonNull(beanType, "Bean type cannot be null"), qualifier);
   }

   @Override
   public <T> BeanRegistration<T> getBeanRegistration(BeanDefinition<T> beanDefinition) {
      return this.resolveBeanRegistration(null, beanDefinition);
   }

   @Override
   public <T> Optional<BeanRegistration<T>> findBeanRegistration(T bean) {
      if (bean == null) {
         return Optional.empty();
      } else {
         BeanRegistration<T> beanRegistration = this.singletonScope.findBeanRegistration(bean);
         return beanRegistration != null ? Optional.of(beanRegistration) : this.customScopeRegistry.findBeanRegistration(bean);
      }
   }

   @Override
   public <T, R> Optional<MethodExecutionHandle<T, R>> findExecutionHandle(Class<T> beanType, String method, Class... arguments) {
      return this.findExecutionHandle(beanType, null, method, arguments);
   }

   @Override
   public MethodExecutionHandle<?, Object> createExecutionHandle(BeanDefinition<? extends Object> beanDefinition, ExecutableMethod<Object, ?> method) {
      return new MethodExecutionHandle<Object, Object>() {
         private Object target;

         @NonNull
         @Override
         public AnnotationMetadata getAnnotationMetadata() {
            return method.getAnnotationMetadata();
         }

         @Override
         public Object getTarget() {
            Object target = this.target;
            if (target == null) {
               synchronized(this) {
                  target = this.target;
                  if (target == null) {
                     target = DefaultBeanContext.this.getBean(beanDefinition);
                     this.target = target;
                  }
               }
            }

            return target;
         }

         @Override
         public Class getDeclaringType() {
            return beanDefinition.getBeanType();
         }

         @Override
         public String getMethodName() {
            return method.getMethodName();
         }

         @Override
         public Argument[] getArguments() {
            return method.getArguments();
         }

         @Override
         public Method getTargetMethod() {
            return method.getTargetMethod();
         }

         @Override
         public ReturnType getReturnType() {
            return method.getReturnType();
         }

         @Override
         public Object invoke(Object... arguments) {
            return method.invoke(this.getTarget(), arguments);
         }

         @NonNull
         @Override
         public ExecutableMethod<?, Object> getExecutableMethod() {
            return method;
         }
      };
   }

   @Override
   public <T, R> Optional<MethodExecutionHandle<T, R>> findExecutionHandle(Class<T> beanType, Qualifier<?> qualifier, String method, Class... arguments) {
      Optional<? extends BeanDefinition<?>> foundBean = this.findBeanDefinition(beanType, qualifier);
      if (foundBean.isPresent()) {
         BeanDefinition<?> beanDefinition = (BeanDefinition)foundBean.get();
         Optional<? extends ExecutableMethod<?, Object>> foundMethod = beanDefinition.findMethod(method, arguments);
         return foundMethod.isPresent()
            ? foundMethod.map(executableMethod -> new DefaultBeanContext.BeanExecutionHandle<>(this, beanType, qualifier, executableMethod))
            : beanDefinition.findPossibleMethods(method).findFirst().filter(m -> {
               Class[] argTypes = m.getArgumentTypes();
               if (argTypes.length == arguments.length) {
                  for(int i = 0; i < argTypes.length; ++i) {
                     if (!arguments[i].isAssignableFrom(argTypes[i])) {
                        return false;
                     }
                  }
   
                  return true;
               } else {
                  return false;
               }
            }).map(executableMethod -> new DefaultBeanContext.BeanExecutionHandle<>(this, beanType, qualifier, executableMethod));
      } else {
         return Optional.empty();
      }
   }

   @Override
   public <T, R> Optional<ExecutableMethod<T, R>> findExecutableMethod(Class<T> beanType, String method, Class[] arguments) {
      if (beanType != null) {
         Collection<BeanDefinition<T>> definitions = this.getBeanDefinitions(beanType);
         if (!definitions.isEmpty()) {
            BeanDefinition<T> beanDefinition = (BeanDefinition)definitions.iterator().next();
            Optional<ExecutableMethod<T, R>> foundMethod = beanDefinition.findMethod(method, arguments);
            if (foundMethod.isPresent()) {
               return foundMethod;
            }

            return beanDefinition.findPossibleMethods(method).findFirst();
         }
      }

      return Optional.empty();
   }

   @Override
   public <T, R> Optional<MethodExecutionHandle<T, R>> findExecutionHandle(T bean, String method, Class[] arguments) {
      if (bean != null) {
         Optional<? extends BeanDefinition<?>> foundBean = this.findBeanDefinition(bean.getClass());
         if (foundBean.isPresent()) {
            BeanDefinition<?> beanDefinition = (BeanDefinition)foundBean.get();
            Optional<? extends ExecutableMethod<?, Object>> foundMethod = beanDefinition.findMethod(method, arguments);
            if (foundMethod.isPresent()) {
               return foundMethod.map(executableMethod -> new DefaultBeanContext.ObjectExecutionHandle(bean, executableMethod));
            }

            return beanDefinition.findPossibleMethods(method)
               .findFirst()
               .map(executableMethod -> new DefaultBeanContext.ObjectExecutionHandle(bean, executableMethod));
         }
      }

      return Optional.empty();
   }

   @Override
   public <T> BeanContext registerSingleton(@NonNull Class<T> type, @NonNull T singleton, Qualifier<T> qualifier, boolean inject) {
      this.purgeCacheForBeanInstance(singleton);
      BeanDefinition<T> beanDefinition;
      if (inject && this.running.get()) {
         beanDefinition = (BeanDefinition)this.findBeanDefinition(type, qualifier).orElse(null);
         if (beanDefinition == null) {
            this.beanCandidateCache.entrySet().removeIf(entry -> ((Argument)entry.getKey()).isInstance(singleton));
            this.beanConcreteCandidateCache.entrySet().removeIf(entry -> ((DefaultBeanContext.BeanCandidateKey)entry.getKey()).beanType.isInstance(singleton));
         }
      } else {
         beanDefinition = null;
      }

      if (beanDefinition != null && beanDefinition.getBeanType().isInstance(singleton)) {
         try (BeanResolutionContext context = this.newResolutionContext(beanDefinition, null)) {
            this.doInject(context, singleton, beanDefinition);
            DefaultBeanContext.BeanKey<T> key = new DefaultBeanContext.BeanKey<>(beanDefinition.asArgument(), qualifier);
            this.singletonScope.registerSingletonBean(BeanRegistration.of(this, key, beanDefinition, singleton), qualifier);
         }
      } else {
         NoInjectionBeanDefinition<T> dynamicRegistration = new NoInjectionBeanDefinition<>(singleton.getClass(), qualifier);
         if (qualifier instanceof Named) {
            BeanDefinitionDelegate<T> delegate = BeanDefinitionDelegate.create(dynamicRegistration);
            delegate.put(BeanDefinition.NAMED_ATTRIBUTE, ((Named)qualifier).getName());
            beanDefinition = delegate;
         } else {
            beanDefinition = dynamicRegistration;
         }

         this.beanDefinitionsClasses.add(dynamicRegistration);
         DefaultBeanContext.BeanKey<T> key = new DefaultBeanContext.BeanKey<>(beanDefinition.asArgument(), qualifier);
         this.singletonScope.registerSingletonBean(BeanRegistration.of(this, key, dynamicRegistration, singleton), qualifier);

         for(Class indexedType : this.indexedTypes) {
            if (indexedType == type || indexedType.isAssignableFrom(type)) {
               Collection<BeanDefinitionReference> indexed = this.resolveTypeIndex(indexedType);
               final BeanDefinition<T> finalBeanDefinition = beanDefinition;
               indexed.add(new AbstractBeanDefinitionReference(type.getName(), type.getName()) {
                  @Override
                  protected Class<? extends BeanDefinition<?>> getBeanDefinitionType() {
                     return finalBeanDefinition.getClass();
                  }

                  @Override
                  public BeanDefinition load() {
                     return finalBeanDefinition;
                  }

                  @Override
                  public Class getBeanType() {
                     return type;
                  }
               });
               break;
            }
         }
      }

      return this;
   }

   private <T> void purgeCacheForBeanInstance(T singleton) {
      this.beanCandidateCache.entrySet().removeIf(entry -> ((Argument)entry.getKey()).isInstance(singleton));
      this.beanConcreteCandidateCache.entrySet().removeIf(entry -> ((DefaultBeanContext.BeanCandidateKey)entry.getKey()).beanType.isInstance(singleton));
      this.singletonBeanRegistrations.entrySet().removeIf(entry -> ((DefaultBeanContext.BeanKey)entry.getKey()).beanType.isInstance(singleton));
      this.containsBeanCache.entrySet().removeIf(entry -> ((DefaultBeanContext.BeanKey)entry.getKey()).beanType.isInstance(singleton));
   }

   @NonNull
   final BeanResolutionContext newResolutionContext(BeanDefinition<?> beanDefinition, @Nullable BeanResolutionContext currentContext) {
      return (BeanResolutionContext)(currentContext == null ? new DefaultBeanContext.SingletonBeanResolutionContext(beanDefinition) : currentContext);
   }

   @Override
   public ClassLoader getClassLoader() {
      return this.classLoader;
   }

   @Override
   public BeanDefinitionValidator getBeanValidator() {
      if (this.beanValidator == null) {
         this.beanValidator = (BeanDefinitionValidator)this.findBean(BeanDefinitionValidator.class).orElse(BeanDefinitionValidator.DEFAULT);
      }

      return this.beanValidator;
   }

   @Override
   public Optional<BeanConfiguration> findBeanConfiguration(String configurationName) {
      BeanConfiguration configuration = (BeanConfiguration)this.beanConfigurations.get(configurationName);
      return configuration != null ? Optional.of(configuration) : Optional.empty();
   }

   @Override
   public <T> BeanDefinition<T> getBeanDefinition(Argument<T> beanType, Qualifier<T> qualifier) {
      return (BeanDefinition<T>)this.findBeanDefinition(beanType, qualifier).orElseThrow(() -> new NoSuchBeanException(beanType, qualifier));
   }

   @Override
   public <T> Optional<BeanDefinition<T>> findBeanDefinition(Argument<T> beanType, Qualifier<T> qualifier) {
      BeanDefinition<T> beanDefinition = this.singletonScope.findCachedSingletonBeanDefinition(beanType, qualifier);
      return beanDefinition != null ? Optional.of(beanDefinition) : this.findConcreteCandidate(null, beanType, qualifier, true);
   }

   private <T> Optional<BeanDefinition<T>> findBeanDefinition(Argument<T> beanType, Qualifier<T> qualifier, boolean throwNonUnique) {
      return this.findConcreteCandidate(null, beanType, qualifier, throwNonUnique);
   }

   @Override
   public <T> Optional<BeanDefinition<T>> findBeanDefinition(Class<T> beanType, Qualifier<T> qualifier) {
      return this.findBeanDefinition(Argument.of(beanType), qualifier);
   }

   @Override
   public <T> Collection<BeanDefinition<T>> getBeanDefinitions(Class<T> beanType) {
      return this.getBeanDefinitions(Argument.of(beanType));
   }

   @Override
   public <T> Collection<BeanDefinition<T>> getBeanDefinitions(Argument<T> beanType) {
      Objects.requireNonNull(beanType, "Bean type cannot be null");
      Collection<BeanDefinition<T>> candidates = this.findBeanCandidatesInternal(null, beanType);
      return Collections.unmodifiableCollection(candidates);
   }

   @Override
   public <T> Collection<BeanDefinition<T>> getBeanDefinitions(Class<T> beanType, Qualifier<T> qualifier) {
      Objects.requireNonNull(beanType, "Bean type cannot be null");
      return this.getBeanDefinitions(Argument.of(beanType), qualifier);
   }

   @Override
   public <T> Collection<BeanDefinition<T>> getBeanDefinitions(Argument<T> beanType, Qualifier<T> qualifier) {
      Objects.requireNonNull(beanType, "Bean type cannot be null");
      Collection<BeanDefinition<T>> candidates = this.findBeanCandidatesInternal(null, beanType);
      if (qualifier != null) {
         candidates = (Collection)qualifier.reduce(beanType.getType(), new ArrayList(candidates).stream()).collect(Collectors.toList());
      }

      return Collections.unmodifiableCollection(candidates);
   }

   @Override
   public <T> boolean containsBean(@NonNull Class<T> beanType, Qualifier<T> qualifier) {
      return this.containsBean(Argument.of(beanType), qualifier);
   }

   @Override
   public <T> boolean containsBean(Argument<T> beanType, Qualifier<T> qualifier) {
      ArgumentUtils.requireNonNull("beanType", beanType);
      DefaultBeanContext.BeanKey<T> beanKey = new DefaultBeanContext.BeanKey<>(beanType, qualifier);
      if (this.containsBeanCache.containsKey(beanKey)) {
         return this.containsBeanCache.get(beanKey);
      } else {
         boolean result = this.singletonScope.containsBean(beanType, qualifier) || this.isCandidatePresent(beanKey.beanType, qualifier);
         this.containsBeanCache.put(beanKey, result);
         return result;
      }
   }

   @NonNull
   @Override
   public <T> T getBean(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier) {
      Objects.requireNonNull(beanType, "Bean type cannot be null");
      return this.getBean(Argument.of(beanType), qualifier);
   }

   @NonNull
   @Override
   public <T> T getBean(@NonNull Class<T> beanType) {
      Objects.requireNonNull(beanType, "Bean type cannot be null");
      return this.getBean(Argument.of(beanType), null);
   }

   @NonNull
   @Override
   public <T> T getBean(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      Objects.requireNonNull(beanType, "Bean type cannot be null");

      try {
         return this.getBean(null, beanType, qualifier);
      } catch (DisabledBeanException var4) {
         if (AbstractBeanContextConditional.LOG.isDebugEnabled()) {
            AbstractBeanContextConditional.LOG.debug("Bean of type [{}] disabled for reason: {}", beanType.getSimpleName(), var4.getMessage());
         }

         throw new NoSuchBeanException(beanType, qualifier);
      }
   }

   @Override
   public <T> Optional<T> findBean(Class<T> beanType, Qualifier<T> qualifier) {
      return this.findBean(null, beanType, qualifier);
   }

   @Override
   public <T> Optional<T> findBean(Argument<T> beanType, Qualifier<T> qualifier) {
      return this.findBean(null, beanType, qualifier);
   }

   @Override
   public <T> Collection<T> getBeansOfType(Class<T> beanType) {
      return this.getBeansOfType(null, Argument.of(beanType));
   }

   @Override
   public <T> Collection<T> getBeansOfType(Class<T> beanType, Qualifier<T> qualifier) {
      return this.getBeansOfType(Argument.of(beanType), qualifier);
   }

   @Override
   public <T> Collection<T> getBeansOfType(Argument<T> beanType) {
      return this.getBeansOfType(null, beanType);
   }

   @Override
   public <T> Collection<T> getBeansOfType(Argument<T> beanType, Qualifier<T> qualifier) {
      return this.getBeansOfType(null, beanType, qualifier);
   }

   @Override
   public <T> Stream<T> streamOfType(Class<T> beanType, Qualifier<T> qualifier) {
      return this.streamOfType(null, beanType, qualifier);
   }

   @Override
   public <T> Stream<T> streamOfType(Argument<T> beanType, Qualifier<T> qualifier) {
      return this.streamOfType(null, beanType, qualifier);
   }

   protected <T> Stream<T> streamOfType(BeanResolutionContext resolutionContext, Class<T> beanType, Qualifier<T> qualifier) {
      return this.streamOfType(resolutionContext, Argument.of(beanType), qualifier);
   }

   @Internal
   public <T> Stream<T> streamOfType(BeanResolutionContext resolutionContext, Argument<T> beanType, Qualifier<T> qualifier) {
      Objects.requireNonNull(beanType, "Bean type cannot be null");
      return this.getBeanRegistrations(resolutionContext, beanType, qualifier).stream().map(BeanRegistration::getBean);
   }

   @NonNull
   @Override
   public <T> T inject(@NonNull T instance) {
      Objects.requireNonNull(instance, "Instance cannot be null");
      Collection<BeanDefinition> candidates = this.findBeanCandidatesForInstance(instance);
      if (candidates.size() == 1) {
         BeanDefinition<T> beanDefinition = (BeanDefinition)candidates.iterator().next();

         try (BeanResolutionContext resolutionContext = this.newResolutionContext(beanDefinition, null)) {
            DefaultBeanContext.BeanKey<T> beanKey = new DefaultBeanContext.BeanKey<>(beanDefinition.getBeanType(), null);
            resolutionContext.addInFlightBean(beanKey, new BeanRegistration<>(beanKey, beanDefinition, instance));
            this.doInject(resolutionContext, instance, beanDefinition);
         }
      } else if (!candidates.isEmpty()) {
         Iterator iterator = candidates.iterator();
         throw new NonUniqueBeanException(instance.getClass(), iterator);
      }

      return instance;
   }

   @NonNull
   @Override
   public <T> T createBean(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.createBean(null, beanType, qualifier);
   }

   @NonNull
   @Override
   public <T> T createBean(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier, @Nullable Map<String, Object> argumentValues) {
      ArgumentUtils.requireNonNull("beanType", (T)beanType);
      Optional<BeanDefinition<T>> candidate = this.findBeanDefinition(Argument.of(beanType), qualifier);
      if (candidate.isPresent()) {
         Object var7;
         try (BeanResolutionContext resolutionContext = this.newResolutionContext((BeanDefinition<?>)candidate.get(), null)) {
            var7 = this.<T>doCreateBean(resolutionContext, (BeanDefinition<T>)candidate.get(), qualifier, argumentValues);
         }

         return (T)var7;
      } else {
         throw new NoSuchBeanException(beanType);
      }
   }

   @NonNull
   @Override
   public <T> T createBean(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier, @Nullable Object... args) {
      ArgumentUtils.requireNonNull("beanType", (T)beanType);
      Argument<T> beanArg = Argument.of(beanType);
      Optional<BeanDefinition<T>> candidate = this.findBeanDefinition(beanArg, qualifier);
      if (candidate.isPresent()) {
         BeanDefinition<T> definition = (BeanDefinition)candidate.get();

         Object var9;
         try (BeanResolutionContext resolutionContext = this.newResolutionContext(definition, null)) {
            var9 = this.<T>doCreateBean(resolutionContext, definition, beanArg, qualifier, args);
         }

         return (T)var9;
      } else {
         throw new NoSuchBeanException(beanType);
      }
   }

   @NonNull
   protected <T> T doCreateBean(
      @NonNull BeanResolutionContext resolutionContext,
      @NonNull BeanDefinition<T> definition,
      @NonNull Argument<T> beanType,
      @Nullable Qualifier<T> qualifier,
      @Nullable Object... args
   ) {
      Map<String, Object> argumentValues = this.resolveArgumentValues(resolutionContext, definition, args);
      if (LOG.isTraceEnabled()) {
         LOG.trace("Computed bean argument values: {}", argumentValues);
      }

      return this.doCreateBean(resolutionContext, definition, qualifier, argumentValues);
   }

   @NonNull
   private <T> Map<String, Object> resolveArgumentValues(BeanResolutionContext resolutionContext, BeanDefinition<T> definition, Object[] args) {
      if (!(definition instanceof ParametrizedBeanFactory)) {
         return Collections.emptyMap();
      } else {
         if (LOG.isTraceEnabled()) {
            LOG.trace("Creating bean for parameters: {}", ArrayUtils.toString(args));
         }

         Argument[] requiredArguments = ((ParametrizedBeanFactory)definition).getRequiredArguments();
         Map<String, Object> argumentValues = new LinkedHashMap(requiredArguments.length);
         BeanResolutionContext.Path currentPath = resolutionContext.getPath();

         for(int i = 0; i < requiredArguments.length; ++i) {
            Argument<?> requiredArgument = requiredArguments[i];

            try (BeanResolutionContext.Path ignored = currentPath.pushConstructorResolve(definition, requiredArgument)) {
               Class<?> argumentType = requiredArgument.getType();
               if (args.length > i) {
                  Object val = args[i];
                  if (val != null) {
                     if (argumentType.isInstance(val) && !CollectionUtils.isIterableOrMap(argumentType)) {
                        argumentValues.put(requiredArgument.getName(), val);
                     } else {
                        argumentValues.put(
                           requiredArgument.getName(),
                           ConversionService.SHARED
                              .convert(val, requiredArgument)
                              .orElseThrow(
                                 () -> new BeanInstantiationException(
                                       resolutionContext,
                                       "Invalid bean @Argument ["
                                          + requiredArgument
                                          + "]. Cannot convert object ["
                                          + val
                                          + "] to required type: "
                                          + argumentType
                                    )
                              )
                        );
                     }
                  } else if (!requiredArgument.isDeclaredNullable()) {
                     throw new BeanInstantiationException(resolutionContext, "Invalid bean @Argument [" + requiredArgument + "]. Argument cannot be null");
                  }
               } else {
                  Optional<?> existingBean = this.findBean(resolutionContext, argumentType, null);
                  if (existingBean.isPresent()) {
                     argumentValues.put(requiredArgument.getName(), existingBean.get());
                  } else if (!requiredArgument.isDeclaredNullable()) {
                     throw new BeanInstantiationException(
                        resolutionContext, "Invalid bean @Argument [" + requiredArgument + "]. No bean found for type: " + argumentType
                     );
                  }
               }
            }
         }

         return argumentValues;
      }
   }

   @Nullable
   @Override
   public <T> T destroyBean(@NonNull Argument<T> beanType, Qualifier<T> qualifier) {
      ArgumentUtils.requireNonNull("beanType", beanType);
      return (T)this.findBeanDefinition(beanType, qualifier).map(this::destroyBean).orElse(null);
   }

   @NonNull
   @Override
   public <T> T destroyBean(@NonNull T bean) {
      ArgumentUtils.requireNonNull("bean", bean);
      Optional<BeanRegistration<T>> beanRegistration = this.findBeanRegistration(bean);
      if (beanRegistration.isPresent()) {
         this.destroyBean((BeanRegistration<T>)beanRegistration.get());
      } else {
         Optional<BeanDefinition<T>> beanDefinition = this.findBeanDefinition(bean.getClass());
         if (beanDefinition.isPresent()) {
            BeanDefinition<T> definition = (BeanDefinition)beanDefinition.get();
            DefaultBeanContext.BeanKey<T> key = new DefaultBeanContext.BeanKey<>(definition, definition.getDeclaredQualifier());
            this.destroyBean(BeanRegistration.of(this, key, definition, bean));
         }
      }

      return bean;
   }

   @Nullable
   @Override
   public <T> T destroyBean(@NonNull Class<T> beanType) {
      ArgumentUtils.requireNonNull("beanType", (T)beanType);
      return this.destroyBean(Argument.of(beanType), null);
   }

   @Nullable
   private <T> T destroyBean(@NonNull BeanDefinition<T> beanDefinition) {
      if (beanDefinition.isSingleton()) {
         BeanRegistration<T> beanRegistration = this.singletonScope.findBeanRegistration(beanDefinition);
         if (beanRegistration != null) {
            this.destroyBean(beanRegistration);
            return beanRegistration.bean;
         }
      }

      throw new IllegalArgumentException(
         "Cannot destroy non-singleton bean using bean definition! Use 'destroyBean(BeanRegistration)` or `destroyBean(<BeanInstance>)`."
      );
   }

   @Override
   public <T> void destroyBean(@NonNull BeanRegistration<T> registration) {
      this.destroyBean(registration, false);
   }

   private <T> void destroyBean(@NonNull BeanRegistration<T> registration, boolean dependent) {
      if (LOG_LIFECYCLE.isDebugEnabled()) {
         LOG_LIFECYCLE.debug("Destroying bean [{}] with identifier [{}]", registration.bean, registration.identifier);
      }

      if (registration.beanDefinition instanceof ProxyBeanDefinition) {
         if (registration.bean instanceof InterceptedBeanProxy) {
            this.destroyProxyTargetBean(registration, dependent);
            return;
         }

         if (dependent && registration.beanDefinition.isSingleton()) {
            return;
         }
      }

      T beanToDestroy = registration.getBean();
      BeanDefinition<T> definition = registration.getBeanDefinition();
      if (beanToDestroy != null) {
         this.purgeCacheForBeanInstance(beanToDestroy);
         if (definition.isSingleton()) {
            this.singletonScope.purgeCacheForBeanInstance(definition, beanToDestroy);
         }
      }

      beanToDestroy = this.triggerPreDestroyListeners(definition, beanToDestroy);
      if (definition instanceof DisposableBeanDefinition) {
         ((DisposableBeanDefinition)definition).dispose(this, beanToDestroy);
      }

      if (beanToDestroy instanceof LifeCycle) {
         try {
            ((LifeCycle)beanToDestroy).stop();
         } catch (Exception var8) {
            throw new BeanDestructionException(definition, var8);
         }
      }

      if (registration instanceof BeanDisposingRegistration) {
         List<BeanRegistration<?>> dependents = ((BeanDisposingRegistration)registration).getDependents();
         if (CollectionUtils.isNotEmpty(dependents)) {
            ListIterator<BeanRegistration<?>> i = dependents.listIterator(dependents.size());

            while(i.hasPrevious()) {
               this.destroyBean((BeanRegistration<T>)i.previous(), true);
            }
         }
      } else {
         try {
            registration.close();
         } catch (Exception var7) {
            throw new BeanDestructionException(definition, var7);
         }
      }

      this.triggerBeanDestroyedListeners(definition, beanToDestroy);
   }

   @NonNull
   private <T> T triggerPreDestroyListeners(@NonNull BeanDefinition<T> beanDefinition, @NonNull T bean) {
      if (this.beanPreDestroyEventListeners == null) {
         this.beanPreDestroyEventListeners = this.loadListeners(BeanPreDestroyEventListener.class).entrySet();
      }

      if (!this.beanPreDestroyEventListeners.isEmpty()) {
         Class<T> beanType = this.getBeanType(beanDefinition);

         for(Entry<Class<?>, List<BeanPreDestroyEventListener>> entry : this.beanPreDestroyEventListeners) {
            if (((Class)entry.getKey()).isAssignableFrom(beanType)) {
               BeanPreDestroyEvent<T> event = new BeanPreDestroyEvent<>(this, beanDefinition, bean);

               for(BeanPreDestroyEventListener<T> listener : (List)entry.getValue()) {
                  try {
                     bean = (T)Objects.requireNonNull(listener.onPreDestroy(event), "PreDestroy event listener illegally returned null: " + listener.getClass());
                  } catch (Exception var10) {
                     throw new BeanDestructionException(beanDefinition, var10);
                  }
               }
            }
         }
      }

      return bean;
   }

   private <T> void destroyProxyTargetBean(@NonNull BeanRegistration<T> registration, boolean dependent) {
      Set<Object> destroyed = Collections.emptySet();
      if (registration instanceof BeanDisposingRegistration) {
         BeanDisposingRegistration<?> disposingRegistration = (BeanDisposingRegistration)registration;
         if (disposingRegistration.getDependents() != null) {
            destroyed = Collections.newSetFromMap(new IdentityHashMap());

            for(BeanRegistration<?> beanRegistration : disposingRegistration.getDependents()) {
               this.destroyBean(beanRegistration, true);
               destroyed.add(beanRegistration.bean);
            }
         }
      }

      BeanDefinition<T> proxyTargetBeanDefinition = (BeanDefinition)this.findProxyTargetBeanDefinition(registration.beanDefinition)
         .orElseThrow(() -> new IllegalStateException("Cannot find a proxy target bean definition for: " + registration.beanDefinition));
      Optional<CustomScope<?>> declaredScope = this.customScopeRegistry.findDeclaredScope(proxyTargetBeanDefinition);
      if (!declaredScope.isPresent()) {
         if (!proxyTargetBeanDefinition.isSingleton()) {
            if (registration.bean instanceof InterceptedBeanProxy) {
               InterceptedBeanProxy<T> interceptedProxy = (InterceptedBeanProxy)registration.bean;
               if (interceptedProxy.hasCachedInterceptedTarget()) {
                  T interceptedTarget = interceptedProxy.interceptedTarget();
                  if (destroyed.contains(interceptedTarget)) {
                     return;
                  }

                  this.destroyBean(
                     BeanRegistration.of(
                        this,
                        new DefaultBeanContext.BeanKey<>(proxyTargetBeanDefinition, proxyTargetBeanDefinition.getDeclaredQualifier()),
                        proxyTargetBeanDefinition,
                        interceptedTarget,
                        registration instanceof BeanDisposingRegistration ? ((BeanDisposingRegistration)registration).getDependents() : null
                     )
                  );
               }
            }

         }
      } else {
         CustomScope<?> customScope = (CustomScope)declaredScope.get();
         if (!dependent) {
            Optional<BeanRegistration<T>> targetBeanRegistration = customScope.findBeanRegistration(proxyTargetBeanDefinition);
            if (targetBeanRegistration.isPresent()) {
               BeanRegistration<T> targetRegistration = (BeanRegistration)targetBeanRegistration.get();
               customScope.remove(targetRegistration.identifier);
            }

         }
      }
   }

   @NonNull
   private <T> void triggerBeanDestroyedListeners(@NonNull BeanDefinition<T> beanDefinition, @NonNull T bean) {
      if (this.beanDestroyedEventListeners == null) {
         this.beanDestroyedEventListeners = this.loadListeners(BeanDestroyedEventListener.class).entrySet();
      }

      if (!this.beanDestroyedEventListeners.isEmpty()) {
         Class<T> beanType = this.getBeanType(beanDefinition);

         for(Entry<Class<?>, List<BeanDestroyedEventListener>> entry : this.beanDestroyedEventListeners) {
            if (((Class)entry.getKey()).isAssignableFrom(beanType)) {
               BeanDestroyedEvent<T> event = new BeanDestroyedEvent<>(this, beanDefinition, bean);

               for(BeanDestroyedEventListener<T> listener : (List)entry.getValue()) {
                  try {
                     listener.onDestroyed(event);
                  } catch (Exception var10) {
                     throw new BeanDestructionException(beanDefinition, var10);
                  }
               }
            }
         }
      }

   }

   @NonNull
   private <T> Class<T> getBeanType(@NonNull BeanDefinition<T> beanDefinition) {
      return beanDefinition instanceof ProxyBeanDefinition ? ((ProxyBeanDefinition)beanDefinition).getTargetType() : beanDefinition.getBeanType();
   }

   @Nullable
   protected <T> BeanRegistration<T> getActiveBeanRegistration(BeanDefinition<T> beanDefinition, Qualifier qualifier) {
      return beanDefinition == null ? null : this.singletonScope.findBeanRegistration(beanDefinition, qualifier);
   }

   @NonNull
   protected <T> T createBean(@Nullable BeanResolutionContext resolutionContext, @NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier) {
      ArgumentUtils.requireNonNull("beanType", (T)beanType);
      Optional<BeanDefinition<T>> concreteCandidate = this.findBeanDefinition(beanType, qualifier);
      if (concreteCandidate.isPresent()) {
         BeanDefinition<T> candidate = (BeanDefinition)concreteCandidate.get();

         Object var8;
         try (BeanResolutionContext context = this.newResolutionContext(candidate, resolutionContext)) {
            var8 = this.<T>doCreateBean(context, candidate, qualifier);
         }

         return (T)var8;
      } else {
         throw new NoSuchBeanException(beanType);
      }
   }

   @Internal
   @NonNull
   protected <T> T inject(@NonNull BeanResolutionContext resolutionContext, @Nullable BeanDefinition<?> requestingBeanDefinition, @NonNull T instance) {
      Class<T> beanType = instance.getClass();
      Optional<BeanDefinition<T>> concreteCandidate = this.findBeanDefinition(beanType, null);
      if (concreteCandidate.isPresent()) {
         BeanDefinition definition = (BeanDefinition)concreteCandidate.get();
         if (requestingBeanDefinition != null && requestingBeanDefinition.equals(definition)) {
            return instance;
         }

         this.doInject(resolutionContext, instance, definition);
      }

      return instance;
   }

   @NonNull
   protected <T> Collection<T> getBeansOfType(@Nullable BeanResolutionContext resolutionContext, @NonNull Argument<T> beanType) {
      return this.getBeansOfType(resolutionContext, beanType, null);
   }

   @Internal
   @NonNull
   public <T> Collection<T> getBeansOfType(@Nullable BeanResolutionContext resolutionContext, @NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      Collection<BeanRegistration<T>> beanRegistrations = this.getBeanRegistrations(resolutionContext, beanType, qualifier);
      List<T> list = new ArrayList(beanRegistrations.size());

      for(BeanRegistration<T> beanRegistration : beanRegistrations) {
         list.add(beanRegistration.getBean());
      }

      return list;
   }

   @NonNull
   @Override
   public <T> T getProxyTargetBean(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier) {
      ArgumentUtils.requireNonNull("beanType", (T)beanType);
      return this.getProxyTargetBean(Argument.of(beanType), qualifier);
   }

   @NonNull
   @Override
   public <T> T getProxyTargetBean(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      BeanDefinition<T> definition = this.getProxyTargetBeanDefinition(beanType, qualifier);
      Qualifier<T> proxyQualifier = qualifier != null ? Qualifiers.byQualifiers(qualifier, PROXY_TARGET_QUALIFIER) : PROXY_TARGET_QUALIFIER;
      return this.resolveBeanRegistration(null, definition, beanType, proxyQualifier).bean;
   }

   @NonNull
   public <T> T getProxyTargetBean(@Nullable BeanResolutionContext resolutionContext, @NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      BeanDefinition<T> definition = this.getProxyTargetBeanDefinition(beanType, qualifier);
      Qualifier<T> proxyQualifier = qualifier != null ? Qualifiers.byQualifiers(qualifier, PROXY_TARGET_QUALIFIER) : PROXY_TARGET_QUALIFIER;
      return this.resolveBeanRegistration(resolutionContext, definition, beanType, proxyQualifier).bean;
   }

   @NonNull
   @Override
   public <T, R> Optional<ExecutableMethod<T, R>> findProxyTargetMethod(@NonNull Class<T> beanType, @NonNull String method, @NonNull Class[] arguments) {
      ArgumentUtils.requireNonNull("beanType", (T)beanType);
      ArgumentUtils.requireNonNull("method", (T)method);
      BeanDefinition<T> definition = this.getProxyTargetBeanDefinition(beanType, null);
      return definition.findMethod(method, arguments);
   }

   @NonNull
   @Override
   public <T, R> Optional<ExecutableMethod<T, R>> findProxyTargetMethod(
      @NonNull Class<T> beanType, Qualifier<T> qualifier, @NonNull String method, Class... arguments
   ) {
      ArgumentUtils.requireNonNull("beanType", (T)beanType);
      ArgumentUtils.requireNonNull("method", (T)method);
      BeanDefinition<T> definition = this.getProxyTargetBeanDefinition(beanType, qualifier);
      return definition.findMethod(method, arguments);
   }

   @Override
   public <T, R> Optional<ExecutableMethod<T, R>> findProxyTargetMethod(
      @NonNull Argument<T> beanType, Qualifier<T> qualifier, @NonNull String method, Class... arguments
   ) {
      ArgumentUtils.requireNonNull("beanType", beanType);
      ArgumentUtils.requireNonNull("method", (T)method);
      BeanDefinition<T> definition = this.getProxyTargetBeanDefinition(beanType, qualifier);
      return definition.findMethod(method, arguments);
   }

   @NonNull
   @Override
   public <T> Optional<BeanDefinition<T>> findProxyTargetBeanDefinition(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.findProxyTargetBeanDefinition(Argument.of(beanType), qualifier);
   }

   @Override
   public <T> Optional<BeanDefinition<T>> findProxyTargetBeanDefinition(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      ArgumentUtils.requireNonNull("beanType", beanType);
      Qualifier<T> proxyQualifier = qualifier != null ? Qualifiers.byQualifiers(qualifier, PROXY_TARGET_QUALIFIER) : PROXY_TARGET_QUALIFIER;
      DefaultBeanContext.BeanCandidateKey<T> key = new DefaultBeanContext.BeanCandidateKey<>(beanType, proxyQualifier, true);
      Optional beanDefinition = (Optional)this.beanConcreteCandidateCache.get(key);
      if (beanDefinition == null) {
         BeanRegistration<T> beanRegistration = this.singletonScope.findCachedSingletonBeanRegistration(beanType, qualifier);
         if (beanRegistration != null) {
            if (LOG.isDebugEnabled()) {
               LOG.debug("Resolved existing bean [{}] for type [{}] and qualifier [{}]", beanRegistration.bean, beanType, qualifier);
            }

            beanDefinition = Optional.of(beanRegistration.beanDefinition);
         } else {
            beanDefinition = this.findConcreteCandidateNoCache(null, beanType, proxyQualifier, true, false);
         }

         this.beanConcreteCandidateCache.put(key, beanDefinition);
      }

      return beanDefinition;
   }

   @NonNull
   @Override
   public Collection<BeanDefinition<?>> getBeanDefinitions(@Nullable Qualifier<Object> qualifier) {
      if (qualifier == null) {
         return Collections.emptyList();
      } else {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Finding candidate beans for qualifier: {}", qualifier);
         }

         if (!this.beanDefinitionsClasses.isEmpty()) {
            Stream<BeanDefinitionReference> reduced = qualifier.reduce(Object.class, this.beanDefinitionsClasses.stream());
            Stream<BeanDefinition> candidateStream = qualifier.reduce(
               Object.class, reduced.map(ref -> ref.load(this)).filter(candidate -> candidate.isEnabled(this))
            );
            Collection candidates = (Collection)candidateStream.collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(candidates)) {
               this.filterProxiedTypes(candidates, true, true, null);
               this.filterReplacedBeans(null, candidates);
            }

            return candidates;
         } else {
            return Collections.emptyList();
         }
      }
   }

   @NonNull
   @Override
   public Collection<BeanDefinition<?>> getAllBeanDefinitions() {
      if (LOG.isDebugEnabled()) {
         LOG.debug("Finding all bean definitions");
      }

      return (Collection<BeanDefinition<?>>)(!this.beanDefinitionsClasses.isEmpty()
         ? (List)this.beanDefinitionsClasses.stream().map(ref -> ref.load(this)).filter(candidate -> candidate.isEnabled(this)).collect(Collectors.toList())
         : (Collection)Collections.emptyMap());
   }

   @NonNull
   @Override
   public Collection<BeanDefinitionReference<?>> getBeanDefinitionReferences() {
      if (!this.beanDefinitionsClasses.isEmpty()) {
         List refs = (List)this.beanDefinitionsClasses.stream().filter(ref -> ref.isEnabled(this)).collect(Collectors.toList());
         return Collections.unmodifiableList(refs);
      } else {
         return Collections.emptyList();
      }
   }

   @NonNull
   public <T> T getBean(@Nullable BeanResolutionContext resolutionContext, @NonNull Class<T> beanType) {
      ArgumentUtils.requireNonNull("beanType", (T)beanType);
      return this.getBean(resolutionContext, Argument.of(beanType), null);
   }

   @NonNull
   @Override
   public <T> T getBean(@NonNull BeanDefinition<T> definition) {
      ArgumentUtils.requireNonNull("definition", definition);
      return this.resolveBeanRegistration(null, definition).bean;
   }

   @NonNull
   public <T> T getBean(@Nullable BeanResolutionContext resolutionContext, @NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.getBean(resolutionContext, Argument.of(beanType), qualifier);
   }

   @NonNull
   public <T> T getBean(@Nullable BeanResolutionContext resolutionContext, @NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      ArgumentUtils.requireNonNull("beanType", beanType);
      return this.resolveBeanRegistration(resolutionContext, beanType, qualifier, true).bean;
   }

   @Internal
   @NonNull
   public <T> T getBean(
      @Nullable BeanResolutionContext resolutionContext,
      @NonNull BeanDefinition<T> beanDefinition,
      @NonNull Argument<T> beanType,
      @Nullable Qualifier<T> qualifier
   ) {
      ArgumentUtils.requireNonNull("beanDefinition", beanDefinition);
      ArgumentUtils.requireNonNull("beanType", beanType);
      return this.resolveBeanRegistration(resolutionContext, beanDefinition, beanType, qualifier).bean;
   }

   @NonNull
   public <T> Optional<T> findBean(@Nullable BeanResolutionContext resolutionContext, @NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.findBean(resolutionContext, Argument.of(beanType), qualifier);
   }

   @Internal
   @NonNull
   public <T> Optional<T> findBean(@Nullable BeanResolutionContext resolutionContext, @NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      ArgumentUtils.requireNonNull("beanType", beanType);
      if (this.thisInterfaces.contains(beanType.getType())) {
         return Optional.of(this);
      } else {
         try {
            BeanRegistration<T> beanRegistration = this.resolveBeanRegistration(resolutionContext, beanType, qualifier, false);
            return beanRegistration != null && beanRegistration.bean != null ? Optional.of(beanRegistration.bean) : Optional.empty();
         } catch (DisabledBeanException var5) {
            if (AbstractBeanContextConditional.LOG.isDebugEnabled()) {
               AbstractBeanContextConditional.LOG.debug("Bean of type [{}] disabled for reason: {}", beanType.getSimpleName(), var5.getMessage());
            }

            return Optional.empty();
         }
      }
   }

   @Override
   public BeanContextConfiguration getContextConfiguration() {
      return this.beanContextConfiguration;
   }

   @Override
   public void publishEvent(@NonNull Object event) {
      if (event != null) {
         this.getBean(Argument.of(ApplicationEventPublisher.class, event.getClass())).publishEvent(event);
      }

   }

   @NonNull
   @Override
   public Future<Void> publishEventAsync(@NonNull Object event) {
      Objects.requireNonNull(event, "Event cannot be null");
      return this.getBean(Argument.of(ApplicationEventPublisher.class, event.getClass())).publishEventAsync(event);
   }

   @NonNull
   @Override
   public <T> Optional<BeanDefinition<T>> findProxyBeanDefinition(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier) {
      ArgumentUtils.requireNonNull("beanType", (T)beanType);
      return this.findProxyBeanDefinition(Argument.of(beanType), qualifier);
   }

   @NonNull
   @Override
   public <T> Optional<BeanDefinition<T>> findProxyBeanDefinition(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      ArgumentUtils.requireNonNull("beanType", beanType);

      for(BeanDefinition<T> beanDefinition : this.getBeanDefinitions(beanType, qualifier)) {
         if (beanDefinition.isProxy()) {
            return Optional.of(beanDefinition);
         }
      }

      return Optional.empty();
   }

   @Internal
   protected void invalidateCaches() {
      this.beanCandidateCache.clear();
      this.beanConcreteCandidateCache.clear();
      this.singletonBeanRegistrations.clear();
   }

   @NonNull
   protected List<BeanDefinitionReference> resolveBeanDefinitionReferences() {
      if (this.beanDefinitionReferences == null) {
         SoftServiceLoader<BeanDefinitionReference> definitions = SoftServiceLoader.load(BeanDefinitionReference.class, this.classLoader);
         this.beanDefinitionReferences = new ArrayList(300);
         definitions.collectAll(this.beanDefinitionReferences, BeanDefinitionReference::isPresent);
      }

      return this.beanDefinitionReferences;
   }

   @NonNull
   @Deprecated
   protected List<BeanDefinitionReference> resolveBeanDefinitionReferences(@Nullable Predicate<BeanDefinitionReference> predicate) {
      if (predicate != null) {
         List<BeanDefinitionReference> allRefs = this.resolveBeanDefinitionReferences();
         List<BeanDefinitionReference> newRefs = new ArrayList(allRefs.size());

         for(BeanDefinitionReference reference : allRefs) {
            if (predicate.test(reference)) {
               newRefs.add(reference);
            }
         }

         return newRefs;
      } else {
         return this.resolveBeanDefinitionReferences();
      }
   }

   @NonNull
   protected Iterable<BeanConfiguration> resolveBeanConfigurations() {
      if (this.beanConfigurationsList == null) {
         SoftServiceLoader<BeanConfiguration> definitions = SoftServiceLoader.load(BeanConfiguration.class, this.classLoader);
         this.beanConfigurationsList = new ArrayList(300);
         definitions.collectAll(this.beanConfigurationsList, null);
      }

      return this.beanConfigurationsList;
   }

   protected void initializeEventListeners() {
      Map<Class<?>, List<BeanCreatedEventListener<?>>> beanCreatedListeners = this.loadCreatedListeners();
      beanCreatedListeners.put(AnnotationProcessor.class, Collections.singletonList(new AnnotationProcessorListener()));
      Map<Class<?>, List<BeanInitializedEventListener>> beanInitializedListeners = this.loadListeners(BeanInitializedEventListener.class);
      this.beanCreationEventListeners = beanCreatedListeners.entrySet();
      this.beanInitializedEventListeners = beanInitializedListeners.entrySet();
   }

   private void handleEagerInitializedDependencies(BeanDefinition<?> listener, Argument<?> listensTo, List<List<Argument<?>>> targets) {
      if (LOG.isWarnEnabled()) {
         List<String> paths = new ArrayList(targets.size());

         for(List<Argument<?>> line : targets) {
            paths.add("    " + (String)line.stream().map(TypeInformation::getType).map(Class::getName).collect(Collectors.joining(" --> ")));
         }

         LOG.warn(
            "The bean created event listener {} will not be executed because one or more other bean created event listeners inject {}:\n{}\nChange at least one point in the path to be lazy initialized by injecting a provider to avoid this issue",
            listener.getBeanType().getName(),
            listensTo.getType().getName(),
            String.join("\n", paths)
         );
      }

   }

   @NonNull
   private Map<Class<?>, List<BeanCreatedEventListener<?>>> loadCreatedListeners() {
      Collection<BeanDefinition<BeanCreatedEventListener>> beanDefinitions = this.getBeanDefinitions(BeanCreatedEventListener.class);
      HashMap<Class<?>, List<BeanCreatedEventListener<?>>> typeToListener = new HashMap(beanDefinitions.size(), 1.0F);
      if (beanDefinitions.isEmpty()) {
         return typeToListener;
      } else {
         HashMap<BeanDefinition<?>, List<List<Argument<?>>>> invalidListeners = new HashMap();
         HashMap<BeanDefinition<?>, Argument<?>> beanCreationTargets = new HashMap();

         for(BeanDefinition<BeanCreatedEventListener> beanCreatedDefinition : beanDefinitions) {
            List<Argument<?>> typeArguments = beanCreatedDefinition.getTypeArguments(BeanCreatedEventListener.class);
            Argument<?> argument = CollectionUtils.last(typeArguments);
            if (argument == null) {
               argument = Argument.OBJECT_ARGUMENT;
            }

            beanCreationTargets.put(beanCreatedDefinition, argument);
         }

         for(BeanDefinition<BeanCreatedEventListener> beanCreatedDefinition : beanDefinitions) {
            try (DefaultBeanContext.ScanningBeanResolutionContext context = new DefaultBeanContext.ScanningBeanResolutionContext(
                  beanCreatedDefinition, beanCreationTargets
               )) {
               BeanCreatedEventListener<?> listener = this.resolveBeanRegistration(context, beanCreatedDefinition).bean;
               List<Argument<?>> typeArguments = beanCreatedDefinition.getTypeArguments(BeanCreatedEventListener.class);
               Argument<?> argument = CollectionUtils.last(typeArguments);
               if (argument == null) {
                  argument = Argument.OBJECT_ARGUMENT;
               }

               ((List)typeToListener.computeIfAbsent(argument.getType(), aClass -> new ArrayList(10))).add(listener);
               Map<BeanDefinition<?>, List<List<Argument<?>>>> foundTargets = context.getFoundTargets();

               for(Entry<BeanDefinition<?>, List<List<Argument<?>>>> entry : foundTargets.entrySet()) {
                  ((List)invalidListeners.computeIfAbsent(entry.getKey(), key -> new ArrayList())).addAll((Collection)entry.getValue());
               }
            }
         }

         for(List<BeanCreatedEventListener<?>> listeners : typeToListener.values()) {
            OrderUtil.sort(listeners);
         }

         for(Entry<BeanDefinition<?>, List<List<Argument<?>>>> entry : invalidListeners.entrySet()) {
            this.handleEagerInitializedDependencies(
               (BeanDefinition<?>)entry.getKey(), (Argument<?>)beanCreationTargets.get(entry.getKey()), (List<List<Argument<?>>>)entry.getValue()
            );
         }

         return typeToListener;
      }
   }

   @NonNull
   private <T extends EventListener> Map<Class<?>, List<T>> loadListeners(@NonNull Class<T> listenerType) {
      Collection<BeanDefinition<T>> beanDefinitions = this.getBeanDefinitions(listenerType);
      HashMap<Class<?>, List<T>> typeToListener = new HashMap(beanDefinitions.size(), 1.0F);

      for(BeanDefinition<T> beanCreatedDefinition : beanDefinitions) {
         try (BeanResolutionContext context = this.newResolutionContext(beanCreatedDefinition, null)) {
            T listener = this.resolveBeanRegistration(context, beanCreatedDefinition).bean;
            List<Argument<?>> typeArguments = beanCreatedDefinition.getTypeArguments(listenerType);
            Argument<?> argument = CollectionUtils.last(typeArguments);
            if (argument == null) {
               argument = Argument.OBJECT_ARGUMENT;
            }

            ((List)typeToListener.computeIfAbsent(argument.getType(), aClass -> new ArrayList(10))).add(listener);
         }
      }

      for(List<T> listenerList : typeToListener.values()) {
         OrderUtil.sort(listenerList);
      }

      return typeToListener;
   }

   protected void initializeContext(
      @NonNull List<BeanDefinitionReference> contextScopeBeans,
      @NonNull List<BeanDefinitionReference> processedBeans,
      @NonNull List<BeanDefinitionReference> parallelBeans
   ) {
      if (CollectionUtils.isNotEmpty(contextScopeBeans)) {
         Collection<BeanDefinition> contextBeans = new ArrayList(contextScopeBeans.size());

         for(BeanDefinitionReference contextScopeBean : contextScopeBeans) {
            try {
               this.loadContextScopeBean(contextScopeBean, contextBeans::add);
            } catch (Throwable var8) {
               throw new BeanInstantiationException("Bean definition [" + contextScopeBean.getName() + "] could not be loaded: " + var8.getMessage(), var8);
            }
         }

         this.filterProxiedTypes(contextBeans, true, false, null);
         this.filterReplacedBeans(null, contextBeans);

         for(BeanDefinition contextScopeDefinition : contextBeans) {
            try {
               this.loadContextScopeBean(contextScopeDefinition);
            } catch (DisabledBeanException var9) {
               if (AbstractBeanContextConditional.LOG.isDebugEnabled()) {
                  AbstractBeanContextConditional.LOG
                     .debug("Bean of type [{}] disabled for reason: {}", contextScopeDefinition.getBeanType().getSimpleName(), var9.getMessage());
               }
            } catch (Throwable var10) {
               throw new BeanInstantiationException(
                  "Bean definition [" + contextScopeDefinition.getName() + "] could not be loaded: " + var10.getMessage(), var10
               );
            }
         }
      }

      if (!processedBeans.isEmpty()) {
         Stream<BeanDefinitionMethodReference<?, ?>> methodStream = processedBeans.stream()
            .filter(ref -> ref.isEnabled(this))
            .map(reference -> {
               try {
                  return reference.load(this);
               } catch (Exception var3x) {
                  throw new BeanInstantiationException("Bean definition [" + reference.getName() + "] could not be loaded: " + var3x.getMessage(), var3x);
               }
            })
            .filter(bean -> bean.isEnabled(this))
            .flatMap(
               beanDefinition -> beanDefinition.getExecutableMethods()
                     .parallelStream()
                     .filter(method -> method.hasStereotype(Executable.class))
                     .map(executableMethod -> BeanDefinitionMethodReference.of(beanDefinition, executableMethod))
            );
         Map<Class<? extends Annotation>, List<BeanDefinitionMethodReference<?, ?>>> byAnnotation = new HashMap(processedBeans.size());
         methodStream.forEach(reference -> {
            List<Class<? extends Annotation>> annotations = reference.getAnnotationTypesByStereotype(Executable.class);
            annotations.forEach(annotation -> {
               List var10000 = (List)byAnnotation.compute(annotation, (ann, list) -> {
                  if (list == null) {
                     list = new ArrayList(10);
                  }

                  list.add(reference);
                  return list;
               });
            });
         });
         byAnnotation.forEach(
            (annotationType, methods) -> this.streamOfType(ExecutableMethodProcessor.class, Qualifiers.byTypeArguments(annotationType))
                  .forEach(
                     processor -> {
                        if (processor instanceof LifeCycle) {
                           ((LifeCycle)processor).start();
                        }
         
                        for(BeanDefinitionMethodReference<?, ?> method : methods) {
                           BeanDefinition<?> beanDefinition = method.getBeanDefinition();
                           if (!beanDefinition.hasStereotype(annotationType)) {
                              if (method.hasDeclaredStereotype(Parallel.class)) {
                                 ForkJoinPool.commonPool()
                                    .execute(
                                       () -> {
                                          try {
                                             processor.process(beanDefinition, method);
                                          } catch (Throwable var6x) {
                                             if (LOG.isErrorEnabled()) {
                                                LOG.error(
                                                   "Error processing bean method "
                                                      + beanDefinition
                                                      + "."
                                                      + method
                                                      + " with processor ("
                                                      + processor
                                                      + "): "
                                                      + var6x.getMessage(),
                                                   var6x
                                                );
                                             }
               
                                             Boolean shutdownOnError = (Boolean)method.booleanValue(Parallel.class, "shutdownOnError").orElse(true);
                                             if (shutdownOnError) {
                                                this.stop();
                                             }
                                          }
               
                                       }
                                    );
                              } else {
                                 processor.process(beanDefinition, method);
                              }
                           }
                        }
         
                        if (processor instanceof LifeCycle) {
                           ((LifeCycle)processor).stop();
                        }
         
                     }
                  )
         );
      }

      if (CollectionUtils.isNotEmpty(parallelBeans)) {
         this.processParallelBeans(parallelBeans);
      }

      Runnable runnable = () -> this.beanDefinitionsClasses.removeIf(beanDefinitionReference -> !beanDefinitionReference.isEnabled(this));
      ForkJoinPool.commonPool().execute(runnable);
   }

   @NonNull
   protected <T> Collection<BeanDefinition<T>> findBeanCandidates(@NonNull Class<T> beanType, @Nullable BeanDefinition<?> filter) {
      return this.findBeanCandidates(null, Argument.of(beanType), filter, true);
   }

   @NonNull
   protected <T> Collection<BeanDefinition<T>> findBeanCandidates(
      @Nullable BeanResolutionContext resolutionContext, @NonNull Argument<T> beanType, @Nullable BeanDefinition<?> filter, boolean filterProxied
   ) {
      Predicate<BeanDefinition<T>> predicate = filter == null ? null : definition -> !definition.equals(filter);
      return this.findBeanCandidates(resolutionContext, beanType, filterProxied, predicate);
   }

   @NonNull
   protected <T> Collection<BeanDefinition<T>> findBeanCandidates(
      @Nullable BeanResolutionContext resolutionContext, @NonNull Argument<T> beanType, boolean filterProxied, Predicate<BeanDefinition<T>> predicate
   ) {
      ArgumentUtils.requireNonNull("beanType", beanType);
      Class<T> beanClass = beanType.getType();
      if (LOG.isDebugEnabled()) {
         LOG.debug("Finding candidate beans for type: {}", beanType);
      }

      Collection<BeanDefinitionReference> beanDefinitionsClasses;
      if (this.indexedTypes.contains(beanClass)) {
         beanDefinitionsClasses = (Collection)this.beanIndex.get(beanClass);
         if (beanDefinitionsClasses == null) {
            beanDefinitionsClasses = Collections.emptyList();
         }
      } else {
         beanDefinitionsClasses = this.beanDefinitionsClasses;
      }

      Set<BeanDefinition<T>> candidates;
      if (!beanDefinitionsClasses.isEmpty()) {
         candidates = new HashSet();

         for(BeanDefinitionReference reference : beanDefinitionsClasses) {
            if (reference.isCandidateBean(beanType) && reference.isEnabled(this, resolutionContext)) {
               BeanDefinition<T> loadedBean;
               try {
                  loadedBean = reference.load(this);
               } catch (Throwable var12) {
                  throw new BeanContextException("Error loading bean [" + reference.getName() + "]: " + var12.getMessage(), var12);
               }

               if (loadedBean.isCandidateBean(beanType) && (predicate == null || predicate.test(loadedBean)) && loadedBean.isEnabled(this, resolutionContext)) {
                  candidates.add(loadedBean);
               }
            }
         }

         if (!candidates.isEmpty()) {
            if (filterProxied) {
               this.filterProxiedTypes(candidates, true, false, null);
            }

            this.filterReplacedBeans(resolutionContext, candidates);
         }
      } else {
         candidates = Collections.emptySet();
      }

      if (LOG.isDebugEnabled()) {
         if (candidates.isEmpty()) {
            LOG.debug("No bean candidates found for type: {}", beanType);
         } else {
            for(BeanDefinition<?> candidate : candidates) {
               LOG.debug("  {} {} {}", candidate.getBeanType(), candidate.getDeclaredQualifier(), candidate);
            }
         }
      }

      return candidates;
   }

   protected <T> Collection<BeanDefinition<T>> transformIterables(
      BeanResolutionContext resolutionContext, Collection<BeanDefinition<T>> candidates, boolean filterProxied
   ) {
      return candidates;
   }

   @NonNull
   protected <T> Collection<BeanDefinition> findBeanCandidatesForInstance(@NonNull T instance) {
      ArgumentUtils.requireNonNull("instance", instance);
      if (LOG.isDebugEnabled()) {
         LOG.debug("Finding candidate beans for instance: {}", instance);
      }

      Collection<BeanDefinitionReference> beanDefinitionsClasses = this.beanDefinitionsClasses;
      Class<?> beanClass = instance.getClass();
      Argument<?> beanType = Argument.of(beanClass);
      Collection<BeanDefinition> beanDefinitions = (Collection)this.beanCandidateCache.get(beanType);
      if (beanDefinitions == null) {
         if (beanDefinitionsClasses.isEmpty()) {
            if (LOG.isDebugEnabled()) {
               LOG.debug("No bean candidates found for instance: {}", instance);
            }

            beanDefinitions = Collections.emptySet();
         } else {
            List<BeanDefinition> candidates = new ArrayList();

            for(BeanDefinitionReference<?> reference : beanDefinitionsClasses) {
               if (reference.isEnabled(this)) {
                  Class<?> candidateType = reference.getBeanType();
                  if (candidateType != null && candidateType.isInstance(instance)) {
                     BeanDefinition<?> candidate = reference.load(this);
                     if (candidate.isEnabled(this)) {
                        candidates.add(candidate);
                     }
                  }
               }
            }

            if (candidates.size() > 1) {
               candidates = (List)candidates.stream()
                  .filter(candidatex -> !(candidatex instanceof NoInjectionBeanDefinition) && candidatex.getBeanType() == beanClass)
                  .collect(Collectors.toList());
            }

            if (LOG.isDebugEnabled()) {
               LOG.debug("Resolved bean candidates {} for instance: {}", candidates, instance);
            }

            beanDefinitions = candidates;
         }

         this.beanCandidateCache.put(beanType, beanDefinitions);
      }

      return beanDefinitions;
   }

   protected synchronized void registerConfiguration(@NonNull BeanConfiguration configuration) {
      ArgumentUtils.requireNonNull("configuration", configuration);
      this.beanConfigurations.put(configuration.getName(), configuration);
   }

   @Internal
   @NonNull
   private <T> T doCreateBean(
      @NonNull BeanResolutionContext resolutionContext,
      @NonNull BeanDefinition<T> beanDefinition,
      @Nullable Qualifier<T> qualifier,
      @Nullable Map<String, Object> argumentValues
   ) {
      return this.doCreateBean(resolutionContext, beanDefinition, qualifier, Argument.of(beanDefinition.getBeanType()), false, argumentValues);
   }

   @Internal
   @NonNull
   final <T> T doCreateBean(@NonNull BeanResolutionContext resolutionContext, @NonNull BeanDefinition<T> beanDefinition, @Nullable Qualifier<T> qualifier) {
      return this.doCreateBean(resolutionContext, beanDefinition, qualifier, Argument.of(beanDefinition.getBeanType()), false, null);
   }

   @Internal
   @NonNull
   @Deprecated
   protected <T> T doCreateBean(
      @NonNull BeanResolutionContext resolutionContext,
      @NonNull BeanDefinition<T> beanDefinition,
      @Nullable Qualifier<T> qualifier,
      boolean isSingleton,
      @Nullable Map<String, Object> argumentValues
   ) {
      return this.doCreateBean(resolutionContext, beanDefinition, qualifier, Argument.of(beanDefinition.getBeanType()), isSingleton, argumentValues);
   }

   @Internal
   @NonNull
   @Deprecated
   protected <T> T doCreateBean(
      @NonNull BeanResolutionContext resolutionContext,
      @NonNull BeanDefinition<T> beanDefinition,
      @Nullable Qualifier<T> qualifier,
      @Nullable Argument<T> qualifierBeanType,
      boolean isSingleton,
      @Nullable Map<String, Object> argumentValues
   ) {
      T bean;
      if (beanDefinition instanceof BeanFactory) {
         bean = this.resolveByBeanFactory(resolutionContext, beanDefinition, qualifier, argumentValues);
      } else {
         bean = this.resolveByBeanDefinition(resolutionContext, beanDefinition);
      }

      return this.postBeanCreated(resolutionContext, beanDefinition, qualifier, bean);
   }

   @NonNull
   private <T> T resolveByBeanDefinition(@NonNull BeanResolutionContext resolutionContext, @NonNull BeanDefinition<T> beanDefinition) {
      ConstructorInjectionPoint<T> constructor = beanDefinition.getConstructor();
      Argument<?>[] requiredConstructorArguments = constructor.getArguments();
      T bean;
      if (requiredConstructorArguments.length == 0) {
         bean = constructor.invoke();
      } else {
         Object[] constructorArgs = new Object[requiredConstructorArguments.length];

         for(int i = 0; i < requiredConstructorArguments.length; ++i) {
            Class<?> argument = requiredConstructorArguments[i].getType();
            constructorArgs[i] = this.getBean(resolutionContext, argument);
         }

         bean = constructor.invoke(constructorArgs);
      }

      this.inject(resolutionContext, null, bean);
      return bean;
   }

   @NonNull
   private <T> T resolveByBeanFactory(
      @NonNull BeanResolutionContext resolutionContext,
      @NonNull BeanDefinition<T> beanDefinition,
      @Nullable Qualifier<T> qualifier,
      @Nullable Map<String, Object> argumentValues
   ) {
      BeanFactory<T> beanFactory = (BeanFactory)beanDefinition;
      Qualifier<T> declaredQualifier = beanDefinition.getDeclaredQualifier();
      boolean propagateQualifier = beanDefinition.isProxy() && declaredQualifier instanceof Named;
      Qualifier prevQualifier = resolutionContext.getCurrentQualifier();

      ParametrizedBeanFactory<T> parametrizedBeanFactory;
      try {
         if (propagateQualifier) {
            resolutionContext.setAttribute(BeanDefinition.NAMED_ATTRIBUTE, ((Named)declaredQualifier).getName());
         }

         resolutionContext.setCurrentQualifier(declaredQualifier != null && !AnyQualifier.INSTANCE.equals(declaredQualifier) ? declaredQualifier : qualifier);
         T bean;
         if (beanFactory instanceof ParametrizedBeanFactory) {
            parametrizedBeanFactory = (ParametrizedBeanFactory)beanDefinition;
            Map<String, Object> convertedValues = this.getRequiredArgumentValues(
               resolutionContext, parametrizedBeanFactory.getRequiredArguments(), argumentValues, beanDefinition
            );
            bean = parametrizedBeanFactory.build(resolutionContext, this, beanDefinition, convertedValues);
         } else {
            bean = beanFactory.build(resolutionContext, this, beanDefinition);
         }

         if (bean == null) {
            throw new BeanInstantiationException(resolutionContext, "Bean Factory [" + beanFactory + "] returned null");
         }

         if (bean instanceof Qualified) {
            ((Qualified)bean).$withBeanQualifier(declaredQualifier);
         }

         parametrizedBeanFactory = bean;
      } catch (DisabledBeanException | BeanInstantiationException | DependencyInjectionException var16) {
         throw var16;
      } catch (Throwable var17) {
         if (!resolutionContext.getPath().isEmpty()) {
            throw new BeanInstantiationException(resolutionContext, var17);
         }

         throw new BeanInstantiationException(beanDefinition, var17);
      } finally {
         resolutionContext.setCurrentQualifier(prevQualifier);
         if (propagateQualifier) {
            resolutionContext.removeAttribute(BeanDefinition.NAMED_ATTRIBUTE);
         }

      }

      return (T)parametrizedBeanFactory;
   }

   @NonNull
   private <T> T postBeanCreated(
      @NonNull BeanResolutionContext resolutionContext, @NonNull BeanDefinition<T> beanDefinition, @Nullable Qualifier<T> qualifier, @NonNull T bean
   ) {
      Qualifier<T> finalQualifier = qualifier != null ? qualifier : beanDefinition.getDeclaredQualifier();
      bean = this.triggerBeanCreatedEventListener(resolutionContext, beanDefinition, bean, finalQualifier);
      if (beanDefinition instanceof ValidatedBeanDefinition) {
         bean = ((ValidatedBeanDefinition)beanDefinition).validate(resolutionContext, bean);
      }

      if (LOG_LIFECYCLE.isDebugEnabled()) {
         LOG_LIFECYCLE.debug("Created bean [{}] from definition [{}] with qualifier [{}]", bean, beanDefinition, finalQualifier);
      }

      return bean;
   }

   @NonNull
   private <T> T triggerBeanCreatedEventListener(
      @NonNull BeanResolutionContext resolutionContext, @NonNull BeanDefinition<T> beanDefinition, @NonNull T bean, @Nullable Qualifier<T> finalQualifier
   ) {
      Class<T> beanType = beanDefinition.getBeanType();
      if (!(bean instanceof BeanCreatedEventListener) && CollectionUtils.isNotEmpty(this.beanCreationEventListeners)) {
         for(Entry<Class<?>, List<BeanCreatedEventListener<?>>> entry : this.beanCreationEventListeners) {
            if (((Class)entry.getKey()).isAssignableFrom(beanType)) {
               DefaultBeanContext.BeanKey<T> beanKey = new DefaultBeanContext.BeanKey<>(beanDefinition, finalQualifier);

               for(BeanCreatedEventListener<?> listener : (List)entry.getValue()) {
                  bean = (T)listener.onCreated(new BeanCreatedEvent<>(this, beanDefinition, beanKey, bean));
                  if (bean == null) {
                     throw new BeanInstantiationException(resolutionContext, "Listener [" + listener + "] returned null from onCreated event");
                  }
               }
            }
         }
      }

      return bean;
   }

   @NonNull
   private <T> Map<String, Object> getRequiredArgumentValues(
      @NonNull BeanResolutionContext resolutionContext,
      @NonNull Argument<?>[] requiredArguments,
      @Nullable Map<String, Object> argumentValues,
      @NonNull BeanDefinition<T> beanDefinition
   ) {
      Map<String, Object> convertedValues;
      if (argumentValues == null) {
         convertedValues = requiredArguments.length == 0 ? null : new LinkedHashMap();
         argumentValues = Collections.emptyMap();
      } else {
         convertedValues = new LinkedHashMap();
      }

      if (convertedValues != null) {
         for(Argument<?> requiredArgument : requiredArguments) {
            String argumentName = requiredArgument.getName();
            Object val = argumentValues.get(argumentName);
            if (val == null) {
               if (!requiredArgument.isDeclaredNullable()) {
                  throw new BeanInstantiationException(
                     resolutionContext,
                     "Missing bean argument ["
                        + requiredArgument
                        + "] for type: "
                        + beanDefinition.getBeanType().getName()
                        + ". Required arguments: "
                        + ArrayUtils.toString(requiredArguments)
                  );
               }
            } else {
               Object convertedValue;
               if (requiredArgument.getType().isInstance(val)) {
                  convertedValue = val;
               } else {
                  convertedValue = ConversionService.SHARED
                     .convert(val, requiredArgument)
                     .orElseThrow(
                        () -> new BeanInstantiationException(
                              resolutionContext,
                              "Invalid bean argument ["
                                 + requiredArgument
                                 + "]. Cannot convert object ["
                                 + val
                                 + "] to required type: "
                                 + requiredArgument.getType()
                           )
                     );
               }

               convertedValues.put(argumentName, convertedValue);
            }
         }

         return convertedValues;
      } else {
         return Collections.emptyMap();
      }
   }

   @NonNull
   protected <T> BeanDefinition<T> findConcreteCandidate(
      @NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier, @NonNull Collection<BeanDefinition<T>> candidates
   ) {
      if (qualifier instanceof AnyQualifier) {
         return (BeanDefinition<T>)candidates.iterator().next();
      } else {
         throw new NonUniqueBeanException(beanType, candidates.iterator());
      }
   }

   protected void processParallelBeans(List<BeanDefinitionReference> parallelBeans) {
      if (!parallelBeans.isEmpty()) {
         List<BeanDefinitionReference> finalParallelBeans = (List)parallelBeans.stream().filter(bdr -> bdr.isEnabled(this)).collect(Collectors.toList());
         if (!finalParallelBeans.isEmpty()) {
            new Thread(
                  () -> {
                     Collection<BeanDefinition> parallelDefinitions = new ArrayList();
                     finalParallelBeans.forEach(
                        beanDefinitionReference -> {
                           try {
                              this.loadContextScopeBean(beanDefinitionReference, parallelDefinitions::add);
                           } catch (Throwable var5) {
                              LOG.error("Parallel Bean definition [" + beanDefinitionReference.getName() + "] could not be loaded: " + var5.getMessage(), var5);
                              Boolean shutdownOnError = (Boolean)beanDefinitionReference.getAnnotationMetadata()
                                 .booleanValue(Parallel.class, "shutdownOnError")
                                 .orElse(true);
                              if (shutdownOnError) {
                                 this.stop();
                              }
                           }
         
                        }
                     );
                     this.filterProxiedTypes(parallelDefinitions, true, false, null);
                     this.filterReplacedBeans(null, parallelDefinitions);
                     parallelDefinitions.forEach(
                        beanDefinition -> ForkJoinPool.commonPool()
                              .execute(
                                 () -> {
                                    try {
                                       this.loadContextScopeBean(beanDefinition);
                                    } catch (Throwable var4) {
                                       LOG.error("Parallel Bean definition [" + beanDefinition.getName() + "] could not be loaded: " + var4.getMessage(), var4);
                                       Boolean shutdownOnError = (Boolean)beanDefinition.getAnnotationMetadata()
                                          .booleanValue(Parallel.class, "shutdownOnError")
                                          .orElse(true);
                                       if (shutdownOnError) {
                                          this.stop();
                                       }
                                    }
               
                                 }
                              )
                     );
                     parallelDefinitions.clear();
                  }
               )
               .start();
         }
      }

   }

   private <T> void filterReplacedBeans(BeanResolutionContext resolutionContext, Collection<? extends BeanType<T>> candidates) {
      if (candidates.size() > 1) {
         List<BeanType<T>> replacementTypes = new ArrayList(2);

         for(BeanType<T> candidate : candidates) {
            if (candidate.getAnnotationMetadata().hasStereotype(REPLACES_ANN)) {
               replacementTypes.add(candidate);
            }
         }

         if (!replacementTypes.isEmpty()) {
            candidates.removeIf(definition -> this.checkIfReplacementExists(resolutionContext, replacementTypes, definition));
         }
      }

   }

   private <T> boolean checkIfReplacementExists(BeanResolutionContext resolutionContext, List<BeanType<T>> replacementTypes, BeanType<T> definitionToBeReplaced) {
      if (!definitionToBeReplaced.isEnabled(this, resolutionContext)) {
         return true;
      } else {
         AnnotationMetadata annotationMetadata = definitionToBeReplaced.getAnnotationMetadata();
         if (annotationMetadata.hasDeclaredStereotype(Infrastructure.class)) {
            return false;
         } else {
            for(BeanType<T> replacementType : replacementTypes) {
               if (this.isNotTheSameDefinition(replacementType, definitionToBeReplaced)
                  && this.isNotProxy(replacementType, definitionToBeReplaced)
                  && this.checkIfReplaces(replacementType, definitionToBeReplaced, annotationMetadata)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   private <T> boolean isNotTheSameDefinition(BeanType<T> replacingCandidate, BeanType<T> definitionToBeReplaced) {
      return replacingCandidate != definitionToBeReplaced;
   }

   private <T> boolean isNotProxy(BeanType<T> replacingCandidate, BeanType<T> definitionToBeReplaced) {
      return !(replacingCandidate instanceof ProxyBeanDefinition)
         || ((ProxyBeanDefinition)replacingCandidate).getTargetDefinitionType() != definitionToBeReplaced.getClass();
   }

   private <T> boolean checkIfReplaces(BeanType<T> replacingCandidate, BeanType<T> definitionToBeReplaced, AnnotationMetadata annotationMetadata) {
      AnnotationValue<Replaces> replacesAnnotation = replacingCandidate.getAnnotation(Replaces.class);
      Class replacedBeanType = (Class)replacesAnnotation.classValue().orElse(this.<T>getCanonicalBeanType(replacingCandidate));
      Optional<String> named = replacesAnnotation.stringValue("named");
      Optional<AnnotationClassValue<?>> qualifier = replacesAnnotation.annotationClassValue("qualifier");
      if (named.isPresent() && qualifier.isPresent()) {
         throw new ConfigurationException("Both \"named\" and \"qualifier\" should not be present: " + replacesAnnotation);
      } else if (named.isPresent()) {
         String name = (String)named.get();
         if (this.qualifiedByNamed(definitionToBeReplaced, replacedBeanType, name)) {
            if (LOG.isDebugEnabled()) {
               LOG.debug(
                  "Bean [{}] replaces existing bean of type [{}] qualified by name [{}]",
                  replacingCandidate.getBeanType(),
                  definitionToBeReplaced.getBeanType(),
                  name
               );
            }

            return true;
         } else {
            return false;
         }
      } else if (qualifier.isPresent()) {
         AnnotationClassValue<?> qualifierClassValue = (AnnotationClassValue)qualifier.get();
         if (this.qualifiedByQualifier(definitionToBeReplaced, replacedBeanType, qualifierClassValue)) {
            if (LOG.isDebugEnabled()) {
               LOG.debug(
                  "Bean [{}] replaces existing bean of type [{}] qualified by qualifier [{}]",
                  replacingCandidate.getBeanType(),
                  definitionToBeReplaced.getBeanType(),
                  qualifierClassValue
               );
            }

            return true;
         } else {
            return false;
         }
      } else {
         Optional<Class<?>> factory = replacesAnnotation.classValue("factory");
         Optional<Class<?>> declaringType = definitionToBeReplaced instanceof BeanDefinition
            ? ((BeanDefinition)definitionToBeReplaced).getDeclaringType()
            : Optional.empty();
         if (factory.isPresent() && declaringType.isPresent()) {
            boolean factoryReplaces = factory.get() == declaringType.get()
               && this.checkIfTypeMatches(definitionToBeReplaced, annotationMetadata, replacedBeanType);
            if (factoryReplaces) {
               if (LOG.isDebugEnabled()) {
                  LOG.debug(
                     "Bean [{}] replaces existing bean of type [{}] in factory type [{}]", replacingCandidate.getBeanType(), replacedBeanType, factory.get()
                  );
               }

               return true;
            } else {
               return false;
            }
         } else {
            boolean isTypeMatches = this.checkIfTypeMatches(definitionToBeReplaced, annotationMetadata, replacedBeanType);
            if (isTypeMatches && LOG.isDebugEnabled()) {
               LOG.debug("Bean [{}] replaces existing bean of type [{}]", replacingCandidate.getBeanType(), replacedBeanType);
            }

            return isTypeMatches;
         }
      }
   }

   private <T> boolean qualifiedByQualifier(BeanType<T> definitionToBeReplaced, Class<T> replacedBeanType, AnnotationClassValue<?> qualifier) {
      Class<? extends Annotation> qualifierClass = (Class)qualifier.getType().orElse(null);
      if (qualifierClass != null && !qualifierClass.isAssignableFrom(Annotation.class)) {
         return Qualifiers.byStereotype(qualifierClass).qualify(replacedBeanType, Stream.of(definitionToBeReplaced)).isPresent();
      } else {
         throw new ConfigurationException(String.format("Default qualifier value was used while replacing %s", replacedBeanType));
      }
   }

   private <T> boolean qualifiedByNamed(BeanType<T> definitionToBeReplaced, Class replacedBeanType, String named) {
      return Qualifiers.byName(named).qualify(replacedBeanType, Stream.of(definitionToBeReplaced)).isPresent();
   }

   private <T> Class<T> getCanonicalBeanType(BeanType<T> beanType) {
      if (beanType instanceof AdvisedBeanType) {
         return (Class<T>)((AdvisedBeanType)beanType).getInterceptedType();
      } else if (beanType instanceof ProxyBeanDefinition) {
         return ((ProxyBeanDefinition)beanType).getTargetType();
      } else {
         AnnotationMetadata annotationMetadata = beanType.getAnnotationMetadata();
         Class<T> bt = beanType.getBeanType();
         if (annotationMetadata.hasStereotype("io.micronaut.aop.Introduction")) {
            Class<? super T> superclass = bt.getSuperclass();
            return superclass != Object.class && superclass != null ? superclass : bt;
         } else if (annotationMetadata.hasStereotype("io.micronaut.aop.Around")) {
            Class<? super T> superclass = bt.getSuperclass();
            return superclass != null ? superclass : bt;
         } else {
            return bt;
         }
      }
   }

   private <T> boolean checkIfTypeMatches(BeanType<T> definitionToBeReplaced, AnnotationMetadata annotationMetadata, Class replacingCandidate) {
      Class<T> bt;
      if (definitionToBeReplaced instanceof ProxyBeanDefinition) {
         bt = ((ProxyBeanDefinition)definitionToBeReplaced).getTargetType();
      } else if (definitionToBeReplaced instanceof AdvisedBeanType) {
         bt = (Class<T>)((AdvisedBeanType)definitionToBeReplaced).getInterceptedType();
      } else {
         bt = definitionToBeReplaced.getBeanType();
         if (annotationMetadata.hasStereotype("io.micronaut.aop.Introduction")) {
            Class<? super T> superclass = bt.getSuperclass();
            if (superclass == Object.class) {
               return replacingCandidate.isAssignableFrom(bt);
            }

            return replacingCandidate == superclass;
         }

         if (annotationMetadata.hasStereotype("io.micronaut.aop.Around")) {
            Class<? super T> superclass = bt.getSuperclass();
            return replacingCandidate == superclass || replacingCandidate == bt;
         }
      }

      if (annotationMetadata.hasAnnotation(DefaultImplementation.class)) {
         Optional<Class> defaultImpl = annotationMetadata.classValue(DefaultImplementation.class);
         if (!defaultImpl.isPresent()) {
            defaultImpl = annotationMetadata.classValue(DefaultImplementation.class, "name");
         }

         if (defaultImpl.filter(impl -> impl == bt).isPresent()) {
            return replacingCandidate.isAssignableFrom(bt);
         } else {
            return replacingCandidate == bt;
         }
      } else {
         return replacingCandidate != Object.class && replacingCandidate.isAssignableFrom(bt);
      }
   }

   private <T> void doInject(BeanResolutionContext resolutionContext, T instance, BeanDefinition definition) {
      definition.inject(resolutionContext, this, instance);
      if (definition instanceof InitializingBeanDefinition) {
         ((InitializingBeanDefinition)definition).initialize(resolutionContext, this, instance);
      }

   }

   private void loadContextScopeBean(BeanDefinitionReference contextScopeBean, Consumer<BeanDefinition> beanDefinitionConsumer) {
      if (contextScopeBean.isEnabled(this)) {
         BeanDefinition beanDefinition = contextScopeBean.load(this);

         try (BeanResolutionContext resolutionContext = this.newResolutionContext(beanDefinition, null)) {
            if (beanDefinition.isEnabled(this, resolutionContext)) {
               beanDefinitionConsumer.accept(beanDefinition);
            }
         }
      }

   }

   private void loadContextScopeBean(BeanDefinition beanDefinition) {
      if (!beanDefinition.isIterable() && !beanDefinition.hasStereotype(ConfigurationReader.class.getName())) {
         this.findOrCreateSingletonBeanRegistration(null, beanDefinition, beanDefinition.asArgument(), null);
      } else {
         for(BeanDefinition beanCandidate : this.transformIterables(null, Collections.singleton(beanDefinition), true)) {
            this.findOrCreateSingletonBeanRegistration(
               null, beanCandidate, beanCandidate.asArgument(), beanCandidate.hasAnnotation(Context.class) ? null : beanDefinition.getDeclaredQualifier()
            );
         }
      }

   }

   @Nullable
   private <T> BeanRegistration<T> resolveBeanRegistration(
      @Nullable BeanResolutionContext resolutionContext, @NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier, boolean throwNoSuchBean
   ) {
      Class<T> beanClass = beanType.getType();
      if (this.thisInterfaces.contains(beanClass)) {
         return new BeanRegistration<>(BeanIdentifier.of(beanClass.getName()), null, (T)this);
      } else if (InjectionPoint.class.isAssignableFrom(beanClass)) {
         return this.provideInjectionPoint(resolutionContext, beanType, qualifier, throwNoSuchBean);
      } else {
         DefaultBeanContext.BeanKey<T> beanKey = new DefaultBeanContext.BeanKey<>(beanType, qualifier);
         if (LOG.isTraceEnabled()) {
            LOG.trace("Looking up existing bean for key: {}", beanKey);
         }

         BeanRegistration<T> inFlightBeanRegistration = resolutionContext != null ? resolutionContext.getInFlightBean(beanKey) : null;
         if (inFlightBeanRegistration != null) {
            return inFlightBeanRegistration;
         } else {
            BeanRegistration<T> beanRegistration = this.singletonScope.findCachedSingletonBeanRegistration(beanType, qualifier);
            if (beanRegistration != null) {
               return beanRegistration;
            } else {
               Optional<BeanDefinition<T>> concreteCandidate = this.findBeanDefinition(beanType, qualifier);
               BeanRegistration<T> registration;
               if (concreteCandidate.isPresent()) {
                  BeanDefinition<T> definition = (BeanDefinition)concreteCandidate.get();
                  if (definition.isContainerType() && beanClass != definition.getBeanType()) {
                     throw new NonUniqueBeanException(beanClass, Collections.singletonList(definition).iterator());
                  }

                  registration = this.resolveBeanRegistration(resolutionContext, definition, beanType, qualifier);
               } else {
                  registration = null;
               }

               if ((registration == null || registration.bean == null) && throwNoSuchBean) {
                  throw new NoSuchBeanException(beanType, qualifier);
               } else {
                  return registration;
               }
            }
         }
      }
   }

   @Nullable
   private <T> BeanRegistration<T> provideInjectionPoint(
      BeanResolutionContext resolutionContext, Argument<T> beanType, Qualifier<T> qualifier, boolean throwNoSuchBean
   ) {
      BeanResolutionContext.Path path = resolutionContext != null ? resolutionContext.getPath() : null;
      BeanResolutionContext.Segment<?> injectionPointSegment = null;
      if (CollectionUtils.isNotEmpty(path)) {
         Iterator<BeanResolutionContext.Segment<?>> i = path.iterator();
         injectionPointSegment = (BeanResolutionContext.Segment)i.next();
         BeanResolutionContext.Segment<?> segment = null;
         if (i.hasNext()) {
            segment = (BeanResolutionContext.Segment)i.next();
            if (segment.getDeclaringType().hasStereotype("io.micronaut.aop.Introduction")) {
               segment = i.hasNext() ? (BeanResolutionContext.Segment)i.next() : null;
            }
         }

         if (segment != null) {
            T ip = segment.getInjectionPoint();
            if (ip != null && beanType.isInstance(ip)) {
               return new BeanRegistration<>(BeanIdentifier.of(InjectionPoint.class.getName()), null, ip);
            }
         }
      }

      if (injectionPointSegment == null || !injectionPointSegment.getArgument().isNullable()) {
         throw new BeanContextException("Failed to obtain injection point. No valid injection path present in path: " + path);
      } else if (throwNoSuchBean) {
         throw new NoSuchBeanException(beanType, qualifier);
      } else {
         return null;
      }
   }

   @NonNull
   private <T> BeanRegistration<T> resolveBeanRegistration(@Nullable BeanResolutionContext resolutionContext, @NonNull BeanDefinition<T> definition) {
      return this.resolveBeanRegistration(resolutionContext, definition, definition.asArgument(), definition.getDeclaredQualifier());
   }

   @NonNull
   private <T> BeanRegistration<T> resolveBeanRegistration(
      @Nullable BeanResolutionContext resolutionContext, @NonNull BeanDefinition<T> definition, @NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier
   ) {
      boolean isScopedProxyDefinition = definition.hasStereotype("io.micronaut.runtime.context.scope.ScopedProxy");
      if (qualifier != null && AnyQualifier.INSTANCE.equals(definition.getDeclaredQualifier())) {
         definition = BeanDefinitionDelegate.create(definition, qualifier);
      }

      if (definition.isSingleton() && !isScopedProxyDefinition) {
         return this.findOrCreateSingletonBeanRegistration(resolutionContext, definition, beanType, qualifier);
      } else {
         boolean isProxy = definition.isProxy();
         if (!isProxy || !isScopedProxyDefinition || qualifier != null && qualifier.contains(PROXY_TARGET_QUALIFIER)) {
            CustomScope<?> customScope = this.findCustomScope(resolutionContext, definition, isProxy, isScopedProxyDefinition);
            if (customScope != null) {
               if (isProxy) {
                  definition = this.getProxyTargetBeanDefinition(beanType, qualifier);
               }

               return this.getOrCreateScopedRegistration(resolutionContext, customScope, qualifier, beanType, definition);
            } else {
               return this.createRegistration(resolutionContext, beanType, qualifier, definition, true);
            }
         } else {
            Qualifier<T> q = qualifier;
            if (qualifier == null) {
               q = definition.getDeclaredQualifier();
            }

            BeanRegistration<T> registration = this.createRegistration(resolutionContext, beanType, q, definition, true);
            T bean = registration.bean;
            if (bean instanceof Qualified) {
               ((Qualified)bean).$withBeanQualifier(q);
            }

            return registration;
         }
      }
   }

   @NonNull
   private <T> BeanRegistration<T> findOrCreateSingletonBeanRegistration(
      @Nullable BeanResolutionContext resolutionContext, @NonNull BeanDefinition<T> definition, @NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier
   ) {
      BeanRegistration<T> beanRegistration = this.singletonScope.findBeanRegistration(definition, beanType, qualifier);
      return beanRegistration != null ? beanRegistration : this.singletonScope.getOrCreate(this, resolutionContext, definition, beanType, qualifier);
   }

   @Nullable
   private <T> CustomScope<?> findCustomScope(
      @Nullable BeanResolutionContext resolutionContext, @NonNull BeanDefinition<T> definition, boolean isProxy, boolean isScopedProxyDefinition
   ) {
      Optional<Class<? extends Annotation>> scope = definition.getScope();
      if (scope.isPresent()) {
         Class<? extends Annotation> scopeAnnotation = (Class)scope.get();
         if (scopeAnnotation == Prototype.class) {
            return null;
         }

         CustomScope<?> customScope = (CustomScope)this.customScopeRegistry.findScope(scopeAnnotation).orElse(null);
         if (customScope != null) {
            return customScope;
         }
      } else {
         Optional<String> scopeName = definition.getScopeName();
         if (scopeName.isPresent()) {
            String scopeAnnotation = (String)scopeName.get();
            if (Prototype.class.getName().equals(scopeAnnotation)) {
               return null;
            }

            CustomScope<?> customScope = (CustomScope)this.customScopeRegistry.findScope(scopeAnnotation).orElse(null);
            if (customScope != null) {
               return customScope;
            }
         }
      }

      if (resolutionContext != null) {
         BeanResolutionContext.Segment<?> currentSegment = (BeanResolutionContext.Segment)resolutionContext.getPath().currentSegment().orElse(null);
         if (currentSegment != null) {
            Argument<?> argument = currentSegment.getArgument();
            CustomScope<?> customScope = (CustomScope)this.customScopeRegistry.findDeclaredScope(argument).orElse(null);
            if (customScope != null) {
               return customScope;
            }
         }
      }

      return isScopedProxyDefinition && isProxy ? null : (CustomScope)this.customScopeRegistry.findDeclaredScope(definition).orElse(null);
   }

   @NonNull
   private <T> BeanRegistration<T> getOrCreateScopedRegistration(
      @Nullable BeanResolutionContext resolutionContext,
      @NonNull CustomScope<?> registeredScope,
      @Nullable Qualifier<T> qualifier,
      @NonNull Argument<T> beanType,
      @NonNull BeanDefinition<T> definition
   ) {
      final DefaultBeanContext.BeanKey<T> beanKey = new DefaultBeanContext.BeanKey<>(beanType, qualifier);
      T bean = registeredScope.getOrCreate(new BeanCreationContext<T>() {
         @NonNull
         @Override
         public BeanDefinition<T> definition() {
            return definition;
         }

         @NonNull
         @Override
         public BeanIdentifier id() {
            return beanKey;
         }

         @NonNull
         @Override
         public CreatedBean<T> create() throws BeanCreationException {
            return DefaultBeanContext.this.createRegistration(resolutionContext, beanType, qualifier, definition, true);
         }
      });
      return BeanRegistration.of(this, beanKey, definition, bean);
   }

   @NonNull
   @Internal
   final <T> BeanRegistration<T> createRegistration(
      @Nullable BeanResolutionContext resolutionContext,
      @NonNull Argument<T> beanType,
      @Nullable Qualifier<T> qualifier,
      @NonNull BeanDefinition<T> definition,
      boolean dependent
   ) {
      BeanRegistration var16;
      try (BeanResolutionContext context = this.newResolutionContext(definition, resolutionContext)) {
         BeanResolutionContext.Path path = context.getPath();
         boolean isNewPath = path.isEmpty();
         if (isNewPath) {
            path.pushBeanCreate(definition, beanType);
         }

         try {
            List<BeanRegistration<?>> parentDependentBeans = context.popDependentBeans();
            T bean = this.doCreateBean(context, definition, qualifier);
            BeanRegistration<?> dependentFactoryBean = context.getAndResetDependentFactoryBean();
            if (dependentFactoryBean != null) {
               this.destroyBean(dependentFactoryBean);
            }

            DefaultBeanContext.BeanKey<T> beanKey = new DefaultBeanContext.BeanKey<>(beanType, qualifier);
            List<BeanRegistration<?>> dependentBeans = context.getAndResetDependentBeans();
            BeanRegistration<T> beanRegistration = BeanRegistration.of(this, beanKey, definition, bean, dependentBeans);
            context.pushDependentBeans(parentDependentBeans);
            if (dependent) {
               context.addDependentBean(beanRegistration);
            }

            var16 = beanRegistration;
         } finally {
            if (isNewPath) {
               path.pop();
            }

         }
      }

      return var16;
   }

   private <T> Optional<BeanDefinition<T>> findConcreteCandidate(
      @Nullable BeanResolutionContext resolutionContext, @NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier, boolean throwNonUnique
   ) {
      if (beanType.getType() == Object.class && qualifier == null) {
         return Optional.empty();
      } else {
         DefaultBeanContext.BeanCandidateKey bk = new DefaultBeanContext.BeanCandidateKey<>(beanType, qualifier, throwNonUnique);
         Optional beanDefinition = (Optional)this.beanConcreteCandidateCache.get(bk);
         if (beanDefinition == null) {
            beanDefinition = this.findConcreteCandidateNoCache(resolutionContext, beanType, qualifier, throwNonUnique, true);
            this.beanConcreteCandidateCache.put(bk, beanDefinition);
         }

         return beanDefinition;
      }
   }

   private <T> Optional<BeanDefinition<T>> findConcreteCandidateNoCache(
      @Nullable BeanResolutionContext resolutionContext,
      @NonNull Argument<T> beanType,
      @Nullable Qualifier<T> qualifier,
      boolean throwNonUnique,
      boolean filterProxied
   ) {
      Predicate<BeanDefinition<T>> predicate = new Predicate<BeanDefinition<T>>() {
         public boolean test(BeanDefinition<T> candidate) {
            if (candidate.isAbstract()) {
               return false;
            } else if (qualifier != null && candidate instanceof NoInjectionBeanDefinition) {
               NoInjectionBeanDefinition noInjectionBeanDefinition = (NoInjectionBeanDefinition)candidate;
               return qualifier.contains(noInjectionBeanDefinition.getQualifier());
            } else {
               return true;
            }
         }
      };
      Collection<BeanDefinition<T>> candidates = new ArrayList(this.findBeanCandidates(resolutionContext, beanType, filterProxied, predicate));
      if (candidates.isEmpty()) {
         return Optional.empty();
      } else {
         this.filterProxiedTypes(candidates, filterProxied, false, predicate);
         int size = candidates.size();
         BeanDefinition<T> definition = null;
         if (size > 0) {
            if (qualifier != null) {
               if (LOG.isDebugEnabled()) {
                  LOG.debug("Qualifying bean [{}] for qualifier: {} ", beanType.getName(), qualifier);
               }

               Stream<BeanDefinition<T>> candidateStream = candidates.stream().filter(c -> {
                  if (!c.isAbstract()) {
                     if (c instanceof NoInjectionBeanDefinition) {
                        NoInjectionBeanDefinition noInjectionBeanDefinition = (NoInjectionBeanDefinition)c;
                        return qualifier.contains(noInjectionBeanDefinition.getQualifier());
                     } else {
                        return true;
                     }
                  } else {
                     return false;
                  }
               });
               Stream<BeanDefinition<T>> qualified = qualifier.reduce(beanType.getType(), candidateStream);
               List<BeanDefinition<T>> beanDefinitionList = (List)qualified.collect(Collectors.toList());
               if (beanDefinitionList.isEmpty()) {
                  if (LOG.isDebugEnabled()) {
                     LOG.debug("No qualifying beans of type [{}] found for qualifier: {} ", beanType.getName(), qualifier);
                  }

                  return Optional.empty();
               }

               definition = this.lastChanceResolve(beanType, qualifier, throwNonUnique, beanDefinitionList);
            } else if (candidates.size() == 1) {
               definition = (BeanDefinition)candidates.iterator().next();
            } else {
               if (LOG.isDebugEnabled()) {
                  LOG.debug("Searching for @Primary for type [{}] from candidates: {} ", beanType.getName(), candidates);
               }

               definition = this.lastChanceResolve(beanType, qualifier, throwNonUnique, candidates);
            }
         }

         if (LOG.isDebugEnabled() && definition != null) {
            if (qualifier != null) {
               LOG.debug("Found concrete candidate [{}] for type: {} {} ", definition, qualifier, beanType.getName());
            } else {
               LOG.debug("Found concrete candidate [{}] for type: {} ", definition, beanType.getName());
            }
         }

         return Optional.ofNullable(definition);
      }
   }

   private <T> void filterProxiedTypes(
      Collection<BeanDefinition<T>> candidates, boolean filterProxied, boolean filterDelegates, Predicate<BeanDefinition<T>> predicate
   ) {
      int count = candidates.size();
      Set<Class> proxiedTypes = new HashSet(count);
      Iterator<BeanDefinition<T>> i = candidates.iterator();
      Collection<BeanDefinition<T>> delegates = (Collection<BeanDefinition<T>>)(filterDelegates ? new ArrayList(count) : Collections.emptyList());

      while(i.hasNext()) {
         BeanDefinition<T> candidate = (BeanDefinition)i.next();
         if (candidate instanceof ProxyBeanDefinition) {
            if (filterProxied) {
               proxiedTypes.add(((ProxyBeanDefinition)candidate).getTargetDefinitionType());
            } else {
               proxiedTypes.add(candidate.getClass());
            }
         } else if (candidate instanceof BeanDefinitionDelegate) {
            BeanDefinition<T> delegate = ((BeanDefinitionDelegate)candidate).getDelegate();
            if (filterDelegates) {
               i.remove();
               if (!delegates.contains(delegate) && (predicate == null || predicate.test(delegate))) {
                  delegates.add(delegate);
               }
            } else if (filterProxied && delegate instanceof ProxyBeanDefinition) {
               proxiedTypes.add(((ProxyBeanDefinition)delegate).getTargetDefinitionType());
            }
         }
      }

      if (filterDelegates) {
         candidates.addAll(delegates);
      }

      if (!proxiedTypes.isEmpty()) {
         candidates.removeIf(
            candidatex -> candidatex instanceof BeanDefinitionDelegate
                  ? proxiedTypes.contains(((BeanDefinitionDelegate)candidatex).getDelegate().getClass())
                  : proxiedTypes.contains(candidatex.getClass())
         );
      }

   }

   private <T> BeanDefinition<T> lastChanceResolve(
      Argument<T> beanType, Qualifier<T> qualifier, boolean throwNonUnique, Collection<BeanDefinition<T>> candidates
   ) {
      Class<T> beanClass = beanType.getType();
      if (candidates.size() > 1) {
         List<BeanDefinition<T>> primary = (List)candidates.stream().filter(BeanType::isPrimary).collect(Collectors.toList());
         if (!primary.isEmpty()) {
            candidates = primary;
         }
      }

      if (candidates.size() == 1) {
         return (BeanDefinition<T>)candidates.iterator().next();
      } else {
         BeanDefinition<T> definition = null;
         candidates = (Collection)candidates.stream().filter(candidate -> !candidate.hasDeclaredStereotype(Secondary.class)).collect(Collectors.toList());
         if (candidates.size() == 1) {
            return (BeanDefinition<T>)candidates.iterator().next();
         } else if (candidates.stream().anyMatch(candidate -> candidate.hasAnnotation(Order.class))) {
            Iterator<BeanDefinition<T>> i = candidates.stream().sorted((bean1, bean2) -> {
               int order1 = OrderUtil.getOrder(bean1.getAnnotationMetadata());
               int order2 = OrderUtil.getOrder(bean2.getAnnotationMetadata());
               return Integer.compare(order1, order2);
            }).iterator();
            if (i.hasNext()) {
               BeanDefinition<T> bean = (BeanDefinition)i.next();
               if (i.hasNext()) {
                  BeanDefinition<T> next = (BeanDefinition)i.next();
                  if (OrderUtil.getOrder(bean.getAnnotationMetadata()) == OrderUtil.getOrder(next.getAnnotationMetadata())) {
                     throw new NonUniqueBeanException(beanType.getType(), candidates.iterator());
                  }
               }

               LOG.debug("Picked bean {} with the highest precedence for type {} and qualifier {}", bean, beanType, qualifier);
               return bean;
            } else {
               throw new NonUniqueBeanException(beanType.getType(), candidates.iterator());
            }
         } else {
            Collection<BeanDefinition<T>> exactMatches = this.filterExactMatch(beanClass, candidates);
            if (exactMatches.size() == 1) {
               definition = (BeanDefinition)exactMatches.iterator().next();
            } else if (throwNonUnique) {
               definition = this.findConcreteCandidate(beanClass, qualifier, candidates);
            }

            return definition;
         }
      }
   }

   private void readAllBeanConfigurations() {
      for(BeanConfiguration beanConfiguration : this.resolveBeanConfigurations()) {
         this.registerConfiguration(beanConfiguration);
      }

   }

   private <T> Collection<BeanDefinition<T>> filterExactMatch(final Class<T> beanType, Collection<BeanDefinition<T>> candidates) {
      List<BeanDefinition<T>> list = new ArrayList(candidates.size());

      for(BeanDefinition<T> candidate : candidates) {
         if (candidate.getBeanType() == beanType) {
            list.add(candidate);
         }
      }

      return list;
   }

   private void readAllBeanDefinitionClasses() {
      List<BeanDefinitionReference> contextScopeBeans = new ArrayList(20);
      List<BeanDefinitionReference> processedBeans = new ArrayList(10);
      List<BeanDefinitionReference> parallelBeans = new ArrayList(10);
      List<BeanDefinitionReference> beanDefinitionReferences = this.resolveBeanDefinitionReferences();
      this.beanDefinitionsClasses.addAll(beanDefinitionReferences);
      Set<BeanConfiguration> configurationsDisabled = new HashSet();

      for(BeanConfiguration bc : this.beanConfigurations.values()) {
         if (!bc.isEnabled(this)) {
            configurationsDisabled.add(bc);
         }
      }

      label58:
      for(BeanDefinitionReference beanDefinitionReference : beanDefinitionReferences) {
         for(BeanConfiguration disableConfiguration : configurationsDisabled) {
            if (disableConfiguration.isWithin(beanDefinitionReference)) {
               this.beanDefinitionsClasses.remove(beanDefinitionReference);
               continue label58;
            }
         }

         AnnotationMetadata annotationMetadata = beanDefinitionReference.getAnnotationMetadata();
         Class[] indexes = annotationMetadata.classValues(INDEXES_TYPE);
         if (indexes.length > 0) {
            for(int i = 0; i < indexes.length; ++i) {
               Class indexedType = indexes[i];
               this.resolveTypeIndex(indexedType).add(beanDefinitionReference);
            }
         } else if (annotationMetadata.hasStereotype("io.micronaut.aop.Adapter")) {
            Class aClass = (Class)annotationMetadata.classValue("io.micronaut.aop.Adapter", "value").orElse(null);
            if (this.indexedTypes.contains(aClass)) {
               this.resolveTypeIndex(aClass).add(beanDefinitionReference);
            }
         }

         if (this.isEagerInit(beanDefinitionReference)) {
            contextScopeBeans.add(beanDefinitionReference);
         } else if (annotationMetadata.hasDeclaredStereotype(PARALLEL_TYPE)) {
            parallelBeans.add(beanDefinitionReference);
         }

         if (beanDefinitionReference.requiresMethodProcessing()) {
            processedBeans.add(beanDefinitionReference);
         }
      }

      List<BeanDefinitionReference> var12 = null;
      this.beanConfigurationsList = null;
      this.initializeEventListeners();
      this.initializeContext(contextScopeBeans, processedBeans, parallelBeans);
   }

   private boolean isEagerInit(BeanDefinitionReference beanDefinitionReference) {
      return beanDefinitionReference.isContextScope()
         || this.eagerInitSingletons && beanDefinitionReference.isSingleton()
         || this.eagerInitStereotypesPresent && beanDefinitionReference.getAnnotationMetadata().hasDeclaredStereotype(this.eagerInitStereotypes);
   }

   @NonNull
   private Collection<BeanDefinitionReference> resolveTypeIndex(Class<?> indexedType) {
      return (Collection<BeanDefinitionReference>)this.beanIndex.computeIfAbsent(indexedType, aClass -> {
         this.indexedTypes.add(indexedType);
         return new ArrayList(20);
      });
   }

   private <T> Collection<BeanDefinition<T>> findBeanCandidatesInternal(BeanResolutionContext resolutionContext, Argument<T> beanType) {
      Collection beanDefinitions = (Collection)this.beanCandidateCache.get(beanType);
      if (beanDefinitions == null) {
         beanDefinitions = this.findBeanCandidates(resolutionContext, beanType, true, null);
         this.beanCandidateCache.put(beanType, beanDefinitions);
      }

      return beanDefinitions;
   }

   @Internal
   public <T> BeanRegistration<T> getBeanRegistration(
      @Nullable BeanResolutionContext resolutionContext, @NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier
   ) {
      return this.resolveBeanRegistration(resolutionContext, beanType, qualifier, true);
   }

   @Internal
   public <T> Collection<BeanRegistration<T>> getBeanRegistrations(
      @Nullable BeanResolutionContext resolutionContext, @NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier
   ) {
      boolean hasQualifier = qualifier != null;
      if (LOG.isDebugEnabled()) {
         if (hasQualifier) {
            LOG.debug("Resolving beans for type: {} {} ", qualifier, beanType.getTypeName());
         } else {
            LOG.debug("Resolving beans for type: {}", beanType.getTypeName());
         }
      }

      DefaultBeanContext.BeanKey<T> key = new DefaultBeanContext.BeanKey<>(beanType, qualifier);
      if (LOG.isTraceEnabled()) {
         LOG.trace("Looking up existing beans for key: {}", key);
      }

      DefaultBeanContext.CollectionHolder<T> existing = (DefaultBeanContext.CollectionHolder)this.singletonBeanRegistrations.get(key);
      if (existing != null && existing.registrations != null) {
         this.logResolvedExistingBeanRegistrations(beanType, qualifier, existing.registrations);
         return existing.registrations;
      } else {
         Collection<BeanDefinition<T>> beanDefinitions = this.findBeanCandidatesInternal(resolutionContext, beanType);
         Stream<BeanDefinition<T>> candidateStream = this.applyBeanResolutionFilters(resolutionContext, beanDefinitions.stream());
         if (qualifier != null) {
            candidateStream = qualifier.reduce(beanType.getType(), candidateStream);
         }

         beanDefinitions = (Collection)candidateStream.collect(Collectors.toList());
         Collection<BeanRegistration<T>> beanRegistrations;
         if (beanDefinitions.isEmpty()) {
            beanRegistrations = Collections.emptySet();
         } else {
            boolean allCandidatesAreSingleton = true;

            for(BeanDefinition<T> definition : beanDefinitions) {
               if (!definition.isSingleton()) {
                  allCandidatesAreSingleton = false;
               }
            }

            if (allCandidatesAreSingleton) {
               DefaultBeanContext.CollectionHolder<T> holder = (DefaultBeanContext.CollectionHolder)this.singletonBeanRegistrations
                  .computeIfAbsent(key, beanKey -> new DefaultBeanContext.CollectionHolder());
               synchronized(holder) {
                  if (holder.registrations != null) {
                     this.logResolvedExistingBeanRegistrations(beanType, qualifier, holder.registrations);
                     return holder.registrations;
                  }

                  holder.registrations = this.resolveBeanRegistrations(resolutionContext, beanDefinitions, beanType, qualifier);
                  return holder.registrations;
               }
            }

            beanRegistrations = this.resolveBeanRegistrations(resolutionContext, beanDefinitions, beanType, qualifier);
         }

         if (LOG.isDebugEnabled() && !beanRegistrations.isEmpty()) {
            if (hasQualifier) {
               LOG.debug("Found {} bean registrations for type [{} {}]", beanRegistrations.size(), qualifier, beanType.getName());
            } else {
               LOG.debug("Found {} bean registrations for type [{}]", beanRegistrations.size(), beanType.getName());
            }

            for(BeanRegistration<?> beanRegistration : beanRegistrations) {
               LOG.debug("  {} {}", beanRegistration.definition(), beanRegistration.definition().getDeclaredQualifier());
            }
         }

         return beanRegistrations;
      }
   }

   private <T> Collection<BeanRegistration<T>> resolveBeanRegistrations(
      BeanResolutionContext resolutionContext, Collection<BeanDefinition<T>> beanDefinitions, Argument<T> beanType, Qualifier<T> qualifier
   ) {
      boolean hasOrderAnnotation = false;
      Set<BeanRegistration<T>> beansOfTypeList = new HashSet();

      for(BeanDefinition<T> definition : beanDefinitions) {
         if (!hasOrderAnnotation && definition.hasAnnotation(Order.class)) {
            hasOrderAnnotation = true;
         }

         this.addCandidateToList(resolutionContext, definition, beanType, qualifier, beansOfTypeList);
      }

      Collection<BeanRegistration<T>> result = beansOfTypeList;
      if (beansOfTypeList != Collections.EMPTY_SET) {
         Stream<BeanRegistration<T>> stream = beansOfTypeList.stream();
         if (Ordered.class.isAssignableFrom(beanType.getType())) {
            result = (Collection)stream.sorted(OrderUtil.COMPARATOR).collect(StreamUtils.toImmutableCollection());
         } else {
            if (hasOrderAnnotation) {
               stream = stream.sorted(BEAN_REGISTRATION_COMPARATOR);
            }

            result = (Collection)stream.collect(StreamUtils.toImmutableCollection());
         }
      }

      return result;
   }

   private <T> void logResolvedExistingBeanRegistrations(Argument<T> beanType, Qualifier<T> qualifier, Collection<BeanRegistration<T>> existing) {
      if (LOG.isDebugEnabled()) {
         if (qualifier == null) {
            LOG.debug("Found {} existing beans for type [{}]: {} ", existing.size(), beanType.getName(), existing);
         } else {
            LOG.debug("Found {} existing beans for type [{} {}]: {} ", existing.size(), qualifier, beanType.getName(), existing);
         }
      }

   }

   private <T> Stream<BeanDefinition<T>> applyBeanResolutionFilters(
      @Nullable BeanResolutionContext resolutionContext, Stream<BeanDefinition<T>> candidateStream
   ) {
      BeanResolutionContext.Segment<?> segment = resolutionContext != null ? (BeanResolutionContext.Segment)resolutionContext.getPath().peek() : null;
      if (segment instanceof AbstractBeanResolutionContext.ConstructorSegment || segment instanceof AbstractBeanResolutionContext.MethodSegment) {
         BeanDefinition<?> declaringBean = segment.getDeclaringType();
         candidateStream = candidateStream.filter(c -> {
            if (c.equals(declaringBean)) {
               return false;
            } else if (declaringBean instanceof ProxyBeanDefinition) {
               return !((ProxyBeanDefinition)declaringBean).getTargetDefinitionType().equals(c.getClass());
            } else {
               return true;
            }
         });
      }

      return candidateStream.filter(c -> !c.isAbstract());
   }

   private <T> void addCandidateToList(
      @Nullable BeanResolutionContext resolutionContext,
      @NonNull BeanDefinition<T> candidate,
      @NonNull Argument<T> beanType,
      @Nullable Qualifier<T> qualifier,
      @NonNull Collection<BeanRegistration<T>> beansOfTypeList
   ) {
      BeanRegistration<T> beanRegistration = null;

      try {
         beanRegistration = this.resolveBeanRegistration(resolutionContext, candidate);
         if (LOG.isDebugEnabled()) {
            LOG.debug("Found a registration {} for candidate: {} with qualifier: {}", beanRegistration, candidate, qualifier);
         }
      } catch (DisabledBeanException var12) {
         if (AbstractBeanContextConditional.LOG.isDebugEnabled()) {
            AbstractBeanContextConditional.LOG.debug("Bean of type [{}] disabled for reason: {}", beanType.getTypeName(), var12.getMessage());
         }
      }

      if (beanRegistration != null) {
         if (candidate.isContainerType()) {
            Object container = beanRegistration.bean;
            if (container instanceof Object[]) {
               container = Arrays.asList(container);
            }

            if (container instanceof Iterable) {
               Iterable<Object> iterable = (Iterable)container;
               int i = 0;

               for(Object o : iterable) {
                  if (o != null && beanType.isInstance(o)) {
                     beansOfTypeList.add(
                        BeanRegistration.of(
                           this,
                           new DefaultBeanContext.BeanKey<>(beanType, Qualifiers.byQualifiers(Qualifiers.byName(String.valueOf(i++)), qualifier)),
                           candidate,
                           (T)o
                        )
                     );
                  }
               }
            }
         } else {
            beansOfTypeList.add(beanRegistration);
         }
      }

   }

   private <T> boolean isCandidatePresent(Argument<T> beanType, Qualifier<T> qualifier) {
      Collection<BeanDefinition<T>> candidates = this.findBeanCandidates(null, beanType, true, null);
      if (!candidates.isEmpty()) {
         this.filterReplacedBeans(null, candidates);
         Stream<BeanDefinition<T>> stream = candidates.stream();
         if (qualifier != null && !(qualifier instanceof AnyQualifier)) {
            stream = qualifier.reduce(beanType.getType(), stream);
         }

         return stream.findAny().isPresent();
      } else {
         return false;
      }
   }

   private static <T> List<T> nullSafe(List<T> list) {
      return list == null ? Collections.emptyList() : list;
   }

   private List<BeanRegistration> topologicalSort(Collection<BeanRegistration> beans) {
      Map<Boolean, List<BeanRegistration>> initial = (Map)beans.stream()
         .sorted(Comparator.comparing(s -> s.getBeanDefinition().getRequiredComponents().size()))
         .collect(Collectors.groupingBy(b -> b.getBeanDefinition().getRequiredComponents().isEmpty()));
      List<BeanRegistration> sorted = new ArrayList(nullSafe((List)initial.get(true)));
      List<BeanRegistration> unsorted = new ArrayList(nullSafe((List)initial.get(false)));
      Set<Class> satisfied = new HashSet();
      Set<Class> unsatisfied = new HashSet();

      while(!unsorted.isEmpty()) {
         boolean acyclic = false;
         unsatisfied.clear();
         Iterator<BeanRegistration> i = unsorted.iterator();

         while(i.hasNext()) {
            BeanRegistration bean = (BeanRegistration)i.next();
            boolean found = false;

            for(Class<?> clazz : bean.getBeanDefinition().getRequiredComponents()) {
               if (!satisfied.contains(clazz)) {
                  if (unsatisfied.contains(clazz)
                     || unsorted.stream().map(BeanRegistration::getBeanDefinition).map(BeanDefinition::getBeanType).anyMatch(clazz::isAssignableFrom)) {
                     found = true;
                     unsatisfied.add(clazz);
                     break;
                  }

                  satisfied.add(clazz);
               }
            }

            if (!found) {
               acyclic = true;
               i.remove();
               sorted.add(0, bean);
            }
         }

         if (!acyclic) {
            sorted.add(0, unsorted.remove(0));
         }
      }

      return sorted;
   }

   @NonNull
   @Override
   public MutableConvertibleValues<Object> getAttributes() {
      return MutableConvertibleValues.of(this.attributes);
   }

   @NonNull
   @Override
   public Optional<Object> getAttribute(CharSequence name) {
      return name != null ? Optional.ofNullable(this.attributes.get(name)) : Optional.empty();
   }

   @NonNull
   @Override
   public <T> Optional<T> getAttribute(CharSequence name, Class<T> type) {
      if (name != null) {
         Object o = this.attributes.get(name);
         if (type.isInstance(o)) {
            return Optional.of(o);
         }

         if (o != null) {
            return ConversionService.SHARED.convert(o, type);
         }
      }

      return Optional.empty();
   }

   @NonNull
   public BeanContext setAttribute(@NonNull CharSequence name, @Nullable Object value) {
      if (name != null) {
         if (value != null) {
            this.attributes.put(name, value);
         } else {
            this.attributes.remove(name);
         }
      }

      return this;
   }

   @NonNull
   @Override
   public <T> Optional<T> removeAttribute(@NonNull CharSequence name, @NonNull Class<T> type) {
      Object o = this.attributes.remove(name);
      return type.isInstance(o) ? Optional.of(o) : Optional.empty();
   }

   @Override
   public void finalizeConfiguration() {
      this.readAllBeanConfigurations();
      this.readAllBeanDefinitionClasses();
   }

   private abstract static class AbstractExecutionHandle<T, R> implements MethodExecutionHandle<T, R> {
      protected final ExecutableMethod<T, R> method;

      AbstractExecutionHandle(ExecutableMethod<T, R> method) {
         this.method = method;
      }

      @NonNull
      @Override
      public ExecutableMethod<?, R> getExecutableMethod() {
         return this.method;
      }

      @Override
      public Argument[] getArguments() {
         return this.method.getArguments();
      }

      public String toString() {
         return this.method.toString();
      }

      @Override
      public String getMethodName() {
         return this.method.getMethodName();
      }

      @Override
      public ReturnType<R> getReturnType() {
         return this.method.getReturnType();
      }

      @Override
      public AnnotationMetadata getAnnotationMetadata() {
         return this.method.getAnnotationMetadata();
      }
   }

   static final class BeanCandidateKey<T> {
      private final Argument<T> beanType;
      private final Qualifier<T> qualifier;
      private final boolean throwNonUnique;
      private final int hashCode;

      BeanCandidateKey(Argument<T> argument, Qualifier<T> qualifier, boolean throwNonUnique) {
         this.beanType = argument;
         this.qualifier = qualifier;
         this.hashCode = argument.typeHashCode();
         this.throwNonUnique = throwNonUnique;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            DefaultBeanContext.BeanCandidateKey<?> beanKey = (DefaultBeanContext.BeanCandidateKey)o;
            return this.beanType.equalsType(beanKey.beanType)
               && Objects.equals(this.qualifier, beanKey.qualifier)
               && this.throwNonUnique == beanKey.throwNonUnique;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.hashCode;
      }
   }

   private static final class BeanExecutionHandle<T, R> extends DefaultBeanContext.AbstractExecutionHandle<T, R> {
      private final BeanContext beanContext;
      private final Class<T> beanType;
      private final Qualifier<T> qualifier;
      private final boolean isSingleton;
      private T target;

      BeanExecutionHandle(BeanContext beanContext, Class<T> beanType, Qualifier<T> qualifier, ExecutableMethod<T, R> method) {
         super(method);
         this.beanContext = beanContext;
         this.beanType = beanType;
         this.qualifier = qualifier;
         this.isSingleton = beanContext.findBeanDefinition(beanType, qualifier).map(BeanDefinition::isSingleton).orElse(false);
      }

      @Override
      public T getTarget() {
         T target = this.target;
         if (target == null) {
            synchronized(this) {
               target = this.target;
               if (target == null) {
                  target = this.beanContext.getBean(this.beanType, this.qualifier);
                  this.target = target;
               }
            }
         }

         return target;
      }

      @Override
      public Method getTargetMethod() {
         return this.method.getTargetMethod();
      }

      @Override
      public Class getDeclaringType() {
         return this.beanType;
      }

      @Override
      public R invoke(Object... arguments) {
         if (this.isSingleton) {
            T target = this.getTarget();
            return this.method.invoke(target, arguments);
         } else {
            return this.method.invoke(this.beanContext.getBean(this.beanType, this.qualifier), arguments);
         }
      }
   }

   static final class BeanKey<T> implements BeanIdentifier {
      final Argument<T> beanType;
      private final Qualifier<T> qualifier;
      private final int hashCode;

      BeanKey(BeanDefinition<T> definition, Qualifier<T> qualifier) {
         this(definition.asArgument(), qualifier);
      }

      BeanKey(Argument<T> argument, Qualifier<T> qualifier) {
         this.beanType = argument;
         this.qualifier = qualifier;
         this.hashCode = argument.typeHashCode();
      }

      BeanKey(Class<T> beanType, Qualifier<T> qualifier, @Nullable Class... typeArguments) {
         this(Argument.of(beanType, typeArguments), qualifier);
      }

      public int length() {
         return this.toString().length();
      }

      public char charAt(int index) {
         return this.toString().charAt(index);
      }

      public CharSequence subSequence(int start, int end) {
         return this.toString().subSequence(start, end);
      }

      public String toString() {
         return (this.qualifier != null ? this.qualifier + " " : "") + this.beanType.getName();
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            DefaultBeanContext.BeanKey<?> beanKey = (DefaultBeanContext.BeanKey)o;
            return this.beanType.equalsType(beanKey.beanType) && Objects.equals(this.qualifier, beanKey.qualifier);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.hashCode;
      }

      @Override
      public String getName() {
         return this.qualifier instanceof Named ? ((Named)this.qualifier).getName() : Primary.SIMPLE_NAME;
      }
   }

   private static final class CollectionHolder<T> {
      Collection<BeanRegistration<T>> registrations;

      private CollectionHolder() {
      }
   }

   private static final class ObjectExecutionHandle<T, R> extends DefaultBeanContext.AbstractExecutionHandle<T, R> {
      private final T target;

      ObjectExecutionHandle(T target, ExecutableMethod<T, R> method) {
         super(method);
         this.target = target;
      }

      @Override
      public T getTarget() {
         return this.target;
      }

      @Override
      public R invoke(Object... arguments) {
         return this.method.invoke(this.target, arguments);
      }

      @Override
      public Method getTargetMethod() {
         return this.method.getTargetMethod();
      }

      @Override
      public Class getDeclaringType() {
         return this.target.getClass();
      }
   }

   private final class ScanningBeanResolutionContext extends DefaultBeanContext.SingletonBeanResolutionContext {
      private final HashMap<BeanDefinition<?>, Argument<?>> beanCreationTargets;
      private final Map<BeanDefinition<?>, List<List<Argument<?>>>> foundTargets = new HashMap();

      private ScanningBeanResolutionContext(BeanDefinition<?> beanDefinition, HashMap<BeanDefinition<?>, Argument<?>> beanCreationTargets) {
         super(beanDefinition);
         this.beanCreationTargets = beanCreationTargets;
      }

      private List<Argument<?>> getHierarchy() {
         List<Argument<?>> hierarchy = new ArrayList(this.path.size());
         Iterator<BeanResolutionContext.Segment<?>> it = this.path.descendingIterator();

         while(it.hasNext()) {
            BeanResolutionContext.Segment<?> segment = (BeanResolutionContext.Segment)it.next();
            hierarchy.add(segment.getArgument());
         }

         return hierarchy;
      }

      @Override
      protected void onNewSegment(BeanResolutionContext.Segment<?> segment) {
         Argument<?> argument = segment.getArgument();
         if (argument.isContainerType()) {
            argument = (Argument)argument.getFirstTypeVariable().orElse(null);
            if (argument == null) {
               return;
            }
         }

         if (!argument.isProvider()) {
            for(Entry<BeanDefinition<?>, Argument<?>> entry : this.beanCreationTargets.entrySet()) {
               if (argument.isAssignableFrom((Argument<?>)entry.getValue())) {
                  ((List)this.foundTargets.computeIfAbsent(entry.getKey(), bd -> new ArrayList(5))).add(this.getHierarchy());
               }
            }

         }
      }

      Map<BeanDefinition<?>, List<List<Argument<?>>>> getFoundTargets() {
         return this.foundTargets;
      }
   }

   private class SingletonBeanResolutionContext extends AbstractBeanResolutionContext {
      public SingletonBeanResolutionContext(BeanDefinition<?> beanDefinition) {
         super(DefaultBeanContext.this, beanDefinition);
      }

      @Override
      public BeanResolutionContext copy() {
         DefaultBeanContext.SingletonBeanResolutionContext copy = DefaultBeanContext.this.new SingletonBeanResolutionContext(this.rootDefinition);
         copy.copyStateFrom(this);
         return copy;
      }

      @Override
      public <T> void addInFlightBean(BeanIdentifier beanIdentifier, BeanRegistration<T> beanRegistration) {
         DefaultBeanContext.this.singlesInCreation.put(beanIdentifier, beanRegistration);
      }

      @Override
      public void removeInFlightBean(BeanIdentifier beanIdentifier) {
         DefaultBeanContext.this.singlesInCreation.remove(beanIdentifier);
      }

      @Nullable
      @Override
      public <T> BeanRegistration<T> getInFlightBean(BeanIdentifier beanIdentifier) {
         return (BeanRegistration<T>)DefaultBeanContext.this.singlesInCreation.get(beanIdentifier);
      }
   }
}
