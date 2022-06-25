package io.micronaut.http.server.exceptions;

import io.micronaut.context.AbstractExecutableMethodsDefinition;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.lang.reflect.Method;
import java.util.Collections;

// $FF: synthetic class
@Generated
final class $HttpStatusHandler$Definition$Exec extends AbstractExecutableMethodsDefinition {
   private static final AbstractExecutableMethodsDefinition.MethodReference[] $METHODS_REFERENCES = new AbstractExecutableMethodsDefinition.MethodReference[]{
      new AbstractExecutableMethodsDefinition.MethodReference(
         HttpStatusHandler.class,
         new AnnotationMetadataHierarchy(
            $HttpStatusHandler$Definition$Reference.$ANNOTATION_METADATA,
            new DefaultAnnotationMetadata(
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("io.micronaut.context.annotation.Executable", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         "handle",
         Argument.of(HttpResponse.class, "io.micronaut.http.HttpResponse", null, Argument.ofTypeVariable(Object.class, "B")),
         new Argument[]{
            Argument.of(HttpRequest.class, "request", null, Argument.ofTypeVariable(Object.class, "B")), Argument.of(HttpStatusException.class, "exception")
         },
         false,
         false
      )
   };

   public $HttpStatusHandler$Definition$Exec() {
      super($METHODS_REFERENCES);
   }

   @Override
   protected final Object dispatch(int var1, Object var2, Object[] var3) {
      switch(var1) {
         case 0:
            return ((HttpStatusHandler)var2).handle((HttpRequest)var3[0], (HttpStatusException)var3[1]);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   protected final Method getTargetMethodByIndex(int var1) {
      switch(var1) {
         case 0:
            return ReflectionUtils.getRequiredMethod(HttpStatusHandler.class, "handle", HttpRequest.class, HttpStatusException.class);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }
}
