package io.micronaut.data.jdbc.convert.vendor;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.data.runtime.convert.DataTypeConverter;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import oracle.sql.TIMESTAMP;

// $FF: synthetic class
@Generated
class $OracleTypeConvertersFactory$FromOracleTimestampToTimestamp3$Definition
   extends AbstractInitializableBeanDefinition<DataTypeConverter>
   implements BeanFactory<DataTypeConverter> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      OracleTypeConvertersFactory.class,
      "fromOracleTimestampToTimestamp",
      null,
      new DefaultAnnotationMetadata(
         AnnotationUtil.internMapOf("io.micronaut.context.annotation.Prototype", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.Bean", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.Bean", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Prototype",
            Collections.EMPTY_MAP,
            "io.micronaut.core.annotation.Indexes",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Indexed",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_0()),
                     AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
                  )
               }
            )
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Bean",
            AnnotationUtil.internListOf("io.micronaut.context.annotation.Prototype"),
            "javax.inject.Scope",
            AnnotationUtil.internListOf("io.micronaut.context.annotation.Prototype")
         ),
         false,
         true
      ),
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.core.convert.TypeConverter",
      new Argument[]{Argument.ofTypeVariable(TIMESTAMP.class, "S"), Argument.ofTypeVariable(Timestamp.class, "T")},
      "io.micronaut.data.runtime.convert.DataTypeConverter",
      new Argument[]{Argument.ofTypeVariable(TIMESTAMP.class, "S"), Argument.ofTypeVariable(Timestamp.class, "T")}
   );

   @Override
   public DataTypeConverter build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      Object var4 = ((DefaultBeanContext)var2).getBean(var1, OracleTypeConvertersFactory.class);
      DataTypeConverter var5 = ((OracleTypeConvertersFactory)var4).fromOracleTimestampToTimestamp();
      return (DataTypeConverter)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DataTypeConverter var4 = (DataTypeConverter)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $OracleTypeConvertersFactory$FromOracleTimestampToTimestamp3$Definition() {
      this(DataTypeConverter.class, $CONSTRUCTOR);
   }

   protected $OracleTypeConvertersFactory$FromOracleTimestampToTimestamp3$Definition(
      Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2
   ) {
      super(
         var1,
         var2,
         $OracleTypeConvertersFactory$FromOracleTimestampToTimestamp3$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         $TYPE_ARGUMENTS,
         Optional.of("io.micronaut.context.annotation.Prototype"),
         false,
         false,
         false,
         false,
         false,
         false,
         false,
         false
      );
   }
}
