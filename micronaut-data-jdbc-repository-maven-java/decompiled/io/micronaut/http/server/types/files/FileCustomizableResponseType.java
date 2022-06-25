package io.micronaut.http.server.types.files;

import io.micronaut.http.MediaType;
import io.micronaut.http.server.types.CustomizableResponseType;

public interface FileCustomizableResponseType extends CustomizableResponseType {
   @Deprecated
   String ATTACHMENT_HEADER = "attachment; filename=\"%s\"";

   long getLastModified();

   long getLength();

   MediaType getMediaType();
}
