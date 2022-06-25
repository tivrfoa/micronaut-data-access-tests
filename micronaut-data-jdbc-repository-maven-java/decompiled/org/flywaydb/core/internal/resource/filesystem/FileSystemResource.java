package org.flywaydb.core.internal.resource.filesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.StandardOpenOption;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.util.BomStrippingReader;

public class FileSystemResource extends LoadableResource {
   private static final Log LOG = LogFactory.getLog(FileSystemResource.class);
   private final File file;
   private final String relativePath;
   private final Charset encoding;
   private final boolean detectEncoding;

   public FileSystemResource(Location location, String fileNameWithPath, Charset encoding, boolean stream) {
      this(location, fileNameWithPath, encoding, false, stream);
   }

   public FileSystemResource(Location location, String fileNameWithPath, Charset encoding, boolean detectEncoding, boolean stream) {
      this.file = new File(new File(fileNameWithPath).getPath());
      this.relativePath = location == null ? this.file.getPath() : location.getPathRelativeToThis(this.file.getPath()).replace("\\", "/");
      this.encoding = encoding;
      this.detectEncoding = detectEncoding;
   }

   @Override
   public String getAbsolutePath() {
      return this.file.getPath();
   }

   @Override
   public String getAbsolutePathOnDisk() {
      return this.file.getAbsolutePath();
   }

   @Override
   public Reader read() {
      Charset charSet = this.encoding;

      try {
         return Channels.newReader(FileChannel.open(this.file.toPath(), StandardOpenOption.READ), charSet.newDecoder(), 4096);
      } catch (IOException var4) {
         LOG.debug(
            "Unable to load filesystem resource"
               + this.file.getPath()
               + " using FileChannel.open. Falling back to FileInputStream implementation. Exception message: "
               + var4.getMessage()
         );

         try {
            return new BufferedReader(new BomStrippingReader(new InputStreamReader(new FileInputStream(this.file), charSet)));
         } catch (IOException var3) {
            throw new FlywayException("Unable to load filesystem resource: " + this.file.getPath() + " (encoding: " + charSet + ")", var3);
         }
      }
   }

   @Override
   public String getFilename() {
      return this.file.getName();
   }

   @Override
   public String getRelativePath() {
      return this.relativePath;
   }
}
