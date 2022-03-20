package ru.baby_benz.kontur.intern.chartographer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.baby_benz.kontur.intern.chartographer.service.ChartasService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/chartas")
public class ChartasController {
    private final ChartasService chartasService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public String createCharta(@RequestParam int width, @RequestParam int height) {
        return chartasService.createCharta(width, height);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/{id}/", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void putFragmentOnCharta(@RequestBody InputStreamResource fragmentData, @PathVariable("id") String chartaId,
                                    @RequestParam int x, @RequestParam int y,
                                    @RequestParam int width, @RequestParam int height) {
        chartasService.putFragment(chartaId, x, y, width, height, fragmentData);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{id}/", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Resource getCharta(@PathVariable("id") String chartaId,
                              @RequestParam int x, @RequestParam int y,
                              @RequestParam int width, @RequestParam int height) {
        return chartasService.getFragment(chartaId, x, y, width, height);
    }
}
