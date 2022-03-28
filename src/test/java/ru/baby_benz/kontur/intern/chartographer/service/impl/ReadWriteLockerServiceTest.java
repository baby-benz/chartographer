package ru.baby_benz.kontur.intern.chartographer.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.ChartaNotFoundException;
import ru.baby_benz.kontur.intern.chartographer.service.LockerService;

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
    public void givenRandomId_whenAddLockAndAcquireInvalidLock_thenExceptionIsThrown() throws InterruptedException {
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

    // TODO: repair mt test
    /*@Test
    public void givenRandomId_whenAddAndAcquireExclusiveLockMul_whenAcquireLock_thenFalseReturned() throws InterruptedException, ExecutionException {
        String id = Mockito.anyString();
        LockType lockType = LockType.EXCLUSIVE;

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, lockType));

        FutureTask<Boolean> futureTask = new FutureTask<>(new AcquireLockCallable(id, lockType));
        futureTask.run();
        assertFalse(futureTask.get());
    }

    @Test
    public void givenRandomId_whenAddAndAcquireSharedLock_whenAcquireLock_thenTrueReturned() throws InterruptedException, ExecutionException {
        String id = Mockito.anyString();
        LockType lockType = LockType.SHARED;

        lockerService.addLock(id);
        assertTrue(lockerService.acquireLock(id, lockType));

        FutureTask<Boolean> futureTask = new FutureTask<>(new AcquireLockCallable(id, lockType));
        futureTask.run();
        assertTrue(futureTask.get());
    }

    @RequiredArgsConstructor
    private class AcquireLockCallable implements Callable<Boolean> {
        private final String id;
        private final LockType lockType;

        @Override
        public Boolean call() throws InterruptedException {
            return lockerService.acquireLock(id, lockType);
        }
    }*/
}