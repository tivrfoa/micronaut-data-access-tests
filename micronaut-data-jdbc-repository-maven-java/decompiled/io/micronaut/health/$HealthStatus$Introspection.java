package io.micronaut.health;

import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Optional;

// $FF: synthetic class
@Generated
final class $HealthStatus$Introspection extends AbstractInitializableBeanIntrospection {
   private static final Argument[] $CONSTRUCTOR_ARGUMENTS = new Argument[]{
      Argument.of(String.class, "name"),
      Argument.of(String.class, "description"),
      Argument.of(Boolean.class, "operational"),
      Argument.of(Integer.class, "severity")
   };
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "name"), 0, -1, 1, true, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(Optional.class, "description", null, Argument.ofTypeVariable(String.class, "T")), 2, -1, 3, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(Optional.class, "operational", null, Argument.ofTypeVariable(Boolean.class, "T")), 4, -1, 5, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(Optional.class, "severity", null, Argument.ofTypeVariable(Integer.class, "T")), 6, -1, 7, true, false
      )
   };

   public $HealthStatus$Introspection() {
      super(HealthStatus.class, $HealthStatus$IntrospectionRef.$ANNOTATION_METADATA, null, $CONSTRUCTOR_ARGUMENTS, $PROPERTIES_REFERENCES, null);
   }

   @Override
   protected final Object dispatchOne(int var1, Object var2, Object var3) {
      switch(var1) {
         case 0:
            return ((HealthStatus)var2).getName();
         case 1:
            throw new UnsupportedOperationException(
               "Cannot create copy of type [io.micronaut.health.HealthStatus]. Property of type [java.util.Optional] is not assignable to constructor argument [severity]"
            );
         case 2:
            return ((HealthStatus)var2).getDescription();
         case 3:
            throw new UnsupportedOperationException(
               "Cannot mutate property [description] that is not mutable via a setter method or constructor argument for type: io.micronaut.health.HealthStatus"
            );
         case 4:
            return ((HealthStatus)var2).getOperational();
         case 5:
            throw new UnsupportedOperationException(
               "Cannot mutate property [operational] that is not mutable via a setter method or constructor argument for type: io.micronaut.health.HealthStatus"
            );
         case 6:
            return ((HealthStatus)var2).getSeverity();
         case 7:
            throw new UnsupportedOperationException(
               "Cannot mutate property [severity] that is not mutable via a setter method or constructor argument for type: io.micronaut.health.HealthStatus"
            );
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      switch(var1.hashCode()) {
         case -1724546052:
            if (var1.equals("description")) {
               return 1;
            }
            break;
         case 3373707:
            if (var1.equals("name")) {
               return 0;
            }
            break;
         case 129704914:
            if (var1.equals("operational")) {
               return 2;
            }
            break;
         case 1478300413:
            if (var1.equals("severity")) {
               return 3;
            }
      }

      return -1;
   }

   @Override
   public Object instantiateInternal(Object[] var1) {
      return new HealthStatus((String)var1[0], (String)var1[1], (Boolean)var1[2], (Integer)var1[3]);
   }
}
