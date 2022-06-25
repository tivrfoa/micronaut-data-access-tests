package org.flywaydb.core.api.output;

import java.util.LinkedList;

public class CompositeResult implements OperationResult {
   public LinkedList<OperationResult> individualResults = new LinkedList();
}
