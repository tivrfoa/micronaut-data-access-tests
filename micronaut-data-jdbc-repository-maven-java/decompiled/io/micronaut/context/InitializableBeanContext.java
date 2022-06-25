package io.micronaut.context;

import io.micronaut.core.annotation.Internal;

@Internal
public interface InitializableBeanContext extends BeanContext {
   void finalizeConfiguration();
}
