package io.micronaut.http.netty.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;

@TargetClass(InternalLoggerFactory.class)
final class Target_io_netty_util_internal_logging_InternalLoggerFactory {
   @Substitute
   private static InternalLoggerFactory newDefaultFactory(String name) {
      return JdkLoggerFactory.INSTANCE;
   }
}
