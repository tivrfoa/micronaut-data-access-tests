package io.netty.handler.ssl;

import java.util.List;
import java.util.Set;

public interface CipherSuiteFilter {
   String[] filterCipherSuites(Iterable<String> var1, List<String> var2, Set<String> var3);
}
