package io.micronaut.scheduling.executor;

import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import javax.validation.constraints.Min;

@EachProperty("micronaut.executors")
public class UserExecutorConfiguration implements ExecutorConfiguration {
   public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
   protected String name;
   protected Integer nThreads;
   private ExecutorType type;
   private Integer parallelism;
   private Integer corePoolSize;
   private Class<? extends ThreadFactory> threadFactoryClass;

   private UserExecutorConfiguration(@Parameter String name) {
      this(name, null, null, null, null, null);
   }

   @ConfigurationInject
   protected UserExecutorConfiguration(
      @Nullable @Parameter String name,
      @Nullable Integer nThreads,
      @Nullable ExecutorType type,
      @Nullable Integer parallelism,
      @Nullable Integer corePoolSize,
      @Nullable Class<? extends ThreadFactory> threadFactoryClass
   ) {
      this.name = name;
      this.nThreads = nThreads == null ? AVAILABLE_PROCESSORS * 2 : nThreads;
      this.type = type == null ? ExecutorType.SCHEDULED : type;
      this.parallelism = parallelism == null ? AVAILABLE_PROCESSORS : parallelism;
      this.corePoolSize = corePoolSize == null ? AVAILABLE_PROCESSORS * 2 : corePoolSize;
      this.threadFactoryClass = threadFactoryClass;
   }

   @NonNull
   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public ExecutorType getType() {
      return this.type;
   }

   @Min(1L)
   @Override
   public Integer getParallelism() {
      return this.parallelism;
   }

   @Min(1L)
   @Override
   public Integer getNumberOfThreads() {
      return this.nThreads;
   }

   @Min(1L)
   @Override
   public Integer getCorePoolSize() {
      return this.corePoolSize;
   }

   @Override
   public Optional<Class<? extends ThreadFactory>> getThreadFactoryClass() {
      return Optional.ofNullable(this.threadFactoryClass);
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setType(ExecutorType type) {
      if (type != null) {
         this.type = type;
      }

   }

   public void setParallelism(Integer parallelism) {
      if (parallelism != null) {
         this.parallelism = parallelism;
      }

   }

   public void setNumberOfThreads(Integer nThreads) {
      if (nThreads != null) {
         this.nThreads = nThreads;
      }

   }

   public void setCorePoolSize(Integer corePoolSize) {
      if (corePoolSize != null) {
         this.corePoolSize = corePoolSize;
      }

   }

   public void setThreadFactoryClass(Class<? extends ThreadFactory> threadFactoryClass) {
      this.threadFactoryClass = threadFactoryClass;
   }

   public static UserExecutorConfiguration of(ExecutorType type) {
      ArgumentUtils.check("type", type).notNull();
      UserExecutorConfiguration configuration = new UserExecutorConfiguration(null);
      configuration.type = type;
      return configuration;
   }

   public static UserExecutorConfiguration of(String name, ExecutorType type) {
      ArgumentUtils.check("name", name).notNull();
      ArgumentUtils.check("type", type).notNull();
      UserExecutorConfiguration configuration = new UserExecutorConfiguration(name);
      configuration.type = type;
      return configuration;
   }

   public static UserExecutorConfiguration of(ExecutorType type, int num) {
      ArgumentUtils.check("type", type).notNull();
      UserExecutorConfiguration configuration = of(type);
      configuration.type = type;
      switch(type) {
         case FIXED:
            configuration.nThreads = num;
            break;
         case SCHEDULED:
            configuration.corePoolSize = num;
            break;
         case WORK_STEALING:
            configuration.parallelism = num;
      }

      return configuration;
   }

   public static UserExecutorConfiguration of(ExecutorType type, int num, @Nullable Class<? extends ThreadFactory> threadFactoryClass) {
      UserExecutorConfiguration configuration = of(type, num);
      if (threadFactoryClass != null) {
         configuration.threadFactoryClass = threadFactoryClass;
      }

      return configuration;
   }
}
