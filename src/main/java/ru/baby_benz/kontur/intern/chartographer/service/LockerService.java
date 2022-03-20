package ru.baby_benz.kontur.intern.chartographer.service;

import ru.baby_benz.kontur.intern.chartographer.service.impl.LockType;

public interface LockerService {
    void createAndAcquireLock(String id, LockType lockType);

    boolean acquireLock(String id, LockType lockType) throws InterruptedException;

    void freeLock(String id, LockType lockType);

    void removeLock(String id);
}
