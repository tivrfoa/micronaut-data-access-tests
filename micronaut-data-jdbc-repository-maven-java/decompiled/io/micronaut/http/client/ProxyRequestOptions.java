package io.micronaut.http.client;

import java.util.Objects;

public final class ProxyRequestOptions {
   private static final ProxyRequestOptions DEFAULT = builder().build();
   private final boolean retainHostHeader;

   private ProxyRequestOptions(ProxyRequestOptions.Builder builder) {
      this.retainHostHeader = builder.retainHostHeader;
   }

   public static ProxyRequestOptions.Builder builder() {
      return new ProxyRequestOptions.Builder();
   }

   public static ProxyRequestOptions getDefault() {
      return DEFAULT;
   }

   public boolean isRetainHostHeader() {
      return this.retainHostHeader;
   }

   public boolean equals(Object o) {
      return o instanceof ProxyRequestOptions && this.isRetainHostHeader() == ((ProxyRequestOptions)o).isRetainHostHeader();
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.isRetainHostHeader()});
   }

   public static final class Builder {
      private boolean retainHostHeader = false;

      private Builder() {
      }

      public ProxyRequestOptions build() {
         return new ProxyRequestOptions(this);
      }

      public ProxyRequestOptions.Builder retainHostHeader(boolean retainHostHeader) {
         this.retainHostHeader = retainHostHeader;
         return this;
      }

      public ProxyRequestOptions.Builder retainHostHeader() {
         return this.retainHostHeader(true);
      }
   }
}
