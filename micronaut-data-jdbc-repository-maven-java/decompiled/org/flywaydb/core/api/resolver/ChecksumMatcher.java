package org.flywaydb.core.api.resolver;

interface ChecksumMatcher {
   boolean checksumMatches(Integer var1);

   boolean checksumMatchesWithoutBeingIdentical(Integer var1);
}
