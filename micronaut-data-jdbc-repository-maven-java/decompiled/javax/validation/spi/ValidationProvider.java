package javax.validation.spi;

import javax.validation.Configuration;
import javax.validation.ValidatorFactory;

public interface ValidationProvider<T extends Configuration<T>> {
   T createSpecializedConfiguration(BootstrapState var1);

   Configuration<?> createGenericConfiguration(BootstrapState var1);

   ValidatorFactory buildValidatorFactory(ConfigurationState var1);
}
