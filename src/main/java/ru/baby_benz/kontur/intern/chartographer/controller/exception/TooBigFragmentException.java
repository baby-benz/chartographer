package ru.baby_benz.kontur.intern.chartographer.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class TooBigFragmentException extends RuntimeException {
    public TooBigFragmentException(final int width, final int height, final int allowedWidth, final int allowedHeight) {
        super(
                "Passed fragment's dimensions (width:"
                        + width
                        + "px,height:"
                        +  height
                        + "px) exceed the maximum allowed dimensions (width:"
                        + allowedWidth
                        + "px,height:"
                        + allowedHeight
                        + "px)"
        );
    }
}
