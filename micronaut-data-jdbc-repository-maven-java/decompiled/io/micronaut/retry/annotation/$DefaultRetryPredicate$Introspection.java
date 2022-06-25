package io.micronaut.retry.annotation;

import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.List;

// $FF: synthetic class
@Generated
final class $DefaultRetryPredicate$Introspection extends AbstractInitializableBeanIntrospection {
   private static final Argument[] $CONSTRUCTOR_ARGUMENTS = new Argument[]{
      Argument.of(List.class, "includes", null, Argument.ofTypeVariable(Class.class, "E", null, Argument.ofTypeVariable(Throwable.class, "T"))),
      Argument.of(List.class, "excludes", null, Argument.ofTypeVariable(Class.class, "E", null, Argument.ofTypeVariable(Throwable.class, "T")))
   };

   public $DefaultRetryPredicate$Introspection() {
      super(DefaultRetryPredicate.class, $DefaultRetryPredicate$IntrospectionRef.$ANNOTATION_METADATA, null, $CONSTRUCTOR_ARGUMENTS, null, null);
   }

   @Override
   public final int propertyIndexOf(String var1) {
      return -1;
   }

   @Override
   public Object instantiate() {
      return new DefaultRetryPredicate();
   }

   @Override
   public Object instantiateInternal(Object[] var1) {
      return new DefaultRetryPredicate((List<Class<? extends Throwable>>)var1[0], (List<Class<? extends Throwable>>)var1[1]);
   }
}
