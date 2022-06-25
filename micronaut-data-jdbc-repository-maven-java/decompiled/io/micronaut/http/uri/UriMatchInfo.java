package io.micronaut.http.uri;

import java.util.List;
import java.util.Map;

public interface UriMatchInfo {
   String getUri();

   Map<String, Object> getVariableValues();

   List<UriMatchVariable> getVariables();

   Map<String, UriMatchVariable> getVariableMap();
}
