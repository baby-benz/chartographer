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
    public void givenMockedId_whenAddLock_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> lockerService.addLock(Mockito.anyString()));
    }

    @Test
    public void givenMockedIdAndExclusiveLockType_whenAddLockAndAcquireLock_thenTrueReturned() throws InterruptedException {
        String id = Mockito.anyString();
        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, LockType.EXCLUSIVE));
    }

    @Test
    public void givenMockedIdAndSharedLockType_whenAddLockAndAcquireLock_thenTrueReturned() throws InterruptedException {
        String id = Mockito.anyString();
        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, LockType.SHARED));
    }

    @Test
    public void givenMockedIdAndMockedLockType_whenAddLockAndAcquireLock_thenExceptionIsThrown() {
        assertThrows(ChartaNotFoundException.class, () -> lockerService.acquireLock(Mockito.anyString(), Mockito.any()));
    }

    @Test
    public void givenMockedIdAndExclusiveLockType_whenAddLockAndAcquireLockInSeparateThread_thenFalseReturned() throws InterruptedException, ExecutionException {
        String id = Mockito.anyString();
        LockType lockType = LockType.EXCLUSIVE;

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, lockType));

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(new AcquireLockTask(id, lockType));
        assertFalse(future.get());
    }

    @Test
    public void givenMockedIdAndSharedLockType_whenAddLockAndAcquireLockInSeparateThread_thenTrueReturned() throws InterruptedException, ExecutionException {
        String id = Mockito.anyString();
        LockType lockType = LockType.SHARED;

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, lockType));

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(new AcquireLockTask(id, lockType));
        assertTrue(future.get());
    }

    @Test
    public void givenMockedIdAndExclusiveLockType_whenAddLockAndAcquireSharedLockInSeparateThread_thenFalseReturned() throws InterruptedException, ExecutionException {
        String id = Mockito.anyString();

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, LockType.EXCLUSIVE));

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(new AcquireLockTask(id, LockType.SHARED));
        assertFalse(future.get());
    }

    @Test
    public void givenMockedIdAndSharedLockType_whenAddLockAndAcquireExclusiveLockInSeparateThread_thenFalseReturned() throws InterruptedException, ExecutionException {
        String id = Mockito.anyString();

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, LockType.SHARED));

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(new AcquireLockTask(id, LockType.EXCLUSIVE));
        assertFalse(future.get());
    }

    @Test
    public void givenMockedIdAndMockedLockType_whenFreeLock_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> lockerService.freeLock(Mockito.anyString(), Mockito.any()));
    }

    @Test
    public void givenMockedIdAndExclusiveLockType_whenAddLockAndFreeLock_thenNoExceptionIsThrown() {
        String id = Mockito.anyString();
        lockerService.addLock(id);
        assertDoesNotThrow(() -> lockerService.freeLock(id, LockType.EXCLUSIVE));
    }

    @Test
    public void givenMockedIdAndSharedLockType_whenAddLockAndFreeLock_thenNoExceptionIsThrown() {
        String id = Mockito.anyString();
        lockerService.addLock(id);
        assertDoesNotThrow(() -> lockerService.freeLock(id, LockType.EXCLUSIVE));
    }

    @Test
    public void givenMockedIdAndExclusiveLockType_whenAddLockAndAcquireLockAndFreeLock_thenNoExceptionIsThrown() throws InterruptedException {
        String id = Mockito.anyString();
        LockType lockType = LockType.EXCLUSIVE;

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, lockType));
        assertDoesNotThrow(() -> lockerService.freeLock(id, lockType));
    }

    @Test
    public void givenMockedIdAndSharedLockType_whenAddLockAndAcquireLockAndFreeLock_thenNoExceptionIsThrown() throws InterruptedException {
        String id = Mockito.anyString();
        LockType lockType = LockType.SHARED;

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, lockType));
        assertDoesNotThrow(() -> lockerService.freeLock(id, lockType));
    }

    @Test
    public void givenMockedIdAndExclusiveLockType_whenAddLockAndAcquireLockInSeparateThreadAndFreeLock_thenTrueReturned() throws InterruptedException, ExecutionException {
        String id = Mockito.anyString();
        LockType lockType = LockType.EXCLUSIVE;

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, lockType));

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(new AcquireLockTask(id, lockType));
        lockerService.freeLock(id, lockType);
        assertTrue(future.get());
    }

    @Test
    public void givenMockedIdAndSharedLockType_whenAddLockAndAcquireLockInSeparateThreadAndFreeLock_thenTrueReturned() throws InterruptedException, ExecutionException {
        String id = Mockito.anyString();
        LockType lockType = LockType.SHARED;

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, lockType));

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(new AcquireLockTask(id, lockType));
        lockerService.freeLock(id, lockType);
        assertTrue(future.get());
    }

    @Test
    public void givenMockedIdAndExclusiveLockType_whenAddLockAndAcquireSharedLockInSeparateThreadAndFreeLock_thenTrueReturned() throws InterruptedException, ExecutionException {
        String id = Mockito.anyString();
        LockType currentThreadLockType = LockType.EXCLUSIVE;

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, currentThreadLockType));

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(new AcquireLockTask(id, LockType.SHARED));
        lockerService.freeLock(id, currentThreadLockType);
        assertTrue(future.get());
    }

    @Test
    public void givenMockedIdAndSharedLockType_whenAddLockAndAcquireExclusiveLockInSeparateThreadAndFreeLock_thenFalseReturned() throws InterruptedException, ExecutionException {
        String id = Mockito.anyString();
        LockType currentThreadLockType = LockType.SHARED;

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, currentThreadLockType));

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(new AcquireLockTask(id, LockType.EXCLUSIVE));
        lockerService.freeLock(id, currentThreadLockType);
        assertTrue(future.get());
    }

    @Test
    public void givenMockedId_whenRemoveLock_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> lockerService.removeLock(Mockito.anyString()));
    }

    @Test
    public void givenMockedId_whenAddLockAndRemoveLock_thenNoExceptionIsThrown() {
        String id = Mockito.anyString();
        lockerService.addLock(id);
        assertDoesNotThrow(() -> lockerService.removeLock(id));
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