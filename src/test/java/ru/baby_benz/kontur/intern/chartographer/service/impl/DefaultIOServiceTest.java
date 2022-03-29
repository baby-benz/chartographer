package ru.baby_benz.kontur.intern.chartographer.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.baby_benz.kontur.intern.chartographer.configuration.ImageProperties;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.ChartaIOException;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.ChartaNotFoundException;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.FileIsLockedException;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.ServiceIsUnavailableException;
import ru.baby_benz.kontur.intern.chartographer.service.IOService;
import ru.baby_benz.kontur.intern.chartographer.service.LockerService;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(ImageProperties.class)
@ActiveProfiles("test")
@Slf4j
public class DefaultIOServiceTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ImageProperties imageProperties;
    private final LockerService lockerService = new ReadWriteLockerService();
    private IOService ioService;

    @BeforeEach
    public void setUp() {
        createTestFolder();
        ioService = new DefaultIOService(imageProperties, lockerService);
    }

    @AfterEach
    public void tearDown() throws IOException {
        deleteTestFolder();
    }

    @Test
    public void givenEmptyId_whenCreateImage_thenFileChecksReturnTrue() throws IOException {
        String fileId = "";
        ioService.createImage(fileId, 2, 2);
        assertTrue(isImageOk(fileId));
    }

    @Test
    public void givenEmptyId_whenReadImage_thenExceptionIsThrown() {
        assertThrows(ChartaNotFoundException.class, () -> ioService.readImage(""));
    }

    @Test
    public void givenEmptyId_whenInterruptThreadAndCreateImage_thenExceptionIsThrown() {
        String fileId = "";
        Thread.currentThread().interrupt();
        assertThrows(ServiceIsUnavailableException.class, () -> ioService.createImage(fileId, 2, 2));
    }

    @Test
    public void givenEmptyId_whenCreateImageAndReadImage_thenReturnedImageEquals() {
        String fileId = "";

        ioService.createImage(fileId, 2, 2);

        final byte[] expectedBytes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        DataBufferByte imageDataBuffer = (DataBufferByte) ioService.readImage(fileId).getRaster().getDataBuffer();
        assertArrayEquals(expectedBytes, imageDataBuffer.getData());
    }

    @Test
    public void givenEmptyId_whenCreateImageAndDeleteTestFolderAndReadImage_thenExceptionIsThrown() throws IOException {
        String fileId = "";
        ioService.createImage(fileId, 2, 2);
        deleteTestFolder();
        assertThrows(ChartaIOException.class, () -> ioService.readImage(fileId));
    }

    @Test
    public void givenEmptyIdAndMockedLockerService_whenReadImage_thenExceptionIsThrown() throws InterruptedException {
        String fileId = "";

        LockerService lockerService = Mockito.mock(LockerService.class);
        Mockito.doReturn(false).when(lockerService).acquireLock(fileId, LockType.SHARED);
        ioService = new DefaultIOService(imageProperties, lockerService);

        assertThrows(FileIsLockedException.class, () -> ioService.readImage(fileId));
    }

    @Test
    public void givenEmptyId_whenCreateImageAndInterruptThreadAndReadImage_thenExceptionIsThrown() {
        String fileId = "";
        ioService.createImage(fileId, 2, 2);
        Thread.currentThread().interrupt();
        assertThrows(ServiceIsUnavailableException.class, () -> ioService.readImage(fileId));
    }

    @Test
    public void givenEmptyId_whenWriteImage_thenExceptionIsThrown() {
        assertThrows(ChartaNotFoundException.class, () -> ioService.writeImage(
                new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "")
        );
    }

    @Test
    public void givenEmptyId_whenCreateImageAndWriteImage_thenFileChecksReturnTrue() throws IOException {
        String fileId = "";
        ioService.createImage(fileId, 2, 2);
        ioService.writeImage(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), fileId);
        assertTrue(isImageOk(fileId));
    }

    @Test
    public void givenEmptyId_whenCreateImageAndDeleteTestFolderAndWriteImage_thenExceptionIsThrown() throws IOException {
        String fileId = "";
        ioService.createImage(fileId, 2, 2);
        deleteTestFolder();
        assertThrows(ChartaIOException.class, () -> ioService.writeImage(
                new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), fileId)
        );
    }

    @Test
    public void givenEmptyIdAndMockedLockerService_whenWriteImage_thenExceptionIsThrown() throws InterruptedException {
        String fileId = "";

        LockerService lockerService = Mockito.mock(LockerService.class);
        Mockito.doReturn(false).when(lockerService).acquireLock(fileId, LockType.SHARED);
        ioService = new DefaultIOService(imageProperties, lockerService);

        assertThrows(FileIsLockedException.class, () -> ioService.writeImage(
                new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), fileId)
        );
    }

    @Test
    public void givenEmptyId_whenCreateImageAndInterruptThreadAndWriteImage_thenExceptionIsThrown() {
        String fileId = "";
        ioService.createImage(fileId, 2, 2);
        Thread.currentThread().interrupt();
        assertThrows(ServiceIsUnavailableException.class, () -> ioService.writeImage(
                new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), fileId)
        );
    }

    @Test
    public void givenEmptyId_whenDeleteImage_thenExceptionIsThrown() {
        assertThrows(ChartaNotFoundException.class, () -> ioService.deleteImage(""));
    }

    @Test
    public void givenEmptyId_whenCreateImageAndDeleteImage_thenFileDoesNotExist() {
        String fileId = "";
        ioService.createImage(fileId, 2, 2);
        ioService.deleteImage(fileId);
        assertFalse(isFileExist(fileId));
    }

    @Test
    public void givenEmptyId_whenCreateImageAndDeleteTestFolderAndDeleteImage_thenExceptionIsThrown() throws IOException {
        String fileId = "";
        ioService.createImage(fileId, 2, 2);
        deleteTestFolder();
        assertThrows(ChartaIOException.class, () -> ioService.deleteImage(fileId));
    }

    @Test
    public void givenEmptyIdAndMockedLockerService_whenDeleteImage_thenExceptionIsThrown() throws InterruptedException {
        String fileId = "";

        LockerService lockerService = Mockito.mock(LockerService.class);
        Mockito.doReturn(false).when(lockerService).acquireLock(fileId, LockType.SHARED);
        ioService = new DefaultIOService(imageProperties, lockerService);

        assertThrows(FileIsLockedException.class, () -> ioService.deleteImage(fileId));
    }

    @Test
    public void givenEmptyId_whenCreateImageAndInterruptThreadAndDeleteImage_thenExceptionIsThrown() {
        String fileId = "";
        ioService.createImage(fileId, 2, 2);
        Thread.currentThread().interrupt();
        assertThrows(ServiceIsUnavailableException.class, () -> ioService.deleteImage(fileId));
    }

    private boolean isFileExist(String fileId) {
        String fileName = fileId + "." + imageProperties.getType();
        Path pathToFile = Path.of(imageProperties.getParentPath(), fileName);
        return Files.exists(pathToFile);
    }

    private boolean isImageOk(String fileId) throws IOException {
        // Byte form of the RGB black 2x2 bmp image
        final byte[] EXPECTED_IMAGE_BYTES = {
                66, 77, 70, 0, 0, 0, 0, 0, 0, 0, 54, 0, 0, 0, 40, 0, 0, 0, 2, 0, 0, 0,
                2, 0, 0, 0, 1, 0, 24, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        String fileName = fileId + "." + imageProperties.getType();
        Path pathToFile = Path.of(imageProperties.getParentPath(), fileName);

        return Files.exists(pathToFile) && Arrays.equals(EXPECTED_IMAGE_BYTES, Files.readAllBytes(pathToFile));
    }

    private void createTestFolder() {
        File chartasFolder = new File(imageProperties.getParentPath());
        if (!chartasFolder.exists()) {
            chartasFolder.mkdir();
        }
    }

    private void deleteTestFolder() throws IOException {
        Path pathTOTestFolder = Path.of(imageProperties.getParentPath());
        if (Files.exists(pathTOTestFolder)) {
            Files.walk(pathTOTestFolder)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach((File fileToBeDeleted) -> {
                        if (!fileToBeDeleted.delete()) {
                            log.warn(
                                    "Error while deleting test folder " + imageProperties.getParentPath()
                                            + ". Please, delete it manually"
                            );
                        }
                    });
        }
    }
}