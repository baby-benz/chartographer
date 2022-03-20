package ru.baby_benz.kontur.intern.chartographer.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.SERVICE_UNAVAILABLE, reason = "The file is locked.")
public class FileIsLockedException extends RuntimeException {
    public FileIsLockedException() {
        super();
    }

    public FileIsLockedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public FileIsLockedException(final String id) {
        super("The image with id " + id + " is being processed by another request");
    }

    public FileIsLockedException(final Throwable cause) {
        super(cause);
    }
}