package io.micronaut.http;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.exceptions.UriSyntaxException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public interface HttpResponse<B> extends HttpMessage<B> {
   HttpStatus getStatus();

   default HttpResponse<B> setAttribute(CharSequence name, Object value) {
      return (HttpResponse<B>)HttpMessage.super.setAttribute(name, value);
   }

   @Nullable
   default String header(@Nullable CharSequence name) {
      return name == null ? null : this.getHeaders().get(name);
   }

   @Nullable
   default B body() {
      return (B)this.getBody().orElse(null);
   }

   default HttpStatus status() {
      return this.getStatus();
   }

   default int code() {
      return this.getStatus().getCode();
   }

   default String reason() {
      return this.getStatus().getReason();
   }

   static <T> MutableHttpResponse<T> ok() {
      return HttpResponseFactory.INSTANCE.ok();
   }

   static <T> MutableHttpResponse<T> notFound() {
      return HttpResponseFactory.INSTANCE.status(HttpStatus.NOT_FOUND);
   }

   static <T> MutableHttpResponse<T> unauthorized() {
      return HttpResponseFactory.INSTANCE.status(HttpStatus.UNAUTHORIZED);
   }

   static <T> MutableHttpResponse<T> notFound(T body) {
      return HttpResponseFactory.INSTANCE.status(HttpStatus.NOT_FOUND).body(body);
   }

   static <T> MutableHttpResponse<T> badRequest() {
      return HttpResponseFactory.INSTANCE.status(HttpStatus.BAD_REQUEST);
   }

   static <T> MutableHttpResponse<T> badRequest(T body) {
      return HttpResponseFactory.INSTANCE.status(HttpStatus.BAD_REQUEST, body);
   }

   static <T> MutableHttpResponse<T> unprocessableEntity() {
      return HttpResponseFactory.INSTANCE.status(HttpStatus.UNPROCESSABLE_ENTITY);
   }

   static <T> MutableHttpResponse<T> notAllowed(HttpMethod... allowed) {
      return HttpResponseFactory.INSTANCE.<T>status(HttpStatus.METHOD_NOT_ALLOWED).headers((Consumer<MutableHttpHeaders>)(headers -> headers.allow(allowed)));
   }

   static <T> MutableHttpResponse<T> notAllowed(Set<HttpMethod> allowed) {
      return notAllowedGeneric(allowed);
   }

   static <T> MutableHttpResponse<T> notAllowedGeneric(Set<? extends CharSequence> allowed) {
      return HttpResponseFactory.INSTANCE
         .<T>status(HttpStatus.METHOD_NOT_ALLOWED)
         .headers((Consumer<MutableHttpHeaders>)(headers -> headers.allowGeneric(allowed)));
   }

   static <T> MutableHttpResponse<T> serverError() {
      return HttpResponseFactory.INSTANCE.status(HttpStatus.INTERNAL_SERVER_ERROR);
   }

   static <T> MutableHttpResponse<T> serverError(T body) {
      return HttpResponseFactory.INSTANCE.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
   }

   static <T> MutableHttpResponse<T> accepted() {
      return HttpResponseFactory.INSTANCE.status(HttpStatus.ACCEPTED);
   }

   static <T> MutableHttpResponse<T> accepted(URI location) {
      return HttpResponseFactory.INSTANCE.<T>status(HttpStatus.ACCEPTED).headers((Consumer<MutableHttpHeaders>)(headers -> headers.location(location)));
   }

   static <T> MutableHttpResponse<T> noContent() {
      return HttpResponseFactory.INSTANCE.status(HttpStatus.NO_CONTENT);
   }

   static <T> MutableHttpResponse<T> notModified() {
      return HttpResponseFactory.INSTANCE.status(HttpStatus.NOT_MODIFIED);
   }

   static <T> MutableHttpResponse<T> ok(T body) {
      return HttpResponseFactory.INSTANCE.ok(body);
   }

   static <T> MutableHttpResponse<T> created(T body) {
      return HttpResponseFactory.INSTANCE.status(HttpStatus.CREATED).body(body);
   }

   static <T> MutableHttpResponse<T> created(URI location) {
      return HttpResponseFactory.INSTANCE.<T>status(HttpStatus.CREATED).headers((Consumer<MutableHttpHeaders>)(headers -> headers.location(location)));
   }

   static <T> MutableHttpResponse<T> created(T body, URI location) {
      return HttpResponseFactory.INSTANCE
         .status(HttpStatus.CREATED)
         .<T>body(body)
         .headers((Consumer<MutableHttpHeaders>)(headers -> headers.location(location)));
   }

   static <T> MutableHttpResponse<T> seeOther(URI location) {
      return HttpResponseFactory.INSTANCE.<T>status(HttpStatus.SEE_OTHER).headers((Consumer<MutableHttpHeaders>)(headers -> headers.location(location)));
   }

   static <T> MutableHttpResponse<T> temporaryRedirect(URI location) {
      return HttpResponseFactory.INSTANCE
         .<T>status(HttpStatus.TEMPORARY_REDIRECT)
         .headers((Consumer<MutableHttpHeaders>)(headers -> headers.location(location)));
   }

   static <T> MutableHttpResponse<T> permanentRedirect(URI location) {
      return HttpResponseFactory.INSTANCE
         .<T>status(HttpStatus.PERMANENT_REDIRECT)
         .headers((Consumer<MutableHttpHeaders>)(headers -> headers.location(location)));
   }

   static <T> MutableHttpResponse<T> redirect(URI location) {
      return HttpResponseFactory.INSTANCE
         .<T>status(HttpStatus.MOVED_PERMANENTLY)
         .headers((Consumer<MutableHttpHeaders>)(headers -> headers.location(location)));
   }

   static <T> MutableHttpResponse<T> status(HttpStatus status) {
      return HttpResponseFactory.INSTANCE.status(status);
   }

   static <T> MutableHttpResponse<T> status(HttpStatus status, String reason) {
      return HttpResponseFactory.INSTANCE.status(status, reason);
   }

   static URI uri(CharSequence uri) {
      try {
         return new URI(uri.toString());
      } catch (URISyntaxException var2) {
         throw new UriSyntaxException(var2);
      }
   }

   default Cookies getCookies() {
      throw new UnsupportedOperationException("Operation not supported on a " + this.getClass() + " response.");
   }

   default Optional<Cookie> getCookie(String name) {
      throw new UnsupportedOperationException("Operation not supported on a " + this.getClass() + " response.");
   }
}
