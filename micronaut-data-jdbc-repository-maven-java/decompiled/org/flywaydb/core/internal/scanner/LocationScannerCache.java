package org.flywaydb.core.internal.scanner;

import java.util.HashMap;
import java.util.Map;
import org.flywaydb.core.internal.scanner.classpath.ClassPathLocationScanner;

public class LocationScannerCache {
   private final Map<String, ClassPathLocationScanner> cache = new HashMap();

   public boolean containsKey(String protocol) {
      return this.cache.containsKey(protocol);
   }

   public ClassPathLocationScanner get(String protocol) {
      return (ClassPathLocationScanner)this.cache.get(protocol);
   }

   public void put(String protocol, ClassPathLocationScanner scanner) {
      this.cache.put(protocol, scanner);
   }
}
