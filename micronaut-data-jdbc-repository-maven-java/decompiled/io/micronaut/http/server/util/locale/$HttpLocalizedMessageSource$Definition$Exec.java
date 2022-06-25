package io.micronaut.http.server.util.locale;

import io.micronaut.context.AbstractExecutableMethodsDefinition;
import io.micronaut.context.AbstractLocalizedMessageSource;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
final class $HttpLocalizedMessageSource$Definition$Exec extends AbstractExecutableMethodsDefinition {
   private static final AbstractExecutableMethodsDefinition.MethodReference[] $METHODS_REFERENCES = new AbstractExecutableMethodsDefinition.MethodReference[]{
      new AbstractExecutableMethodsDefinition.MethodReference(
         AbstractLocalizedMessageSource.class,
         new AnnotationMetadataHierarchy(
            $HttpLocalizedMessageSource$Definition$Reference.$ANNOTATION_METADATA,
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         "getMessage",
         Argument.of(Optional.class, "java.util.Optional", null, Argument.ofTypeVariable(String.class, "T")),
         new Argument[]{
            Argument.of(
               String.class,
               "arg0",
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
            Argument.of(Object[].class, "arg1")
         },
         false,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         AbstractLocalizedMessageSource.class,
         new AnnotationMetadataHierarchy(
            $HttpLocalizedMessageSource$Definition$Reference.$ANNOTATION_METADATA,
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         "getMessage",
         Argument.of(Optional.class, "java.util.Optional", null, Argument.ofTypeVariable(String.class, "T")),
         new Argument[]{
            Argument.of(
               String.class,
               "arg0",
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
            Argument.of(Map.class, "arg1", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(Object.class, "V"))
         },
         false,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         AbstractLocalizedMessageSource.class,
         new AnnotationMetadataHierarchy(
            $HttpLocalizedMessageSource$Definition$Reference.$ANNOTATION_METADATA,
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         "getMessage",
         Argument.of(Optional.class, "java.util.Optional", null, Argument.ofTypeVariable(String.class, "T")),
         new Argument[]{
            Argument.of(
               String.class,
               "arg0",
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
            )
         },
         false,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         HttpLocalizedMessageSource.class,
         $HttpLocalizedMessageSource$Definition$Reference.$ANNOTATION_METADATA,
         "setRequest",
         Argument.VOID,
         new Argument[]{Argument.of(HttpRequest.class, "request", null, Argument.ofTypeVariable(Object.class, "B"))},
         false,
         false
      )
   };

   public $HttpLocalizedMessageSource$Definition$Exec() {
      super($METHODS_REFERENCES);
   }

   @Override
   protected final Object dispatch(int var1, Object var2, Object[] var3) {
      switch(var1) {
         case 0:
            return ((AbstractLocalizedMessageSource)var2).getMessage((String)var3[0], var3[1]);
         case 1:
            return ((AbstractLocalizedMessageSource)var2).getMessage((String)var3[0], (Map<String, Object>)var3[1]);
         case 2:
            return ((AbstractLocalizedMessageSource)var2).getMessage((String)var3[0]);
         case 3:
            ((HttpLocalizedMessageSource)var2).setRequest((HttpRequest<?>)var3[0]);
            return null;
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   protected final Method getTargetMethodByIndex(int var1) {
      switch(var1) {
         case 0:
            return ReflectionUtils.getRequiredMethod(AbstractLocalizedMessageSource.class, "getMessage", String.class, Object[].class);
         case 1:
            return ReflectionUtils.getRequiredMethod(AbstractLocalizedMessageSource.class, "getMessage", String.class, Map.class);
         case 2:
            return ReflectionUtils.getRequiredMethod(AbstractLocalizedMessageSource.class, "getMessage", String.class);
         case 3:
            return ReflectionUtils.getRequiredMethod(HttpLocalizedMessageSource.class, "setRequest", HttpRequest.class);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }
}
