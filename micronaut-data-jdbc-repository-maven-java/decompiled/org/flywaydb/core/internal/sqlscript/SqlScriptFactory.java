package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.resource.LoadableResource;

public interface SqlScriptFactory {
   SqlScript createSqlScript(LoadableResource var1, boolean var2, ResourceProvider var3);
}
