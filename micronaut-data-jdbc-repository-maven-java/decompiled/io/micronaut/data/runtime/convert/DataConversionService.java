package io.micronaut.data.runtime.convert;

import io.micronaut.core.convert.ConversionService;

public interface DataConversionService<Impl extends DataConversionService> extends ConversionService<Impl> {
}
