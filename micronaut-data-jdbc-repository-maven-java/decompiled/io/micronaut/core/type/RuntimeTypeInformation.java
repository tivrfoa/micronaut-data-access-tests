package io.micronaut.core.type;

import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.ServiceLoader;

@Internal
final class RuntimeTypeInformation {
   private static final Map<Class<?>, Argument<?>> WRAPPER_TO_TYPE = new HashMap(3);
   private static final Collection<TypeInformationProvider> TYPE_INFORMATION_PROVIDERS;

   static boolean isSpecifiedSingle(AnnotationMetadataProvider annotationMetadata) {
      for(TypeInformationProvider provider : TYPE_INFORMATION_PROVIDERS) {
         if (provider.isSpecifiedSingle(annotationMetadata)) {
            return true;
         }
      }

      return false;
   }

   static boolean isSingle(Class<?> type) {
      for(TypeInformationProvider provider : TYPE_INFORMATION_PROVIDERS) {
         if (provider.isSingle(type)) {
            return true;
         }
      }

      return false;
   }

   static boolean isReactive(Class<?> type) {
      for(TypeInformationProvider provider : TYPE_INFORMATION_PROVIDERS) {
         if (provider.isReactive(type)) {
            return true;
         }
      }

      return false;
   }

   static boolean isCompletable(Class<?> type) {
      for(TypeInformationProvider provider : TYPE_INFORMATION_PROVIDERS) {
         if (provider.isCompletable(type)) {
            return true;
         }
      }

      return false;
   }

   static <T> boolean isWrapperType(Class<T> type) {
      for(TypeInformationProvider provider : TYPE_INFORMATION_PROVIDERS) {
         if (provider.isWrapperType(type)) {
            return true;
         }
      }

      return type == Optional.class || WRAPPER_TO_TYPE.containsKey(type);
   }

   static <T> Argument<?> getWrappedType(@NonNull TypeInformation<?> typeInfo) {
      Argument<?> a = (Argument)WRAPPER_TO_TYPE.get(typeInfo.getType());
      return a != null ? a : (Argument)typeInfo.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
   }

   static {
      WRAPPER_TO_TYPE.put(OptionalDouble.class, Argument.DOUBLE);
      WRAPPER_TO_TYPE.put(OptionalLong.class, Argument.LONG);
      WRAPPER_TO_TYPE.put(OptionalInt.class, Argument.INT);
      ServiceLoader<TypeInformationProvider> loader = ServiceLoader.load(TypeInformationProvider.class);
      List<TypeInformationProvider> informationProviders = new ArrayList(2);

      for(TypeInformationProvider informationProvider : loader) {
         informationProviders.add(informationProvider);
      }

      TYPE_INFORMATION_PROVIDERS = Collections.unmodifiableList(informationProviders);
   }
}
