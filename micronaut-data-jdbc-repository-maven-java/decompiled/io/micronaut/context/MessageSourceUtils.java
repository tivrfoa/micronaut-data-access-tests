package io.micronaut.context;

import io.micronaut.core.annotation.NonNull;
import java.util.HashMap;
import java.util.Map;

public class MessageSourceUtils {
   @NonNull
   public static Map<String, Object> variables(@NonNull Object... args) {
      Map<String, Object> variables = new HashMap();
      int count = 0;

      for(Object value : args) {
         variables.put(String.valueOf(count), value);
         ++count;
      }

      return variables;
   }
}
