package io.micronaut.core.cli;

import io.micronaut.core.annotation.Internal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

@Internal
class DefaultCommandLine implements CommandLine {
   private Properties systemProperties = new Properties();
   private LinkedHashMap<String, Object> undeclaredOptions = new LinkedHashMap();
   private LinkedHashMap<Option, Object> declaredOptions = new LinkedHashMap();
   private List<String> remainingArgs = new ArrayList();
   private String[] rawArguments = new String[0];

   @Override
   public CommandLine parseNew(String[] args) {
      DefaultCommandLine defaultCommandLine = new DefaultCommandLine();
      defaultCommandLine.systemProperties.putAll(this.systemProperties);
      defaultCommandLine.undeclaredOptions.putAll(this.undeclaredOptions);
      defaultCommandLine.declaredOptions.putAll(this.declaredOptions);
      CommandLineParser parser = new CommandLineParser();
      return parser.parse(defaultCommandLine, args);
   }

   @Override
   public Map<Option, Object> getOptions() {
      return this.declaredOptions;
   }

   @Override
   public List<String> getRemainingArgs() {
      return this.remainingArgs;
   }

   @Override
   public Properties getSystemProperties() {
      return this.systemProperties;
   }

   @Override
   public boolean hasOption(String name) {
      return this.declaredOptions.containsKey(new Option(name, null)) || this.undeclaredOptions.containsKey(name);
   }

   @Override
   public Object optionValue(String name) {
      Option opt = new Option(name, null);
      if (this.declaredOptions.containsKey(opt)) {
         return this.declaredOptions.get(opt);
      } else {
         return this.undeclaredOptions.containsKey(name) ? this.undeclaredOptions.get(name) : null;
      }
   }

   @Override
   public String getRemainingArgsString() {
      return this.remainingArgsToString(" ", false);
   }

   @Override
   public Entry<String, Object> lastOption() {
      Iterator<Entry<String, Object>> i = this.undeclaredOptions.entrySet().iterator();

      while(i.hasNext()) {
         Entry<String, Object> next = (Entry)i.next();
         if (!i.hasNext()) {
            return next;
         }
      }

      return null;
   }

   @Override
   public String getRemainingArgsWithOptionsString() {
      return this.remainingArgsToString(" ", true);
   }

   @Override
   public Map<String, Object> getUndeclaredOptions() {
      return Collections.unmodifiableMap(this.undeclaredOptions);
   }

   @Override
   public String[] getRawArguments() {
      return this.rawArguments;
   }

   void addDeclaredOption(Option option) {
      this.addDeclaredOption(option, Boolean.TRUE);
   }

   void addUndeclaredOption(String option) {
      this.undeclaredOptions.put(option, Boolean.TRUE);
   }

   void addUndeclaredOption(String option, Object value) {
      this.undeclaredOptions.put(option, value);
   }

   void addDeclaredOption(Option option, Object value) {
      this.declaredOptions.put(option, value);
   }

   void addRemainingArg(String arg) {
      this.remainingArgs.add(arg);
   }

   void addSystemProperty(String name, String value) {
      this.systemProperties.put(name, value);
   }

   void setRawArguments(String[] args) {
      this.rawArguments = args;
   }

   private String remainingArgsToString(String separator, boolean includeOptions) {
      StringBuilder sb = new StringBuilder();
      String sep = "";
      List<String> args = new ArrayList(this.remainingArgs);
      if (includeOptions) {
         for(Entry<String, Object> entry : this.undeclaredOptions.entrySet()) {
            if (entry.getValue() instanceof Boolean && entry.getValue()) {
               args.add('-' + (String)entry.getKey());
            } else {
               args.add('-' + (String)entry.getKey() + '=' + entry.getValue());
            }
         }
      }

      for(String arg : args) {
         sb.append(sep).append(arg);
         sep = separator;
      }

      return sb.toString();
   }
}
