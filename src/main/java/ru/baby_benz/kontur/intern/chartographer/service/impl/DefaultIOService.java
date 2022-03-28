package ru.baby_benz.kontur.intern.chartographer.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.baby_benz.kontur.intern.chartographer.configuration.ImageProperties;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.ChartaIOException;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.FileIsLockedException;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.ServiceIsUnavailableException;
import ru.baby_benz.kontur.intern.chartographer.service.IOService;
import ru.baby_benz.kontur.intern.chartographer.service.LockerService;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
@Service
public class DefaultIOService implements IOService {
    private final ImageProperties imageProperties;
    private final LockerService lockerService;

    @PostConstruct
    private void prepareService() {
        createChartasFolder();
        discoverChartas();
    }

    private void createChartasFolder() {
        File chartasFolder = new File(imageProperties.getParentPath());
        if (!chartasFolder.exists()) {
            chartasFolder.mkdir();
        }
    }

    private void discoverChartas() {
        File chartasFolder = new File(imageProperties.getParentPath());
        File[] chartasFiles = chartasFolder.listFiles();
        if (chartasFiles != null && chartasFiles.length > 0) {
            String fileName;
            for (File chartaFile : chartasFiles) {
                fileName = chartaFile.getName();
                lockerService.addLock(fileName.substring(0, fileName.lastIndexOf('.')));
            }
        }
    }

    @Override
    public void createCharta(String id, int width, int height) {
        BufferedImage bmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        String fileName = id + "." + imageProperties.getType();

        try {
            ImageIO.write(
                    bmp,
                    imageProperties.getType(),
                    Files.newOutputStream(Path.of(imageProperties.getParentPath(), fileName))
            );
        } catch (IOException ioException) {
            throw new ChartaIOException("I/O error occurred while creating a charta");
        }

        lockerService.addLock(id);
    }

    @Override
    public BufferedImage readCharta(String id) {
        LockType lockType = LockType.SHARED;

        try {
            boolean isLockAcquired = lockerService.acquireLock(id, lockType);

            if (!isLockAcquired) {
                throw new FileIsLockedException(id);
            }

            String fileName = id + "." + imageProperties.getType();
            try {
                return ImageIO.read(Files.newInputStream(Path.of(imageProperties.getParentPath(), fileName)));
            } catch (IOException e) {
                lockerService.freeLock(id, lockType);
                throw new ChartaIOException("I/O error occurred while reading a charta");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceIsUnavailableException("Service is shutting down. Please, retry later");
        } finally {
            lockerService.freeLock(id, lockType);
        }
    }

    @Override
    public BufferedImage read(Resource resource) {
        try {
            return ImageIO.read(resource.getInputStream());
        } catch (IOException ioException) {
            throw new ChartaIOException("I/O error occurred while reading a resource");
        }
    }

    @Override
    public void writeCharta(String id, BufferedImage charta) {
        LockType lockType = LockType.EXCLUSIVE;

        try {
            boolean isLockAcquired = lockerService.acquireLock(id, lockType);

            if (!isLockAcquired) {
                throw new FileIsLockedException(id);
            }

            String fileName = id + "." + imageProperties.getType();
            try {
                ImageIO.write(
                        charta,
                        imageProperties.getType(),
                        Files.newOutputStream(Path.of(imageProperties.getParentPath(), fileName))
                );
            } catch (IOException ioException) {
                lockerService.freeLock(id, lockType);
                throw new ChartaIOException("I/O error occurred while writing a charta");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceIsUnavailableException("Service is shutting down. Please, retry later");
        } finally {
            lockerService.freeLock(id, lockType);
        }
    }

    @Override
    public ByteArrayOutputStream writeCharta(BufferedImage image) {
        final ByteArrayOutputStream output = new ByteArrayOutputStream() {
            @Override
            public synchronized byte[] toByteArray() {
                return this.buf;
            }
        };
        try {
            ImageIO.write(image, imageProperties.getType(), output);
            return output;
        } catch (IOException e) {
            throw new ChartaIOException("I/O error occurred while writing a charta");
        }
    }

    @Override
    public void deleteCharta(String id) {
        LockType lockType = LockType.EXCLUSIVE;
        try {
            boolean isLockAcquired = lockerService.acquireLock(id, lockType);

            if (!isLockAcquired) {
                throw new FileIsLockedException(id);
            }

            String fileName = id + "." + imageProperties.getType();
            try {
                Files.delete(Path.of(imageProperties.getParentPath(), fileName));
                lockerService.removeLock(id);
            } catch (IOException e) {
                lockerService.freeLock(id, lockType);
                throw new ChartaIOException("I/O error occurred while deleting a charta");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceIsUnavailableException("Service is shutting down. Please, retry later");
        }
    }
}
