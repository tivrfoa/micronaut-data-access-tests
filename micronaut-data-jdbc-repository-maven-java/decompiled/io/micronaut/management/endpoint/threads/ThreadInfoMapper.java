package io.micronaut.management.endpoint.threads;

import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.management.endpoint.threads.impl.DefaultThreadInfoMapper;
import java.lang.management.ThreadInfo;
import org.reactivestreams.Publisher;

@DefaultImplementation(DefaultThreadInfoMapper.class)
public interface ThreadInfoMapper<T> {
   Publisher<T> mapThreadInfo(Publisher<ThreadInfo> threads);
}
