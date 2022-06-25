package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.util.OptionHelper;
import java.util.Map;
import java.util.Map.Entry;

public class MDCConverter extends ClassicConverter {
   private String key;
   private String defaultValue = "";

   @Override
   public void start() {
      String[] keyInfo = OptionHelper.extractDefaultReplacement(this.getFirstOption());
      this.key = keyInfo[0];
      if (keyInfo[1] != null) {
         this.defaultValue = keyInfo[1];
      }

      super.start();
   }

   @Override
   public void stop() {
      this.key = null;
      super.stop();
   }

   public String convert(ILoggingEvent event) {
      Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
      if (mdcPropertyMap == null) {
         return this.defaultValue;
      } else if (this.key == null) {
         return this.outputMDCForAllKeys(mdcPropertyMap);
      } else {
         String value = (String)mdcPropertyMap.get(this.key);
         return value != null ? value : this.defaultValue;
      }
   }

   private String outputMDCForAllKeys(Map<String, String> mdcPropertyMap) {
      StringBuilder buf = new StringBuilder();
      boolean first = true;

      for(Entry<String, String> entry : mdcPropertyMap.entrySet()) {
         if (first) {
            first = false;
         } else {
            buf.append(", ");
         }

         buf.append((String)entry.getKey()).append('=').append((String)entry.getValue());
      }

      return buf.toString();
   }

   public String getKey() {
      return this.key;
   }
}
