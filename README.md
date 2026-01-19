# BucketGuard
BucketGuard is a high-performance, dependency-free Java library for token bucket rate limiting.

Most rate limiting libraries are either too heavy (bringing in large transitive dependencies) or lack advanced features like keyed limits and burst control. BucketGuard is designed for efficiency: it provides a robust, lock-free implementation of the Generic Cell Rate Algorithm (GCRA) with zero allocations on the hot path. It is suitable for protecting high-throughput APIs and backend services.

## Features
*   **Zero Dependencies**: The library depends only on the Java 21 standard library. You don't need to worry about classpath conflicts.
*   **High Performance**: Uses `AtomicLong` and `VarHandle` for lock-free state management. The hot path involves zero object allocations.
*   **Keyed Limiting**: Efficiently manages millions of independent buckets. Supports eviction policies like LRU and Expire-After-Access to keep memory usage bounded.
*   **Burst Control**: Configurable burst allowance. You can allow requests to stack up to capacity, or strictly space them out by disabling bursts.
*   **Contention Strategies**: Choose between `ATOMIC` (CAS-based) for general use or `STRIPED` (partitioned) for extremely high concurrency scenarios to minimize contention.
*   **Strict Math**: Optional overflow safety ensures correct behavior even with large token counts or long-running up-times.

## Installation
This library is available via JitPack.

### Gradle (Groovy)
```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.frosxt:BucketGuard:v1.0.0'
}
```

### Gradle (Kotlin)
```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.frosxt:BucketGuard:v1.0.0")
}
```

### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.frosxt</groupId>
    <artifactId>BucketGuard</artifactId>
    <version>v1.0.0</version>
</dependency>
```

## Requirements
*   Java 21 or newer
*   Gradle or Maven


## Documentation
Documentation is available on the [Wiki](https://github.com/frosxt/BucketGuard/wiki)

## License
MIT License. See LICENSE.
