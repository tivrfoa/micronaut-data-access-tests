package io.micronaut.http.hateoas;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.value.OptionalMultiValues;
import io.micronaut.http.annotation.Produces;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

@Produces({"application/hal+json"})
@Introspected
public abstract class AbstractResource<Impl extends AbstractResource> implements Resource {
   private final Map<CharSequence, List<Link>> linkMap = new LinkedHashMap(1);
   private final Map<CharSequence, List<Resource>> embeddedMap = new LinkedHashMap(1);

   public Impl link(@Nullable CharSequence ref, @Nullable Link link) {
      if (StringUtils.isNotEmpty(ref) && link != null) {
         List<Link> links = (List)this.linkMap.computeIfAbsent(ref, charSequence -> new ArrayList());
         links.add(link);
      }

      return (Impl)this;
   }

   public Impl link(@Nullable CharSequence ref, @Nullable String link) {
      if (StringUtils.isNotEmpty(ref) && link != null) {
         List<Link> links = (List)this.linkMap.computeIfAbsent(ref, charSequence -> new ArrayList());
         links.add(Link.of(link));
      }

      return (Impl)this;
   }

   public Impl embedded(CharSequence ref, Resource resource) {
      if (StringUtils.isNotEmpty(ref) && resource != null) {
         List<Resource> resources = (List)this.embeddedMap.computeIfAbsent(ref, charSequence -> new ArrayList());
         resources.add(resource);
      }

      return (Impl)this;
   }

   public Impl embedded(CharSequence ref, Resource... resource) {
      if (StringUtils.isNotEmpty(ref) && resource != null) {
         List<Resource> resources = (List)this.embeddedMap.computeIfAbsent(ref, charSequence -> new ArrayList());
         resources.addAll(Arrays.asList(resource));
      }

      return (Impl)this;
   }

   public Impl embedded(CharSequence ref, List<Resource> resourceList) {
      if (StringUtils.isNotEmpty(ref) && resourceList != null) {
         List<Resource> resources = (List)this.embeddedMap.computeIfAbsent(ref, charSequence -> new ArrayList());
         resources.addAll(resourceList);
      }

      return (Impl)this;
   }

   @JsonProperty("_links")
   @Override
   public OptionalMultiValues<Link> getLinks() {
      return OptionalMultiValues.of(this.linkMap);
   }

   @JsonProperty("_embedded")
   @Override
   public OptionalMultiValues<Resource> getEmbedded() {
      return OptionalMultiValues.of(this.embeddedMap);
   }

   @Internal
   @ReflectiveAccess
   @JsonProperty("_links")
   public final void setLinks(Map<String, Object> links) {
      for(Entry<String, Object> entry : links.entrySet()) {
         String name = (String)entry.getKey();
         Object value = entry.getValue();
         if (value instanceof Map) {
            Map<String, Object> linkMap = (Map)value;
            this.link(name, linkMap);
         }
      }

   }

   @Internal
   @ReflectiveAccess
   @JsonProperty("_embedded")
   public final void setEmbedded(Map<String, List<Resource>> embedded) {
      this.embeddedMap.putAll(embedded);
   }

   private void link(String name, Map<String, Object> linkMap) {
      ConvertibleValues<Object> values = ConvertibleValues.of(linkMap);
      Optional<String> uri = values.get(Link.HREF, String.class);
      uri.ifPresent(uri1 -> {
         Link.Builder link = Link.build(uri1);
         values.get("templated", Boolean.class).ifPresent(link::templated);
         values.get("hreflang", String.class).ifPresent(link::hreflang);
         values.get("title", String.class).ifPresent(link::title);
         values.get("profile", String.class).ifPresent(link::profile);
         values.get("deprecation", String.class).ifPresent(link::deprecation);
         this.link(name, link.build());
      });
   }
}
