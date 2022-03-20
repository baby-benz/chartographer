package ru.baby_benz.kontur.intern.chartographer.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.baby_benz.kontur.intern.chartographer.service.LockerService;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class ReadWriteLockerService implements LockerService {
    @Value("${service.image.lock.trying-time}")
    private long tryingTime;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public boolean acquireExclusiveLock() throws InterruptedException {
        return lock.writeLock().tryLock(tryingTime, TimeUnit.SECONDS);
    }

    @Override
    public boolean acquireSharedLock() throws InterruptedException {
        return lock.readLock().tryLock(tryingTime, TimeUnit.SECONDS);
    }

    @Override
    public void freeExclusiveLock() {
        lock.writeLock().unlock();
    }

    @Override
    public void freeSharedLock() {
        lock.readLock().unlock();
    }
}
