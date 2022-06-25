package io.micronaut.context;

import io.micronaut.context.annotation.ConfigurationReader;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Map;

public interface ApplicationContextBuilder {
   @NonNull
   default ApplicationContextBuilder eagerInitConfiguration(boolean eagerInitConfiguration) {
      return eagerInitConfiguration ? this.eagerInitAnnotated(ConfigurationReader.class) : this;
   }

   @NonNull
   default ApplicationContextBuilder eagerInitSingletons(boolean eagerInitSingletons) {
      return eagerInitSingletons ? this.eagerInitAnnotated(Singleton.class) : this;
   }

   @NonNull
   ApplicationContextBuilder eagerInitAnnotated(Class<? extends Annotation>... annotations);

   @NonNull
   ApplicationContextBuilder overrideConfigLocations(String... configLocations);

   @NonNull
   ApplicationContextBuilder singletons(@Nullable Object... beans);

   @NonNull
   ApplicationContextBuilder deduceEnvironment(@Nullable Boolean deduceEnvironment);

   @NonNull
   ApplicationContextBuilder environments(@Nullable String... environments);

   @NonNull
   ApplicationContextBuilder defaultEnvironments(@Nullable String... environments);

   @NonNull
   ApplicationContextBuilder packages(@Nullable String... packages);

   @NonNull
   ApplicationContextBuilder properties(@Nullable Map<String, Object> properties);

   @NonNull
   ApplicationContextBuilder propertySources(@Nullable PropertySource... propertySources);

   @NonNull
   ApplicationContextBuilder environmentPropertySource(boolean environmentPropertySource);

   @NonNull
   ApplicationContextBuilder environmentVariableIncludes(@Nullable String... environmentVariables);

   @NonNull
   ApplicationContextBuilder environmentVariableExcludes(@Nullable String... environmentVariables);

   @NonNull
   ApplicationContextBuilder mainClass(@Nullable Class mainClass);

   @NonNull
   ApplicationContextBuilder classLoader(@Nullable ClassLoader classLoader);

   @NonNull
   ApplicationContext build();

   @NonNull
   ApplicationContextBuilder include(@Nullable String... configurations);

   @NonNull
   ApplicationContextBuilder exclude(@Nullable String... configurations);

   @NonNull
   ApplicationContextBuilder banner(boolean isEnabled);

   @NonNull
   ApplicationContextBuilder allowEmptyProviders(boolean shouldAllow);

   @NonNull
   default ApplicationContextBuilder args(@Nullable String... args) {
      return this;
   }

   @NonNull
   default ApplicationContextBuilder bootstrapEnvironment(boolean bootstrapEnv) {
      return this;
   }

   @NonNull
   default ApplicationContext start() {
      return this.build().start();
   }

   @NonNull
   default <T extends AutoCloseable> T run(@NonNull Class<T> type) {
      ArgumentUtils.requireNonNull("type", (T)type);
      ApplicationContext applicationContext = this.start();
      T bean = applicationContext.getBean(type);
      if (bean instanceof LifeCycle) {
         LifeCycle lifeCycle = (LifeCycle)bean;
         if (!lifeCycle.isRunning()) {
            lifeCycle.start();
         }
      }

      return bean;
   }
}
