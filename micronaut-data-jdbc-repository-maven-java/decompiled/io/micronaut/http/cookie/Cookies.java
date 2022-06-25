package io.micronaut.http.cookie;

import io.micronaut.core.convert.value.ConvertibleValues;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface Cookies extends ConvertibleValues<Cookie> {
   Set<Cookie> getAll();

   Optional<Cookie> findCookie(CharSequence name);

   default Cookie get(CharSequence name) {
      return (Cookie)this.findCookie(name).orElse(null);
   }

   @Override
   default Set<String> names() {
      return (Set<String>)this.getAll().stream().map(Cookie::getName).collect(Collectors.toSet());
   }
}
