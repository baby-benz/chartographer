package ru.baby_benz.kontur.intern.chartographer.service.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.ChartaNotFoundException;
import ru.baby_benz.kontur.intern.chartographer.service.LockerService;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ReadWriteLockerServiceTest {
    private LockerService lockerService;

    @BeforeEach
    public void setUp() {
        lockerService = new ReadWriteLockerService();
    }

    @Test
    public void givenEmptyId_whenAddLock_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> lockerService.addLock(""));
    }

    @ParameterizedTest
    @EnumSource(LockType.class)
    public void givenEmptyIdAndLockType_whenAddLockAndAcquireLock_thenTrueReturned(LockType lockType) throws InterruptedException {
        String id = Mockito.anyString();
        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, lockType));
    }

    @ParameterizedTest
    @EnumSource(LockType.class)
    public void givenEmptyIdAndLockType_whenAcquireLock_thenExceptionIsThrown(LockType lockType) {
        assertThrows(ChartaNotFoundException.class, () -> lockerService.acquireLock("", lockType));
    }

    @Test
    public void givenEmptyIdAndExclusiveLockType_whenAddLockAndAcquireLockInSeparateThread_thenFalseReturned() throws InterruptedException, ExecutionException {
        String id = "";
        LockType lockType = LockType.EXCLUSIVE;

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, lockType));

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(new AcquireLockTask(id, lockType));
        assertFalse(future.get());
    }

    @Test
    public void givenEmptyIdAndSharedLockType_whenAddLockAndAcquireLockInSeparateThread_thenTrueReturned() throws InterruptedException, ExecutionException {
        String id = "";
        LockType lockType = LockType.SHARED;

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, lockType));

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(new AcquireLockTask(id, lockType));
        assertTrue(future.get());
    }

    @Test
    public void givenEmptyIdAndExclusiveLockType_whenAddLockAndAcquireSharedLockInSeparateThread_thenFalseReturned() throws InterruptedException, ExecutionException {
        String id = "";

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, LockType.EXCLUSIVE));

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(new AcquireLockTask(id, LockType.SHARED));
        assertFalse(future.get());
    }

    @Test
    public void givenEmptyIdAndSharedLockType_whenAddLockAndAcquireExclusiveLockInSeparateThread_thenFalseReturned() throws InterruptedException, ExecutionException {
        String id = "";

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, LockType.SHARED));

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(new AcquireLockTask(id, LockType.EXCLUSIVE));
        assertFalse(future.get());
    }

    @ParameterizedTest
    @EnumSource(LockType.class)
    public void givenEmptyIdAndLockType_whenFreeLock_thenNoExceptionIsThrown(LockType lockType) {
        assertDoesNotThrow(() -> lockerService.freeLock("", lockType));
    }

    @ParameterizedTest
    @EnumSource(LockType.class)
    public void givenEmptyIdAndLockType_whenAddLockAndFreeLock_thenNoExceptionIsThrown(LockType lockType) {
        String id = "";
        lockerService.addLock(id);
        assertDoesNotThrow(() -> lockerService.freeLock(id, lockType));
    }

    @ParameterizedTest
    @EnumSource(LockType.class)
    public void givenEmptyIdAndLockType_whenAddLockAndAcquireLockAndFreeLock_thenNoExceptionIsThrown(LockType lockType) throws InterruptedException {
        String id = "";

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, lockType));
        assertDoesNotThrow(() -> lockerService.freeLock(id, lockType));
    }

    @ParameterizedTest
    @MethodSource("lockTypeProvider")
    public void givenEmptyIdAndLockType_whenAddLockAndAcquireLockInSeparateThreadAndFreeLock_thenTrueReturned(
            LockType mainLockType, LockType separateLockType) throws InterruptedException, ExecutionException {
        String id = Mockito.anyString();

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, mainLockType));

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(new AcquireLockTask(id, separateLockType));
        lockerService.freeLock(id, mainLockType);
        assertTrue(future.get());
    }

    @Test
    public void givenEmptyId_whenRemoveLock_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> lockerService.removeLock(""));
    }

    @Test
    public void givenEmptyId_whenAddLockAndRemoveLock_thenNoExceptionIsThrown() {
        String id = "";
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

    private static Stream<Arguments> lockTypeProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (LockType timeUnit1 : LockType.values()) {
            for (LockType timeUnit2 : LockType.values()) {
                argumentBuilder.add(Arguments.of(timeUnit1, timeUnit2));
            }
        }
        return argumentBuilder.build();
    }
}