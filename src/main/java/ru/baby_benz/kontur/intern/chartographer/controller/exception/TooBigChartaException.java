package ru.baby_benz.kontur.intern.chartographer.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class TooBigChartaException extends RuntimeException {
    public TooBigChartaException(final int width, final int height, final int allowedWidth, final int allowedHeight) {
        super(
                "Passed charta dimensions (width:"
                        + width
                        + "px,height:"
                        +  height
                        + "px) exceed the maximum allowable dimensions (width:"
                        + allowedWidth
                        + "px,height:"
                        + allowedHeight
                        + "px)"
        );
    }
}
