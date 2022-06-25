package io.micronaut.core.convert.format;

import io.micronaut.core.naming.conventions.StringConvention;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Inherited
public @interface MapFormat {
   MapFormat.MapTransformation transformation() default MapFormat.MapTransformation.NESTED;

   StringConvention keyFormat() default StringConvention.HYPHENATED;

   public static enum MapTransformation {
      NESTED,
      FLAT;
   }
}
