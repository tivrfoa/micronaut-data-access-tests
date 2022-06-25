package io.micronaut.context.annotation;

import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.TrueCondition;
import io.micronaut.core.annotation.InstantiatedMember;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD})
@Repeatable(Requirements.class)
public @interface Requires {
   String[] env() default {};

   String[] notEnv() default {};

   String property() default "";

   String notEquals() default "";

   @InstantiatedMember
   Class<? extends Condition> condition() default TrueCondition.class;

   Requires.Sdk sdk() default Requires.Sdk.MICRONAUT;

   String configuration() default "";

   String value() default "";

   String defaultValue() default "";

   String pattern() default "";

   String version() default "";

   Class[] classes() default {};

   Class<? extends Annotation>[] entities() default {};

   Class[] beans() default {};

   Class[] missing() default {};

   @AliasFor(
      member = "missing"
   )
   String[] missingClasses() default {};

   Class[] missingBeans() default {};

   String[] missingConfigurations() default {};

   String missingProperty() default "";

   String[] resources() default {};

   Requires.Family[] os() default {};

   Requires.Family[] notOs() default {};

   Class bean() default void.class;

   String beanProperty() default "";

   public static enum Family {
      LINUX,
      MAC_OS,
      WINDOWS,
      SOLARIS,
      OTHER;
   }

   public static enum Sdk {
      JAVA,
      GROOVY,
      KOTLIN,
      MICRONAUT;
   }
}
