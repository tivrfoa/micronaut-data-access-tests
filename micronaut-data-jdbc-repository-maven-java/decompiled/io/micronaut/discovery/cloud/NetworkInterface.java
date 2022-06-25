package io.micronaut.discovery.cloud;

import java.io.Serializable;

public class NetworkInterface implements Serializable {
   private String ipv4;
   private String ipv6;
   private String name;
   private String mac;
   private String id;
   private String gateway;
   private String network;
   private String netmask;

   public String getIpv4() {
      return this.ipv4;
   }

   public String getIpv6() {
      return this.ipv6;
   }

   public String getName() {
      return this.name;
   }

   public String getMac() {
      return this.mac;
   }

   public String getId() {
      return this.id;
   }

   public String getGateway() {
      return this.gateway;
   }

   public String getNetwork() {
      return this.network;
   }

   public String getNetmask() {
      return this.netmask;
   }

   protected void setIpv4(String ipv4) {
      this.ipv4 = ipv4;
   }

   protected void setIpv6(String ipv6) {
      this.ipv6 = ipv6;
   }

   protected void setName(String name) {
      this.name = name;
   }

   protected void setMac(String mac) {
      this.mac = mac;
   }

   protected void setId(String id) {
      this.id = id;
   }

   protected void setGateway(String gateway) {
      this.gateway = gateway;
   }

   protected void setNetwork(String network) {
      this.network = network;
   }

   protected void setNetmask(String netmask) {
      this.netmask = netmask;
   }
}
