package com.github.frosxt.bucketguard.test;

import com.github.frosxt.bucketguard.api.time.TimeSource;
import java.util.concurrent.atomic.AtomicLong;

public class FakeTimeSource implements TimeSource {
    private final AtomicLong now = new AtomicLong(0);

    @Override
    public long nanoTime() {
        return now.get();
    }

    public void set(long nanos) {
        now.set(nanos);
    }

    public void advance(long nanos) {
        now.addAndGet(nanos);
    }
}
