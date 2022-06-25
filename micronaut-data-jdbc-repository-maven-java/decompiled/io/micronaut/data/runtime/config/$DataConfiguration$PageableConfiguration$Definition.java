package io.micronaut.data.runtime.config;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DataConfiguration$PageableConfiguration$Definition
   extends AbstractInitializableBeanDefinition<DataConfiguration.PageableConfiguration>
   implements BeanFactory<DataConfiguration.PageableConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DataConfiguration.PageableConfiguration.class, "<init>", null, null, false
   );
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS;

   @Override
   public DataConfiguration.PageableConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DataConfiguration.PageableConfiguration var4 = new DataConfiguration.PageableConfiguration();
      return (DataConfiguration.PageableConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DataConfiguration.PageableConfiguration var4 = (DataConfiguration.PageableConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.data.pageable.sort-ignore-case")) {
            var4.setSortIgnoreCase(
               super.getPropertyValueForSetter(
                  var1, var2, "setSortIgnoreCase", $INJECTION_METHODS[0].arguments[0], "micronaut.data.pageable.sort-ignore-case", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.data.pageable.sort-delimiter")) {
            var4.setSortDelimiter(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setSortDelimiter", $INJECTION_METHODS[1].arguments[0], "micronaut.data.pageable.sort-delimiter", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.data.pageable.max-page-size")) {
            var4.setMaxPageSize(
               super.getPropertyValueForSetter(var1, var2, "setMaxPageSize", $INJECTION_METHODS[2].arguments[0], "micronaut.data.pageable.max-page-size", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.data.pageable.default-page-size")) {
            var4.setDefaultPageSize(
               super.getPropertyValueForSetter(
                  var1, var2, "setDefaultPageSize", $INJECTION_METHODS[3].arguments[0], "micronaut.data.pageable.default-page-size", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.data.pageable.sort-parameter-name")) {
            var4.setSortParameterName(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setSortParameterName", $INJECTION_METHODS[4].arguments[0], "micronaut.data.pageable.sort-parameter-name", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.data.pageable.size-parameter-name")) {
            var4.setSizeParameterName(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setSizeParameterName", $INJECTION_METHODS[5].arguments[0], "micronaut.data.pageable.size-parameter-name", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.data.pageable.page-parameter-name")) {
            var4.setPageParameterName(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setPageParameterName", $INJECTION_METHODS[6].arguments[0], "micronaut.data.pageable.page-parameter-name", null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   static {
      Map var0;
      $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
         new AbstractInitializableBeanDefinition.MethodReference(
            DataConfiguration.PageableConfiguration.class,
            "setSortIgnoreCase",
            new Argument[]{Argument.of(Boolean.TYPE, "sortIgnoreCase")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property",
                           AnnotationUtil.mapOf("name", "micronaut.data.pageable.sort-ignore-case"),
                           var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Property")
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.data.pageable.sort-ignore-case"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DataConfiguration.PageableConfiguration.class,
            "setSortDelimiter",
            new Argument[]{Argument.of(String.class, "sortDelimiter")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.data.pageable.sort-delimiter"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.data.pageable.sort-delimiter"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DataConfiguration.PageableConfiguration.class,
            "setMaxPageSize",
            new Argument[]{Argument.of(Integer.TYPE, "maxPageSize")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.data.pageable.max-page-size"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.data.pageable.max-page-size"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DataConfiguration.PageableConfiguration.class,
            "setDefaultPageSize",
            new Argument[]{Argument.of(Integer.TYPE, "defaultPageSize")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.data.pageable.default-page-size"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.data.pageable.default-page-size"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DataConfiguration.PageableConfiguration.class,
            "setSortParameterName",
            new Argument[]{Argument.of(String.class, "sortParameterName")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.data.pageable.sort-parameter-name"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.data.pageable.sort-parameter-name"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DataConfiguration.PageableConfiguration.class,
            "setSizeParameterName",
            new Argument[]{Argument.of(String.class, "sizeParameterName")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.data.pageable.size-parameter-name"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.data.pageable.size-parameter-name"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DataConfiguration.PageableConfiguration.class,
            "setPageParameterName",
            new Argument[]{Argument.of(String.class, "pageParameterName")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.data.pageable.page-parameter-name"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "micronaut.data.pageable.page-parameter-name"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         )
      };
   }

   public $DataConfiguration$PageableConfiguration$Definition() {
      this(DataConfiguration.PageableConfiguration.class, $CONSTRUCTOR);
   }

   protected $DataConfiguration$PageableConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DataConfiguration$PageableConfiguration$Definition$Reference.$ANNOTATION_METADATA,
         $INJECTION_METHODS,
         null,
         null,
         null,
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         false,
         true,
         false,
         true,
         false,
         false
      );
   }
}
