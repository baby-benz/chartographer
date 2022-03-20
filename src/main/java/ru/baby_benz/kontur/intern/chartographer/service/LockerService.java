package ru.baby_benz.kontur.intern.chartographer.service;

public interface LockerService {
    boolean acquireExclusiveLock() throws InterruptedException;

    boolean acquireSharedLock() throws InterruptedException;

    void freeExclusiveLock();

    void freeSharedLock();
}
