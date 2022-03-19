package ru.baby_benz.kontur.intern.chartographer.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ChartaIOException extends RuntimeException {
    public ChartaIOException() {
        super();
    }

    public ChartaIOException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ChartaIOException(final String message) {
        super(message);
    }

    public ChartaIOException(final Throwable cause) {
        super(cause);
    }
}
