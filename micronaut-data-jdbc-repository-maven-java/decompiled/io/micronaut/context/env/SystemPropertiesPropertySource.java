package io.micronaut.context.env;

public class SystemPropertiesPropertySource extends MapPropertySource {
   public static final String NAME = "system";
   public static final int POSITION = -100;

   public SystemPropertiesPropertySource() {
      super("system", System.getProperties());
   }

   @Override
   public int getOrder() {
      return -100;
   }
}
