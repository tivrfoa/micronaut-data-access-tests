package io.micronaut.discovery.cloud.digitalocean;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.discovery.cloud.ComputeInstanceMetadata;
import io.micronaut.discovery.cloud.ComputeInstanceMetadataResolver;
import io.micronaut.discovery.cloud.ComputeInstanceMetadataResolverUtils;
import io.micronaut.discovery.cloud.NetworkInterface;
import io.micronaut.jackson.core.tree.JsonNodeTreeCodec;
import io.micronaut.jackson.databind.JacksonDatabindMapper;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.JsonStreamConfig;
import io.micronaut.json.tree.JsonNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Requires(
   env = {"digitalocean"}
)
public class DigitalOceanMetadataResolver implements ComputeInstanceMetadataResolver {
   private static final Logger LOG = LoggerFactory.getLogger(DigitalOceanMetadataResolver.class);
   private static final int READ_TIMEOUT_IN_MILLS = 5000;
   private static final int CONNECTION_TIMEOUT_IN_MILLS = 5000;
   private final DigitalOceanMetadataConfiguration configuration;
   private final JsonFactory jsonFactory;
   private final JsonStreamConfig jsonStreamConfig;
   private DigitalOceanInstanceMetadata cachedMetadata;

   @Inject
   public DigitalOceanMetadataResolver(DigitalOceanMetadataConfiguration configuration, JsonFactory jsonFactory, JsonMapper mapper) {
      this.configuration = configuration;
      this.jsonFactory = jsonFactory;
      this.jsonStreamConfig = mapper.getStreamConfig();
   }

   public DigitalOceanMetadataResolver() {
      this.configuration = new DigitalOceanMetadataConfiguration();
      this.jsonFactory = new JsonFactory();
      this.jsonStreamConfig = JsonStreamConfig.DEFAULT;
   }

   public DigitalOceanMetadataResolver(ObjectMapper objectMapper, DigitalOceanMetadataConfiguration configuration) {
      this(configuration, objectMapper.getFactory(), new JacksonDatabindMapper(objectMapper));
   }

   @Override
   public Optional<ComputeInstanceMetadata> resolve(Environment environment) {
      if (!this.configuration.isEnabled()) {
         return Optional.empty();
      } else if (this.cachedMetadata != null) {
         this.cachedMetadata.setCached(true);
         return Optional.of(this.cachedMetadata);
      } else {
         DigitalOceanInstanceMetadata instanceMetadata = new DigitalOceanInstanceMetadata();

         try {
            String metadataUrl = this.configuration.getUrl();
            JsonNode metadataJson = ComputeInstanceMetadataResolverUtils.readMetadataUrl(
               new URL(metadataUrl), 5000, 5000, JsonNodeTreeCodec.getInstance().withConfig(this.jsonStreamConfig), this.jsonFactory, new HashMap()
            );
            if (metadataJson != null) {
               instanceMetadata.setInstanceId(this.textValue(metadataJson, DigitalOceanMetadataKeys.DROPLET_ID));
               instanceMetadata.setName(this.textValue(metadataJson, DigitalOceanMetadataKeys.HOSTNAME));
               instanceMetadata.setVendorData(this.textValue(metadataJson, DigitalOceanMetadataKeys.VENDOR_DATA));
               instanceMetadata.setUserData(this.textValue(metadataJson, DigitalOceanMetadataKeys.USER_DATA));
               instanceMetadata.setRegion(this.textValue(metadataJson, DigitalOceanMetadataKeys.REGION));
               JsonNode networkInterfaces = metadataJson.get(DigitalOceanMetadataKeys.INTERFACES.getName());
               List<NetworkInterface> privateInterfaces = this.processJsonInterfaces(
                  networkInterfaces.get(DigitalOceanMetadataKeys.PRIVATE_INTERFACES.getName()),
                  instanceMetadata::setPrivateIpV4,
                  instanceMetadata::setPrivateIpV6
               );
               List<NetworkInterface> publicInterfaces = this.processJsonInterfaces(
                  networkInterfaces.get(DigitalOceanMetadataKeys.PUBLIC_INTERFACES.getName()), instanceMetadata::setPublicIpV4, instanceMetadata::setPublicIpV6
               );
               List<NetworkInterface> allInterfaces = new ArrayList();
               allInterfaces.addAll(publicInterfaces);
               allInterfaces.addAll(privateInterfaces);
               instanceMetadata.setInterfaces(allInterfaces);
               ComputeInstanceMetadataResolverUtils.populateMetadata(instanceMetadata, metadataJson);
               this.cachedMetadata = instanceMetadata;
               return Optional.of(instanceMetadata);
            }
         } catch (MalformedURLException var9) {
            if (LOG.isErrorEnabled()) {
               LOG.error("Digital Ocean metadataUrl value is invalid!: " + this.configuration.getUrl(), var9);
            }
         } catch (IOException var10) {
            if (LOG.isErrorEnabled()) {
               LOG.error("Error connecting to" + this.configuration.getUrl() + "reading instance metadata", var10);
            }
         }

         return Optional.empty();
      }
   }

   private List<NetworkInterface> processJsonInterfaces(JsonNode interfaces, Consumer<String> ipv4Setter, Consumer<String> ipv6Setter) {
      List<NetworkInterface> networkInterfaces = new ArrayList();
      if (interfaces != null) {
         AtomicReference<Integer> networkCounter = new AtomicReference(0);
         interfaces.values().forEach(jsonNode -> {
            DigitalOceanNetworkInterface networkInterface = new DigitalOceanNetworkInterface();
            networkInterface.setId(networkCounter.toString());
            JsonNode ipv4 = jsonNode.get(DigitalOceanMetadataKeys.IPV4.getName());
            if (ipv4 != null) {
               networkInterface.setIpv4(this.textValue(ipv4, DigitalOceanMetadataKeys.IP_ADDRESS));
               networkInterface.setNetmask(this.textValue(ipv4, DigitalOceanMetadataKeys.NETMASK));
               networkInterface.setGateway(this.textValue(ipv4, DigitalOceanMetadataKeys.GATEWAY));
            }

            JsonNode ipv6 = jsonNode.get(DigitalOceanMetadataKeys.IPV6.getName());
            if (ipv6 != null) {
               networkInterface.setIpv6(this.textValue(ipv6, DigitalOceanMetadataKeys.IP_ADDRESS));
               networkInterface.setIpv6Gateway(this.textValue(ipv6, DigitalOceanMetadataKeys.GATEWAY));
               networkInterface.setCidr(ipv6.get(DigitalOceanMetadataKeys.CIDR.getName()).getIntValue());
            }

            networkInterface.setMac(this.textValue(jsonNode, DigitalOceanMetadataKeys.MAC));
            networkCounter.getAndSet(networkCounter.get() + 1);
            networkInterfaces.add(networkInterface);
         });
         JsonNode firstIpv4 = interfaces.get(0).get(DigitalOceanMetadataKeys.IPV4.getName());
         ipv4Setter.accept(this.textValue(firstIpv4, DigitalOceanMetadataKeys.IP_ADDRESS));
         JsonNode firstIpv6 = interfaces.get(0).get(DigitalOceanMetadataKeys.IPV6.getName());
         if (firstIpv6 != null) {
            ipv6Setter.accept(this.textValue(firstIpv6, DigitalOceanMetadataKeys.IP_ADDRESS));
         }
      }

      return networkInterfaces;
   }

   private String textValue(JsonNode node, DigitalOceanMetadataKeys key) {
      JsonNode value = node.get(key.getName());
      return value != null ? value.coerceStringValue() : null;
   }
}
