package org.flywaydb.core.internal.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileWriter;
import org.flywaydb.core.api.FlywayException;

public class JsonUtils {
   public static String jsonToFile(String folder, String filename, String json) {
      return jsonToFile(folder, filename, JsonParser.parseString(json).getAsJsonObject());
   }

   public static String jsonToFile(String folder, String filename, Object json) {
      Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
      String fullFilename = folder + File.separator + filename + ".json";

      try {
         FileWriter fileWriter = new FileWriter(fullFilename);

         String var6;
         try {
            gson.toJson(json, fileWriter);
            var6 = fullFilename;
         } catch (Throwable var9) {
            try {
               fileWriter.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }

            throw var9;
         }

         fileWriter.close();
         return var6;
      } catch (Exception var10) {
         throw new FlywayException("Unable to write JSON to file: " + var10.getMessage());
      }
   }

   public static Object parseJsonArray(String json) {
      return JsonParser.parseString(json).getAsJsonArray();
   }

   private JsonUtils() {
   }
}
