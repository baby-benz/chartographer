package ru.baby_benz.kontur.intern.chartographer.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.*;
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
    @Value("${service.image.charta.maximum-dimensions.width}")
    private int maximumAllowedChartaWidth;
    @Value("${service.image.charta.maximum-dimensions.height}")
    private int maximumAllowedChartaHeight;
    @Value("${service.image.fragment.maximum-dimensions.width}")
    private int maximumAllowedFragmentWidth;
    @Value("${service.image.fragment.maximum-dimensions.height}")
    private int maximumAllowedFragmentHeight;

    @PostConstruct
    private void createChartasFolder() {
        File chartasFolder = new File(parentPath);
        if (!chartasFolder.exists()) {
            chartasFolder.mkdir();
        }
    }

    @Override
    public String createCharta(int width, int height) {
        if (width <= maximumAllowedChartaWidth && height <= maximumAllowedChartaHeight) {
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
        } else {
            throw new TooBigChartaException(width, height, maximumAllowedChartaWidth, maximumAllowedChartaHeight);
        }
    }

    @Override
    public void putFragment(String id, int x, int y, int width, int height, Resource fragmentData) {
        if (width <= maximumAllowedFragmentWidth && height <= maximumAllowedFragmentWidth) {
            if (isPlaneNegative(x, y, width, height)) {
                throw new FragmentNegativePlaneException(x, y, width, height);
            }

            LockType lockType = LockType.EXCLUSIVE;

            try {
                boolean isLockAcquired = lockerService.acquireLock(id, lockType);

                if (isLockAcquired) {
                    try {
                        insertFragmentIntoCharta(id, x, y, width, height, fragmentData);
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
        } else {
            throw new TooBigFragmentException(width, height, maximumAllowedFragmentWidth, maximumAllowedFragmentHeight);
        }
    }

    @Override
    public InputStreamResource getFragment(String id, int x, int y, int width, int height) {
        if (isPlaneNegative(x, y, width, height)) {
            throw new FragmentNegativePlaneException(x, y, width, height);
        }

        LockType lockType = LockType.SHARED;

        try {
            boolean isLockAcquired = lockerService.acquireLock(id, lockType);

            if (isLockAcquired) {
                String chartaFileName = id + "." + imageType.toLowerCase();
                try (InputStream is = new BufferedInputStream(Files.newInputStream(Path.of(parentPath, chartaFileName)))) {
                    return extractFragmentFromCharta(is, x, y, width, height);
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

    private void insertFragmentIntoCharta(File chartaFile, int x, int y, int width, int height, Resource fragmentData) throws IOException {
        BufferedImage charta = ImageIO.read(chartaFile);

        int chartaWidth = charta.getWidth();
        int chartaHeight = charta.getHeight();

        checkFragmentIntersection(x, y, chartaWidth, chartaHeight);

        BufferedImage fragment = ImageIO.read(fragmentData.getInputStream());

        fragment = cropToSize(fragment, x, y, width, height, chartaWidth, chartaHeight);

        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }

        BufferedImage fragmentOnCharta = drawFragmentOnCharta(charta, x, y, fragment);

        ImageIO.write(fragmentOnCharta, imageType, chartaFile);
    }

    private InputStreamResource extractFragmentFromCharta(InputStream is, int x, int y, int width, int height) throws IOException {
        BufferedImage charta = ImageIO.read(is);

        int chartaWidth = charta.getWidth();
        int chartaHeight = charta.getHeight();

        checkFragmentIntersection(x, y, chartaWidth, chartaHeight);

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        if (x < 0) {
            width += x;
            x = 0;
        }

        if (y < 0) {
            height += y;
            y = 0;
        }

        int fragmentEndX = x + width;
        int fragmentEndY = y + height;

        if (fragmentEndX > chartaWidth) {
            width = fragmentEndX - chartaWidth;
        }
        if (fragmentEndY > chartaHeight) {
            height = fragmentEndY - chartaHeight;
        }

        ImageIO.write(charta.getSubimage(x, y, width, height), imageType, os);

        return new InputStreamResource(new ByteArrayInputStream(os.toByteArray()));
    }

    private BufferedImage drawFragmentOnCharta(BufferedImage charta, int x, int y, BufferedImage fragment) {
        BufferedImage fragmentOnCharta = new BufferedImage(charta.getWidth(), charta.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D fragmentOnChartaGraphics = fragmentOnCharta.createGraphics();

        fragmentOnChartaGraphics.drawImage(charta, 0, 0, null);
        fragmentOnChartaGraphics.drawImage(fragment, x, y, null);

        fragmentOnChartaGraphics.dispose();

        return fragmentOnCharta;
    }

    private void checkFragmentIntersection(int x, int y, int chartaWidth, int chartaHeight) {
        if (!doesFragmentIntersectCharta(x, y, chartaWidth, chartaHeight)) {
            throw new NoIntersectionException(x, y, chartaWidth, chartaHeight);
        }
    }

    private boolean isPlaneNegative(int x, int y, int fragmentWidth, int fragmentHeight) {
        return x + fragmentWidth <= 0 || y + fragmentHeight <= 0;
    }

    private boolean doesFragmentIntersectCharta(int x, int y, int chartaWidth, int chartaHeight) {
        return x < chartaWidth && y < chartaHeight;
    }

    private BufferedImage cropToSize(BufferedImage image, int x, int y, int width, int height,
                                     int targetWidth, int targetHeight) {
        int xCropFrom = 0, yCropFrom = 0;

        if (x + width > targetWidth) {
            width = targetWidth - x;
        }
        if (y + height > targetHeight) {
            height = targetHeight - y;
        }
        if (x < 0) {
            xCropFrom = -x;
            width += x;
        }
        if (y < 0) {
            yCropFrom = -y;
            height += y;
        }

        return image.getSubimage(xCropFrom, yCropFrom, width, height);
    }
}
