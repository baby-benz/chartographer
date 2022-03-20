package ru.baby_benz.kontur.intern.chartographer.service;

import org.springframework.core.io.Resource;

public interface ChartasService {
    String createCharta(int width, int height);
    void putFragment(String id, int x, int y, int width, int height, Resource fragmentData);
}
