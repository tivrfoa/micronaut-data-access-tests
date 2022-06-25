package io.micronaut.core.graal;

import io.micronaut.core.annotation.Internal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Internal
final class StaticServiceDefinitions {
   final Map<String, Set<String>> serviceTypeMap = new HashMap();
}
