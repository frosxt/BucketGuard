package com.github.frosxt.bucketguard.runtime.bucket.atomic;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * Handles CAS operations on the AtomicBucket state.
 */
public final class AtomicStateCodec {
    private static final VarHandle TAT;

    static {
        try {
            TAT = MethodHandles.lookup().findVarHandle(AtomicStateCodec.class, "tat", long.class);
        } catch (final ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings("unused")
    private volatile long tat;

    public AtomicStateCodec() {
        this.tat = 0;
    }

    public long getTat() {
        return (long) TAT.get(this);
    }

    public boolean compareAndSetTat(final long expected, final long flow) {
        return TAT.compareAndSet(this, expected, flow);
    }
}
