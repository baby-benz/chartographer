package ru.baby_benz.kontur.intern.chartographer.service.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.ChartaNotFoundException;
import ru.baby_benz.kontur.intern.chartographer.service.LockerService;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

public class ReadWriteLockerServiceTest {
    private LockerService lockerService;

    @BeforeEach
    public void setUp() {
        lockerService = new ReadWriteLockerService();
    }

    @Test
    public void givenRandomId_whenAddLock_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> lockerService.addLock(Mockito.anyString()));
    }

    @Test
    public void givenRandomId_whenAddLockAndAcquireExclusiveLock_thenTrueReturned() throws InterruptedException {
        String id = Mockito.anyString();
        LockType lockType = LockType.EXCLUSIVE;
        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, lockType));
    }

    @Test
    public void givenRandomId_whenAddLockAndAcquireSharedLock_thenTrueReturned() throws InterruptedException {
        String id = Mockito.anyString();
        LockType lockType = LockType.SHARED;
        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, lockType));
    }

    @Test
    public void givenRandomId_whenAddLockAndAcquireInvalidLock_thenExceptionIsThrown() {
        String id = Mockito.anyString();
        lockerService.addLock(id);
        assertThrows(IllegalArgumentException.class, () -> lockerService.acquireLock(
                id,
                LockType.valueOf(LockType.class, "Invalid"))
        );
    }

    @Test
    public void givenRandomId_whenAddAndAcquireExclusiveLock_thenExceptionIsThrown() {
        assertThrows(ChartaNotFoundException.class, () -> lockerService.acquireLock(Mockito.anyString(), Mockito.any()));
    }

    @Test
    public void givenRandomId_whenAddAndAcquireExclusiveLockInSeparateThread_thenFalseReturned() throws InterruptedException, ExecutionException {
        String id = Mockito.anyString();
        LockType lockType = LockType.EXCLUSIVE;

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, lockType));

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(new AcquireLockTask(id, lockType));
        assertFalse(future.get());
    }

    @Test
    public void givenRandomId_whenAddAndAcquireSharedLockInSeparateThread_thenTrueReturned() throws InterruptedException, ExecutionException {
        String id = Mockito.anyString();
        LockType lockType = LockType.SHARED;

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, lockType));

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(new AcquireLockTask(id, lockType));
        assertTrue(future.get());
    }

    @RequiredArgsConstructor
    private class AcquireLockTask implements Callable<Boolean> {
        private final String id;
        private final LockType lockType;

        @Override
        public Boolean call() throws InterruptedException {
            return lockerService.acquireLock(id, lockType);
        }
    }
}