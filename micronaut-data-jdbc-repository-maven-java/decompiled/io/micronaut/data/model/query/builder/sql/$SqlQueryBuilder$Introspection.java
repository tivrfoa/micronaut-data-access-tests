package io.micronaut.data.model.query.builder.sql;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collections;

// $FF: synthetic class
@Generated
final class $SqlQueryBuilder$Introspection extends AbstractInitializableBeanIntrospection {
   private static final AnnotationMetadata $FIELD_CONSTRUCTOR_ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.internMapOf("io.micronaut.core.annotation.Creator", Collections.EMPTY_MAP),
      Collections.EMPTY_MAP,
      Collections.EMPTY_MAP,
      AnnotationUtil.internMapOf("io.micronaut.core.annotation.Creator", Collections.EMPTY_MAP),
      Collections.EMPTY_MAP,
      false,
      true
   );
   private static final Argument[] $CONSTRUCTOR_ARGUMENTS = new Argument[]{Argument.of(AnnotationMetadata.class, "annotationMetadata")};
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(Dialect.class, "dialect"), 0, -1, 1, true, false)
   };

   public $SqlQueryBuilder$Introspection() {
      super(
         SqlQueryBuilder.class,
         $SqlQueryBuilder$IntrospectionRef.$ANNOTATION_METADATA,
         $FIELD_CONSTRUCTOR_ANNOTATION_METADATA,
         $CONSTRUCTOR_ARGUMENTS,
         $PROPERTIES_REFERENCES,
         null
      );
   }

   @Override
   protected final Object dispatchOne(int var1, Object var2, Object var3) {
      switch(var1) {
         case 0:
            return ((SqlQueryBuilder)var2).getDialect();
         case 1:
            throw new UnsupportedOperationException(
               "Cannot mutate property [dialect] that is not mutable via a setter method or constructor argument for type: io.micronaut.data.model.query.builder.sql.SqlQueryBuilder"
            );
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      return var1.equals("dialect") ? 0 : -1;
   }

   @Override
   public Object instantiate() {
      return new SqlQueryBuilder();
   }

   @Override
   public Object instantiateInternal(Object[] var1) {
      return new SqlQueryBuilder((AnnotationMetadata)var1[0]);
   }
}
