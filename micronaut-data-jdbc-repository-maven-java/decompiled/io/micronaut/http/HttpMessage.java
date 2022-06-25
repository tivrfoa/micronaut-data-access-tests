package io.micronaut.http;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.attr.MutableAttributeHolder;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.http.util.HttpUtil;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

public interface HttpMessage<B> extends MutableAttributeHolder {
   @NonNull
   HttpHeaders getHeaders();

   @NonNull
   @Override
   MutableConvertibleValues<Object> getAttributes();

   @NonNull
   Optional<B> getBody();

   @NonNull
   default Charset getCharacterEncoding() {
      return (Charset)HttpUtil.resolveCharset(this).orElse(StandardCharsets.UTF_8);
   }

   @NonNull
   default HttpMessage<B> setAttribute(@NonNull CharSequence name, Object value) {
      return (HttpMessage<B>)MutableAttributeHolder.super.setAttribute(name, value);
   }

   @NonNull
   default <T> Optional<T> getBody(@NonNull Argument<T> type) {
      ArgumentUtils.requireNonNull("type", type);
      return this.getBody().flatMap(b -> ConversionService.SHARED.convert(b, ConversionContext.of(type)));
   }

   @NonNull
   default <T> Optional<T> getBody(@NonNull Class<T> type) {
      ArgumentUtils.requireNonNull("type", (T)type);
      return this.getBody(Argument.of(type));
   }

   @NonNull
   default Optional<Locale> getLocale() {
      return this.getHeaders().findFirst("Content-Language").map(Locale::new);
   }

   default long getContentLength() {
      return this.getHeaders().contentLength().orElse(-1L);
   }

   @NonNull
   default Optional<MediaType> getContentType() {
      return this.getHeaders().contentType();
   }
}
