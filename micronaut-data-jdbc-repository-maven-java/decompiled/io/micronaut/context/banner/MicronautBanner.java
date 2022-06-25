package io.micronaut.context.banner;

import io.micronaut.core.version.VersionUtils;
import java.io.PrintStream;

public class MicronautBanner implements Banner {
   private static final String[] MICRONAUT_BANNER = new String[]{
      " __  __ _                                  _   ",
      "|  \\/  (_) ___ _ __ ___  _ __   __ _ _   _| |_ ",
      "| |\\/| | |/ __| '__/ _ \\| '_ \\ / _` | | | | __|",
      "| |  | | | (__| | | (_) | | | | (_| | |_| | |_ ",
      "|_|  |_|_|\\___|_|  \\___/|_| |_|\\__,_|\\__,_|\\__|"
   };
   private static final String MICRONAUT = "  Micronaut";
   private final PrintStream out;

   public MicronautBanner(PrintStream out) {
      this.out = out;
   }

   @Override
   public void print() {
      for(String line : MICRONAUT_BANNER) {
         this.out.println(line);
      }

      String version = VersionUtils.getMicronautVersion();
      version = version != null ? " (v" + version + ")" : "";
      this.out.println("  Micronaut" + version + "\n");
   }
}
