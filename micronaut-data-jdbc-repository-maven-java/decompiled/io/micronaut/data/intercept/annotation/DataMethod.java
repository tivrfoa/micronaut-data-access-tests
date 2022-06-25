package io.micronaut.data.intercept.annotation;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.Internal;
import io.micronaut.data.intercept.DataInterceptor;
import io.micronaut.data.model.DataType;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Internal
@Inherited
public @interface DataMethod {
   String NAME = DataMethod.class.getName();
   String META_MEMBER_EXPANDABLE_QUERY = "expandableQuery";
   String META_MEMBER_EXPANDABLE_COUNT_QUERY = "expandableCountQuery";
   String META_MEMBER_COUNT_QUERY = "countQuery";
   String META_MEMBER_RESULT_TYPE = "resultType";
   String META_MEMBER_RESULT_DATA_TYPE = "resultDataType";
   String META_MEMBER_ROOT_ENTITY = "rootEntity";
   String META_MEMBER_INTERCEPTOR = "interceptor";
   String META_MEMBER_PARAMETER_BINDING = "parameterBinding";
   String META_MEMBER_PARAMETER_BINDING_PATHS = "parameterBindingPaths";
   String META_MEMBER_PARAMETER_AUTO_POPULATED_PROPERTY_PATHS = "parameterBindingAutoPopulatedPaths";
   String META_MEMBER_PARAMETER_AUTO_POPULATED_PREVIOUS_PROPERTY_PATHS = "parameterBindingAutoPopulatedPreviousPaths";
   String META_MEMBER_PARAMETER_AUTO_POPULATED_PREVIOUS_PROPERTY_INDEXES = "parameterBindingAutoPopulatedPrevious";
   String META_MEMBER_ID_TYPE = "idType";
   String META_MEMBER_PAGE_SIZE = "pageSize";
   String META_MEMBER_PAGE_INDEX = "pageIndex";
   String META_MEMBER_ENTITY = "entity";
   String META_MEMBER_ID = "id";
   String META_MEMBER_DTO = "dto";
   String META_MEMBER_OPTIMISTIC_LOCK = "optimisticLock";
   String META_MEMBER_QUERY_BUILDER = "queryBuilder";
   String META_MEMBER_RAW_QUERY = "rawQuery";
   String META_MEMBER_RAW_COUNT_QUERY = "rawCountQuery";
   String META_MEMBER_PARAMETER_TYPE_DEFS = "parameterTypeDefs";
   String META_MEMBER_PARAMETER_CONVERTERS = "parameterConverters";
   String META_MEMBER_PARAMETERS = "parameters";
   String META_MEMBER_OPERATION_TYPE = "opType";

   Class<? extends DataInterceptor> interceptor();

   Class<?> rootEntity() default void.class;

   Class<?> resultType() default void.class;

   DataType resultDataType() default DataType.OBJECT;

   Class<?> idType() default Serializable.class;

   Property[] parameterBinding() default {};

   String pageable() default "";

   String entity() default "";

   String id() default "";

   int pageSize() default -1;

   long pageIndex() default 0L;

   DataMethodQueryParameter[] parameters() default {};

   public static enum OperationType {
      QUERY,
      COUNT,
      EXISTS,
      UPDATE,
      DELETE,
      INSERT;
   }
}
