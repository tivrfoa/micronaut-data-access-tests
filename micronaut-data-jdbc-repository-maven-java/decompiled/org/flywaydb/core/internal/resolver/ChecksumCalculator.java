package org.flywaydb.core.internal.resolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.util.BomFilter;
import org.flywaydb.core.internal.util.IOUtils;

public class ChecksumCalculator {
   public static int calculate(LoadableResource... loadableResources) {
      return calculateChecksumForResource(loadableResources[0]);
   }

   private static int calculateChecksumForResource(LoadableResource resource) {
      CRC32 crc32 = new CRC32();
      BufferedReader bufferedReader = null;

      try {
         bufferedReader = new BufferedReader(resource.read(), 4096);
         String line = bufferedReader.readLine();
         if (line != null) {
            line = BomFilter.FilterBomFromString(line);

            do {
               crc32.update(line.getBytes(StandardCharsets.UTF_8));
            } while((line = bufferedReader.readLine()) != null);
         }
      } catch (IOException var7) {
         throw new FlywayException("Unable to calculate checksum of " + resource.getFilename() + "\r\n" + var7.getMessage(), var7);
      } finally {
         IOUtils.close(bufferedReader);
      }

      return (int)crc32.getValue();
   }

   private ChecksumCalculator() {
   }
}
