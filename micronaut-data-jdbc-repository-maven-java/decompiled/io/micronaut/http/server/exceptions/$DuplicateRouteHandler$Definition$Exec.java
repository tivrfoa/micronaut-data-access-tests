package io.micronaut.http.server.exceptions;

import io.micronaut.context.AbstractExecutableMethodsDefinition;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.web.router.exceptions.DuplicateRouteException;
import java.lang.reflect.Method;
import java.util.Collections;

// $FF: synthetic class
@Generated
final class $DuplicateRouteHandler$Definition$Exec extends AbstractExecutableMethodsDefinition {
   private static final AbstractExecutableMethodsDefinition.MethodReference[] $METHODS_REFERENCES = new AbstractExecutableMethodsDefinition.MethodReference[]{
      new AbstractExecutableMethodsDefinition.MethodReference(
         DuplicateRouteHandler.class,
         new AnnotationMetadataHierarchy(
            $DuplicateRouteHandler$Definition$Reference.$ANNOTATION_METADATA,
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
            Argument.of(HttpRequest.class, "request", null, Argument.ofTypeVariable(Object.class, "B")),
            Argument.of(DuplicateRouteException.class, "exception")
         },
         false,
         false
      )
   };

   public $DuplicateRouteHandler$Definition$Exec() {
      super($METHODS_REFERENCES);
   }

   @Override
   protected final Object dispatch(int var1, Object var2, Object[] var3) {
      switch(var1) {
         case 0:
            return ((DuplicateRouteHandler)var2).handle((HttpRequest)var3[0], (DuplicateRouteException)var3[1]);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   protected final Method getTargetMethodByIndex(int var1) {
      switch(var1) {
         case 0:
            return ReflectionUtils.getRequiredMethod(DuplicateRouteHandler.class, "handle", HttpRequest.class, DuplicateRouteException.class);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }
}
