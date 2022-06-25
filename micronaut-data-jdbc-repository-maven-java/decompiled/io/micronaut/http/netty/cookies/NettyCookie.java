package io.micronaut.http.netty.cookies;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.SameSite;
import io.netty.handler.codec.http.cookie.CookieHeaderNames;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import java.util.Objects;
import java.util.Optional;

@Internal
public class NettyCookie implements Cookie {
   private final io.netty.handler.codec.http.cookie.Cookie nettyCookie;

   public NettyCookie(io.netty.handler.codec.http.cookie.Cookie nettyCookie) {
      this.nettyCookie = nettyCookie;
   }

   public NettyCookie(String name, String value) {
      Objects.requireNonNull(name, "Argument name cannot be null");
      Objects.requireNonNull(value, "Argument value cannot be null");
      this.nettyCookie = new DefaultCookie(name, value);
   }

   public io.netty.handler.codec.http.cookie.Cookie getNettyCookie() {
      return this.nettyCookie;
   }

   @NonNull
   @Override
   public String getName() {
      return this.nettyCookie.name();
   }

   @NonNull
   @Override
   public String getValue() {
      return this.nettyCookie.value();
   }

   @Override
   public String getDomain() {
      return this.nettyCookie.domain();
   }

   @Override
   public String getPath() {
      return this.nettyCookie.path();
   }

   @Override
   public boolean isHttpOnly() {
      return this.nettyCookie.isHttpOnly();
   }

   @Override
   public boolean isSecure() {
      return this.nettyCookie.isSecure();
   }

   @Override
   public long getMaxAge() {
      return this.nettyCookie.maxAge();
   }

   @NonNull
   @Override
   public Cookie maxAge(long maxAge) {
      this.nettyCookie.setMaxAge(maxAge);
      return this;
   }

   @Override
   public Optional<SameSite> getSameSite() {
      if (this.nettyCookie instanceof DefaultCookie) {
         CookieHeaderNames.SameSite sameSite = ((DefaultCookie)this.nettyCookie).sameSite();
         if (sameSite != null) {
            return Optional.of(SameSite.valueOf(sameSite.name()));
         }
      }

      return Optional.empty();
   }

   @NonNull
   @Override
   public Cookie sameSite(@Nullable SameSite sameSite) {
      if (this.nettyCookie instanceof DefaultCookie) {
         ((DefaultCookie)this.nettyCookie).setSameSite(sameSite == null ? null : CookieHeaderNames.SameSite.valueOf(sameSite.name()));
      }

      return this;
   }

   @NonNull
   @Override
   public Cookie value(@NonNull String value) {
      this.nettyCookie.setValue(value);
      return this;
   }

   @NonNull
   @Override
   public Cookie domain(String domain) {
      this.nettyCookie.setDomain(domain);
      return this;
   }

   @NonNull
   @Override
   public Cookie path(String path) {
      this.nettyCookie.setPath(path);
      return this;
   }

   @NonNull
   @Override
   public Cookie secure(boolean secure) {
      this.nettyCookie.setSecure(secure);
      return this;
   }

   @NonNull
   @Override
   public Cookie httpOnly(boolean httpOnly) {
      this.nettyCookie.setHttpOnly(httpOnly);
      return this;
   }

   public int compareTo(Cookie o) {
      NettyCookie nettyCookie = (NettyCookie)o;
      return nettyCookie.nettyCookie.compareTo(this.nettyCookie);
   }
}
