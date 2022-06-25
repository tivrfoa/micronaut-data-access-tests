package com.fasterxml.jackson.datatype.jdk8;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.util.VersionUtil;

public final class PackageVersion implements Versioned {
   public static final Version VERSION = VersionUtil.parseVersion("2.13.2", "com.fasterxml.jackson.datatype", "jackson-datatype-jdk8");

   @Override
   public Version version() {
      return VERSION;
   }
}
