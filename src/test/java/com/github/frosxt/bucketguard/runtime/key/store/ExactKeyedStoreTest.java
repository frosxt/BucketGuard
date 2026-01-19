package com.github.frosxt.bucketguard.runtime.key.store;

import com.github.frosxt.bucketguard.api.spec.TokenBucketSpec;
import com.github.frosxt.bucketguard.api.time.TimeSource;
import com.github.frosxt.bucketguard.runtime.bucket.Bucket;
import com.github.frosxt.bucketguard.runtime.bucket.BucketFactory;
import com.github.frosxt.bucketguard.runtime.key.store.exact.ExactKeyedStore;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class ExactKeyedStoreTest {
    private static final TokenBucketSpec TEST_SPEC = TokenBucketSpec.builder().capacity(10).build();

    private Bucket createBucket() {
        return BucketFactory.create(TEST_SPEC);
    }

    @Test
    void exactLruEvictionOrder() {
        // maxKeys=3, no expiry
        final List<String> evicted = new ArrayList<>();
        final ExactKeyedStore<String> store = new ExactKeyedStore<>(3, 0, evicted::add, TimeSource.system());

        // Insert A, B, C
        store.getOrCreate("A", this::createBucket);
        store.getOrCreate("B", this::createBucket);
        store.getOrCreate("C", this::createBucket);

        assertEquals(3, store.size());

        // Access A to make it MRU: order is now B, C, A
        store.get("A");

        // Insert D - should evict B (oldest)
        store.getOrCreate("D", this::createBucket);

        assertEquals(3, store.size());
        assertEquals(1, evicted.size());
        assertEquals("B", evicted.getFirst());

        // Verify B is gone
        assertNull(store.get("B"));
        // Verify others are present
        assertNotNull(store.get("A"));
        assertNotNull(store.get("C"));
        assertNotNull(store.get("D"));
    }

    @Test
    void exactExpireAfterAccess() {
        final AtomicLong fakeTime = new AtomicLong(0);
        final TimeSource timeSource = fakeTime::get;
        final List<String> evicted = new ArrayList<>();

        // maxKeys=100, expire=50ns
        final ExactKeyedStore<String> store = new ExactKeyedStore<>(100, 50, evicted::add, timeSource);

        // Insert A at time 0
        store.getOrCreate("A", this::createBucket);
        assertEquals(1, store.size());

        // Advance 49ns - still valid
        fakeTime.set(49);
        assertNotNull(store.get("A"));
        assertEquals(0, evicted.size());

        // Advance 49ns more from last access (now at 98ns, but lastAccess was 49ns)
        // lastAccess = 49, now = 98, diff = 49 < 50 -> still valid
        fakeTime.set(98);
        assertNotNull(store.get("A"));

        // Advance 51ns from last access (now = 149, lastAccess = 98, diff = 51 >= 50)
        fakeTime.set(149);
        assertNull(store.get("A"));
        assertEquals(1, evicted.size());
        assertEquals("A", evicted.getFirst());
        assertEquals(0, store.size());
    }

    @Test
    void boundNeverExceeded() {
        final ExactKeyedStore<String> store = new ExactKeyedStore<>(5, 0, null, TimeSource.system());

        for (int i = 0; i < 100; i++) {
            store.getOrCreate("key-" + i, this::createBucket);
            assertTrue(store.size() <= 5, "Size exceeded max: " + store.size());
        }

        assertEquals(5, store.size());
    }

    @Test
    void removalListenerSingleFire() {
        final List<String> evicted = new ArrayList<>();
        final ExactKeyedStore<String> store = new ExactKeyedStore<>(2, 0, evicted::add, TimeSource.system());

        // Insert A, B, C (C triggers eviction of A)
        store.getOrCreate("A", this::createBucket);
        store.getOrCreate("B", this::createBucket);
        store.getOrCreate("C", this::createBucket);

        assertEquals(1, evicted.size());
        assertEquals("A", evicted.getFirst());

        // Insert D (triggers eviction of B)
        store.getOrCreate("D", this::createBucket);
        assertEquals(2, evicted.size());
        assertEquals("B", evicted.get(1));

        // Manual remove of C
        store.remove("C");
        assertEquals(3, evicted.size());
        assertEquals("C", evicted.get(2));

        // Clear should fire for D
        store.clear();
        assertEquals(4, evicted.size());
        assertEquals("D", evicted.get(3));
    }

    @Test
    void pruneRemovesExpiredEntries() {
        final AtomicLong fakeTime = new AtomicLong(0);
        final TimeSource timeSource = fakeTime::get;
        final List<String> evicted = new ArrayList<>();

        final ExactKeyedStore<String> store = new ExactKeyedStore<>(100, 50, evicted::add, timeSource);

        store.getOrCreate("A", this::createBucket);
        store.getOrCreate("B", this::createBucket);

        fakeTime.set(25);
        store.getOrCreate("C", this::createBucket);

        assertEquals(3, store.size());
        assertEquals(0, evicted.size());

        // Advance past expiry for A and B but not C
        fakeTime.set(60);
        store.prune();

        assertEquals(1, store.size());
        assertEquals(2, evicted.size());
        assertTrue(evicted.contains("A"));
        assertTrue(evicted.contains("B"));
        assertNotNull(store.get("C"));
    }

    @Test
    void snapshotReturnsBoundedResults() {
        final ExactKeyedStore<String> store = new ExactKeyedStore<>(100, 0, null, TimeSource.system());

        for (int i = 0; i < 50; i++) {
            store.getOrCreate("key-" + i, this::createBucket);
        }

        assertEquals(50, store.size());

        var snapshot = store.snapshot(10);
        assertEquals(10, snapshot.size());

        snapshot = store.snapshot(100);
        assertEquals(50, snapshot.size());
    }
}
