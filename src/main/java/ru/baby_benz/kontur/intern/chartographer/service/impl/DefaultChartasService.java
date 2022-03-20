package ru.baby_benz.kontur.intern.chartographer.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.ChartaIOException;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.ChartaNotFoundException;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.FileIsLockedException;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.ServiceIsUnavailableException;
import ru.baby_benz.kontur.intern.chartographer.service.ChartasService;
import ru.baby_benz.kontur.intern.chartographer.service.LockerService;
import ru.baby_benz.kontur.intern.chartographer.util.IdGenerator;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

@RequiredArgsConstructor
@Service
public class DefaultChartasService implements ChartasService {
    private final LockerService lockerService;
    @Value("${service.image.type}")
    private String imageType;
    @Value("${service.image.parent-path}")
    private String parentPath;
    private final List<String> pathsToChartas = new ArrayList<>();

    @PostConstruct
    private void prepareService() {
        createChartasFolder();
        discoverChartas();
    }

    private void createChartasFolder() {
        File chartasFolder = new File(parentPath);
        if (!chartasFolder.exists()){
            chartasFolder.mkdir();
        }
    }

    private void discoverChartas() {
        File chartasFolder = new File(parentPath);
        File[] chartasFiles = chartasFolder.listFiles();
        String fileName;
        if (chartasFiles != null && chartasFiles.length > 0) {
            for(File chartaFile : chartasFiles) {
                fileName = chartaFile.getName();
                pathsToChartas.add(fileName.substring(0, fileName.lastIndexOf('.')));
            }
        }
    }

    @Override
    public String createCharta(int width, int height) {
        BufferedImage bmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        String id = IdGenerator.getUUID().toString();
        String fileName = id + "." + imageType.toLowerCase();

        try {
            ImageIO.write(bmp, imageType, Files.newOutputStream(Path.of(parentPath, fileName)));
        } catch (IOException ioException) {
            throw new ChartaIOException("Error creating a new charta.");
        }

        return id;
    }

    @Override
    public void putFragment(String id, int x, int y, int width, int height, Resource fragmentData) {
        if (pathsToChartas.contains(id)) {
            String fileName = id + "." + imageType.toLowerCase();
            File chartaFile = new File(parentPath, fileName);

            try {
                BufferedImage fragment = ImageIO.read(fragmentData.getInputStream());

                BufferedImage fragmentOnCharta =  drawFragmentOnCharta(chartaFile, x, y, fragment);

                ImageIO.write(fragmentOnCharta, imageType, chartaFile);
            } catch (IOException ioException) {
                throw new ChartaIOException("I/O error during fragment putting occurred");
            }
        } else {
            throw new ChartaNotFoundException(id);
        }
    }

    @Override
    public InputStreamResource getFragment(String id, int x, int y, int width, int height) {
        if (pathsToChartas.contains(id)) {
            try {
                boolean isLockAcquired = lockerService.acquireSharedLock();

                if (isLockAcquired) {
                    String fileName = id + "." + imageType.toLowerCase();
                    try (InputStream is = new BufferedInputStream(Files.newInputStream(Path.of(parentPath, fileName)))) {
                        BufferedImage image = ImageIO.read(is);

                        ByteArrayOutputStream os = new ByteArrayOutputStream();

                        ImageIO.write(image.getSubimage(x, y, width, height), imageType, os);

                        return new InputStreamResource(new ByteArrayInputStream(os.toByteArray()));
                    } catch (IOException e) {
                        lockerService.freeSharedLock();
                        throw new ChartaIOException("I/O error during fragment extraction occurred");
                    }
                } else {
                    throw new FileIsLockedException(id);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServiceIsUnavailableException("Service is shutting down. Please, retry later");
            }
        } else {
            throw new ChartaNotFoundException(id);
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
}
