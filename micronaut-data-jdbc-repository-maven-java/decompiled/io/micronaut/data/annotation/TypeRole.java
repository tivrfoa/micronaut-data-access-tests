package io.micronaut.data.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Documented
@Inherited
public @interface TypeRole {
   String PAGEABLE = "pageable";
   String SORT = "sort";
   String ID = "id";
   String ENTITY = "entity";
   String ENTITIES = "entities";
   @Deprecated
   String LAST_UPDATED_PROPERTY = "lastUpdatedProperty";
   String SLICE = "slice";
   String PAGE = "page";

   String role();

   Class<?> type();
}
