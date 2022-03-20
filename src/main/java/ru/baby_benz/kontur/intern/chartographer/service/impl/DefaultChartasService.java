package ru.baby_benz.kontur.intern.chartographer.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.ChartaIOException;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.FileIsLockedException;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.IllegalCoordinateFormatException;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.ServiceIsUnavailableException;
import ru.baby_benz.kontur.intern.chartographer.service.ChartasService;
import ru.baby_benz.kontur.intern.chartographer.service.LockerService;
import ru.baby_benz.kontur.intern.chartographer.util.IdGenerator;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
@Service
public class DefaultChartasService implements ChartasService {
    private final LockerService lockerService;
    @Value("${service.image.type}")
    private String imageType;
    @Value("${service.image.parent-path}")
    private String parentPath;

    @PostConstruct
    private void createChartasFolder() {
        File chartasFolder = new File(parentPath);
        if (!chartasFolder.exists()) {
            chartasFolder.mkdir();
        }
    }

    @Override
    public String createCharta(int width, int height) {
        String id = IdGenerator.getUUID().toString();
        LockType lockType = LockType.EXCLUSIVE;
        try {
            lockerService.createAndAcquireLock(id, LockType.EXCLUSIVE);

            BufferedImage bmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            String fileName = id + "." + imageType.toLowerCase();

            try {
                ImageIO.write(bmp, imageType, Files.newOutputStream(Path.of(parentPath, fileName)));
            } catch (IOException ioException) {
                throw new ChartaIOException("I/O error during creating a new charta");
            }

            return id;
        } finally {
            lockerService.freeLock(id, lockType);
        }
    }

    @Override
    public void putFragment(String id, int x, int y, int width, int height, Resource fragmentData) {
        checkForPlaneNegativity(x, y, width, height);
        LockType lockType = LockType.EXCLUSIVE;
        try {
            boolean isLockAcquired = lockerService.acquireLock(id, lockType);

            if (isLockAcquired) {
                String fileName = id + "." + imageType.toLowerCase();
                File chartaFile = new File(parentPath, fileName);

                try {
                    BufferedImage fragment = ImageIO.read(fragmentData.getInputStream());

                    if (x < 0) {
                        width += x;
                        x = 0;
                    }

                    if (y < 0) {
                        height += y;
                        y = 0;
                    }

                    BufferedImage fragmentOnCharta = drawFragmentOnCharta(chartaFile, x, y, fragment);

                    ImageIO.write(fragmentOnCharta, imageType, chartaFile);
                } catch (IOException ioException) {
                    lockerService.freeLock(id, lockType);
                    throw new ChartaIOException("I/O error during fragment putting occurred");
                }
            } else {
                throw new FileIsLockedException(id);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceIsUnavailableException("Service is shutting down. Please, retry later");
        } finally {
            lockerService.freeLock(id, lockType);
        }
    }

    @Override
    public InputStreamResource getFragment(String id, int x, int y, int width, int height) {
        checkForPlaneNegativity(x, y, width, height);
        LockType lockType = LockType.SHARED;
        try {
            boolean isLockAcquired = lockerService.acquireLock(id, lockType);

            if (isLockAcquired) {
                String fileName = id + "." + imageType.toLowerCase();
                try (InputStream is = new BufferedInputStream(Files.newInputStream(Path.of(parentPath, fileName)))) {
                    BufferedImage image = ImageIO.read(is);

                    ByteArrayOutputStream os = new ByteArrayOutputStream();

                    if (x < 0) {
                        width += x;
                        x = 0;
                    }

                    if (y < 0) {
                        height += y;
                        y = 0;
                    }

                    ImageIO.write(image.getSubimage(x, y, width, height), imageType, os);

                    return new InputStreamResource(new ByteArrayInputStream(os.toByteArray()));
                } catch (IOException e) {
                    lockerService.freeLock(id, lockType);
                    throw new ChartaIOException("I/O error during fragment extraction occurred");
                }
            } else {
                throw new FileIsLockedException(id);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceIsUnavailableException("Service is shutting down. Please, retry later");
        } finally {
            lockerService.freeLock(id, lockType);
        }
    }

    @Override
    public void deleteCharta(String id) {
        LockType lockType = LockType.EXCLUSIVE;
        try {
            boolean isLockAcquired = lockerService.acquireLock(id, lockType);

            if (isLockAcquired) {
                String fileName = id + "." + imageType.toLowerCase();
                try {
                    Files.delete(Path.of(parentPath, fileName));
                    lockerService.removeLock(id);
                } catch (IOException e) {
                    lockerService.freeLock(id, lockType);
                    throw new ChartaIOException("I/O error during charta deletion occurred");
                }
            } else {
                throw new FileIsLockedException(id);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceIsUnavailableException("Service is shutting down. Please, retry later");
        }
    }

    private BufferedImage drawFragmentOnCharta(File chartaFile, int x, int y, BufferedImage fragment) throws IOException {
        BufferedImage charta = ImageIO.read(chartaFile);
        BufferedImage fragmentOnCharta = new BufferedImage(charta.getWidth(), charta.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D fragmentOnChartaGraphics = fragmentOnCharta.createGraphics();

        fragmentOnChartaGraphics.drawImage(charta, 0, 0, null);
        fragmentOnChartaGraphics.drawImage(fragment, x, y, null);

        fragmentOnChartaGraphics.dispose();

        return fragmentOnCharta;
    }

    private void checkForPlaneNegativity(int x, int y, int width, int height) {
        if (x + width < 0 || y + height < 0) {
            throw new IllegalCoordinateFormatException(x, y, width, height);
        }
    }
}
