package ru.baby_benz.kontur.intern.chartographer.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.ChartaNotFoundException;
import ru.baby_benz.kontur.intern.chartographer.service.LockerService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class ReadWriteLockerService implements LockerService {
    private final ConcurrentMap<String, ReadWriteLock> lockMap = new ConcurrentHashMap<>();
    @Value("${service.image.lock.trying-time}")
    private long tryingTime;

    @Override
    public void addLock(String id) {
        lockMap.put(id, new ReentrantReadWriteLock());
    }

    @Override
    public void addAndAcquireLock(String id, LockType lockType) {
        lockMap.computeIfAbsent(id, v -> {
            ReadWriteLock lock = new ReentrantReadWriteLock();
            if (lockType.equals(LockType.SHARED)) {
                lock.readLock().lock();
            } else if (lockType.equals(LockType.EXCLUSIVE)) {
                lock.writeLock().lock();
            } else {
                throw new IllegalArgumentException();
            }
            return lock;
        });
    }

    @Override
    public boolean acquireLock(String id, LockType lockType) throws InterruptedException {
        ReadWriteLock lock = lockMap.get(id);
        if (lock != null) {
            if (lockType.equals(LockType.SHARED)) {
                return lock.readLock().tryLock(tryingTime, TimeUnit.SECONDS);
            } else if (lockType.equals(LockType.EXCLUSIVE)) {
                return lock.writeLock().tryLock(tryingTime, TimeUnit.SECONDS);
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
