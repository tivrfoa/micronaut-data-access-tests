package javax.validation.bootstrap;

import javax.validation.Configuration;
import javax.validation.ValidationProviderResolver;

public interface GenericBootstrap {
   GenericBootstrap providerResolver(ValidationProviderResolver var1);

   Configuration<?> configure();
}
