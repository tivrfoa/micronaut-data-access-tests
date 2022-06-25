package io.micronaut.http.cookie;

import io.micronaut.core.io.service.ServiceDefinition;
import io.micronaut.core.io.service.SoftServiceLoader;

public interface CookieFactory {
   CookieFactory INSTANCE = (CookieFactory)SoftServiceLoader.load(CookieFactory.class)
      .firstOr("io.micronaut.http.netty.cookies.NettyCookieFactory", CookieFactory.class.getClassLoader())
      .map(ServiceDefinition::load)
      .orElse(null);

   Cookie create(String name, String value);
}
