package org.flywaydb.core.internal.resource;

import java.io.Reader;
import java.io.StringReader;
import org.flywaydb.core.api.resource.LoadableResource;

public class StringResource extends LoadableResource {
   private final String str;

   public StringResource(String str) {
      this.str = str;
   }

   @Override
   public Reader read() {
      return new StringReader(this.str);
   }

   @Override
   public String getAbsolutePath() {
      return "";
   }

   @Override
   public String getAbsolutePathOnDisk() {
      return "";
   }

   @Override
   public String getFilename() {
      return "";
   }

   @Override
   public String getRelativePath() {
      return "";
   }
}
