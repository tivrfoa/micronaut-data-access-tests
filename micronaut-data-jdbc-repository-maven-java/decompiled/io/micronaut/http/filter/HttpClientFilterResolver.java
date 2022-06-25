package io.micronaut.http.filter;

import io.micronaut.core.annotation.AnnotationMetadataProvider;

public interface HttpClientFilterResolver<T extends AnnotationMetadataProvider> extends HttpFilterResolver<HttpClientFilter, T> {
}
