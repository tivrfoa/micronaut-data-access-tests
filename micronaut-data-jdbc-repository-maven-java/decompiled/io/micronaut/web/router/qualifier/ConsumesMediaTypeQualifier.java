package io.micronaut.web.router.qualifier;

import io.micronaut.context.Qualifier;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.inject.BeanType;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConsumesMediaTypeQualifier<T> implements Qualifier<T> {
   private final MediaType contentType;

   public ConsumesMediaTypeQualifier(MediaType contentType) {
      this.contentType = contentType;
   }

   @Override
   public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
      return candidates.filter(candidate -> {
         MediaType[] consumes = MediaType.of((CharSequence[])candidate.getAnnotationMetadata().stringValues(Consumes.class));
         if (ArrayUtils.isNotEmpty(consumes)) {
            Set<String> consumedTypes = (Set)Arrays.stream(consumes).map(MediaType::getExtension).collect(Collectors.toSet());
            return consumedTypes.contains(this.contentType.getExtension());
         } else {
            return false;
         }
      });
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ConsumesMediaTypeQualifier that = (ConsumesMediaTypeQualifier)o;
         return this.contentType.equals(that.contentType);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.contentType.hashCode();
   }

   public String toString() {
      return "Content-Type: " + this.contentType;
   }
}
