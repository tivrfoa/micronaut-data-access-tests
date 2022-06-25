package io.micronaut.context;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationReader;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.env.BootstrapPropertySourceLocator;
import io.micronaut.context.env.CachedEnvironment;
import io.micronaut.context.env.DefaultEnvironment;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.TypeConverter;
import io.micronaut.core.convert.TypeConverterRegistrar;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.core.naming.Named;
import io.micronaut.core.naming.conventions.StringConvention;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.BeanConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanDefinitionReference;
import io.micronaut.inject.qualifiers.PrimaryQualifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DefaultApplicationContext extends DefaultBeanContext implements ApplicationContext {
   private final ConversionService conversionService;
   private final ClassPathResourceLoader resourceLoader;
   private final ApplicationContextConfiguration configuration;
   private Environment environment;

   public DefaultApplicationContext(@NonNull String... environmentNames) {
      this(ClassPathResourceLoader.defaultLoader(DefaultApplicationContext.class.getClassLoader()), environmentNames);
   }

   public DefaultApplicationContext(@NonNull ClassPathResourceLoader resourceLoader, @NonNull String... environmentNames) {
      this(new ApplicationContextConfiguration() {
         @NonNull
         @Override
         public ClassLoader getClassLoader() {
            return this.getResourceLoader().getClassLoader();
         }

         @NonNull
         @Override
         public ClassPathResourceLoader getResourceLoader() {
            ArgumentUtils.requireNonNull("resourceLoader", resourceLoader);
            return resourceLoader;
         }

         @NonNull
         @Override
         public List<String> getEnvironments() {
            ArgumentUtils.requireNonNull("environmentNames", environmentNames);
            return Arrays.asList(environmentNames);
         }
      });
   }

   public DefaultApplicationContext(@NonNull ApplicationContextConfiguration configuration) {
      super(configuration);
      ArgumentUtils.requireNonNull("configuration", configuration);
      this.configuration = configuration;
      this.conversionService = this.createConversionService();
      this.resourceLoader = configuration.getResourceLoader();
   }

   @NonNull
   @Override
   public <T> ApplicationContext registerSingleton(@NonNull Class<T> type, @NonNull T singleton, @Nullable Qualifier<T> qualifier, boolean inject) {
      return (ApplicationContext)super.registerSingleton(type, singleton, qualifier, inject);
   }

   @NonNull
   protected Environment createEnvironment(@NonNull ApplicationContextConfiguration configuration) {
      return new DefaultApplicationContext.RuntimeConfiguredEnvironment(configuration, this.isBootstrapEnabled(configuration));
   }

   private boolean isBootstrapEnabled(ApplicationContextConfiguration configuration) {
      String bootstrapContextProp = System.getProperty("micronaut.bootstrap.context");
      if (bootstrapContextProp != null) {
         return Boolean.parseBoolean(bootstrapContextProp);
      } else {
         Boolean configBootstrapEnabled = configuration.isBootstrapEnvironmentEnabled();
         return configBootstrapEnabled != null ? configBootstrapEnabled : this.isBootstrapPropertySourceLocatorPresent();
      }
   }

   private boolean isBootstrapPropertySourceLocatorPresent() {
      for(BeanDefinitionReference beanDefinitionReference : this.resolveBeanDefinitionReferences()) {
         if (BootstrapPropertySourceLocator.class.isAssignableFrom(beanDefinitionReference.getBeanType())) {
            return true;
         }
      }

      return false;
   }

   @NonNull
   protected ConversionService createConversionService() {
      return ConversionService.SHARED;
   }

   @NonNull
   @Override
   public ConversionService<?> getConversionService() {
      return this.conversionService;
   }

   @NonNull
   @Override
   public Environment getEnvironment() {
      if (this.environment == null) {
         this.environment = this.createEnvironment(this.configuration);
      }

      return this.environment;
   }

   @NonNull
   @Override
   public synchronized ApplicationContext start() {
      this.startEnvironment();
      return (ApplicationContext)super.start();
   }

   @NonNull
   @Override
   public synchronized ApplicationContext stop() {
      return (ApplicationContext)super.stop();
   }

   @Override
   public boolean containsProperty(String name) {
      return this.getEnvironment().containsProperty(name);
   }

   @Override
   public boolean containsProperties(String name) {
      return this.getEnvironment().containsProperties(name);
   }

   @Override
   public <T> Optional<T> getProperty(String name, ArgumentConversionContext<T> conversionContext) {
      return this.getEnvironment().getProperty(name, conversionContext);
   }

   @NonNull
   @Override
   public Collection<String> getPropertyEntries(@NonNull String name) {
      return this.getEnvironment().getPropertyEntries(name);
   }

   @NonNull
   @Override
   public Map<String, Object> getProperties(@Nullable String name, @Nullable StringConvention keyFormat) {
      return this.getEnvironment().getProperties(name, keyFormat);
   }

   @Override
   protected void registerConfiguration(BeanConfiguration configuration) {
      if (this.getEnvironment().isActive(configuration)) {
         super.registerConfiguration(configuration);
      }

   }

   protected void startEnvironment() {
      Environment defaultEnvironment = this.getEnvironment();
      defaultEnvironment.start();
      this.registerSingleton(Environment.class, defaultEnvironment, null, false);
   }

   @Override
   protected void initializeContext(
      List<BeanDefinitionReference> contextScopeBeans, List<BeanDefinitionReference> processedBeans, List<BeanDefinitionReference> parallelBeans
   ) {
      this.initializeTypeConverters(this);
      super.initializeContext(contextScopeBeans, processedBeans, parallelBeans);
   }

   @Override
   protected <T> Collection<BeanDefinition<T>> findBeanCandidates(
      BeanResolutionContext resolutionContext, Argument<T> beanType, BeanDefinition<?> filter, boolean filterProxied
   ) {
      Collection<BeanDefinition<T>> candidates = super.findBeanCandidates(resolutionContext, beanType, filter, filterProxied);
      return this.transformIterables(resolutionContext, candidates, filterProxied);
   }

   @Override
   protected <T> Collection<BeanDefinition<T>> findBeanCandidates(
      BeanResolutionContext resolutionContext, Argument<T> beanType, boolean filterProxied, Predicate<BeanDefinition<T>> predicate
   ) {
      Collection<BeanDefinition<T>> candidates = super.findBeanCandidates(resolutionContext, beanType, filterProxied, predicate);
      return this.transformIterables(resolutionContext, candidates, filterProxied);
   }

   @Override
   protected <T> Collection<BeanDefinition<T>> transformIterables(
      BeanResolutionContext resolutionContext, Collection<BeanDefinition<T>> candidates, boolean filterProxied
   ) {
      if (candidates.isEmpty()) {
         return candidates;
      } else {
         List<BeanDefinition<T>> transformedCandidates = new ArrayList();

         for(BeanDefinition<T> candidate : candidates) {
            if (candidate.isIterable()) {
               if (candidate.hasDeclaredStereotype(EachProperty.class)) {
                  this.transformEachPropertyBeanDefinition(resolutionContext, candidate, transformedCandidates);
               } else if (candidate.hasDeclaredStereotype(EachBean.class)) {
                  this.transformEachBeanBeanDefinition(resolutionContext, candidate, transformedCandidates, filterProxied);
               }
            } else if (candidate.hasStereotype(ConfigurationReader.class)) {
               this.transformConfigurationReaderBeanDefinition(resolutionContext, candidate, transformedCandidates);
            } else {
               transformedCandidates.add(candidate);
            }
         }

         if (LOG.isDebugEnabled()) {
            LOG.debug("Finalized bean definitions candidates: {}", candidates);

            for(BeanDefinition<?> definition : transformedCandidates) {
               LOG.debug("  {} {} {}", definition.getBeanType(), definition.getDeclaredQualifier(), definition);
            }
         }

         return transformedCandidates;
      }
   }

   private <T> void transformConfigurationReaderBeanDefinition(
      BeanResolutionContext resolutionContext, BeanDefinition<T> candidate, List<BeanDefinition<T>> transformedCandidates
   ) {
      String prefix = (String)candidate.stringValue(ConfigurationReader.class, "prefix").orElse(null);
      if (prefix != null) {
         int mapIndex = prefix.indexOf("*");
         int arrIndex = prefix.indexOf("[*]");
         boolean isList = arrIndex > -1;
         boolean isMap = mapIndex > -1;
         if (isList || isMap) {
            int startIndex = isList ? arrIndex : mapIndex;
            String eachProperty = prefix.substring(0, startIndex);
            if (eachProperty.endsWith(".")) {
               eachProperty = eachProperty.substring(0, eachProperty.length() - 1);
            }

            if (StringUtils.isEmpty(eachProperty)) {
               throw new IllegalArgumentException("Blank value specified to @Each property for bean: " + candidate);
            }

            if (isList) {
               this.transformConfigurationReaderList(resolutionContext, candidate, prefix, eachProperty, transformedCandidates);
            } else {
               this.transformConfigurationReaderMap(resolutionContext, candidate, prefix, eachProperty, transformedCandidates);
            }

            return;
         }
      }

      transformedCandidates.add(candidate);
   }

   private <T> void transformConfigurationReaderMap(
      BeanResolutionContext resolutionContext, BeanDefinition<T> candidate, String prefix, String eachProperty, List<BeanDefinition<T>> transformedCandidates
   ) {
      Map entries = this.getProperty(eachProperty, Map.class, Collections.emptyMap());
      if (!entries.isEmpty()) {
         for(Object key : entries.keySet()) {
            BeanDefinitionDelegate<T> delegate = BeanDefinitionDelegate.create(candidate);
            delegate.put(EachProperty.class.getName(), delegate.getBeanType());
            delegate.put(Named.class.getName(), key.toString());
            if (delegate.isEnabled(this, resolutionContext) && this.containsProperties(prefix.replace("*", key.toString()))) {
               transformedCandidates.add(delegate);
            }
         }
      }

   }

   private <T> void transformConfigurationReaderList(
      BeanResolutionContext resolutionContext, BeanDefinition<T> candidate, String prefix, String eachProperty, List<BeanDefinition<T>> transformedCandidates
   ) {
      List entries = this.getProperty(eachProperty, List.class, Collections.emptyList());
      if (!entries.isEmpty()) {
         for(int i = 0; i < entries.size(); ++i) {
            if (entries.get(i) != null) {
               BeanDefinitionDelegate<T> delegate = BeanDefinitionDelegate.create(candidate);
               String index = String.valueOf(i);
               delegate.put("Array", index);
               delegate.put(Named.class.getName(), index);
               if (delegate.isEnabled(this, resolutionContext) && this.containsProperties(prefix.replace("*", index))) {
                  transformedCandidates.add(delegate);
               }
            }
         }
      }

   }

   private <T> void transformEachBeanBeanDefinition(
      BeanResolutionContext resolutionContext, BeanDefinition<T> candidate, List<BeanDefinition<T>> transformedCandidates, boolean filterProxied
   ) {
      Class dependentType = (Class)candidate.classValue(EachBean.class).orElse(null);
      if (dependentType == null) {
         transformedCandidates.add(candidate);
      } else {
         Collection<BeanDefinition> dependentCandidates = this.findBeanCandidates(resolutionContext, Argument.of(dependentType), filterProxied, null);
         if (!dependentCandidates.isEmpty()) {
            for(BeanDefinition dependentCandidate : dependentCandidates) {
               Qualifier qualifier;
               if (dependentCandidate instanceof BeanDefinitionDelegate) {
                  qualifier = dependentCandidate.resolveDynamicQualifier();
               } else {
                  qualifier = dependentCandidate.getDeclaredQualifier();
               }

               if (qualifier == null && dependentCandidate.isPrimary()) {
                  qualifier = PrimaryQualifier.INSTANCE;
               }

               BeanDefinitionDelegate<?> delegate = BeanDefinitionDelegate.create(candidate, qualifier);
               if (dependentCandidate.isPrimary()) {
                  delegate.put(BeanDefinitionDelegate.PRIMARY_ATTRIBUTE, true);
               }

               if (qualifier != null) {
                  String qualifierKey = "javax.inject.Qualifier";
                  Argument<?>[] arguments = candidate.getConstructor().getArguments();

                  for(Argument<?> argument : arguments) {
                     Class<?> argumentType = argument.getType();
                     if (argumentType.equals(dependentType)) {
                        delegate.put(qualifierKey, Collections.singletonMap(argument, qualifier));
                        break;
                     }
                  }

                  if (qualifier instanceof Named) {
                     delegate.put(Named.class.getName(), ((Named)qualifier).getName());
                  }

                  if (delegate.isEnabled(this, resolutionContext)) {
                     transformedCandidates.add(delegate);
                  }
               }
            }
         }

      }
   }

   private <T> void transformEachPropertyBeanDefinition(
      BeanResolutionContext resolutionContext, BeanDefinition<T> candidate, List<BeanDefinition<T>> transformedCandidates
   ) {
      boolean isList = candidate.booleanValue(EachProperty.class, "list").orElse(false);
      String property = (String)candidate.stringValue(ConfigurationReader.class, "prefix")
         .map(prefix -> prefix.substring(0, prefix.length() - (isList ? 3 : 2)))
         .orElseGet(() -> (String)candidate.stringValue(EachProperty.class).orElse(null));
      String primaryPrefix = (String)candidate.stringValue(EachProperty.class, "primary").orElse(null);
      if (StringUtils.isEmpty(property)) {
         throw new IllegalArgumentException("Blank value specified to @Each property for bean: " + candidate);
      } else {
         if (isList) {
            this.transformEachPropertyOfList(resolutionContext, candidate, primaryPrefix, property, transformedCandidates);
         } else {
            this.transformEachPropertyOfMap(resolutionContext, candidate, primaryPrefix, property, transformedCandidates);
         }

      }
   }

   private <T> void transformEachPropertyOfMap(
      BeanResolutionContext resolutionContext,
      BeanDefinition<T> candidate,
      String primaryPrefix,
      String property,
      List<BeanDefinition<T>> transformedCandidates
   ) {
      for(String key : this.getEnvironment().getPropertyEntries(property)) {
         BeanDefinitionDelegate<T> delegate = BeanDefinitionDelegate.create(candidate);
         if (primaryPrefix != null && primaryPrefix.equals(key)) {
            delegate.put(BeanDefinitionDelegate.PRIMARY_ATTRIBUTE, true);
         }

         delegate.put(EachProperty.class.getName(), delegate.getBeanType());
         delegate.put(Named.class.getName(), key);
         if (delegate.isEnabled(this, resolutionContext)) {
            transformedCandidates.add(delegate);
         }
      }

   }

   private <T> void transformEachPropertyOfList(
      BeanResolutionContext resolutionContext,
      BeanDefinition<T> candidate,
      String primaryPrefix,
      String property,
      List<BeanDefinition<T>> transformedCandidates
   ) {
      List<?> entries = this.getEnvironment().getProperty(property, List.class, Collections.emptyList());
      int i = 0;

      for(Object entry : entries) {
         if (entry != null) {
            BeanDefinitionDelegate<T> delegate = BeanDefinitionDelegate.create(candidate);
            String index = String.valueOf(i);
            if (primaryPrefix != null && primaryPrefix.equals(index)) {
               delegate.put(BeanDefinitionDelegate.PRIMARY_ATTRIBUTE, true);
            }

            delegate.put("Array", index);
            delegate.put(Named.class.getName(), index);
            if (delegate.isEnabled(this, resolutionContext)) {
               transformedCandidates.add(delegate);
            }
         }

         ++i;
      }

   }

   @Override
   protected <T> BeanDefinition<T> findConcreteCandidate(Class<T> beanType, Qualifier<T> qualifier, Collection<BeanDefinition<T>> candidates) {
      if (!(qualifier instanceof Named)) {
         return super.findConcreteCandidate(beanType, qualifier, candidates);
      } else {
         for(BeanDefinition<T> candidate : candidates) {
            if (!candidate.isIterable()) {
               return super.findConcreteCandidate(beanType, qualifier, candidates);
            }
         }

         for(BeanDefinition<T> candidate : candidates) {
            if (candidate instanceof BeanDefinitionDelegate) {
               Qualifier<T> delegateQualifier = candidate.resolveDynamicQualifier();
               if (delegateQualifier != null && delegateQualifier.equals(qualifier)) {
                  return candidate;
               }
            }
         }

         return super.findConcreteCandidate(beanType, qualifier, candidates);
      }
   }

   @Override
   public Optional<String> resolvePlaceholders(String str) {
      return this.getEnvironment().getPlaceholderResolver().resolvePlaceholders(str);
   }

   @Override
   public String resolveRequiredPlaceholders(String str) throws ConfigurationException {
      return this.getEnvironment().getPlaceholderResolver().resolveRequiredPlaceholders(str);
   }

   protected void initializeTypeConverters(BeanContext beanContext) {
      for(BeanRegistration<TypeConverter> typeConverterRegistration : beanContext.getBeanRegistrations(TypeConverter.class)) {
         TypeConverter typeConverter = typeConverterRegistration.getBean();
         List<Argument<?>> typeArguments = typeConverterRegistration.getBeanDefinition().getTypeArguments(TypeConverter.class);
         if (typeArguments.size() == 2) {
            Class<?> source = ((Argument)typeArguments.get(0)).getType();
            Class<?> target = ((Argument)typeArguments.get(1)).getType();
            if (source != Object.class || target != Object.class) {
               this.getConversionService().addConverter(source, target, typeConverter);
            }
         }
      }

      for(TypeConverterRegistrar registrar : beanContext.getBeansOfType(TypeConverterRegistrar.class)) {
         registrar.register(this.conversionService);
      }

   }

   private class BootstrapApplicationContext extends DefaultApplicationContext {
      private final DefaultApplicationContext.BootstrapEnvironment bootstrapEnvironment;

      BootstrapApplicationContext(DefaultApplicationContext.BootstrapEnvironment bootstrapEnvironment, String... activeEnvironments) {
         super(DefaultApplicationContext.this.resourceLoader, activeEnvironments);
         this.bootstrapEnvironment = bootstrapEnvironment;
      }

      @NonNull
      @Override
      public Environment getEnvironment() {
         return this.bootstrapEnvironment;
      }

      @NonNull
      protected DefaultApplicationContext.BootstrapEnvironment createEnvironment(@NonNull ApplicationContextConfiguration configuration) {
         return this.bootstrapEnvironment;
      }

      @NonNull
      @Override
      protected List<BeanDefinitionReference> resolveBeanDefinitionReferences() {
         List<BeanDefinitionReference> refs = DefaultApplicationContext.this.resolveBeanDefinitionReferences();
         List<BeanDefinitionReference> beanDefinitionReferences = new ArrayList(100);

         for(BeanDefinitionReference reference : refs) {
            if (reference.isAnnotationPresent(BootstrapContextCompatible.class)) {
               beanDefinitionReferences.add(reference);
            }
         }

         return beanDefinitionReferences;
      }

      @NonNull
      @Override
      protected Iterable<BeanConfiguration> resolveBeanConfigurations() {
         return DefaultApplicationContext.this.resolveBeanConfigurations();
      }

      @Override
      protected void startEnvironment() {
         this.registerSingleton(Environment.class, this.bootstrapEnvironment, null, false);
      }

      @Override
      protected void initializeEventListeners() {
      }

      @Override
      protected void initializeContext(
         List<BeanDefinitionReference> contextScopeBeans, List<BeanDefinitionReference> processedBeans, List<BeanDefinitionReference> parallelBeans
      ) {
      }

      @Override
      protected void processParallelBeans(List<BeanDefinitionReference> parallelBeans) {
      }

      @Override
      public void publishEvent(@NonNull Object event) {
      }
   }

   private static class BootstrapEnvironment extends DefaultEnvironment {
      private List<PropertySource> propertySourceList;

      BootstrapEnvironment(
         ClassPathResourceLoader resourceLoader,
         ConversionService conversionService,
         ApplicationContextConfiguration configuration,
         String... activeEnvironments
      ) {
         super(new ApplicationContextConfiguration() {
            @Override
            public Optional<Boolean> getDeduceEnvironments() {
               return Optional.of(false);
            }

            @NonNull
            @Override
            public ClassLoader getClassLoader() {
               return resourceLoader.getClassLoader();
            }

            @NonNull
            @Override
            public List<String> getEnvironments() {
               return Arrays.asList(activeEnvironments);
            }

            @Override
            public boolean isEnvironmentPropertySource() {
               return configuration.isEnvironmentPropertySource();
            }

            @Nullable
            @Override
            public List<String> getEnvironmentVariableIncludes() {
               return configuration.getEnvironmentVariableIncludes();
            }

            @Nullable
            @Override
            public List<String> getEnvironmentVariableExcludes() {
               return configuration.getEnvironmentVariableExcludes();
            }

            @NonNull
            @Override
            public ConversionService<?> getConversionService() {
               return conversionService;
            }

            @NonNull
            @Override
            public ClassPathResourceLoader getResourceLoader() {
               return resourceLoader;
            }
         });
      }

      @Override
      protected String getPropertySourceRootName() {
         String bootstrapName = CachedEnvironment.getProperty("micronaut.bootstrap.name");
         return StringUtils.isNotEmpty(bootstrapName) ? bootstrapName : "bootstrap";
      }

      @Override
      protected boolean shouldDeduceEnvironments() {
         return false;
      }

      public List<PropertySource> getRefreshablePropertySources() {
         return this.refreshablePropertySources;
      }

      @Override
      protected List<PropertySource> readPropertySourceList(String name) {
         if (this.propertySourceList == null) {
            this.propertySourceList = (List)super.readPropertySourceList(name)
               .stream()
               .map(DefaultApplicationContext.BootstrapPropertySource::new)
               .collect(Collectors.toList());
         }

         return this.propertySourceList;
      }
   }

   private static class BootstrapPropertySource implements PropertySource {
      private final PropertySource delegate;

      BootstrapPropertySource(PropertySource bootstrapPropertySource) {
         this.delegate = bootstrapPropertySource;
      }

      public String toString() {
         return this.getName();
      }

      @Override
      public PropertySource.PropertyConvention getConvention() {
         return this.delegate.getConvention();
      }

      @Override
      public String getName() {
         return this.delegate.getName();
      }

      @Override
      public Object get(String key) {
         return this.delegate.get(key);
      }

      public Iterator<String> iterator() {
         return this.delegate.iterator();
      }

      @Override
      public int getOrder() {
         return this.delegate.getOrder() + 10;
      }
   }

   private class RuntimeConfiguredEnvironment extends DefaultEnvironment {
      private final ApplicationContextConfiguration configuration;
      private BootstrapPropertySourceLocator bootstrapPropertySourceLocator;
      private DefaultApplicationContext.BootstrapEnvironment bootstrapEnvironment;
      private final boolean bootstrapEnabled;

      RuntimeConfiguredEnvironment(ApplicationContextConfiguration configuration, boolean bootstrapEnabled) {
         super(configuration);
         this.configuration = configuration;
         this.bootstrapEnabled = bootstrapEnabled;
      }

      boolean isRuntimeConfigured() {
         return this.bootstrapEnabled;
      }

      @Override
      public Environment stop() {
         if (this.bootstrapEnvironment != null) {
            this.bootstrapEnvironment.stop();
         }

         return super.stop();
      }

      @Override
      public Environment start() {
         if (this.bootstrapEnvironment == null && this.isRuntimeConfigured()) {
            this.bootstrapEnvironment = this.createBootstrapEnvironment((String[])this.getActiveNames().toArray(new String[0]));
            this.startBootstrapEnvironment();
         }

         return super.start();
      }

      @Override
      protected synchronized List<PropertySource> readPropertySourceList(String name) {
         if (this.bootstrapEnvironment != null) {
            DefaultBeanContext.LOG.info("Reading bootstrap environment configuration");
            this.refreshablePropertySources.addAll(this.bootstrapEnvironment.getRefreshablePropertySources());
            String[] environmentNamesArray = (String[])this.getActiveNames().toArray(new String[0]);
            BootstrapPropertySourceLocator bootstrapPropertySourceLocator = this.resolveBootstrapPropertySourceLocator(environmentNamesArray);

            for(PropertySource propertySource : bootstrapPropertySourceLocator.findPropertySources(this.bootstrapEnvironment)) {
               this.addPropertySource(propertySource);
               this.refreshablePropertySources.add(propertySource);
            }

            for(PropertySource bootstrapPropertySource : this.bootstrapEnvironment.getPropertySources()) {
               this.addPropertySource(bootstrapPropertySource);
            }
         }

         return super.readPropertySourceList(name);
      }

      private BootstrapPropertySourceLocator resolveBootstrapPropertySourceLocator(String... environmentNames) {
         if (this.bootstrapPropertySourceLocator == null) {
            DefaultApplicationContext.BootstrapApplicationContext bootstrapContext = DefaultApplicationContext.this.new BootstrapApplicationContext(
               this.bootstrapEnvironment, environmentNames
            );
            bootstrapContext.start();
            if (bootstrapContext.containsBean(BootstrapPropertySourceLocator.class)) {
               DefaultApplicationContext.this.initializeTypeConverters(bootstrapContext);
               this.bootstrapPropertySourceLocator = bootstrapContext.getBean(BootstrapPropertySourceLocator.class);
            } else {
               this.bootstrapPropertySourceLocator = BootstrapPropertySourceLocator.EMPTY_LOCATOR;
            }
         }

         return this.bootstrapPropertySourceLocator;
      }

      private DefaultApplicationContext.BootstrapEnvironment createBootstrapEnvironment(String... environmentNames) {
         return new DefaultApplicationContext.BootstrapEnvironment(this.resourceLoader, this.conversionService, this.configuration, environmentNames);
      }

      private void startBootstrapEnvironment() {
         for(PropertySource source : this.propertySources.values()) {
            this.bootstrapEnvironment.addPropertySource(source);
         }

         this.bootstrapEnvironment.start();

         for(String pkg : this.bootstrapEnvironment.getPackages()) {
            this.addPackage(pkg);
         }

      }
   }
}
