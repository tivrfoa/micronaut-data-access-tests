package io.micronaut.validation.validator.constraints;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;
import javax.validation.constraints.Pattern;

// $FF: synthetic class
@Generated
class $PatternValidator$Definition extends AbstractInitializableBeanDefinition<PatternValidator> implements BeanFactory<PatternValidator> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      PatternValidator.class, "<init>", null, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.validation.validator.constraints.AbstractPatternValidator",
      new Argument[]{Argument.of(Pattern.class, "A")},
      "io.micronaut.validation.validator.constraints.ConstraintValidator",
      new Argument[]{Argument.of(Pattern.class, "A"), Argument.of(CharSequence.class, "T")},
      "javax.validation.ConstraintValidator",
      new Argument[]{Argument.of(Pattern.class, "A"), Argument.of(CharSequence.class, "T")}
   );

   @Override
   public PatternValidator build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      PatternValidator var4 = new PatternValidator();
      return (PatternValidator)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      PatternValidator var4 = (PatternValidator)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $PatternValidator$Definition() {
      this(PatternValidator.class, $CONSTRUCTOR);
   }

   protected $PatternValidator$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $PatternValidator$Definition$Reference.$ANNOTATION_METADATA,
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
