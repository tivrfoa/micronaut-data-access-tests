package io.micronaut.core.cli;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

public interface CommandLine {
   List<String> getRemainingArgs();

   Properties getSystemProperties();

   Map<Option, Object> getOptions();

   boolean hasOption(String name);

   Object optionValue(String name);

   Entry<String, Object> lastOption();

   String getRemainingArgsString();

   String getRemainingArgsWithOptionsString();

   Map<String, Object> getUndeclaredOptions();

   CommandLine parseNew(String[] args);

   String[] getRawArguments();

   static CommandLine.Builder build() {
      return new CommandLineParser();
   }

   static CommandLine parse(String... args) {
      return (CommandLine)(args != null && args.length != 0 ? new CommandLineParser().parse(args) : new DefaultCommandLine());
   }

   public interface Builder<T extends CommandLine.Builder> {
      T addOption(String name, String description);

      CommandLine parseString(String string);

      CommandLine parse(String... args);
   }
}
