package io.micronaut.runtime.server.watch.event;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.scheduling.io.watch.event.FileChangedEvent;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $FileWatchRestartListener$Definition
   extends AbstractInitializableBeanDefinition<FileWatchRestartListener>
   implements BeanFactory<FileWatchRestartListener> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      FileWatchRestartListener.class,
      "<init>",
      new Argument[]{
         Argument.of(
            EmbeddedApplication.class,
            "embeddedApplication",
            null,
            Argument.ofTypeVariable(
               EmbeddedApplication.class, "T", null, Argument.ofTypeVariable(EmbeddedApplication.class, "T", null, Argument.ZERO_ARGUMENTS)
            )
         )
      },
      new DefaultAnnotationMetadata(
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         Collections.EMPTY_MAP,
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         false,
         true
      ),
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.event.ApplicationEventListener", new Argument[]{Argument.of(FileChangedEvent.class, "E")}
   );

   @Override
   public FileWatchRestartListener build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      FileWatchRestartListener var4 = new FileWatchRestartListener((EmbeddedApplication<?>)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (FileWatchRestartListener)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      FileWatchRestartListener var4 = (FileWatchRestartListener)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $FileWatchRestartListener$Definition() {
      this(FileWatchRestartListener.class, $CONSTRUCTOR);
   }

   protected $FileWatchRestartListener$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $FileWatchRestartListener$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         $TYPE_ARGUMENTS,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         false,
         true,
         false,
         false,
         false,
         false
      );
   }
}
