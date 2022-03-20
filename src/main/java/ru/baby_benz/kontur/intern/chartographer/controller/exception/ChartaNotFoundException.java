package ru.baby_benz.kontur.intern.chartographer.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ChartaNotFoundException extends RuntimeException {
    public ChartaNotFoundException() {
        super("Charta with specified id not found");
    }

    public ChartaNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ChartaNotFoundException(final String id) {
        super("Charta with id " +  id + " not found");
    }

    public ChartaNotFoundException(final Throwable cause) {
        super(cause);
    }
}
