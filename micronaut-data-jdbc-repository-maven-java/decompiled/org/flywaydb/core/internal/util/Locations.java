package org.flywaydb.core.internal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

public class Locations {
   private static final Log LOG = LogFactory.getLog(Locations.class);
   private final List<Location> locations = new ArrayList();

   public Locations(String... rawLocations) {
      List<Location> normalizedLocations = new ArrayList();

      for(String rawLocation : rawLocations) {
         normalizedLocations.add(new Location(rawLocation));
      }

      this.processLocations(normalizedLocations);
   }

   public Locations(List<Location> rawLocations) {
      this.processLocations(rawLocations);
   }

   public List<Location> getLocations() {
      return this.locations;
   }

   private void processLocations(List<Location> rawLocations) {
      List<Location> sortedLocations = new ArrayList(rawLocations);
      Collections.sort(sortedLocations);

      for(Location normalizedLocation : sortedLocations) {
         if (this.locations.contains(normalizedLocation)) {
            LOG.warn("Discarding duplicate location '" + normalizedLocation + "'");
         } else {
            Location parentLocation = this.getParentLocationIfExists(normalizedLocation, this.locations);
            if (parentLocation != null) {
               LOG.warn("Discarding location '" + normalizedLocation + "' as it is a sub-location of '" + parentLocation + "'");
            } else {
               this.locations.add(normalizedLocation);
            }
         }
      }

   }

   private Location getParentLocationIfExists(Location location, List<Location> finalLocations) {
      return (Location)finalLocations.stream().filter(fl -> fl.isParentOf(location)).findFirst().orElse(null);
   }
}
