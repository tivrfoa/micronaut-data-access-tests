package io.micronaut.health;

import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.server.EmbeddedServer;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Requirements({@Requires(
   notEnv = {"android", "function"}
), @Requires(
   property = "micronaut.application.name"
), @Requires(
   condition = HeartbeatDiscoveryClientCondition.class
), @Requires(
   beans = {EmbeddedServer.class}
)})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD})
public @interface HeartbeatEnabled {
}
