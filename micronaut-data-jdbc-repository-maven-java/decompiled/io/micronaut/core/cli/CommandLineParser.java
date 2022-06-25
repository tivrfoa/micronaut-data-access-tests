package io.micronaut.core.cli;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.cli.exceptions.ParseException;
import io.micronaut.core.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

@Internal
class CommandLineParser implements CommandLine.Builder<CommandLineParser> {
   private static final String DEFAULT_PADDING = "        ";
   private Map<String, Option> declaredOptions = new HashMap();
   private int longestOptionNameLength = 0;
   private String usageMessage;

   public CommandLineParser addOption(String name, String description) {
      int length = name.length();
      if (length > this.longestOptionNameLength) {
         this.longestOptionNameLength = length;
      }

      this.declaredOptions.put(name, new Option(name, description));
      return this;
   }

   @Override
   public CommandLine parseString(String string) {
      return this.parse(translateCommandline(string));
   }

   @Override
   public CommandLine parse(String... args) {
      DefaultCommandLine cl = this.createCommandLine();
      return this.parse(cl, args);
   }

   CommandLine parse(DefaultCommandLine cl, String[] args) {
      this.parseInternal(cl, args);
      return cl;
   }

   public String getOptionsHelpMessage() {
      String ls = System.getProperty("line.separator");
      this.usageMessage = "Available options:";
      StringBuilder sb = new StringBuilder(this.usageMessage);
      sb.append(ls);

      for(Option option : this.declaredOptions.values()) {
         String name = option.getName();
         int extraPadding = this.longestOptionNameLength - name.length();
         sb.append(" -").append(name);

         for(int i = 0; i < extraPadding; ++i) {
            sb.append(' ');
         }

         sb.append("        ").append(option.getDescription()).append(ls);
      }

      return sb.toString();
   }

   private void parseInternal(DefaultCommandLine cl, String[] args) {
      cl.setRawArguments(args);
      String lastOptionName = null;

      for(String arg : args) {
         if (arg != null) {
            String trimmed = arg.trim();
            if (StringUtils.isNotEmpty(trimmed)) {
               if (trimmed.charAt(0) == '"' && trimmed.charAt(trimmed.length() - 1) == '"') {
                  trimmed = trimmed.substring(1, trimmed.length() - 1);
               }

               if (trimmed.charAt(0) == '-') {
                  lastOptionName = this.processOption(cl, trimmed);
               } else if (lastOptionName != null) {
                  Option opt = (Option)this.declaredOptions.get(lastOptionName);
                  if (opt != null) {
                     cl.addDeclaredOption(opt, trimmed);
                  } else {
                     cl.addUndeclaredOption(lastOptionName, trimmed);
                  }

                  lastOptionName = null;
               } else {
                  cl.addRemainingArg(trimmed);
               }
            }
         }
      }

   }

   protected DefaultCommandLine createCommandLine() {
      return new DefaultCommandLine();
   }

   protected String processOption(DefaultCommandLine cl, String arg) {
      if (arg.length() < 2) {
         return null;
      } else if (arg.charAt(1) == 'D' && arg.contains("=")) {
         this.processSystemArg(cl, arg);
         return null;
      } else {
         arg = (arg.charAt(1) == '-' ? arg.substring(2, arg.length()) : arg.substring(1, arg.length())).trim();
         if (arg.contains("=")) {
            String[] split = arg.split("=", 2);
            String name = split[0].trim();
            this.validateOptionName(name);
            String value = split.length > 1 ? split[1].trim() : "";
            if (this.declaredOptions.containsKey(name)) {
               cl.addDeclaredOption((Option)this.declaredOptions.get(name), value);
            } else {
               cl.addUndeclaredOption(name, value);
            }

            return null;
         } else {
            this.validateOptionName(arg);
            if (this.declaredOptions.containsKey(arg)) {
               cl.addDeclaredOption((Option)this.declaredOptions.get(arg));
            } else {
               cl.addUndeclaredOption(arg);
            }

            return arg;
         }
      }
   }

   protected void processSystemArg(DefaultCommandLine cl, String arg) {
      int i = arg.indexOf(61);
      String name = arg.substring(2, i);
      String value = arg.substring(i + 1, arg.length());
      cl.addSystemProperty(name, value);
   }

   private void validateOptionName(String name) {
      if (name.contains(" ")) {
         throw new ParseException("Invalid argument: " + name);
      }
   }

   static String[] translateCommandline(String toProcess) {
      if (toProcess != null && toProcess.length() != 0) {
         int normal = 0;
         int inQuote = 1;
         int inDoubleQuote = 2;
         int state = 0;
         StringTokenizer tok = new StringTokenizer(toProcess, "\"' ", true);
         ArrayList<String> result = new ArrayList();
         StringBuilder current = new StringBuilder();
         boolean lastTokenHasBeenQuoted = false;

         while(tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            switch(state) {
               case 1:
                  if ("'".equals(nextTok)) {
                     lastTokenHasBeenQuoted = true;
                     state = 0;
                  } else {
                     current.append(nextTok);
                  }
                  continue;
               case 2:
                  if ("\"".equals(nextTok)) {
                     lastTokenHasBeenQuoted = true;
                     state = 0;
                  } else {
                     current.append(nextTok);
                  }
                  continue;
            }

            if ("'".equals(nextTok)) {
               state = 1;
            } else if ("\"".equals(nextTok)) {
               state = 2;
            } else if (" ".equals(nextTok)) {
               if (lastTokenHasBeenQuoted || current.length() != 0) {
                  result.add(current.toString());
                  current.setLength(0);
               }
            } else {
               current.append(nextTok);
            }

            lastTokenHasBeenQuoted = false;
         }

         if (lastTokenHasBeenQuoted || current.length() != 0) {
            result.add(current.toString());
         }

         if (state != 1 && state != 2) {
            return (String[])result.toArray(new String[0]);
         } else {
            throw new ParseException("unbalanced quotes in " + toProcess);
         }
      } else {
         return new String[0];
      }
   }
}
