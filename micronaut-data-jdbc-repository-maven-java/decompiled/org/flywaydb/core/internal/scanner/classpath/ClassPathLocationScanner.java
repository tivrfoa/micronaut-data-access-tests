package org.flywaydb.core.internal.scanner.classpath;

import java.net.URL;
import java.util.Set;

public interface ClassPathLocationScanner {
   Set<String> findResourceNames(String var1, URL var2);
}
