package ru.baby_benz.kontur.intern.chartographer.service;

import org.springframework.core.io.Resource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public interface IOService {
    void createCharta(String id, int width, int height);
    BufferedImage readCharta(String id);
    BufferedImage read(Resource resource);
    void writeCharta(String id, BufferedImage charta);
    ByteArrayOutputStream writeCharta(BufferedImage charta);
    void deleteCharta(String id);
}
