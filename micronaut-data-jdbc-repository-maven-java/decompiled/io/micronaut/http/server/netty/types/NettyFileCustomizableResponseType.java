package io.micronaut.http.server.netty.types;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.server.types.files.FileCustomizableResponseType;

@Internal
public interface NettyFileCustomizableResponseType extends FileCustomizableResponseType, NettyCustomizableResponseType {
}
