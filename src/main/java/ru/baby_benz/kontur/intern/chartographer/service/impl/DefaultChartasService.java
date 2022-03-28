package ru.baby_benz.kontur.intern.chartographer.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.baby_benz.kontur.intern.chartographer.configuration.ImageProperties;
import ru.baby_benz.kontur.intern.chartographer.controller.exception.*;
import ru.baby_benz.kontur.intern.chartographer.service.ChartasService;
import ru.baby_benz.kontur.intern.chartographer.service.IOService;
import ru.baby_benz.kontur.intern.chartographer.util.IdGenerator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

@RequiredArgsConstructor
@Service
public class DefaultChartasService implements ChartasService {
    private final ImageProperties imageProperties;
    private final IOService ioService;

    @Override
    public String createCharta(int width, int height) {
        if (exceedsChartaMaxDims(width, height)) {
            throw new TooBigChartaException(
                    width,
                    height,
                    getChartaMaxWidth(),
                    getChartaMaxHeight()
            );
        }
        if (width <= 0 || height <= 0) {
            throw new NegativeDimensionsException("Charta", width, height);
        }

        String id = IdGenerator.getUUID().toString();
        ioService.createCharta(id, width, height);
        return id;
    }

    @Override
    public void putFragment(String id, int x, int y, int width, int height, Resource fragmentData) {
        if (isPlaneNegative(x, y, width, height)) {
            throw new FragmentNegativePlaneException(x, y, width, height);
        }

        BufferedImage charta = ioService.readCharta(id);
        charta = insertFragment(charta, x, y, width, height, fragmentData);
        ioService.writeCharta(id, charta);
    }

    @Override
    public InputStreamResource getFragment(String id, int x, int y, int width, int height) {
        if (exceedsFragmentMaxDims(width, height)) {
            throw new TooBigFragmentException(
                    width,
                    height,
                    getFragmentMaxWidth(),
                    getFragmentMaxHeight()
            );
        }
        if (width <= 0 || height <= 0) {
            throw new NegativeDimensionsException("Fragment", width, height);
        }
        if (isPlaneNegative(x, y, width, height)) {
            throw new FragmentNegativePlaneException(x, y, width, height);
        }

        return extractFragment(ioService.readCharta(id), x, y, width, height);
    }

    @Override
    public void deleteCharta(String id) {
        ioService.deleteCharta(id);
    }

    private InputStreamResource extractFragment(BufferedImage charta, int x, int y, int width, int height) {
        int chartaWidth = charta.getWidth();
        int chartaHeight = charta.getHeight();

        if (!intersectsCharta(x, y, chartaWidth, chartaHeight)) {
            throw new NoIntersectionException(x, y, chartaWidth, chartaHeight);
        }

        BufferedImage fragment = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        if (x + width > chartaWidth) {
            width = chartaWidth - x;
        }
        if (y + height > chartaHeight) {
            height = chartaHeight - y;
        }

        int fragmentX = 0, fragmentY = 0;
        if (x < 0) {
            width += x;
            fragmentX = -x;
            x = 0;
        }
        if (y < 0) {
            height += y;
            fragmentY = -y;
            y = 0;
        }

        Graphics fragmentGraphics = fragment.getGraphics();
        BufferedImage partOfChartaToFragment = charta.getSubimage(x, y, width, height);
        fragmentGraphics.drawImage(partOfChartaToFragment, fragmentX, fragmentY, null);
        fragmentGraphics.dispose();

        return new InputStreamResource(new ByteArrayInputStream(ioService.writeCharta(fragment).toByteArray()));
    }

    private BufferedImage insertFragment(BufferedImage charta, int x, int y, int width, int height, Resource fragmentData) {
        int chartaWidth = charta.getWidth();
        int chartaHeight = charta.getHeight();

        if (!intersectsCharta(x, y, chartaWidth, chartaHeight)) {
            throw new NoIntersectionException(x, y, chartaWidth, chartaHeight);
        }

        BufferedImage fragment = ioService.read(fragmentData);

        fragment = cropToSize(fragment, x, y, width, height, chartaWidth, chartaHeight);

        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }

        BufferedImage fragmentOnCharta = new BufferedImage(charta.getWidth(), charta.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D fragmentOnChartaGraphics = fragmentOnCharta.createGraphics();

        fragmentOnChartaGraphics.drawImage(charta, 0, 0, null);
        fragmentOnChartaGraphics.drawImage(fragment, x, y, null);

        fragmentOnChartaGraphics.dispose();

        return fragmentOnCharta;
    }

    private boolean isPlaneNegative(int x, int y, int fragmentWidth, int fragmentHeight) {
        return x + fragmentWidth <= 0 || y + fragmentHeight <= 0;
    }

    private boolean intersectsCharta(int x, int y, int chartaWidth, int chartaHeight) {
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

    private boolean exceedsChartaMaxDims(int width, int height) {
        return width > getChartaMaxWidth() || height > getChartaMaxHeight();
    }

    private boolean exceedsFragmentMaxDims(int width, int height) {
        return width > getFragmentMaxWidth() || height > getFragmentMaxHeight();
    }

    private int getChartaMaxWidth() {
        return imageProperties.getCharta().getMaxDimensions().getWidth();
    }

    private int getChartaMaxHeight() {
        return imageProperties.getCharta().getMaxDimensions().getHeight();
    }

    private int getFragmentMaxWidth() {
        return imageProperties.getFragment().getMaxDimensions().getWidth();
    }

    private int getFragmentMaxHeight() {
        return imageProperties.getFragment().getMaxDimensions().getHeight();
    }
}
