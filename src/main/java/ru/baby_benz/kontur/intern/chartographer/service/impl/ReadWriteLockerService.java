package ru.baby_benz.kontur.intern.chartographer.service.impl;

import org.springframework.stereotype.Service;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.ChartaNotFoundException;
import ru.baby_benz.kontur.intern.chartographer.service.LockerService;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class ReadWriteLockerService implements LockerService {
    private final ConcurrentMap<String, ReadWriteLock> lockMap = new ConcurrentHashMap<>();
    private static final Duration TRYING_SECONDS_DURATION = Duration.ofSeconds(10);

    @Override
    public void addLock(String id) {
        lockMap.put(id, new ReentrantReadWriteLock());
    }

    @Override
    public boolean acquireLock(String id, LockType lockType) throws InterruptedException {
        ReadWriteLock lock = lockMap.get(id);
        if (lock != null) {
            if (lockType.equals(LockType.SHARED)) {
                return lock.readLock().tryLock(TRYING_SECONDS_DURATION.getSeconds(), TimeUnit.SECONDS);
            } else if (lockType.equals(LockType.EXCLUSIVE)) {
                return lock.writeLock().tryLock(TRYING_SECONDS_DURATION.getSeconds(), TimeUnit.SECONDS);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new ChartaNotFoundException(id);
        }
    }

    @Override
    public void freeLock(String id, LockType lockType) {
        ReadWriteLock lock = lockMap.get(id);
        if (lock != null) {
            try {
                if (lockType.equals(LockType.SHARED)) {
                    lock.readLock().unlock();
                } else if (lockType.equals(LockType.EXCLUSIVE)) {
                    lock.writeLock().unlock();
                } else {
                    throw new IllegalArgumentException();
                }
            } catch (IllegalMonitorStateException ignore) {
            }
        }
    }

    @Override
    public void removeLock(String id) {
        lockMap.remove(id);
    }
}
