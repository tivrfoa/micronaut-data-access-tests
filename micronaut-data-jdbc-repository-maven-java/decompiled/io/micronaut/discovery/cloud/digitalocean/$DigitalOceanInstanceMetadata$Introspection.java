package io.micronaut.discovery.cloud.digitalocean;

import io.micronaut.context.env.ComputePlatform;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.discovery.cloud.NetworkInterface;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// $FF: synthetic class
@Generated
final class $DigitalOceanInstanceMetadata$Introspection extends AbstractInitializableBeanIntrospection {
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(ComputePlatform.class, "computePlatform"), 0, -1, 1, true, false),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "userData"), 2, 3, -1, false, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "vendorData"), 4, 5, -1, false, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "imageId"), 6, 7, -1, false, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "account"), 8, 9, -1, false, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(Map.class, "metadata", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(String.class, "V")),
         10,
         11,
         -1,
         false,
         true
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(List.class, "interfaces", null, Argument.ofTypeVariable(NetworkInterface.class, "E")), 12, 13, -1, false, true
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(Map.class, "tags", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(String.class, "V")), 14, 15, -1, false, true
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "name"), 16, 17, -1, false, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "region"), 18, 19, -1, false, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "instanceId"), 20, 21, -1, false, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "machineType"), 22, 23, -1, false, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "availabilityZone"), 24, 25, -1, false, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "localHostname"), 26, 27, -1, false, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "privateHostname"), 28, -1, 29, true, false),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "publicIpV4"), 30, 31, -1, false, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "publicIpV6"), 32, 33, -1, false, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "privateIpV4"), 34, 35, -1, false, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "privateIpV6"), 36, 37, -1, false, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "description"), 38, 39, -1, false, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "publicHostname"), 40, 41, -1, false, true),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            Boolean.TYPE,
            "cached",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JsonIgnore", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JacksonAnnotation", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("com.fasterxml.jackson.annotation.JsonIgnore", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "com.fasterxml.jackson.annotation.JacksonAnnotation", AnnotationUtil.internListOf("com.fasterxml.jackson.annotation.JsonIgnore")
               ),
               false,
               true
            ),
            null
         ),
         42,
         43,
         -1,
         false,
         true
      )
   };

   public $DigitalOceanInstanceMetadata$Introspection() {
      super(DigitalOceanInstanceMetadata.class, $DigitalOceanInstanceMetadata$IntrospectionRef.$ANNOTATION_METADATA, null, null, $PROPERTIES_REFERENCES, null);
   }

   @Override
   protected final Object dispatchOne(int var1, Object var2, Object var3) {
      switch(var1) {
         case 0:
            return ((DigitalOceanInstanceMetadata)var2).getComputePlatform();
         case 1:
            throw new UnsupportedOperationException(
               "Cannot mutate property [computePlatform] that is not mutable via a setter method or constructor argument for type: io.micronaut.discovery.cloud.digitalocean.DigitalOceanInstanceMetadata"
            );
         case 2:
            return ((DigitalOceanInstanceMetadata)var2).getUserData();
         case 3:
            ((DigitalOceanInstanceMetadata)var2).setUserData((String)var3);
            return null;
         case 4:
            return ((DigitalOceanInstanceMetadata)var2).getVendorData();
         case 5:
            ((DigitalOceanInstanceMetadata)var2).setVendorData((String)var3);
            return null;
         case 6:
            return ((DigitalOceanInstanceMetadata)var2).getImageId();
         case 7:
            ((DigitalOceanInstanceMetadata)var2).setImageId((String)var3);
            return null;
         case 8:
            return ((DigitalOceanInstanceMetadata)var2).getAccount();
         case 9:
            ((DigitalOceanInstanceMetadata)var2).setAccount((String)var3);
            return null;
         case 10:
            return ((DigitalOceanInstanceMetadata)var2).getMetadata();
         case 11:
            ((DigitalOceanInstanceMetadata)var2).setMetadata((Map<String, String>)var3);
            return null;
         case 12:
            return ((DigitalOceanInstanceMetadata)var2).getInterfaces();
         case 13:
            ((DigitalOceanInstanceMetadata)var2).setInterfaces((List<NetworkInterface>)var3);
            return null;
         case 14:
            return ((DigitalOceanInstanceMetadata)var2).getTags();
         case 15:
            ((DigitalOceanInstanceMetadata)var2).setTags((Map<String, String>)var3);
            return null;
         case 16:
            return ((DigitalOceanInstanceMetadata)var2).getName();
         case 17:
            ((DigitalOceanInstanceMetadata)var2).setName((String)var3);
            return null;
         case 18:
            return ((DigitalOceanInstanceMetadata)var2).getRegion();
         case 19:
            ((DigitalOceanInstanceMetadata)var2).setRegion((String)var3);
            return null;
         case 20:
            return ((DigitalOceanInstanceMetadata)var2).getInstanceId();
         case 21:
            ((DigitalOceanInstanceMetadata)var2).setInstanceId((String)var3);
            return null;
         case 22:
            return ((DigitalOceanInstanceMetadata)var2).getMachineType();
         case 23:
            ((DigitalOceanInstanceMetadata)var2).setMachineType((String)var3);
            return null;
         case 24:
            return ((DigitalOceanInstanceMetadata)var2).getAvailabilityZone();
         case 25:
            ((DigitalOceanInstanceMetadata)var2).setAvailabilityZone((String)var3);
            return null;
         case 26:
            return ((DigitalOceanInstanceMetadata)var2).getLocalHostname();
         case 27:
            ((DigitalOceanInstanceMetadata)var2).setLocalHostname((String)var3);
            return null;
         case 28:
            return ((DigitalOceanInstanceMetadata)var2).getPrivateHostname();
         case 29:
            throw new UnsupportedOperationException(
               "Cannot mutate property [privateHostname] that is not mutable via a setter method or constructor argument for type: io.micronaut.discovery.cloud.digitalocean.DigitalOceanInstanceMetadata"
            );
         case 30:
            return ((DigitalOceanInstanceMetadata)var2).getPublicIpV4();
         case 31:
            ((DigitalOceanInstanceMetadata)var2).setPublicIpV4((String)var3);
            return null;
         case 32:
            return ((DigitalOceanInstanceMetadata)var2).getPublicIpV6();
         case 33:
            ((DigitalOceanInstanceMetadata)var2).setPublicIpV6((String)var3);
            return null;
         case 34:
            return ((DigitalOceanInstanceMetadata)var2).getPrivateIpV4();
         case 35:
            ((DigitalOceanInstanceMetadata)var2).setPrivateIpV4((String)var3);
            return null;
         case 36:
            return ((DigitalOceanInstanceMetadata)var2).getPrivateIpV6();
         case 37:
            ((DigitalOceanInstanceMetadata)var2).setPrivateIpV6((String)var3);
            return null;
         case 38:
            return ((DigitalOceanInstanceMetadata)var2).getDescription();
         case 39:
            ((DigitalOceanInstanceMetadata)var2).setDescription((String)var3);
            return null;
         case 40:
            return ((DigitalOceanInstanceMetadata)var2).getPublicHostname();
         case 41:
            ((DigitalOceanInstanceMetadata)var2).setPublicHostname((String)var3);
            return null;
         case 42:
            return ((DigitalOceanInstanceMetadata)var2).isCached();
         case 43:
            ((DigitalOceanInstanceMetadata)var2).setCached((Boolean)var3);
            return null;
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      switch(var1.hashCode()) {
         case -1737685826:
            if (var1.equals("localHostname")) {
               return 13;
            }
            break;
         case -1724546052:
            if (var1.equals("description")) {
               return 19;
            }
            break;
         case -1674934361:
            if (var1.equals("availabilityZone")) {
               return 12;
            }
            break;
         case -1598539174:
            if (var1.equals("interfaces")) {
               return 6;
            }
            break;
         case -1368047326:
            if (var1.equals("cached")) {
               return 21;
            }
            break;
         case -1177318867:
            if (var1.equals("account")) {
               return 4;
            }
            break;
         case -934795532:
            if (var1.equals("region")) {
               return 9;
            }
            break;
         case -783502634:
            if (var1.equals("privateHostname")) {
               return 14;
            }
            break;
         case -450004177:
            if (var1.equals("metadata")) {
               return 5;
            }
            break;
         case -266964459:
            if (var1.equals("userData")) {
               return 1;
            }
            break;
         case -218117087:
            if (var1.equals("machineType")) {
               return 11;
            }
            break;
         case 3373707:
            if (var1.equals("name")) {
               return 8;
            }
            break;
         case 3552281:
            if (var1.equals("tags")) {
               return 7;
            }
            break;
         case 267975100:
            if (var1.equals("publicHostname")) {
               return 20;
            }
            break;
         case 693839122:
            if (var1.equals("vendorData")) {
               return 2;
            }
            break;
         case 902024336:
            if (var1.equals("instanceId")) {
               return 10;
            }
            break;
         case 1256926666:
            if (var1.equals("computePlatform")) {
               return 0;
            }
            break;
         case 1904161806:
            if (var1.equals("publicIpV4")) {
               return 15;
            }
            break;
         case 1904161808:
            if (var1.equals("publicIpV6")) {
               return 16;
            }
            break;
         case 1911932886:
            if (var1.equals("imageId")) {
               return 3;
            }
            break;
         case 1971292712:
            if (var1.equals("privateIpV4")) {
               return 17;
            }
            break;
         case 1971292714:
            if (var1.equals("privateIpV6")) {
               return 18;
            }
      }

      return -1;
   }

   @Override
   public Object instantiate() {
      return new DigitalOceanInstanceMetadata();
   }
}
