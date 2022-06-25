package io.micronaut.context.banner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceBanner implements Banner {
   private static final Logger LOG = LoggerFactory.getLogger(ResourceBanner.class);
   private final URL resource;
   private final PrintStream out;

   public ResourceBanner(URL resource, PrintStream out) {
      this.resource = resource;
      this.out = out;
   }

   @Override
   public void print() {
      try {
         BufferedReader reader = new BufferedReader(new InputStreamReader(this.resource.openStream(), StandardCharsets.UTF_8));
         Throwable var2 = null;

         try {
            String banner = (String)reader.lines().collect(Collectors.joining("\n"));
            this.out.println(banner + "\n");
         } catch (Throwable var12) {
            var2 = var12;
            throw var12;
         } finally {
            if (reader != null) {
               if (var2 != null) {
                  try {
                     reader.close();
                  } catch (Throwable var11) {
                     var2.addSuppressed(var11);
                  }
               } else {
                  reader.close();
               }
            }

         }
      } catch (IOException var14) {
         if (LOG.isErrorEnabled()) {
            LOG.error("There was an error printing the banner.");
         }
      }

   }
}
