package com.innovatewithomer.bizora.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SingleInstanceLockTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldRejectASecondInstanceAndAllowOneAfterRelease() {
        Path lockFile = temporaryDirectory.resolve("bizora.lock");
        try (SingleInstanceLock first = SingleInstanceLock.tryAcquire(lockFile)) {
            assertNotNull(first);
            assertNull(SingleInstanceLock.tryAcquire(lockFile));
        }

        try (SingleInstanceLock next = SingleInstanceLock.tryAcquire(lockFile)) {
            assertNotNull(next);
        }
    }
}
