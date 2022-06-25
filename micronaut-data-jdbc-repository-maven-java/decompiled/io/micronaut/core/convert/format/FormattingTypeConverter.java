package io.micronaut.core.convert.format;

import io.micronaut.core.convert.TypeConverter;
import java.lang.annotation.Annotation;

public interface FormattingTypeConverter<S, T, A extends Annotation> extends TypeConverter<S, T> {
   Class<A> annotationType();
}
