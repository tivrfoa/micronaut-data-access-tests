package io.micronaut.http.util;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.TypeInformationProvider;
import io.micronaut.http.HttpResponse;

@Internal
public final class HttpTypeInformationProvider implements TypeInformationProvider {
   @Override
   public boolean isWrapperType(Class<?> type) {
      return type == HttpResponse.class || TypeInformationProvider.super.isWrapperType(type);
   }
}
