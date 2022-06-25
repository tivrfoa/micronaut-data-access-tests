package io.micronaut.context;

import io.micronaut.context.env.CommandLinePropertySource;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.cli.CommandLine;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.core.io.service.SoftServiceLoader;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.core.util.StringUtils;
import java.lang.annotation.Annotation;
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

public class DefaultApplicationContextBuilder implements ApplicationContextBuilder, ApplicationContextConfiguration {
   private List<Object> singletons = new ArrayList();
   private List<String> environments = new ArrayList();
   private List<String> defaultEnvironments = new ArrayList();
   private List<String> packages = new ArrayList();
   private Map<String, Object> properties = new LinkedHashMap();
   private List<PropertySource> propertySources = new ArrayList();
   private Collection<String> configurationIncludes = new HashSet();
   private Collection<String> configurationExcludes = new HashSet();
   private Boolean deduceEnvironments = null;
   private ClassLoader classLoader = this.getClass().getClassLoader();
   private boolean envPropertySource = true;
   private List<String> envVarIncludes = new ArrayList();
   private List<String> envVarExcludes = new ArrayList();
   private String[] args = new String[0];
   private Set<Class<? extends Annotation>> eagerInitAnnotated = new HashSet(3);
   private String[] overrideConfigLocations;
   private boolean banner = true;
   private ClassPathResourceLoader classPathResourceLoader;
   private boolean allowEmptyProviders = false;
   private Boolean bootstrapEnvironment = null;

   protected DefaultApplicationContextBuilder() {
      loadApplicationContextCustomizer(this.resolveClassLoader()).configure(this);
   }

   private ClassLoader resolveClassLoader() {
      ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
      return contextClassLoader != null ? contextClassLoader : DefaultApplicationContextBuilder.class.getClassLoader();
   }

   @Override
   public boolean isAllowEmptyProviders() {
      return this.allowEmptyProviders;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder eagerInitAnnotated(Class<? extends Annotation>... annotations) {
      if (annotations != null) {
         this.eagerInitAnnotated.addAll(Arrays.asList(annotations));
      }

      return this;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder overrideConfigLocations(String... configLocations) {
      this.overrideConfigLocations = configLocations;
      return this;
   }

   @Nullable
   @Override
   public List<String> getOverrideConfigLocations() {
      return this.overrideConfigLocations == null ? null : Arrays.asList(this.overrideConfigLocations);
   }

   @Override
   public boolean isBannerEnabled() {
      return this.banner;
   }

   @Nullable
   @Override
   public Boolean isBootstrapEnvironmentEnabled() {
      return this.bootstrapEnvironment;
   }

   @Override
   public Set<Class<? extends Annotation>> getEagerInitAnnotated() {
      return Collections.unmodifiableSet(this.eagerInitAnnotated);
   }

   @NonNull
   @Override
   public ApplicationContextBuilder singletons(Object... beans) {
      if (beans != null) {
         this.singletons.addAll(Arrays.asList(beans));
      }

      return this;
   }

   @NonNull
   @Override
   public ClassPathResourceLoader getResourceLoader() {
      if (this.classPathResourceLoader == null) {
         if (this.classLoader != null) {
            this.classPathResourceLoader = ClassPathResourceLoader.defaultLoader(this.classLoader);
         } else {
            this.classPathResourceLoader = ClassPathResourceLoader.defaultLoader(this.getClass().getClassLoader());
         }
      }

      return this.classPathResourceLoader;
   }

   @NonNull
   @Override
   public ClassLoader getClassLoader() {
      return this.classLoader;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder deduceEnvironment(@Nullable Boolean deduceEnvironments) {
      this.deduceEnvironments = deduceEnvironments;
      return this;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder environments(@Nullable String... environments) {
      if (environments != null) {
         this.environments.addAll(Arrays.asList(environments));
      }

      return this;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder defaultEnvironments(@Nullable String... environments) {
      if (environments != null) {
         this.defaultEnvironments.addAll(Arrays.asList(environments));
      }

      return this;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder packages(@Nullable String... packages) {
      if (packages != null) {
         this.packages.addAll(Arrays.asList(packages));
      }

      return this;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder properties(@Nullable Map<String, Object> properties) {
      if (properties != null) {
         this.properties.putAll(properties);
      }

      return this;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder propertySources(@Nullable PropertySource... propertySources) {
      if (propertySources != null) {
         this.propertySources.addAll(Arrays.asList(propertySources));
      }

      return this;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder environmentPropertySource(boolean environmentPropertySource) {
      this.envPropertySource = environmentPropertySource;
      return this;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder environmentVariableIncludes(@Nullable String... environmentVariables) {
      if (environmentVariables != null) {
         this.envVarIncludes.addAll(Arrays.asList(environmentVariables));
      }

      return this;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder environmentVariableExcludes(@Nullable String... environmentVariables) {
      if (environmentVariables != null) {
         this.envVarExcludes.addAll(Arrays.asList(environmentVariables));
      }

      return this;
   }

   @Override
   public Optional<Boolean> getDeduceEnvironments() {
      return Optional.ofNullable(this.deduceEnvironments);
   }

   @NonNull
   @Override
   public List<String> getEnvironments() {
      return this.environments;
   }

   @NonNull
   @Override
   public List<String> getDefaultEnvironments() {
      return this.defaultEnvironments;
   }

   @Override
   public boolean isEnvironmentPropertySource() {
      return this.envPropertySource;
   }

   @Nullable
   @Override
   public List<String> getEnvironmentVariableIncludes() {
      return this.envVarIncludes.isEmpty() ? null : this.envVarIncludes;
   }

   @Nullable
   @Override
   public List<String> getEnvironmentVariableExcludes() {
      return this.envVarExcludes.isEmpty() ? null : this.envVarExcludes;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder mainClass(Class mainClass) {
      if (mainClass != null) {
         if (this.classLoader == null) {
            this.classLoader = mainClass.getClassLoader();
         }

         String name = mainClass.getPackage().getName();
         if (StringUtils.isNotEmpty(name)) {
            this.packages(name);
         }
      }

      return this;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder classLoader(ClassLoader classLoader) {
      if (classLoader != null) {
         this.classLoader = classLoader;
      }

      return this;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder args(@Nullable String... args) {
      if (args != null) {
         this.args = args;
      }

      return this;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder bootstrapEnvironment(boolean bootstrapEnv) {
      this.bootstrapEnvironment = bootstrapEnv;
      return this;
   }

   @NonNull
   @Override
   public ApplicationContext build() {
      ApplicationContext applicationContext = this.newApplicationContext();
      Environment environment = applicationContext.getEnvironment();
      if (!this.packages.isEmpty()) {
         for(String aPackage : this.packages) {
            environment.addPackage(aPackage);
         }
      }

      if (!this.properties.isEmpty()) {
         PropertySource contextProperties = PropertySource.of("context", this.properties, 0);
         environment.addPropertySource(contextProperties);
      }

      if (this.args.length > 0) {
         CommandLine commandLine = CommandLine.parse(this.args);
         environment.addPropertySource(new CommandLinePropertySource(commandLine));
      }

      if (!this.propertySources.isEmpty()) {
         for(PropertySource propertySource : this.propertySources) {
            environment.addPropertySource(propertySource);
         }
      }

      if (!this.singletons.isEmpty()) {
         for(Object singleton : this.singletons) {
            applicationContext.registerSingleton(singleton);
         }
      }

      if (!this.configurationIncludes.isEmpty()) {
         environment.addConfigurationIncludes((String[])this.configurationIncludes.toArray(StringUtils.EMPTY_STRING_ARRAY));
      }

      if (!this.configurationExcludes.isEmpty()) {
         environment.addConfigurationExcludes((String[])this.configurationExcludes.toArray(StringUtils.EMPTY_STRING_ARRAY));
      }

      return applicationContext;
   }

   @NonNull
   protected ApplicationContext newApplicationContext() {
      return new DefaultApplicationContext(this);
   }

   @NonNull
   @Override
   public ApplicationContextBuilder include(@Nullable String... configurations) {
      if (configurations != null) {
         this.configurationIncludes.addAll(Arrays.asList(configurations));
      }

      return this;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder exclude(@Nullable String... configurations) {
      if (configurations != null) {
         this.configurationExcludes.addAll(Arrays.asList(configurations));
      }

      return this;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder banner(boolean isEnabled) {
      this.banner = isEnabled;
      return this;
   }

   @NonNull
   @Override
   public ApplicationContextBuilder allowEmptyProviders(boolean shouldAllow) {
      this.allowEmptyProviders = shouldAllow;
      return this;
   }

   @NonNull
   private static ApplicationContextConfigurer loadApplicationContextCustomizer(@Nullable ClassLoader classLoader) {
      SoftServiceLoader<ApplicationContextConfigurer> loader = classLoader != null
         ? SoftServiceLoader.load(ApplicationContextConfigurer.class, classLoader)
         : SoftServiceLoader.load(ApplicationContextConfigurer.class);
      final List<ApplicationContextConfigurer> configurers = new ArrayList(10);
      loader.collectAll(configurers);
      if (configurers.isEmpty()) {
         return ApplicationContextConfigurer.NO_OP;
      } else if (configurers.size() == 1) {
         return (ApplicationContextConfigurer)configurers.get(0);
      } else {
         OrderUtil.sort(configurers);
         return new ApplicationContextConfigurer() {
            @Override
            public void configure(ApplicationContextBuilder builder) {
               for(ApplicationContextConfigurer customizer : configurers) {
                  customizer.configure(builder);
               }

            }
         };
      }
   }
}
