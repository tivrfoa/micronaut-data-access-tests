package org.flywaydb.core.internal.authentication;

import java.util.List;

public interface ExternalAuthFileReader {
   List<String> getAllContents();
}
