package io.micronaut.data.model.naming;

import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collections;

// $FF: synthetic class
@Generated
final class $NamingStrategies$UpperCase$Introspection extends AbstractInitializableBeanIntrospection {
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            String.class,
            "foreignKeySuffix",
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
         -1,
         1,
         true,
         false
      )
   };

   public $NamingStrategies$UpperCase$Introspection() {
      super(NamingStrategies.UpperCase.class, $NamingStrategies$UpperCase$IntrospectionRef.$ANNOTATION_METADATA, null, null, $PROPERTIES_REFERENCES, null);
   }

   @Override
   protected final Object dispatchOne(int var1, Object var2, Object var3) {
      switch(var1) {
         case 0:
            return ((NamingStrategies.UpperCase)var2).getForeignKeySuffix();
         case 1:
            throw new UnsupportedOperationException(
               "Cannot mutate property [foreignKeySuffix] that is not mutable via a setter method or constructor argument for type: io.micronaut.data.model.naming.NamingStrategies$UpperCase"
            );
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      return var1.equals("foreignKeySuffix") ? 0 : -1;
   }

   @Override
   public Object instantiate() {
      return new NamingStrategies.UpperCase();
   }
}
