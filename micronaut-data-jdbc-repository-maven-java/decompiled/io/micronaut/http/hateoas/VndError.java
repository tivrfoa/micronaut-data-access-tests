package io.micronaut.http.hateoas;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Produces;
import java.util.List;

@Produces({"application/vnd.error+json"})
public class VndError extends JsonError {
   public VndError(String message) {
      super(message);
   }

   @Internal
   VndError() {
   }

   public VndError path(@Nullable String path) {
      return (VndError)super.path(path);
   }

   public VndError logref(@Nullable String logref) {
      return (VndError)super.logref(logref);
   }

   public VndError link(@Nullable CharSequence ref, @Nullable Link link) {
      return (VndError)super.link(ref, link);
   }

   public VndError link(@Nullable CharSequence ref, @Nullable String link) {
      return (VndError)super.link(ref, link);
   }

   public VndError embedded(CharSequence ref, Resource resource) {
      return (VndError)super.embedded(ref, resource);
   }

   public VndError embedded(CharSequence ref, Resource... resource) {
      return (VndError)super.embedded(ref, resource);
   }

   public VndError embedded(CharSequence ref, List<Resource> resourceList) {
      return (VndError)super.embedded(ref, resourceList);
   }
}
