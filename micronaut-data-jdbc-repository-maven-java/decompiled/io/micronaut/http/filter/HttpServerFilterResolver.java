package io.micronaut.http.filter;

import io.micronaut.core.annotation.AnnotationMetadataProvider;

public interface HttpServerFilterResolver<T extends AnnotationMetadataProvider> extends HttpFilterResolver<HttpFilter, T> {
}
