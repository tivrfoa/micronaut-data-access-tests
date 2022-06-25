package io.micronaut.context;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;
import java.util.Locale;
import java.util.Objects;

public abstract class AbstractMessageSource implements MessageSource {
   private static final char QUOT = '\'';
   private static final char L_BRACE = '{';
   private static final char R_BRACE = '}';

   @NonNull
   @Override
   public String interpolate(@NonNull String template, @NonNull MessageSource.MessageContext context) {
      ArgumentUtils.requireNonNull("template", template);
      ArgumentUtils.requireNonNull("context", context);
      StringBuilder builder = new StringBuilder();

      for(int i = 0; i < template.length(); ++i) {
         char c = template.charAt(i);
         if (c == '\'') {
            int next = i + 1;
            if (next < template.length()) {
               c = template.charAt(next);
               if (c == '\'') {
                  ++i;
                  builder.append('\'');
               } else {
                  StringBuilder escaped;
                  for(escaped = new StringBuilder(); c != '\''; c = template.charAt(next)) {
                     escaped.append(c);
                     if (++next >= template.length()) {
                        break;
                     }
                  }

                  if (escaped.length() > 0) {
                     i = next;
                     builder.append(escaped);
                  }
               }
            }
         } else if (c != '{') {
            builder.append(c);
         } else {
            StringBuilder variable = new StringBuilder();
            int next = i + 1;
            if (next >= template.length()) {
               builder.append(c);
            } else {
               for(c = template.charAt(next); c != '}'; c = template.charAt(next)) {
                  variable.append(c);
                  if (++next >= template.length()) {
                     break;
                  }
               }

               if (variable.length() > 0) {
                  i = next;
                  String var = variable.toString();
                  if (c == '}') {
                     Object val = context.getVariables().get(var);
                     if (val != null) {
                        builder.append(val);
                     } else {
                        String resolved = (String)this.getMessage(var, context).orElse(var);
                        builder.append(resolved);
                     }
                  } else {
                     builder.append('{').append(var);
                  }
               }
            }
         }
      }

      return builder.toString();
   }

   protected final class MessageKey {
      final Locale locale;
      final String code;

      public MessageKey(@NonNull Locale locale, @NonNull String code) {
         this.locale = locale;
         this.code = code;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            AbstractMessageSource.MessageKey key = (AbstractMessageSource.MessageKey)o;
            return this.locale.equals(key.locale) && this.code.equals(key.code);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.locale, this.code});
      }
   }
}
