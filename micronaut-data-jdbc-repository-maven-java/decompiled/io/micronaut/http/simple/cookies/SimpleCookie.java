package io.micronaut.http.simple.cookies;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.SameSite;
import java.util.Objects;
import java.util.Optional;

public class SimpleCookie implements Cookie {
   private final String name;
   private String value;
   private String domain;
   private String path;
   private boolean httpOnly;
   private boolean secure;
   private long maxAge;
   private SameSite sameSite;

   public SimpleCookie(String name, String value) {
      this.name = name;
      this.value = value;
   }

   @NonNull
   @Override
   public String getName() {
      return this.name;
   }

   @NonNull
   @Override
   public String getValue() {
      return this.value;
   }

   @Override
   public String getDomain() {
      return this.domain;
   }

   @Override
   public String getPath() {
      return this.path;
   }

   @Override
   public boolean isHttpOnly() {
      return this.httpOnly;
   }

   @Override
   public boolean isSecure() {
      return this.secure;
   }

   @Override
   public long getMaxAge() {
      return this.maxAge;
   }

   @Override
   public Optional<SameSite> getSameSite() {
      return Optional.ofNullable(this.sameSite);
   }

   @NonNull
   @Override
   public Cookie sameSite(SameSite sameSite) {
      this.sameSite = sameSite;
      return this;
   }

   @NonNull
   @Override
   public Cookie maxAge(long maxAge) {
      this.maxAge = maxAge;
      return this;
   }

   @NonNull
   @Override
   public Cookie value(@NonNull String value) {
      this.value = value;
      return this;
   }

   @NonNull
   @Override
   public Cookie domain(String domain) {
      this.domain = domain;
      return this;
   }

   @NonNull
   @Override
   public Cookie path(String path) {
      this.path = path;
      return this;
   }

   @NonNull
   @Override
   public Cookie secure(boolean secure) {
      this.secure = secure;
      return this;
   }

   @NonNull
   @Override
   public Cookie httpOnly(boolean httpOnly) {
      this.httpOnly = httpOnly;
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Cookie)) {
         return false;
      } else {
         Cookie that = (Cookie)o;
         if (!this.getName().equals(that.getName())) {
            return false;
         } else {
            if (this.getPath() == null) {
               if (that.getPath() != null) {
                  return false;
               }
            } else {
               if (that.getPath() == null) {
                  return false;
               }

               if (!this.getPath().equals(that.getPath())) {
                  return false;
               }
            }

            if (this.getDomain() == null) {
               return that.getDomain() == null;
            } else {
               return this.getDomain().equalsIgnoreCase(that.getDomain());
            }
         }
      }
   }

   public int compareTo(Cookie c) {
      int v = this.getName().compareTo(c.getName());
      if (v != 0) {
         return v;
      } else {
         if (this.getPath() == null) {
            if (c.getPath() != null) {
               return -1;
            }
         } else {
            if (c.getPath() == null) {
               return 1;
            }

            v = this.getPath().compareTo(c.getPath());
            if (v != 0) {
               return v;
            }
         }

         if (this.getDomain() == null) {
            return c.getDomain() != null ? -1 : 0;
         } else {
            return c.getDomain() == null ? 1 : this.getDomain().compareToIgnoreCase(c.getDomain());
         }
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.name, this.domain, this.path});
   }

   public String toString() {
      StringBuilder buf = new StringBuilder().append(this.getName()).append('=').append(this.getValue());
      if (this.getDomain() != null) {
         buf.append(", domain=").append(this.getDomain());
      }

      if (this.getPath() != null) {
         buf.append(", path=").append(this.getPath());
      }

      if (this.getMaxAge() >= 0L) {
         buf.append(", maxAge=").append(this.getMaxAge()).append('s');
      }

      if (this.isSecure()) {
         buf.append(", secure");
      }

      if (this.isHttpOnly()) {
         buf.append(", HTTPOnly");
      }

      if (this.getSameSite().isPresent()) {
         buf.append(", SameSite=").append(this.getSameSite().get());
      }

      return buf.toString();
   }
}
