package io.micronaut.http;

public interface HttpResponseProvider {
   HttpResponse<?> getResponse();
}
