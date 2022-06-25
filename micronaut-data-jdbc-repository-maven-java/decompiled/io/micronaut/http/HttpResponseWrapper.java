package io.micronaut.http;

public class HttpResponseWrapper<B> extends HttpMessageWrapper<B> implements HttpResponse<B> {
   public HttpResponseWrapper(HttpResponse<B> delegate) {
      super(delegate);
   }

   @Override
   public HttpStatus getStatus() {
      return this.getDelegate().getStatus();
   }

   public HttpResponse<B> getDelegate() {
      return (HttpResponse<B>)super.getDelegate();
   }
}
