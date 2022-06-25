package io.micronaut.buffer.netty;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultByteBufAllocatorConfiguration$Definition
   extends AbstractInitializableBeanDefinition<DefaultByteBufAllocatorConfiguration>
   implements BeanFactory<DefaultByteBufAllocatorConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultByteBufAllocatorConfiguration.class, "<init>", null, null, false
   );

   @Override
   public DefaultByteBufAllocatorConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultByteBufAllocatorConfiguration var4 = new DefaultByteBufAllocatorConfiguration();
      return (DefaultByteBufAllocatorConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DefaultByteBufAllocatorConfiguration var4 = (DefaultByteBufAllocatorConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "netty.default.allocator.num-heap-arenas")) {
            var4.setNumHeapArenas(
               (Integer)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setNumHeapArenas",
                  Argument.of(
                     Integer.class,
                     "numHeapArenas",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "netty.default.allocator.num-heap-arenas",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "netty.default.allocator.num-direct-arenas")) {
            var4.setNumDirectArenas(
               (Integer)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setNumDirectArenas",
                  Argument.of(
                     Integer.class,
                     "numDirectArenas",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "netty.default.allocator.num-direct-arenas",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "netty.default.allocator.page-size")) {
            var4.setPageSize(
               (Integer)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setPageSize",
                  Argument.of(
                     Integer.class,
                     "pageSize",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "netty.default.allocator.page-size",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "netty.default.allocator.max-order")) {
            var4.setMaxOrder(
               (Integer)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setMaxOrder",
                  Argument.of(
                     Integer.class,
                     "maxOrder",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "netty.default.allocator.max-order",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "netty.default.allocator.chunk-size")) {
            var4.setChunkSize(
               (Integer)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setChunkSize",
                  Argument.of(
                     Integer.class,
                     "chunkSize",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "netty.default.allocator.chunk-size",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "netty.default.allocator.small-cache-size")) {
            var4.setSmallCacheSize(
               (Integer)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setSmallCacheSize",
                  Argument.of(
                     Integer.class,
                     "smallCacheSize",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "netty.default.allocator.small-cache-size",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "netty.default.allocator.normal-cache-size")) {
            var4.setNormalCacheSize(
               (Integer)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setNormalCacheSize",
                  Argument.of(
                     Integer.class,
                     "normalCacheSize",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "netty.default.allocator.normal-cache-size",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "netty.default.allocator.use-cache-for-all-threads")) {
            var4.setUseCacheForAllThreads(
               (Boolean)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setUseCacheForAllThreads",
                  Argument.of(
                     Boolean.class,
                     "useCacheForAllThreads",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "netty.default.allocator.use-cache-for-all-threads",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "netty.default.allocator.max-cached-buffer-capacity")) {
            var4.setMaxCachedBufferCapacity(
               (Integer)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setMaxCachedBufferCapacity",
                  Argument.of(
                     Integer.class,
                     "maxCachedBufferCapacity",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "netty.default.allocator.max-cached-buffer-capacity",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "netty.default.allocator.cache-trim-interval")) {
            var4.setCacheTrimInterval(
               (Integer)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setCacheTrimInterval",
                  Argument.of(
                     Integer.class,
                     "cacheTrimInterval",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "netty.default.allocator.cache-trim-interval",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "netty.default.allocator.max-cached-byte-buffers-per-chunk")) {
            var4.setMaxCachedByteBuffersPerChunk(
               (Integer)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setMaxCachedByteBuffersPerChunk",
                  Argument.of(
                     Integer.class,
                     "maxCachedByteBuffersPerChunk",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "netty.default.allocator.max-cached-byte-buffers-per-chunk",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $DefaultByteBufAllocatorConfiguration$Definition() {
      this(DefaultByteBufAllocatorConfiguration.class, $CONSTRUCTOR);
   }

   protected $DefaultByteBufAllocatorConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultByteBufAllocatorConfiguration$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         false,
         true,
         false,
         true,
         false,
         false
      );
   }
}
