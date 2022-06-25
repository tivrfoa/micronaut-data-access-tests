package org.flywaydb.core.internal.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.api.resource.Resource;
import org.flywaydb.core.internal.util.StringUtils;

public class ResourceNameValidator {
   private static final Log LOG = LogFactory.getLog(ResourceNameValidator.class);

   public void validateSQLMigrationNaming(ResourceProvider provider, Configuration configuration) {
      List<String> errorsFound = new ArrayList();
      ResourceNameParser resourceNameParser = new ResourceNameParser(configuration);

      for(Resource resource : this.getAllSqlResources(provider, configuration)) {
         String filename = resource.getFilename();
         LOG.debug("Validating " + filename);
         if (!this.isSpecialResourceFile(configuration, filename)) {
            ResourceName result = resourceNameParser.parse(filename);
            if (!result.isValid()) {
               errorsFound.add(result.getValidityMessage());
            }
         }
      }

      if (!errorsFound.isEmpty()) {
         if (configuration.isValidateMigrationNaming()) {
            throw new FlywayException("Invalid SQL filenames found:\r\n" + StringUtils.collectionToDelimitedString(errorsFound, "\r\n"));
         }

         LOG.info(errorsFound.size() + " SQL migrations were detected but not run because they did not follow the filename convention.");
         LOG.info("If this is in error, enable debug logging or 'validateMigrationNaming' to fail fast and see a list of the invalid file names.");
      }

   }

   private Collection<LoadableResource> getAllSqlResources(ResourceProvider provider, Configuration configuration) {
      return provider.getResources("", configuration.getSqlMigrationSuffixes());
   }

   private boolean isSpecialResourceFile(Configuration configuration, String filename) {
      return false;
   }
}
