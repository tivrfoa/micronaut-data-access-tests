package io.micronaut.scheduling.annotation;

import io.micronaut.context.annotation.Executable;
import io.micronaut.context.annotation.Parallel;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Executable(
   processOnStartup = true
)
@Repeatable(Schedules.class)
@Parallel
public @interface Scheduled {
   String cron() default "";

   String zoneId() default "";

   String fixedDelay() default "";

   String initialDelay() default "";

   String fixedRate() default "";

   String scheduler() default "scheduled";
}
