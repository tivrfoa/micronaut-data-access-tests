package io.micronaut.http.server.netty;

import io.micronaut.context.BeanLocator;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.discovery.EmbeddedServerInstance;
import io.micronaut.discovery.cloud.ComputeInstanceMetadata;
import io.micronaut.discovery.cloud.ComputeInstanceMetadataResolver;
import io.micronaut.discovery.metadata.ServiceInstanceMetadataContributor;
import io.micronaut.runtime.server.EmbeddedServer;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Prototype
@Internal
class NettyEmbeddedServerInstance implements EmbeddedServerInstance {
   private final String id;
   private final NettyHttpServer nettyHttpServer;
   private final Environment environment;
   private final List<ServiceInstanceMetadataContributor> metadataContributors;
   private final BeanLocator beanLocator;
   private ConvertibleValues<String> instanceMetadata;

   NettyEmbeddedServerInstance(
      @Parameter String id,
      @Parameter NettyHttpServer nettyHttpServer,
      Environment environment,
      BeanLocator beanLocator,
      List<ServiceInstanceMetadataContributor> metadataContributors
   ) {
      this.id = id;
      this.nettyHttpServer = nettyHttpServer;
      this.environment = environment;
      this.beanLocator = beanLocator;
      this.metadataContributors = metadataContributors;
   }

   @Override
   public EmbeddedServer getEmbeddedServer() {
      return this.nettyHttpServer;
   }

   @Override
   public String getId() {
      return this.id;
   }

   @Override
   public URI getURI() {
      return this.nettyHttpServer.getURI();
   }

   @Override
   public ConvertibleValues<String> getMetadata() {
      if (this.instanceMetadata == null) {
         Map<String, String> cloudMetadata = new HashMap();
         ComputeInstanceMetadata computeInstanceMetadata = (ComputeInstanceMetadata)this.beanLocator
            .findBean(ComputeInstanceMetadataResolver.class)
            .flatMap(computeInstanceMetadataResolver -> computeInstanceMetadataResolver.resolve(this.environment))
            .orElse(null);
         if (computeInstanceMetadata != null) {
            cloudMetadata = computeInstanceMetadata.getMetadata();
         }

         if (CollectionUtils.isNotEmpty(this.metadataContributors)) {
            for(ServiceInstanceMetadataContributor metadataContributor : this.metadataContributors) {
               metadataContributor.contribute(this, cloudMetadata);
            }
         }

         Map<String, String> metadata = this.nettyHttpServer.getServerConfiguration().getApplicationConfiguration().getInstance().getMetadata();
         if (cloudMetadata != null) {
            cloudMetadata.putAll(metadata);
         }

         this.instanceMetadata = ConvertibleValues.of(cloudMetadata);
      }

      return this.instanceMetadata;
   }
}
