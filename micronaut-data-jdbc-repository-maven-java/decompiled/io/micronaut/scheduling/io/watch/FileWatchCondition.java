package io.micronaut.scheduling.io.watch;

import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.util.CollectionUtils;
import java.io.File;
import java.util.List;

@Introspected
public class FileWatchCondition implements Condition {
   @Override
   public boolean matches(ConditionContext context) {
      List<String> paths = (List)context.getProperty("micronaut.io.watch.paths", ConversionContext.LIST_OF_STRING).orElse(null);
      if (CollectionUtils.isNotEmpty(paths)) {
         boolean matchedPaths = paths.stream().anyMatch(p -> new File(p).exists());
         if (!matchedPaths) {
            context.fail("File watch disabled because no paths matching the watch pattern exist (Paths: " + paths + ")");
         }

         return matchedPaths;
      } else {
         context.fail("File watch disabled because no watch paths specified");
         return false;
      }
   }
}
