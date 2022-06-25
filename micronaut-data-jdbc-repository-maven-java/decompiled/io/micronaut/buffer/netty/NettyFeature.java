package io.micronaut.buffer.netty;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.jdk.SystemPropertiesSupport;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.graal.AutomaticFeatureUtils;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.graalvm.nativeimage.hosted.Feature.BeforeAnalysisAccess;

@Internal
@AutomaticFeature
final class NettyFeature implements Feature {
   public void beforeAnalysis(BeforeAnalysisAccess access) {
      RuntimeClassInitialization.initializeAtRunTime(new String[]{"io.netty"});
      RuntimeClassInitialization.initializeAtBuildTime(
         new String[]{
            "io.netty.util.internal.shaded.org.jctools",
            "io.netty.util.internal.logging.InternalLoggerFactory",
            "io.netty.util.internal.logging.Slf4JLoggerFactory",
            "io.netty.util.internal.logging.LocationAwareSlf4JLogger"
         }
      );

      try {
         InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
      } catch (Throwable var8) {
      }

      this.registerClasses(access, "io.netty.channel.kqueue.KQueueChannelOption", "io.netty.channel.epoll.EpollChannelOption");
      this.registerMethods(access, "io.netty.buffer.AbstractByteBufAllocator", "toLeakAwareBuffer");
      this.registerMethods(access, "io.netty.buffer.AdvancedLeakAwareByteBuf", "touch", "recordLeakNonRefCountingOperation");
      this.registerMethods(access, "io.netty.util.ReferenceCountUtil", "touch");
      System.setProperty("io.netty.tryReflectionSetAccessible", "true");
      ((SystemPropertiesSupport)ImageSingletons.lookup(SystemPropertiesSupport.class)).initializeProperty("io.netty.tryReflectionSetAccessible", "true");

      try {
         RuntimeReflection.register(new Executable[]{access.findClassByName("java.nio.DirectByteBuffer").getDeclaredConstructor(Long.TYPE, Integer.TYPE)});
      } catch (NoSuchMethodException var7) {
         throw new RuntimeException(var7);
      }

      Class<?> unsafeOld = access.findClassByName("sun.misc.Unsafe");
      if (unsafeOld != null) {
         try {
            RuntimeReflection.register(new Executable[]{unsafeOld.getDeclaredMethod("allocateUninitializedArray", Class.class, Integer.TYPE)});
         } catch (NoSuchMethodException var6) {
         }
      }

      Class<?> unsafeNew = access.findClassByName("jdk.internal.misc.Unsafe");
      if (unsafeNew != null) {
         try {
            RuntimeReflection.register(new Executable[]{unsafeNew.getDeclaredMethod("allocateUninitializedArray", Class.class, Integer.TYPE)});
         } catch (NoSuchMethodException var5) {
         }
      }

   }

   private void registerClasses(BeforeAnalysisAccess access, String... classes) {
      for(String clazz : classes) {
         AutomaticFeatureUtils.registerClassForRuntimeReflection(access, clazz);
         AutomaticFeatureUtils.registerFieldsForRuntimeReflection(access, clazz);
      }

   }

   private void registerMethods(BeforeAnalysisAccess access, String clz, String... methods) {
      for(Method declaredMethod : access.findClassByName(clz).getDeclaredMethods()) {
         if (Arrays.asList(methods).contains(declaredMethod.getName())) {
            RuntimeReflection.register(new Executable[]{declaredMethod});
         }
      }

   }
}
