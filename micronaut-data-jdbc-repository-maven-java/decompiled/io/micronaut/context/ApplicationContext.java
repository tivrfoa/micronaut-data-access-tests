package io.micronaut.context;

import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertyPlaceholderResolver;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.value.PropertyResolver;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public interface ApplicationContext extends BeanContext, PropertyResolver, PropertyPlaceholderResolver {
   @NonNull
   ConversionService<?> getConversionService();

   @NonNull
   Environment getEnvironment();

   @NonNull
   ApplicationContext start();

   @NonNull
   ApplicationContext stop();

   @NonNull
   <T> ApplicationContext registerSingleton(@NonNull Class<T> type, @NonNull T singleton, @Nullable Qualifier<T> qualifier, boolean inject);

   @NonNull
   default <T> ApplicationContext registerSingleton(@NonNull Class<T> type, @NonNull T singleton, @Nullable Qualifier<T> qualifier) {
      return this.registerSingleton(type, singleton, qualifier, true);
   }

   @NonNull
   default <T> ApplicationContext registerSingleton(@NonNull Class<T> type, @NonNull T singleton) {
      return this.registerSingleton(type, singleton, null, true);
   }

   @NonNull
   default ApplicationContext registerSingleton(@NonNull Object singleton, boolean inject) {
      return (ApplicationContext)BeanContext.super.registerSingleton(singleton, inject);
   }

   @NonNull
   default ApplicationContext environment(@NonNull Consumer<Environment> consumer) {
      ArgumentUtils.requireNonNull("consumer", consumer);
      consumer.accept(this.getEnvironment());
      return this;
   }

   @NonNull
   default ApplicationContext registerSingleton(@NonNull Object singleton) {
      ArgumentUtils.requireNonNull("singleton", singleton);
      Class type = singleton.getClass();
      return this.registerSingleton(type, singleton);
   }

   @NonNull
   static ApplicationContext run(@NonNull String... environments) {
      ArgumentUtils.requireNonNull("environments", environments);
      return builder(environments).start();
   }

   @NonNull
   static ApplicationContext run() {
      return run(StringUtils.EMPTY_STRING_ARRAY);
   }

   @NonNull
   static ApplicationContext run(@NonNull Map<String, Object> properties, @NonNull String... environments) {
      ArgumentUtils.requireNonNull("environments", environments);
      ArgumentUtils.requireNonNull("properties", properties);
      PropertySource propertySource = PropertySource.of("context", properties, 0);
      return run(propertySource, environments);
   }

   @NonNull
   static ApplicationContext run(@NonNull PropertySource properties, @NonNull String... environments) {
      ArgumentUtils.requireNonNull("environments", environments);
      ArgumentUtils.requireNonNull("properties", properties);
      return builder(environments).propertySources(properties).start();
   }

   @NonNull
   static <T extends AutoCloseable> T run(@NonNull Class<T> type, @NonNull String... environments) {
      ArgumentUtils.requireNonNull("type", (T)type);
      ArgumentUtils.requireNonNull("environments", (T)environments);
      return run(type, Collections.emptyMap(), environments);
   }

   @NonNull
   static <T extends AutoCloseable> T run(@NonNull Class<T> type, @NonNull Map<String, Object> properties, @NonNull String... environments) {
      ArgumentUtils.requireNonNull("environments", (T)environments);
      ArgumentUtils.requireNonNull("properties", (T)properties);
      ArgumentUtils.requireNonNull("type", (T)type);
      PropertySource propertySource = PropertySource.of("context", properties, 0);
      return run(type, propertySource, environments);
   }

   @NonNull
   static <T extends AutoCloseable> T run(@NonNull Class<T> type, @NonNull PropertySource propertySource, @NonNull String... environments) {
      ArgumentUtils.requireNonNull("propertySource", propertySource);
      ArgumentUtils.requireNonNull("environments", (T)environments);
      ArgumentUtils.requireNonNull("type", (T)type);
      T bean = builder(environments).mainClass(type).propertySources(propertySource).start().getBean(type);
      if (bean instanceof LifeCycle) {
         LifeCycle lifeCycle = (LifeCycle)bean;
         if (!lifeCycle.isRunning()) {
            lifeCycle.start();
         }
      }

      return bean;
   }

   @NonNull
   static ApplicationContextBuilder builder(@NonNull String... environments) {
      ArgumentUtils.requireNonNull("environments", environments);
      return new DefaultApplicationContextBuilder().environments(environments);
   }

   @NonNull
   static ApplicationContextBuilder builder(@NonNull Map<String, Object> properties, @NonNull String... environments) {
      ArgumentUtils.requireNonNull("environments", environments);
      ArgumentUtils.requireNonNull("properties", properties);
      return new DefaultApplicationContextBuilder().properties(properties).environments(environments);
   }

   @NonNull
   static ApplicationContextBuilder builder() {
      return new DefaultApplicationContextBuilder();
   }

   @NonNull
   static ApplicationContext run(@NonNull ClassLoader classLoader, @NonNull String... environments) {
      ArgumentUtils.requireNonNull("environments", environments);
      ArgumentUtils.requireNonNull("classLoader", classLoader);
      return builder(classLoader, environments).start();
   }

   @NonNull
   static ApplicationContextBuilder builder(@NonNull ClassLoader classLoader, @NonNull String... environments) {
      ArgumentUtils.requireNonNull("environments", environments);
      ArgumentUtils.requireNonNull("classLoader", classLoader);
      return builder(environments).classLoader(classLoader);
   }

   @NonNull
   static ApplicationContextBuilder builder(@NonNull Class mainClass, @NonNull String... environments) {
      ArgumentUtils.requireNonNull("environments", environments);
      ArgumentUtils.requireNonNull("mainClass", mainClass);
      return builder(environments).mainClass(mainClass);
   }
}
