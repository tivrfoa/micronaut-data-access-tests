package io.micronaut.management.endpoint.info.source;

import io.micronaut.context.AbstractExecutableMethodsDefinition;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import java.lang.reflect.Method;
import org.reactivestreams.Publisher;

// $FF: synthetic class
@Generated
final class $ConfigurationInfoSource$Definition$Exec extends AbstractExecutableMethodsDefinition {
   private static final AbstractExecutableMethodsDefinition.MethodReference[] $METHODS_REFERENCES = new AbstractExecutableMethodsDefinition.MethodReference[]{
      new AbstractExecutableMethodsDefinition.MethodReference(
         ConfigurationInfoSource.class,
         $ConfigurationInfoSource$Definition$Reference.$ANNOTATION_METADATA,
         "getSource",
         Argument.of(Publisher.class, "org.reactivestreams.Publisher", null, Argument.ofTypeVariable(PropertySource.class, "T")),
         null,
         false,
         false
      )
   };

   public $ConfigurationInfoSource$Definition$Exec() {
      super($METHODS_REFERENCES);
   }

   @Override
   protected final Object dispatch(int var1, Object var2, Object[] var3) {
      switch(var1) {
         case 0:
            return ((ConfigurationInfoSource)var2).getSource();
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   protected final Method getTargetMethodByIndex(int var1) {
      switch(var1) {
         case 0:
            return ReflectionUtils.getRequiredMethod(ConfigurationInfoSource.class, "getSource", ReflectionUtils.EMPTY_CLASS_ARRAY);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }
}
