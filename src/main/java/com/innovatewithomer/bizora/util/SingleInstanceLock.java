package com.innovatewithomer.bizora.util;

import com.innovatewithomer.bizora.config.DatabaseConfig;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class SingleInstanceLock implements AutoCloseable {

    private final FileChannel channel;
    private final FileLock lock;

    private SingleInstanceLock(FileChannel channel, FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    public static SingleInstanceLock tryAcquire() {
        return tryAcquire(DatabaseConfig.getDataDirectory().resolve("bizora.lock"));
    }

    static SingleInstanceLock tryAcquire(Path path) {
        FileChannel channel = null;
        try {
            channel = FileChannel.open(
                    path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE
            );
            FileLock lock = channel.tryLock();
            if (lock == null) {
                channel.close();
                return null;
            }
            return new SingleInstanceLock(channel, lock);
        } catch (OverlappingFileLockException exception) {
            closeQuietly(channel);
            return null;
        } catch (IOException exception) {
            closeQuietly(channel);
            throw new RuntimeException("Unable to create the Bizora application lock.", exception);
        }
    }

    @Override
    public void close() {
        try {
            if (lock.isValid()) lock.release();
        } catch (IOException exception) {
            AppLogger.warning("Application lock could not be released cleanly.", exception);
        } finally {
            closeQuietly(channel);
        }
    }

    private static void closeQuietly(FileChannel channel) {
        if (channel == null) return;
        try {
            channel.close();
        } catch (IOException ignored) {
            // Nothing else can be done while shutting down or rejecting a second instance.
        }
    }
}
