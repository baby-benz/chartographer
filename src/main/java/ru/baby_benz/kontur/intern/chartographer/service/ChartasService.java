package ru.baby_benz.kontur.intern.chartographer.service;

import java.io.InputStream;

public interface ChartasService {
    String createCharta(int width, int height);
    void putFragment(String id, int x, int y, int width, int height, InputStream fragmentData);
    InputStream getFragment(String id, int x, int y, int width, int height);
}
