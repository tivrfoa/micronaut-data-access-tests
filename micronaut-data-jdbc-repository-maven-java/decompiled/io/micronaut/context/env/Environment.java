package io.micronaut.context.env;

import io.micronaut.context.LifeCycle;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.io.scan.BeanIntrospectionScanner;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.value.PropertyResolver;
import io.micronaut.inject.BeanConfiguration;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public interface Environment extends PropertyResolver, LifeCycle<Environment>, ConversionService<Environment>, ResourceLoader {
   String MICRONAUT = "micronaut";
   String TEST = "test";
   String DEVELOPMENT = "dev";
   String ANDROID = "android";
   String CLI = "cli";
   String CLOUD = "cloud";
   String FUNCTION = "function";
   String BOOTSTRAP_NAME_PROPERTY = "micronaut.bootstrap.name";
   String BOOTSTRAP_CONTEXT_PROPERTY = "micronaut.bootstrap.context";
   String CLOUD_PLATFORM_PROPERTY = "micronaut.cloud.platform";
   String ENVIRONMENTS_PROPERTY = "micronaut.environments";
   String ENVIRONMENTS_ENV = "MICRONAUT_ENVIRONMENTS";
   String BOOTSTRAP_NAME = "bootstrap";
   String DEFAULT_NAME = "application";
   String GOOGLE_COMPUTE = "gcp";
   String GAE = "gae";
   String AMAZON_EC2 = "ec2";
   String AZURE = "azure";
   String ORACLE_CLOUD = "oraclecloud";
   String DIGITAL_OCEAN = "digitalocean";
   String BARE_METAL = "baremetal";
   String IBM = "ibm";
   String KUBERNETES = "k8s";
   String CLOUD_FOUNDRY = "pcf";
   String HEROKU = "heroku";
   String PROPERTY_SOURCES_KEY = "micronaut.config.files";
   String HOSTNAME = "HOSTNAME";
   String DEDUCE_ENVIRONMENT_PROPERTY = "micronaut.env.deduction";
   String DEDUCE_ENVIRONMENT_ENV = "MICRONAUT_ENV_DEDUCTION";

   Set<String> getActiveNames();

   Collection<PropertySource> getPropertySources();

   Environment addPropertySource(PropertySource propertySource);

   Environment removePropertySource(PropertySource propertySource);

   Environment addPackage(String pkg);

   Environment addConfigurationExcludes(String... names);

   Environment addConfigurationIncludes(String... names);

   Collection<String> getPackages();

   PropertyPlaceholderResolver getPlaceholderResolver();

   Map<String, Object> refreshAndDiff();

   default Environment addPropertySource(String name, @Nullable Map<String, ? super Object> values) {
      return StringUtils.isNotEmpty(name) && CollectionUtils.isNotEmpty(values) ? this.addPropertySource(PropertySource.of(name, values)) : this;
   }

   default Environment addPackage(Package pkg) {
      this.addPackage(pkg.getName());
      return this;
   }

   default Stream<Class<?>> scan(Class<? extends Annotation> annotation) {
      BeanIntrospectionScanner scanner = new BeanIntrospectionScanner();
      return scanner.scan(annotation, this.getPackages());
   }

   default Stream<Class<?>> scan(Class<? extends Annotation> annotation, String... packages) {
      BeanIntrospectionScanner scanner = new BeanIntrospectionScanner();
      return scanner.scan(annotation, Arrays.asList(packages));
   }

   default ClassLoader getClassLoader() {
      return Environment.class.getClassLoader();
   }

   default boolean isPresent(String className) {
      return ClassUtils.isPresent(className, this.getClassLoader());
   }

   boolean isActive(BeanConfiguration configuration);

   Collection<PropertySourceLoader> getPropertySourceLoaders();
}
