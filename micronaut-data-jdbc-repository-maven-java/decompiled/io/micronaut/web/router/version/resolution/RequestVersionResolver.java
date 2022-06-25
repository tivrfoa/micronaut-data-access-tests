package io.micronaut.web.router.version.resolution;

import io.micronaut.http.HttpRequest;

public interface RequestVersionResolver extends VersionResolver<HttpRequest<?>, String> {
}
