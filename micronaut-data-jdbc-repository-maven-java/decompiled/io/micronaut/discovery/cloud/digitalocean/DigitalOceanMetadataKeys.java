package io.micronaut.discovery.cloud.digitalocean;

public enum DigitalOceanMetadataKeys {
   DROPLET_ID("droplet_id"),
   HOSTNAME("hostname"),
   VENDOR_DATA("vendor_data"),
   USER_DATA("user_data"),
   PUBLIC_KEYS("public_keys"),
   REGION("region"),
   INTERFACES("interfaces"),
   PRIVATE_INTERFACES("private"),
   PUBLIC_INTERFACES("public"),
   IPV4("ipv4"),
   IPV6("ipv6"),
   MAC("mac"),
   INTERFACE_TYPE("type"),
   IP_ADDRESS("ip_address"),
   NETMASK("netmask"),
   GATEWAY("gateway"),
   CIDR("cidr"),
   FLOATING_IP("floating_ip"),
   FLOATING_IP_ACTIVE("active"),
   DNS("dns"),
   NAMESERVERS("nameservers"),
   FEATURES("features");

   private final String name;

   private DigitalOceanMetadataKeys(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }
}
