package io.micronaut.context.env;

import io.micronaut.core.cli.CommandLine;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CommandLinePropertySource extends MapPropertySource {
   public static final int POSITION = 0;
   public static final String NAME = "cli";

   public CommandLinePropertySource(CommandLine commandLine) {
      super("cli", resolveValues(commandLine));
   }

   @Override
   public int getOrder() {
      return 0;
   }

   private static Map<String, Object> resolveValues(CommandLine commandLine) {
      if (commandLine == null) {
         return Collections.emptyMap();
      } else {
         LinkedHashMap<String, Object> map = new LinkedHashMap(commandLine.getUndeclaredOptions());

         for(Entry<Object, Object> entry : commandLine.getSystemProperties().entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue());
         }

         return map;
      }
   }
}
