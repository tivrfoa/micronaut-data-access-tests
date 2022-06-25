package org.flywaydb.core.internal.scanner;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.flywaydb.core.internal.scanner.classpath.ClassPathLocationScanner;

public class ResourceNameCache {
   private final Map<ClassPathLocationScanner, Map<URL, Set<String>>> resourceNameCache = new HashMap();

   public void put(ClassPathLocationScanner classPathLocationScanner, Map<URL, Set<String>> map) {
      this.resourceNameCache.put(classPathLocationScanner, map);
   }

   public void put(ClassPathLocationScanner classPathLocationScanner, URL resolvedUrl, Set<String> names) {
      ((Map)this.resourceNameCache.get(classPathLocationScanner)).put(resolvedUrl, names);
   }

   public Set<String> get(ClassPathLocationScanner classPathLocationScanner, URL resolvedUrl) {
      return (Set<String>)((Map)this.resourceNameCache.get(classPathLocationScanner)).get(resolvedUrl);
   }
}
