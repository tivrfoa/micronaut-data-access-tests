package io.micronaut.http.hateoas;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Introspected;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Introspected
public final class GenericResource extends AbstractResource<GenericResource> {
   private final Map<String, Object> additionalProperties = new LinkedHashMap();

   @Internal
   @JsonAnySetter
   public void addProperty(String key, Object v) {
      this.additionalProperties.put(key, v);
   }

   @JsonAnyGetter
   public Map<String, Object> getAdditionalProperties() {
      return this.additionalProperties;
   }

   public boolean equals(Object o) {
      return o instanceof GenericResource
         && this.getLinks().equals(((GenericResource)o).getLinks())
         && this.getEmbedded().equals(((GenericResource)o).getEmbedded())
         && this.getAdditionalProperties().equals(((GenericResource)o).getAdditionalProperties());
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.getLinks(), this.getEmbedded(), this.getAdditionalProperties()});
   }

   public String toString() {
      StringBuilder sb = new StringBuilder()
         .append("GenericResource{")
         .append("_links=")
         .append(this.getLinks())
         .append(", _embedded=")
         .append(this.getEmbedded());
      this.additionalProperties.forEach((k, v) -> sb.append(", ").append(k).append('=').append(v));
      return sb.append('}').toString();
   }
}
