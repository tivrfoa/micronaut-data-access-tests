package io.micronaut.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
@InterceptorBinding(
   kind = InterceptorKind.AROUND
)
public @interface Around {
   boolean proxyTarget() default false;

   boolean hotswap() default false;

   boolean lazy() default false;

   boolean cacheableLazyTarget() default false;

   Around.ProxyTargetConstructorMode proxyTargetMode() default Around.ProxyTargetConstructorMode.ERROR;

   public static enum ProxyTargetConstructorMode {
      ERROR,
      WARN,
      ALLOW;
   }
}
