package io.micronaut.scheduling.executor;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;

// $FF: synthetic class
@Generated
final class $UserExecutorConfiguration$Introspection extends AbstractInitializableBeanIntrospection {
   private static final AnnotationMetadata $FIELD_CONSTRUCTOR_ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.internMapOf("io.micronaut.context.annotation.ConfigurationInject", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("io.micronaut.core.annotation.Creator", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("io.micronaut.core.annotation.Creator", Collections.EMPTY_MAP),
      AnnotationUtil.internMapOf("io.micronaut.context.annotation.ConfigurationInject", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("io.micronaut.core.annotation.Creator", AnnotationUtil.internListOf("io.micronaut.context.annotation.ConfigurationInject")),
      false,
      true
   );
   private static final Argument[] $CONSTRUCTOR_ARGUMENTS = new Argument[]{
      Argument.of(
         String.class,
         "name",
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP, "javax.annotation.Nullable", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP, "javax.annotation.Nullable", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf(
               "io.micronaut.core.bind.annotation.Bindable",
               AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter"),
               "javax.inject.Qualifier",
               AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter")
            ),
            false,
            true
         ),
         null
      ),
      Argument.of(
         Integer.class,
         "nThreads",
         new DefaultAnnotationMetadata(
            AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            Collections.EMPTY_MAP,
            AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            false,
            true
         ),
         null
      ),
      Argument.of(
         ExecutorType.class,
         "type",
         new DefaultAnnotationMetadata(
            AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            Collections.EMPTY_MAP,
            AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            false,
            true
         ),
         null
      ),
      Argument.of(
         Integer.class,
         "parallelism",
         new DefaultAnnotationMetadata(
            AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            Collections.EMPTY_MAP,
            AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            false,
            true
         ),
         null
      ),
      Argument.of(
         Integer.class,
         "corePoolSize",
         new DefaultAnnotationMetadata(
            AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            Collections.EMPTY_MAP,
            AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            false,
            true
         ),
         null
      ),
      Argument.of(
         Class.class,
         "threadFactoryClass",
         new DefaultAnnotationMetadata(
            AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            Collections.EMPTY_MAP,
            AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            false,
            true
         ),
         Argument.ofTypeVariable(ThreadFactory.class, "T")
      )
   };
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES;
   private static final int[] INDEX_1 = new int[]{2, 3, 4};

   static {
      Map var0;
      $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
         new AbstractInitializableBeanIntrospection.BeanPropertyRef(
            Argument.of(
               String.class,
               "name",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               null
            ),
            0,
            1,
            -1,
            false,
            true
         ),
         new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(ExecutorType.class, "type"), 2, 3, -1, false, true),
         new AbstractInitializableBeanIntrospection.BeanPropertyRef(
            Argument.ofTypeVariable(
               Integer.class,
               "parallelism",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "javax.validation.constraints.Min$List",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "javax.validation.constraints.Min",
                              AnnotationUtil.mapOf("value", 1L),
                              var0 = AnnotationMetadataSupport.getDefaultValues("javax.validation.constraints.Min")
                           )
                        }
                     )
                  ),
                  AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                  AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                  AnnotationUtil.mapOf(
                     "javax.validation.constraints.Min$List",
                     AnnotationUtil.mapOf(
                        "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.Min", AnnotationUtil.mapOf("value", 1L), var0)}
                     )
                  ),
                  AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.Min")),
                  false,
                  true
               ),
               null
            ),
            4,
            5,
            -1,
            false,
            true
         ),
         new AbstractInitializableBeanIntrospection.BeanPropertyRef(
            Argument.ofTypeVariable(
               Integer.class,
               "numberOfThreads",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "javax.validation.constraints.Min$List",
                     AnnotationUtil.mapOf(
                        "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.Min", AnnotationUtil.mapOf("value", 1L), var0)}
                     )
                  ),
                  AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                  AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                  AnnotationUtil.mapOf(
                     "javax.validation.constraints.Min$List",
                     AnnotationUtil.mapOf(
                        "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.Min", AnnotationUtil.mapOf("value", 1L), var0)}
                     )
                  ),
                  AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.Min")),
                  false,
                  true
               ),
               null
            ),
            6,
            7,
            -1,
            false,
            true
         ),
         new AbstractInitializableBeanIntrospection.BeanPropertyRef(
            Argument.ofTypeVariable(
               Integer.class,
               "corePoolSize",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "javax.validation.constraints.Min$List",
                     AnnotationUtil.mapOf(
                        "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.Min", AnnotationUtil.mapOf("value", 1L), var0)}
                     )
                  ),
                  AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                  AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                  AnnotationUtil.mapOf(
                     "javax.validation.constraints.Min$List",
                     AnnotationUtil.mapOf(
                        "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.Min", AnnotationUtil.mapOf("value", 1L), var0)}
                     )
                  ),
                  AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.Min")),
                  false,
                  true
               ),
               null
            ),
            8,
            9,
            -1,
            false,
            true
         ),
         new AbstractInitializableBeanIntrospection.BeanPropertyRef(
            Argument.of(
               Optional.class, "threadFactoryClass", null, Argument.ofTypeVariable(Class.class, "T", null, Argument.ofTypeVariable(ThreadFactory.class, "T"))
            ),
            10,
            -1,
            11,
            true,
            false
         )
      };
   }

   public $UserExecutorConfiguration$Introspection() {
      super(
         UserExecutorConfiguration.class,
         $UserExecutorConfiguration$IntrospectionRef.$ANNOTATION_METADATA,
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
            return ((UserExecutorConfiguration)var2).getName();
         case 1:
            ((UserExecutorConfiguration)var2).setName((String)var3);
            return null;
         case 2:
            return ((UserExecutorConfiguration)var2).getType();
         case 3:
            ((UserExecutorConfiguration)var2).setType((ExecutorType)var3);
            return null;
         case 4:
            return ((UserExecutorConfiguration)var2).getParallelism();
         case 5:
            ((UserExecutorConfiguration)var2).setParallelism((Integer)var3);
            return null;
         case 6:
            return ((UserExecutorConfiguration)var2).getNumberOfThreads();
         case 7:
            ((UserExecutorConfiguration)var2).setNumberOfThreads((Integer)var3);
            return null;
         case 8:
            return ((UserExecutorConfiguration)var2).getCorePoolSize();
         case 9:
            ((UserExecutorConfiguration)var2).setCorePoolSize((Integer)var3);
            return null;
         case 10:
            return ((UserExecutorConfiguration)var2).getThreadFactoryClass();
         case 11:
            throw new UnsupportedOperationException(
               "Cannot mutate property [threadFactoryClass] that is not mutable via a setter method or constructor argument for type: io.micronaut.scheduling.executor.UserExecutorConfiguration"
            );
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      switch(var1.hashCode()) {
         case -96107780:
            if (var1.equals("corePoolSize")) {
               return 4;
            }
            break;
         case 3373707:
            if (var1.equals("name")) {
               return 0;
            }
            break;
         case 3575610:
            if (var1.equals("type")) {
               return 1;
            }
            break;
         case 363738249:
            if (var1.equals("numberOfThreads")) {
               return 3;
            }
            break;
         case 369377720:
            if (var1.equals("threadFactoryClass")) {
               return 5;
            }
            break;
         case 635164956:
            if (var1.equals("parallelism")) {
               return 2;
            }
      }

      return -1;
   }

   @Override
   protected final BeanProperty findIndexedProperty(Class var1, String var2) {
      String var3 = var1.getName();
      return var3.equals("javax.validation.Constraint") && var2 == null ? this.getPropertyByIndex(4) : null;
   }

   @Override
   public final Collection getIndexedProperties(Class var1) {
      String var2 = var1.getName();
      return (Collection)(var2.equals("javax.validation.Constraint") ? this.getBeanPropertiesIndexedSubset(INDEX_1) : Collections.emptyList());
   }

   @Override
   public Object instantiateInternal(Object[] var1) {
      return new UserExecutorConfiguration(
         (String)var1[0], (Integer)var1[1], (ExecutorType)var1[2], (Integer)var1[3], (Integer)var1[4], (Class<? extends ThreadFactory>)var1[5]
      );
   }
}
