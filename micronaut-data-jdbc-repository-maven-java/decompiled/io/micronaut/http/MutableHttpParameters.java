package io.micronaut.http;

import java.util.Collections;
import java.util.List;

public interface MutableHttpParameters extends HttpParameters {
   default MutableHttpParameters add(CharSequence name, CharSequence value) {
      this.add(name, Collections.singletonList(value));
      return this;
   }

   MutableHttpParameters add(CharSequence name, List<CharSequence> values);
}
