package ru.baby_benz.kontur.intern.chartographer.service;

import org.springframework.core.io.Resource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public interface IOService {
    void createImage(String fileId, int width, int height);

    BufferedImage readImage(String fileId);

    BufferedImage readImage(Resource resource);

    void writeImage(BufferedImage charta, String fileId);

    ByteArrayOutputStream writeImage(BufferedImage charta);

    void deleteImage(String fileId);
}
