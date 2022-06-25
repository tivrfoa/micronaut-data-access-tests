package io.micronaut.http.server.types.files;

import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import java.io.File;

public class SystemFile implements FileCustomizableResponseType {
   private final File file;
   private final MediaType mediaType;
   private String attachmentName;

   public SystemFile(File file) {
      this.file = file;
      this.mediaType = MediaType.forFilename(file.getName());
   }

   public SystemFile(File file, MediaType mediaType) {
      this.file = file;
      this.mediaType = mediaType;
   }

   @Override
   public long getLastModified() {
      return this.file.lastModified();
   }

   @Override
   public long getLength() {
      return this.file.length();
   }

   @Override
   public MediaType getMediaType() {
      return this.mediaType;
   }

   public File getFile() {
      return this.file;
   }

   public SystemFile attach() {
      this.attachmentName = this.file.getName();
      return this;
   }

   public SystemFile attach(String attachmentName) {
      this.attachmentName = attachmentName;
      return this;
   }

   @Override
   public void process(MutableHttpResponse response) {
      if (this.attachmentName != null) {
         response.header("Content-Disposition", StreamedFile.buildAttachmentHeader(this.attachmentName));
      }

   }
}
