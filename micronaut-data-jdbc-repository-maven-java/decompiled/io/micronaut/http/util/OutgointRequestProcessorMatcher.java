package io.micronaut.http.util;

import java.util.regex.Pattern;

public interface OutgointRequestProcessorMatcher {
   Pattern getServiceIdPattern();

   Pattern getUriPattern();
}
