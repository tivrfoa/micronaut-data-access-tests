package io.micronaut.core.convert;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Internal
class DefaultArgumentConversionContext<T> implements ArgumentConversionContext<T> {
   private final Argument<T> argument;
   private final Locale finalLocale;
   private final Charset finalCharset;
   private final List<ConversionError> conversionErrors = new ArrayList(3);

   DefaultArgumentConversionContext(Argument<T> argument, Locale finalLocale, Charset finalCharset) {
      this.argument = argument;
      this.finalLocale = finalLocale;
      this.finalCharset = finalCharset;
   }

   @Override
   public Locale getLocale() {
      return this.finalLocale;
   }

   @Override
   public Charset getCharset() {
      return this.finalCharset;
   }

   @Override
   public void reject(Exception exception) {
      if (exception != null) {
         this.conversionErrors.add((ConversionError)() -> exception);
      }

   }

   @Override
   public void reject(Object value, Exception exception) {
      if (exception != null) {
         this.conversionErrors.add(new ConversionError() {
            @Override
            public Optional<Object> getOriginalValue() {
               return value != null ? Optional.of(value) : Optional.empty();
            }

            @Override
            public Exception getCause() {
               return exception;
            }
         });
      }

   }

   @Override
   public Optional<ConversionError> getLastError() {
      return !this.conversionErrors.isEmpty() ? Optional.of(this.conversionErrors.get(this.conversionErrors.size() - 1)) : Optional.empty();
   }

   @Override
   public Iterator<ConversionError> iterator() {
      return Collections.unmodifiableCollection(this.conversionErrors).iterator();
   }

   @Override
   public Argument<T> getArgument() {
      return this.argument;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DefaultArgumentConversionContext<?> that = (DefaultArgumentConversionContext)o;
         return Objects.equals(this.getArgument(), that.getArgument())
            && Objects.equals(this.finalLocale, that.finalLocale)
            && Objects.equals(this.finalCharset, that.finalCharset);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.argument, this.finalLocale, this.finalCharset});
   }

   public String toString() {
      return this.argument.toString();
   }
}
