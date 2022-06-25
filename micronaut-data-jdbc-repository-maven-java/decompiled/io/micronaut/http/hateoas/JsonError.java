package io.micronaut.http.hateoas;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.http.annotation.Produces;
import java.util.Optional;

@Produces({"application/json"})
public class JsonError extends AbstractResource<JsonError> {
   public static final Argument<JsonError> TYPE = Argument.of(JsonError.class);
   private String message;
   private String logref;
   private String path;

   public JsonError(String message) {
      this.message = message;
   }

   @Internal
   JsonError() {
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public String getMessage() {
      return this.message;
   }

   @JsonProperty("logref")
   public Optional<String> getLogref() {
      return this.logref == null ? Optional.empty() : Optional.of(this.logref);
   }

   @JsonProperty("path")
   public Optional<String> getPath() {
      return this.path == null ? Optional.empty() : Optional.of(this.path);
   }

   @JsonProperty
   public JsonError path(@Nullable String path) {
      this.path = path;
      return this;
   }

   @JsonProperty
   public JsonError logref(@Nullable String logref) {
      this.logref = logref;
      return this;
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      if (this.logref != null) {
         builder.append('[').append(this.logref).append("] ");
      }

      if (this.path != null) {
         builder.append(' ').append(this.path).append(" - ");
      }

      builder.append(this.message);
      return builder.toString();
   }
}
