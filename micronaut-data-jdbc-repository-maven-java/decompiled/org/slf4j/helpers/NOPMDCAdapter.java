package org.slf4j.helpers;

import java.util.Map;
import org.slf4j.spi.MDCAdapter;

public class NOPMDCAdapter implements MDCAdapter {
   @Override
   public void clear() {
   }

   @Override
   public String get(String key) {
      return null;
   }

   @Override
   public void put(String key, String val) {
   }

   @Override
   public void remove(String key) {
   }

   @Override
   public Map<String, String> getCopyOfContextMap() {
      return null;
   }

   @Override
   public void setContextMap(Map<String, String> contextMap) {
   }
}
