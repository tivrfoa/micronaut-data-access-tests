package io.micronaut.http.client.multipart;

import io.micronaut.core.annotation.NonNull;

abstract class Part<D> {
   protected final String name;

   Part(String name) {
      if (name == null) {
         throw new IllegalArgumentException("Adding parts with a null name is not allowed");
      } else {
         this.name = name;
      }
   }

   abstract D getContent();

   @NonNull
   abstract <T> T getData(@NonNull MultipartDataFactory<T> factory);
}
