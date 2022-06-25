package io.micronaut.discovery.cloud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractComputeInstanceMetadata implements ComputeInstanceMetadata {
   protected String region;
   protected String availabilityZone;
   private List<NetworkInterface> interfaces = Collections.emptyList();
   private Map<String, String> metadata;
   private String name;
   private String localHostname;
   private String publicHostname;
   private String description;
   private String machineType;
   private String instanceId;
   private String account;
   private String imageId;
   private String publicIpV4;
   private String publicIpV6;
   private String privateIpV4;
   private String privateIpV6;
   private boolean cached = false;
   private Map<String, String> tags = Collections.emptyMap();

   @Override
   public String getImageId() {
      return this.imageId;
   }

   @Override
   public String getAccount() {
      return this.account;
   }

   @Override
   public Map<String, String> getMetadata() {
      return this.metadata;
   }

   @Override
   public List<NetworkInterface> getInterfaces() {
      return this.interfaces;
   }

   @Override
   public Map<String, String> getTags() {
      return this.tags;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public String getRegion() {
      return this.region;
   }

   @Override
   public String getInstanceId() {
      return this.instanceId;
   }

   @Override
   public String getMachineType() {
      return this.machineType;
   }

   @Override
   public String getAvailabilityZone() {
      return this.availabilityZone;
   }

   @Override
   public String getLocalHostname() {
      return this.localHostname;
   }

   @Override
   public String getPrivateHostname() {
      return this.localHostname;
   }

   @Override
   public String getPublicIpV4() {
      return this.publicIpV4;
   }

   @Override
   public String getPublicIpV6() {
      return this.publicIpV6;
   }

   @Override
   public String getPrivateIpV4() {
      return this.privateIpV4;
   }

   @Override
   public String getPrivateIpV6() {
      return this.privateIpV6;
   }

   @Override
   public String getDescription() {
      return this.description;
   }

   @Override
   public String getPublicHostname() {
      return this.publicHostname;
   }

   @JsonIgnore
   @Override
   public boolean isCached() {
      return this.cached;
   }

   public void setInterfaces(List<NetworkInterface> interfaces) {
      this.interfaces = interfaces;
   }

   public void setMetadata(Map<String, String> metadata) {
      this.metadata = metadata;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setAvailabilityZone(String availabilityZone) {
      this.availabilityZone = availabilityZone;
   }

   public void setLocalHostname(String localHostname) {
      this.localHostname = localHostname;
   }

   public void setPublicHostname(String publicHostname) {
      this.publicHostname = publicHostname;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public void setMachineType(String machineType) {
      this.machineType = machineType;
   }

   public void setInstanceId(String instanceId) {
      this.instanceId = instanceId;
   }

   public void setRegion(String region) {
      this.region = region;
   }

   public void setAccount(String account) {
      this.account = account;
   }

   public void setImageId(String imageId) {
      this.imageId = imageId;
   }

   public void setPublicIpV4(String publicIpV4) {
      this.publicIpV4 = publicIpV4;
   }

   public void setPublicIpV6(String publicIpV6) {
      this.publicIpV6 = publicIpV6;
   }

   public void setPrivateIpV4(String privateIpV4) {
      this.privateIpV4 = privateIpV4;
   }

   public void setPrivateIpV6(String privateIpV6) {
      this.privateIpV6 = privateIpV6;
   }

   @JsonIgnore
   public void setCached(boolean cached) {
      this.cached = cached;
   }

   public void setTags(Map<String, String> tags) {
      this.tags = tags;
   }
}
