package io.micronaut.discovery.cloud;

import io.micronaut.context.env.ComputePlatform;
import java.util.List;
import java.util.Map;

public interface ComputeInstanceMetadata {
   Map<String, String> getMetadata();

   List<NetworkInterface> getInterfaces();

   ComputePlatform getComputePlatform();

   Map<String, String> getTags();

   String getName();

   String getInstanceId();

   String getMachineType();

   String getAvailabilityZone();

   String getRegion();

   String getLocalHostname();

   String getPrivateHostname();

   String getPublicHostname();

   String getPublicIpV4();

   String getPublicIpV6();

   String getPrivateIpV4();

   String getPrivateIpV6();

   String getDescription();

   String getAccount();

   String getImageId();

   boolean isCached();
}
