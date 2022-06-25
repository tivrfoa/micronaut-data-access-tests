package io.micronaut.inject.provider;

import io.micronaut.context.BeanProvider;
import io.micronaut.core.type.TypeInformationProvider;
import jakarta.inject.Provider;

public final class ProviderTypeInformationProvider implements TypeInformationProvider {
   @Override
   public boolean isWrapperType(Class<?> type) {
      return BeanProvider.class == type || Provider.class == type || type.getName().equals("javax.inject.Provider");
   }
}
