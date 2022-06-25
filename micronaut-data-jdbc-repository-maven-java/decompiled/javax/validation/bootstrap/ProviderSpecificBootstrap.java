package javax.validation.bootstrap;

import javax.validation.Configuration;
import javax.validation.ValidationProviderResolver;

public interface ProviderSpecificBootstrap<T extends Configuration<T>> {
   ProviderSpecificBootstrap<T> providerResolver(ValidationProviderResolver var1);

   T configure();
}
