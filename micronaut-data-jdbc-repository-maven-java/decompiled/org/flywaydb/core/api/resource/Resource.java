package org.flywaydb.core.api.resource;

public interface Resource {
   String getAbsolutePath();

   String getAbsolutePathOnDisk();

   String getFilename();

   String getRelativePath();
}
