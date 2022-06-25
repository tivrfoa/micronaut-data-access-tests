package io.micronaut.core.convert.format;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Format("KB")
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ReadableBytes {
}
