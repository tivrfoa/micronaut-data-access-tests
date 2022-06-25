package io.micronaut.context.env;

import io.micronaut.context.ApplicationContextConfiguration;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.file.DefaultFileSystemResourceLoader;
import io.micronaut.core.io.file.FileSystemResourceLoader;
import io.micronaut.core.io.scan.BeanIntrospectionScanner;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.core.io.service.SoftServiceLoader;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.optim.StaticOptimizations;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.BeanConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEnvironment extends PropertySourcePropertyResolver implements Environment {
   private static final List<PropertySource> CONSTANT_PROPERTY_SOURCES = (List<PropertySource>)StaticOptimizations.get(ConstantPropertySources.class)
      .map(ConstantPropertySources::getSources)
      .orElse(Collections.emptyList());
   private static final String EC2_LINUX_HYPERVISOR_FILE = "/sys/hypervisor/uuid";
   private static final String EC2_LINUX_BIOS_VENDOR_FILE = "/sys/devices/virtual/dmi/id/bios_vendor";
   private static final String EC2_WINDOWS_HYPERVISOR_CMD = "wmic path win32_computersystemproduct get uuid";
   private static final String FILE_SEPARATOR = ",";
   private static final Logger LOG = LoggerFactory.getLogger(DefaultEnvironment.class);
   private static final String AWS_LAMBDA_FUNCTION_NAME_ENV = "AWS_LAMBDA_FUNCTION_NAME";
   private static final String K8S_ENV = "KUBERNETES_SERVICE_HOST";
   private static final String PCF_ENV = "VCAP_SERVICES";
   private static final String HEROKU_DYNO = "DYNO";
   private static final String GOOGLE_APPENGINE_ENVIRONMENT = "GAE_ENV";
   private static final int DEFAULT_READ_TIMEOUT = 500;
   private static final int DEFAULT_CONNECT_TIMEOUT = 500;
   private static final String GOOGLE_COMPUTE_METADATA = "metadata.google.internal";
   private static final String ORACLE_CLOUD_ASSET_TAG_FILE = "/sys/devices/virtual/dmi/id/chassis_asset_tag";
   private static final String ORACLE_CLOUD_WINDOWS_ASSET_TAG_CMD = "wmic systemenclosure get smbiosassettag";
   private static final String DO_SYS_VENDOR_FILE = "/sys/devices/virtual/dmi/id/sys_vendor";
   private static final Boolean DEDUCE_ENVIRONMENT_DEFAULT = true;
   private static final List<String> DEFAULT_CONFIG_LOCATIONS = Arrays.asList("classpath:/", "file:config/");
   protected final ClassPathResourceLoader resourceLoader;
   protected final List<PropertySource> refreshablePropertySources = new ArrayList(10);
   private DefaultEnvironment.EnvironmentsAndPackage environmentsAndPackage;
   private final Set<String> names;
   private final ClassLoader classLoader;
   private final Collection<String> packages = new ConcurrentLinkedQueue();
   private final BeanIntrospectionScanner annotationScanner;
   private Collection<String> configurationIncludes = new HashSet(3);
   private Collection<String> configurationExcludes = new HashSet(3);
   private final AtomicBoolean running = new AtomicBoolean(false);
   private Collection<PropertySourceLoader> propertySourceLoaderList;
   private final Map<String, PropertySourceLoader> loaderByFormatMap = new ConcurrentHashMap();
   private final Map<String, Boolean> presenceCache = new ConcurrentHashMap();
   private final AtomicBoolean reading = new AtomicBoolean(false);
   private final Boolean deduceEnvironments;
   private final ApplicationContextConfiguration configuration;
   private final Collection<String> configLocations;

   public DefaultEnvironment(@NonNull ApplicationContextConfiguration configuration) {
      super(configuration.getConversionService());
      this.configuration = configuration;
      this.resourceLoader = configuration.getResourceLoader();
      Set<String> environments = new LinkedHashSet(3);
      List<String> specifiedNames = new ArrayList(configuration.getEnvironments());
      specifiedNames.addAll(
         0,
         (Collection)Stream.of(CachedEnvironment.getProperty("micronaut.environments"), CachedEnvironment.getenv("MICRONAUT_ENVIRONMENTS"))
            .filter(StringUtils::isNotEmpty)
            .flatMap(s -> Arrays.stream(s.split(",")))
            .map(String::trim)
            .collect(Collectors.toList())
      );
      this.deduceEnvironments = (Boolean)configuration.getDeduceEnvironments().orElse(null);
      DefaultEnvironment.EnvironmentsAndPackage environmentsAndPackage = this.getEnvironmentsAndPackage(specifiedNames);
      if (environmentsAndPackage.enviroments.isEmpty() && specifiedNames.isEmpty()) {
         specifiedNames = configuration.getDefaultEnvironments();
      }

      environments.addAll(environmentsAndPackage.enviroments);
      String aPackage = environmentsAndPackage.aPackage;
      if (aPackage != null) {
         this.packages.add(aPackage);
      }

      environments.removeAll(specifiedNames);
      environments.addAll(specifiedNames);
      this.classLoader = configuration.getClassLoader();
      this.annotationScanner = this.createAnnotationScanner(this.classLoader);
      this.names = environments;
      if (LOG.isInfoEnabled() && !environments.isEmpty()) {
         LOG.info("Established active environments: {}", environments);
      }

      List<String> configLocations = (List<String>)(configuration.getOverrideConfigLocations() == null
         ? new ArrayList(DEFAULT_CONFIG_LOCATIONS)
         : configuration.getOverrideConfigLocations());
      Collections.reverse(configLocations);
      this.configLocations = configLocations;
   }

   @Override
   public boolean isPresent(String className) {
      return this.presenceCache.computeIfAbsent(className, s -> ClassUtils.isPresent(className, this.getClassLoader()));
   }

   @Override
   public PropertyPlaceholderResolver getPlaceholderResolver() {
      return this.propertyPlaceholderResolver;
   }

   @Override
   public Stream<Class<?>> scan(Class<? extends Annotation> annotation) {
      return this.annotationScanner.scan(annotation, this.getPackages());
   }

   @Override
   public Stream<Class<?>> scan(Class<? extends Annotation> annotation, String... packages) {
      return this.annotationScanner.scan(annotation, packages);
   }

   @Override
   public ClassLoader getClassLoader() {
      return this.classLoader;
   }

   @Override
   public boolean isActive(BeanConfiguration configuration) {
      String name = configuration.getName();
      return !this.configurationExcludes.contains(name) && (this.configurationIncludes.isEmpty() || this.configurationIncludes.contains(name));
   }

   public DefaultEnvironment addPropertySource(PropertySource propertySource) {
      this.propertySources.put(propertySource.getName(), propertySource);
      if (this.isRunning() && !this.reading.get()) {
         this.resetCaches();
         this.processPropertySource(propertySource, PropertySource.PropertyConvention.JAVA_PROPERTIES);
      }

      return this;
   }

   @Override
   public Environment removePropertySource(PropertySource propertySource) {
      this.propertySources.remove(propertySource.getName());
      if (this.isRunning() && !this.reading.get()) {
         this.resetCaches();
      }

      return this;
   }

   public DefaultEnvironment addPropertySource(String name, Map<String, ? super Object> values) {
      return (DefaultEnvironment)super.addPropertySource(name, values);
   }

   @Override
   public Environment addPackage(String pkg) {
      if (!this.packages.contains(pkg)) {
         this.packages.add(pkg);
      }

      return this;
   }

   @Override
   public Environment addConfigurationExcludes(@Nullable String... names) {
      if (names != null) {
         this.configurationExcludes.addAll(Arrays.asList(names));
      }

      return this;
   }

   @Override
   public Environment addConfigurationIncludes(String... names) {
      if (names != null) {
         this.configurationIncludes.addAll(Arrays.asList(names));
      }

      return this;
   }

   @Override
   public Collection<String> getPackages() {
      return Collections.unmodifiableCollection(this.packages);
   }

   @Override
   public Set<String> getActiveNames() {
      return this.names;
   }

   @Override
   public Collection<PropertySource> getPropertySources() {
      return Collections.unmodifiableCollection(this.propertySources.values());
   }

   public Environment start() {
      if (this.running.compareAndSet(false, true)) {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Starting environment {} for active names {}", this, this.getActiveNames());
         }

         if (this.reading.compareAndSet(false, true)) {
            this.readPropertySources(this.getPropertySourceRootName());
            this.reading.set(false);
         }
      }

      return this;
   }

   @Override
   public boolean isRunning() {
      return this.running.get();
   }

   public Environment stop() {
      this.running.set(false);
      this.reading.set(false);
      this.propertySources.values().removeAll(this.refreshablePropertySources);
      synchronized(this.catalog) {
         for(int i = 0; i < this.catalog.length; ++i) {
            this.catalog[i] = null;
         }

         this.resetCaches();
         return this;
      }
   }

   @Override
   public Map<String, Object> refreshAndDiff() {
      Map<String, Object>[] copiedCatalog = this.copyCatalog();
      this.refresh();
      return this.diffCatalog(copiedCatalog, this.catalog);
   }

   @Override
   public <T> Optional<T> convert(Object object, Class<T> targetType, ConversionContext context) {
      return this.conversionService.convert(object, targetType, context);
   }

   @Override
   public <S, T> boolean canConvert(Class<S> sourceType, Class<T> targetType) {
      return this.conversionService.canConvert(sourceType, targetType);
   }

   public <S, T> Environment addConverter(Class<S> sourceType, Class<T> targetType, TypeConverter<S, T> typeConverter) {
      this.conversionService.addConverter(sourceType, targetType, typeConverter);
      return this;
   }

   public <S, T> Environment addConverter(Class<S> sourceType, Class<T> targetType, Function<S, T> typeConverter) {
      this.conversionService.addConverter(sourceType, targetType, typeConverter);
      return this;
   }

   @Override
   public Optional<InputStream> getResourceAsStream(String path) {
      return this.resourceLoader.getResourceAsStream(path);
   }

   @Override
   public Optional<URL> getResource(String path) {
      return this.resourceLoader.getResource(path);
   }

   @Override
   public Stream<URL> getResources(String path) {
      return this.resourceLoader.getResources(path);
   }

   @Override
   public boolean supportsPrefix(String path) {
      return this.resourceLoader.supportsPrefix(path);
   }

   @Override
   public ResourceLoader forBase(String basePath) {
      return this.resourceLoader.forBase(basePath);
   }

   protected boolean shouldDeduceEnvironments() {
      if (this.deduceEnvironments != null) {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Environment deduction was set explicitly via builder to: " + this.deduceEnvironments);
         }

         return this.deduceEnvironments;
      } else {
         String deduceProperty = CachedEnvironment.getProperty("micronaut.env.deduction");
         String deduceEnv = CachedEnvironment.getenv("MICRONAUT_ENV_DEDUCTION");
         if (StringUtils.isNotEmpty(deduceEnv)) {
            boolean deduce = Boolean.parseBoolean(deduceEnv);
            if (LOG.isDebugEnabled()) {
               LOG.debug("Environment deduction was set via environment variable to: " + deduce);
            }

            return deduce;
         } else if (StringUtils.isNotEmpty(deduceProperty)) {
            boolean deduce = Boolean.parseBoolean(deduceProperty);
            if (LOG.isDebugEnabled()) {
               LOG.debug("Environment deduction was set via system property to: " + deduce);
            }

            return deduce;
         } else {
            boolean deduceDefault = DEDUCE_ENVIRONMENT_DEFAULT;
            if (LOG.isDebugEnabled()) {
               LOG.debug("Environment deduction is using the default of: " + deduceDefault);
            }

            return deduceDefault;
         }
      }
   }

   protected BeanIntrospectionScanner createAnnotationScanner(ClassLoader classLoader) {
      return new BeanIntrospectionScanner();
   }

   protected String getPropertySourceRootName() {
      return "application";
   }

   protected void readPropertySources(String name) {
      this.refreshablePropertySources.clear();
      List<PropertySource> propertySources = this.readPropertySourceList(name);
      this.addDefaultPropertySources(propertySources);
      String propertySourcesSystemProperty = CachedEnvironment.getProperty("micronaut.config.files");
      if (propertySourcesSystemProperty != null) {
         propertySources.addAll(this.readPropertySourceListFromFiles(propertySourcesSystemProperty));
      }

      String propertySourcesEnv = this.readPropertySourceListKeyFromEnvironment();
      if (propertySourcesEnv != null) {
         propertySources.addAll(this.readPropertySourceListFromFiles(propertySourcesEnv));
      }

      this.refreshablePropertySources.addAll(propertySources);
      this.readConstantPropertySources(name, propertySources);
      propertySources.addAll(this.propertySources.values());
      OrderUtil.sort(propertySources);

      for(PropertySource propertySource : propertySources) {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Processing property source: {}", propertySource.getName());
         }

         this.processPropertySource(propertySource, propertySource.getConvention());
      }

   }

   private void readConstantPropertySources(String name, List<PropertySource> propertySources) {
      Set<String> propertySourceNames = (Set)Stream.concat(Stream.of(name), this.getActiveNames().stream().map(env -> name + "-" + env))
         .collect(Collectors.toSet());
      this.getConstantPropertySources().stream().filter(p -> propertySourceNames.contains(p.getName())).forEach(propertySources::add);
   }

   protected List<PropertySource> getConstantPropertySources() {
      return CONSTANT_PROPERTY_SOURCES;
   }

   protected String readPropertySourceListKeyFromEnvironment() {
      return CachedEnvironment.getenv(StringUtils.convertDotToUnderscore("micronaut.config.files"));
   }

   protected List<PropertySource> readPropertySourceListFromFiles(String files) {
      List<PropertySource> propertySources = new ArrayList();
      Collection<PropertySourceLoader> propertySourceLoaders = this.getPropertySourceLoaders();
      Optional<Collection<String>> filePathList = Optional.ofNullable(files)
         .filter(value -> !value.isEmpty())
         .map(value -> value.split(","))
         .map(Arrays::asList)
         .map(Collections::unmodifiableList);
      filePathList.ifPresent(
         list -> {
            if (!list.isEmpty()) {
               int order = -250;
   
               for(String filePath : list) {
                  if (!propertySourceLoaders.isEmpty()) {
                     String extension = NameUtils.extension(filePath);
                     String fileName = NameUtils.filename(filePath);
                     Optional<PropertySourceLoader> propertySourceLoader = Optional.ofNullable(this.loaderByFormatMap.get(extension));
                     if (!propertySourceLoader.isPresent()) {
                        throw new ConfigurationException("Unsupported properties file format while reading " + fileName + "." + extension + " from " + filePath);
                     }
   
                     if (LOG.isDebugEnabled()) {
                        LOG.debug("Reading property sources from loader: {}", propertySourceLoader);
                     }
   
                     Optional<Map<String, Object>> properties = this.readPropertiesFromLoader(
                        fileName, filePath, (PropertySourceLoader)propertySourceLoader.get()
                     );
                     if (properties.isPresent()) {
                        propertySources.add(PropertySource.of(filePath, (Map<String, Object>)properties.get(), order));
                     }
   
                     ++order;
                  }
               }
            }
   
         }
      );
      return propertySources;
   }

   protected List<PropertySource> readPropertySourceList(String name) {
      List<PropertySource> propertySources = new ArrayList();
      Iterator var3 = this.configLocations.iterator();

      while(true) {
         ResourceLoader resourceLoader;
         while(true) {
            if (!var3.hasNext()) {
               return propertySources;
            }

            String configLocation = (String)var3.next();
            if (configLocation.equals("classpath:/")) {
               resourceLoader = this;
               break;
            }

            if (configLocation.startsWith("classpath:")) {
               resourceLoader = this.forBase(configLocation);
               break;
            }

            if (!configLocation.startsWith("file:")) {
               throw new ConfigurationException("Unsupported config location format: " + configLocation);
            }

            configLocation = configLocation.substring(5);
            Path configLocationPath = Paths.get(configLocation);
            if (Files.exists(configLocationPath, new LinkOption[0])
               && Files.isDirectory(configLocationPath, new LinkOption[0])
               && Files.isReadable(configLocationPath)) {
               resourceLoader = new DefaultFileSystemResourceLoader(configLocationPath);
               break;
            }
         }

         this.readPropertySourceList(name, resourceLoader, propertySources);
      }
   }

   private void readPropertySourceList(String name, ResourceLoader resourceLoader, List<PropertySource> propertySources) {
      Collection<PropertySourceLoader> propertySourceLoaders = this.getPropertySourceLoaders();
      if (propertySourceLoaders.isEmpty()) {
         this.loadPropertySourceFromLoader(name, new PropertiesPropertySourceLoader(), propertySources, resourceLoader);
      } else {
         for(PropertySourceLoader propertySourceLoader : propertySourceLoaders) {
            if (LOG.isDebugEnabled()) {
               LOG.debug("Reading property sources from loader: {}", propertySourceLoader);
            }

            this.loadPropertySourceFromLoader(name, propertySourceLoader, propertySources, resourceLoader);
         }
      }

   }

   protected void addDefaultPropertySources(List<PropertySource> propertySources) {
      if (!this.propertySources.containsKey("system")) {
         propertySources.add(new SystemPropertiesPropertySource());
      }

      if (!this.propertySources.containsKey("env") && this.configuration.isEnvironmentPropertySource()) {
         List<String> includes = this.configuration.getEnvironmentVariableIncludes();
         List<String> excludes = this.configuration.getEnvironmentVariableExcludes();
         if (this.names.contains("k8s")) {
            propertySources.add(new KubernetesEnvironmentPropertySource(includes, excludes));
         } else {
            propertySources.add(new EnvironmentPropertySource(includes, excludes));
         }
      }

   }

   protected SoftServiceLoader<PropertySourceLoader> readPropertySourceLoaders() {
      return SoftServiceLoader.load(PropertySourceLoader.class, this.getClassLoader());
   }

   @Override
   public Collection<PropertySourceLoader> getPropertySourceLoaders() {
      Collection<PropertySourceLoader> propertySourceLoaderList = this.propertySourceLoaderList;
      if (propertySourceLoaderList == null) {
         synchronized(this) {
            propertySourceLoaderList = this.propertySourceLoaderList;
            if (propertySourceLoaderList == null) {
               propertySourceLoaderList = this.evaluatePropertySourceLoaders();
               this.propertySourceLoaderList = propertySourceLoaderList;
            }
         }
      }

      return propertySourceLoaderList;
   }

   private Collection<PropertySourceLoader> evaluatePropertySourceLoaders() {
      SoftServiceLoader<PropertySourceLoader> definitions = this.readPropertySourceLoaders();
      Collection<PropertySourceLoader> allLoaders = new ArrayList(10);
      definitions.collectAll(allLoaders);

      for(PropertySourceLoader propertySourceLoader : allLoaders) {
         for(String extension : propertySourceLoader.getExtensions()) {
            this.loaderByFormatMap.put(extension, propertySourceLoader);
         }
      }

      return allLoaders;
   }

   private void loadPropertySourceFromLoader(
      String name, PropertySourceLoader propertySourceLoader, List<PropertySource> propertySources, ResourceLoader resourceLoader
   ) {
      Optional<PropertySource> defaultPropertySource = propertySourceLoader.load(name, resourceLoader);
      defaultPropertySource.ifPresent(propertySources::add);
      Set<String> activeNames = this.getActiveNames();
      int i = 0;

      for(String activeName : activeNames) {
         Optional<PropertySource> propertySource = propertySourceLoader.loadEnv(name, resourceLoader, ActiveEnvironment.of(activeName, i));
         propertySource.ifPresent(propertySources::add);
         ++i;
      }

   }

   private Optional<Map<String, Object>> readPropertiesFromLoader(String fileName, String filePath, PropertySourceLoader propertySourceLoader) throws ConfigurationException {
      ResourceLoader loader = (ResourceLoader)new ResourceResolver().getSupportingLoader(filePath).orElse(FileSystemResourceLoader.defaultLoader());

      try {
         Optional<InputStream> inputStream = loader.getResourceAsStream(filePath);
         if (inputStream.isPresent()) {
            return Optional.of(propertySourceLoader.read(fileName, (InputStream)inputStream.get()));
         } else {
            throw new ConfigurationException("Failed to read configuration file: " + filePath);
         }
      } catch (IOException var6) {
         throw new ConfigurationException("Unsupported properties file: " + fileName);
      }
   }

   private DefaultEnvironment.EnvironmentsAndPackage getEnvironmentsAndPackage(List<String> specifiedNames) {
      DefaultEnvironment.EnvironmentsAndPackage environmentsAndPackage = this.environmentsAndPackage;
      boolean extendedDeduction = !specifiedNames.contains("function");
      if (environmentsAndPackage == null) {
         synchronized(DefaultEnvironment.EnvironmentsAndPackage.class) {
            environmentsAndPackage = this.environmentsAndPackage;
            if (environmentsAndPackage == null) {
               environmentsAndPackage = deduceEnvironmentsAndPackage(this.shouldDeduceEnvironments(), extendedDeduction, extendedDeduction, !extendedDeduction);
               this.environmentsAndPackage = environmentsAndPackage;
            }
         }
      }

      return environmentsAndPackage;
   }

   private static DefaultEnvironment.EnvironmentsAndPackage deduceEnvironmentsAndPackage(
      boolean deduceEnvironments, boolean deduceComputePlatform, boolean inspectTrace, boolean deduceFunctionPlatform
   ) {
      DefaultEnvironment.EnvironmentsAndPackage environmentsAndPackage = new DefaultEnvironment.EnvironmentsAndPackage();
      Set<String> environments = environmentsAndPackage.enviroments;
      if (inspectTrace) {
         performStackTraceInspection(deduceEnvironments, environmentsAndPackage, environments);
      }

      if (deduceEnvironments && !environments.contains("android")) {
         performEnvironmentDeduction(deduceComputePlatform, environments);
      }

      if (deduceFunctionPlatform) {
         performFunctionDeduction(environments);
      }

      return environmentsAndPackage;
   }

   private static void performFunctionDeduction(Set<String> environments) {
      if (StringUtils.isNotEmpty(CachedEnvironment.getenv("AWS_LAMBDA_FUNCTION_NAME"))) {
         environments.add("ec2");
         environments.add("cloud");
      }

   }

   private static void performEnvironmentDeduction(boolean deduceComputePlatform, Set<String> environments) {
      if (StringUtils.isNotEmpty(CachedEnvironment.getenv("KUBERNETES_SERVICE_HOST"))) {
         environments.add("k8s");
         environments.add("cloud");
      }

      if (StringUtils.isNotEmpty(CachedEnvironment.getenv("VCAP_SERVICES"))) {
         environments.add("pcf");
         environments.add("cloud");
      }

      if (StringUtils.isNotEmpty(CachedEnvironment.getenv("DYNO"))) {
         environments.add("heroku");
         environments.add("cloud");
         deduceComputePlatform = false;
      }

      if (StringUtils.isNotEmpty(CachedEnvironment.getenv("GAE_ENV"))) {
         environments.add("gae");
         environments.add("gcp");
         deduceComputePlatform = false;
      }

      if (deduceComputePlatform) {
         performComputePlatformDeduction(environments);
      }

   }

   private static void performComputePlatformDeduction(Set<String> environments) {
      ComputePlatform computePlatform = determineCloudProvider();
      if (computePlatform != null) {
         switch(computePlatform) {
            case GOOGLE_COMPUTE:
               environments.add("gcp");
               environments.add("cloud");
               break;
            case AMAZON_EC2:
               environments.add("ec2");
               environments.add("cloud");
               break;
            case ORACLE_CLOUD:
               environments.add("oraclecloud");
               environments.add("cloud");
               break;
            case AZURE:
               environments.add("azure");
               environments.add("cloud");
               break;
            case IBM:
               environments.add("ibm");
               environments.add("cloud");
               break;
            case DIGITAL_OCEAN:
               environments.add("digitalocean");
               environments.add("cloud");
            case OTHER:
         }
      }

   }

   private static void performStackTraceInspection(
      boolean deduceEnvironments, DefaultEnvironment.EnvironmentsAndPackage environmentsAndPackage, Set<String> environments
   ) {
      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
      int len = stackTrace.length;

      for(int i = 0; i < len; ++i) {
         StackTraceElement stackTraceElement = stackTrace[i];
         String className = stackTraceElement.getClassName();
         analyzeStackTraceElement(deduceEnvironments, environmentsAndPackage, environments, stackTrace, len, i, stackTraceElement, className);
      }

   }

   private static void analyzeStackTraceElement(
      boolean deduceEnvironments,
      DefaultEnvironment.EnvironmentsAndPackage environmentsAndPackage,
      Set<String> environments,
      StackTraceElement[] stackTrace,
      int len,
      int i,
      StackTraceElement stackTraceElement,
      String className
   ) {
      if (className.startsWith("io.micronaut")) {
         int nextIndex = i + 1;
         if (nextIndex < len) {
            StackTraceElement next = stackTrace[nextIndex];
            if (!next.getClassName().startsWith("io.micronaut")) {
               environmentsAndPackage.aPackage = NameUtils.getPackageName(next.getClassName());
            }
         }
      }

      if (stackTraceElement.getMethodName().contains("$spock_")) {
         environmentsAndPackage.aPackage = NameUtils.getPackageName(className);
      }

      if (deduceEnvironments) {
         if (Stream.of("org.spockframework", "org.junit", "io.kotlintest", "io.kotest").anyMatch(className::startsWith)) {
            environments.add("test");
         }

         if (className.startsWith("com.android")) {
            environments.add("android");
         }
      }

   }

   private Map<String, Object> diffCatalog(Map<String, Object>[] original, Map<String, Object>[] newCatalog) {
      Map<String, Object> changes = new LinkedHashMap();

      for(int i = 0; i < original.length; ++i) {
         Map<String, Object> map = original[i];
         Map<String, Object> newMap = newCatalog[i];
         boolean hasNew = newMap != null;
         boolean hasOld = map != null;
         if (!hasOld && hasNew) {
            changes.putAll(newMap);
         } else if (!hasNew && hasOld) {
            changes.putAll(map);
         } else if (hasOld && hasNew) {
            this.diffMap(map, newMap, changes);
         }
      }

      return changes;
   }

   private void diffMap(Map<String, Object> map, Map<String, Object> newMap, Map<String, Object> changes) {
      for(Entry<String, Object> entry : newMap.entrySet()) {
         String key = (String)entry.getKey();
         Object newValue = entry.getValue();
         if (!map.containsKey(key)) {
            changes.put(key, newValue);
         } else {
            Object oldValue = map.get(key);
            boolean hasNew = newValue != null;
            boolean hasOld = oldValue != null;
            if (hasNew && !hasOld) {
               changes.put(key, null);
            } else if (hasOld && !hasNew) {
               changes.put(key, oldValue);
            } else if (hasNew && hasOld && !newValue.equals(oldValue)) {
               changes.put(key, oldValue);
            }
         }
      }

   }

   private Map<String, Object>[] copyCatalog() {
      Map<String, Object>[] newCatalog = new Map[this.catalog.length];

      for(int i = 0; i < this.catalog.length; ++i) {
         Map<String, Object> entry = this.catalog[i];
         if (entry != null) {
            newCatalog[i] = new LinkedHashMap(entry);
         }
      }

      return newCatalog;
   }

   private static ComputePlatform determineCloudProvider() {
      String computePlatform = CachedEnvironment.getProperty("micronaut.cloud.platform");
      if (computePlatform != null) {
         try {
            return ComputePlatform.valueOf(computePlatform);
         } catch (IllegalArgumentException var2) {
            throw new ConfigurationException("Illegal value specified for [micronaut.cloud.platform]: " + computePlatform);
         }
      } else {
         boolean isWindows = CachedEnvironment.getProperty("os.name").toLowerCase().startsWith("windows");
         if (isWindows) {
            if (isEC2Windows()) {
               return ComputePlatform.AMAZON_EC2;
            }
         } else if (isEC2Linux()) {
            return ComputePlatform.AMAZON_EC2;
         }

         if (isGoogleCompute()) {
            return ComputePlatform.GOOGLE_COMPUTE;
         } else {
            if (isWindows) {
               if (isOracleCloudWindows()) {
                  return ComputePlatform.ORACLE_CLOUD;
               }
            } else if (isOracleCloudLinux()) {
               return ComputePlatform.ORACLE_CLOUD;
            }

            return isDigitalOcean() ? ComputePlatform.DIGITAL_OCEAN : ComputePlatform.BARE_METAL;
         }
      }
   }

   private static boolean isGoogleCompute() {
      try {
         InetAddress.getByName("metadata.google.internal");
         return true;
      } catch (Exception var1) {
         return false;
      }
   }

   private static boolean isOracleCloudLinux() {
      return readFile("/sys/devices/virtual/dmi/id/chassis_asset_tag").toLowerCase().contains("oraclecloud");
   }

   private static Optional<Process> runWindowsCmd(String cmd) {
      try {
         ProcessBuilder builder = new ProcessBuilder(new String[0]);
         builder.command("cmd.exe", "/c", cmd);
         builder.redirectErrorStream(true);
         builder.directory(new File(CachedEnvironment.getProperty("user.home")));
         Process process = builder.start();
         return Optional.of(process);
      } catch (IOException var3) {
         return Optional.empty();
      }
   }

   private static StringBuilder readProcessStream(Process process) {
      StringBuilder stdout = new StringBuilder();

      try {
         InputStream is = process.getInputStream();
         InputStreamReader isr = new InputStreamReader(is);
         BufferedReader br = new BufferedReader(isr);

         String line;
         while((line = br.readLine()) != null) {
            stdout.append(line);
         }
      } catch (IOException var6) {
      }

      return stdout;
   }

   private static boolean isOracleCloudWindows() {
      Optional<Process> optionalProcess = runWindowsCmd("wmic systemenclosure get smbiosassettag");
      if (!optionalProcess.isPresent()) {
         return false;
      } else {
         Process process = (Process)optionalProcess.get();
         StringBuilder stdout = readProcessStream(process);

         try {
            int exitValue = process.waitFor();
            if (exitValue == 0 && stdout.toString().toLowerCase().contains("oraclecloud")) {
               return true;
            }
         } catch (InterruptedException var4) {
         }

         return false;
      }
   }

   private static boolean isEC2Linux() {
      if (readFile("/sys/hypervisor/uuid").startsWith("ec2")) {
         return true;
      } else {
         return readFile("/sys/devices/virtual/dmi/id/bios_vendor").toLowerCase().startsWith("amazon ec2");
      }
   }

   private static String readFile(String path) {
      try {
         Path pathPath = Paths.get(path);
         return !Files.exists(pathPath, new LinkOption[0]) ? "" : new String(Files.readAllBytes(pathPath)).trim();
      } catch (IOException var2) {
         return "";
      }
   }

   private static boolean isEC2Windows() {
      Optional<Process> optionalProcess = runWindowsCmd("wmic path win32_computersystemproduct get uuid");
      if (!optionalProcess.isPresent()) {
         return false;
      } else {
         Process process = (Process)optionalProcess.get();
         StringBuilder stdout = readProcessStream(process);

         try {
            int exitValue = process.waitFor();
            if (exitValue == 0 && stdout.toString().startsWith("EC2")) {
               return true;
            }
         } catch (InterruptedException var4) {
         }

         return false;
      }
   }

   private static boolean isDigitalOcean() {
      return "digitalocean".equalsIgnoreCase(readFile("/sys/devices/virtual/dmi/id/sys_vendor"));
   }

   @Override
   public void close() {
      try {
         super.close();
      } catch (Exception var2) {
         throw new RuntimeException("Failed to close!", var2);
      }

      this.stop();
   }

   private static class EnvironmentsAndPackage {
      String aPackage;
      Set<String> enviroments = new LinkedHashSet(1);

      private EnvironmentsAndPackage() {
      }
   }
}
