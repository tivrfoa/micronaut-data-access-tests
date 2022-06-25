package ch.qos.logback.core.status;

import java.io.PrintStream;

public class OnErrorConsoleStatusListener extends OnPrintStreamStatusListenerBase {
   @Override
   protected PrintStream getPrintStream() {
      return System.err;
   }
}
