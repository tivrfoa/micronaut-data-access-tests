package io.micronaut.core.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.io.service.SoftServiceLoader;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import org.graalvm.nativeimage.ImageSingletons;

@TargetClass(SoftServiceLoader.class)
@Internal
final class ServiceLoaderInitialization {
   private ServiceLoaderInitialization() {
   }

   @Substitute
   private static Set<String> computeServiceTypeNames(URI uri, String path) {
      StaticServiceDefinitions ssd = (StaticServiceDefinitions)ImageSingletons.lookup(StaticServiceDefinitions.class);
      return (Set<String>)ssd.serviceTypeMap.getOrDefault(path, Collections.emptySet());
   }
}
