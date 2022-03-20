package ru.baby_benz.kontur.intern.chartographer.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.SERVICE_UNAVAILABLE, reason = "The service shutting down.")
public class ServiceIsUnavailableException extends RuntimeException {
    public ServiceIsUnavailableException() {
        super();
    }

    public ServiceIsUnavailableException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ServiceIsUnavailableException(final String message) {
        super(message);
    }

    public ServiceIsUnavailableException(final Throwable cause) {
        super(cause);
    }
}
