package io.micronaut.management.endpoint.threads;

import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Read;
import java.lang.management.ManagementFactory;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Endpoint("threaddump")
public class ThreadDumpEndpoint {
   private final ThreadInfoMapper<?> threadInfoMapper;

   ThreadDumpEndpoint(ThreadInfoMapper<?> threadInfoMapper) {
      this.threadInfoMapper = threadInfoMapper;
   }

   @Read
   Publisher getThreadDump() {
      return this.threadInfoMapper.mapThreadInfo(Flux.fromArray(ManagementFactory.getThreadMXBean().dumpAllThreads(true, true)));
   }
}
