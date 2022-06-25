package io.micronaut.http;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.cookie.Cookies;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

public class HttpRequestWrapper<B> extends HttpMessageWrapper<B> implements HttpRequest<B> {
   public HttpRequestWrapper(HttpRequest<B> delegate) {
      super(delegate);
   }

   public HttpRequest<B> getDelegate() {
      return (HttpRequest<B>)super.getDelegate();
   }

   @Override
   public HttpVersion getHttpVersion() {
      return this.getDelegate().getHttpVersion();
   }

   @Override
   public Collection<MediaType> accept() {
      return this.getDelegate().accept();
   }

   @NonNull
   @Override
   public Optional<Principal> getUserPrincipal() {
      return this.getDelegate().getUserPrincipal();
   }

   @NonNull
   @Override
   public <T extends Principal> Optional<T> getUserPrincipal(Class<T> principalType) {
      return this.getDelegate().getUserPrincipal(principalType);
   }

   @Override
   public HttpRequest<B> setAttribute(CharSequence name, Object value) {
      return this.getDelegate().setAttribute(name, value);
   }

   @Override
   public Optional<Locale> getLocale() {
      return this.getDelegate().getLocale();
   }

   @Override
   public Optional<Certificate> getCertificate() {
      return this.getDelegate().getCertificate();
   }

   @Override
   public Cookies getCookies() {
      return this.getDelegate().getCookies();
   }

   @Override
   public HttpParameters getParameters() {
      return this.getDelegate().getParameters();
   }

   @Override
   public HttpMethod getMethod() {
      return this.getDelegate().getMethod();
   }

   @Override
   public String getMethodName() {
      return this.getDelegate().getMethodName();
   }

   @Override
   public URI getUri() {
      return this.getDelegate().getUri();
   }

   @Override
   public String getPath() {
      return this.getDelegate().getPath();
   }

   @Override
   public InetSocketAddress getRemoteAddress() {
      return this.getDelegate().getRemoteAddress();
   }

   @Override
   public InetSocketAddress getServerAddress() {
      return this.getDelegate().getServerAddress();
   }

   @Override
   public String getServerName() {
      return this.getDelegate().getServerName();
   }

   @Override
   public boolean isSecure() {
      return this.getDelegate().isSecure();
   }
}
