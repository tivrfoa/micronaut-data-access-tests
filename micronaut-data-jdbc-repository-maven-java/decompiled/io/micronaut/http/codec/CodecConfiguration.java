package io.micronaut.http.codec;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.http.MediaType;
import java.util.Collections;
import java.util.List;

@EachProperty("micronaut.codec")
public class CodecConfiguration {
   public static final String PREFIX = "micronaut.codec";
   private List<MediaType> additionalTypes = Collections.emptyList();

   public List<MediaType> getAdditionalTypes() {
      return this.additionalTypes;
   }

   public void setAdditionalTypes(List<MediaType> additionalTypes) {
      this.additionalTypes = additionalTypes;
   }
}
