package io.micronaut.http.client.multipart;

import io.micronaut.core.annotation.NonNull;

class StringPart extends Part<String> {
   protected final String value;

   StringPart(String name, String value) {
      super(name);
      if (value == null) {
         this.value = "";
      } else {
         this.value = value;
      }

   }

   String getContent() {
      return this.value;
   }

   @NonNull
   @Override
   <T> T getData(@NonNull MultipartDataFactory<T> factory) {
      return factory.createAttribute(this.name, this.value);
   }
}
