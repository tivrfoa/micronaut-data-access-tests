package io.micronaut.http.hateoas;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.value.OptionalMultiValues;

@Introspected
public interface Resource {
   String LINKS = "_links";
   String EMBEDDED = "_embedded";

   @JsonProperty("_links")
   default OptionalMultiValues<? extends Link> getLinks() {
      return OptionalMultiValues.empty();
   }

   @JsonProperty("_embedded")
   default OptionalMultiValues<? extends Resource> getEmbedded() {
      return OptionalMultiValues.empty();
   }

   @Internal
   @JsonCreator(
      mode = JsonCreator.Mode.DELEGATING
   )
   static Resource deserialize(GenericResource genericResource) {
      return genericResource;
   }
}
