package io.micronaut.management.endpoint.threads.impl;

import io.micronaut.management.endpoint.threads.ThreadInfoMapper;
import jakarta.inject.Singleton;
import java.lang.management.ThreadInfo;
import org.reactivestreams.Publisher;

@Singleton
public class DefaultThreadInfoMapper implements ThreadInfoMapper<ThreadInfo> {
   @Override
   public Publisher<ThreadInfo> mapThreadInfo(Publisher<ThreadInfo> threads) {
      return threads;
   }
}
