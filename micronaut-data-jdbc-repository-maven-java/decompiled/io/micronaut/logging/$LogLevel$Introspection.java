package io.micronaut.logging;

import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;

// $FF: synthetic class
@Generated
final class $LogLevel$Introspection extends AbstractInitializableBeanIntrospection {
   private static final Argument[] $CONSTRUCTOR_ARGUMENTS = new Argument[]{Argument.of(String.class, "name")};

   public $LogLevel$Introspection() {
      super(LogLevel.class, $LogLevel$IntrospectionRef.$ANNOTATION_METADATA, null, $CONSTRUCTOR_ARGUMENTS, null, null);
   }

   @Override
   public final int propertyIndexOf(String var1) {
      return -1;
   }

   @Override
   public Object instantiateInternal(Object[] var1) {
      return LogLevel.valueOf((String)var1[0]);
   }
}
