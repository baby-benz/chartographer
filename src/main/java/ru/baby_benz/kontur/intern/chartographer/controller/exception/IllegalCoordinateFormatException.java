package ru.baby_benz.kontur.intern.chartographer.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class IllegalCoordinateFormatException extends RuntimeException {
    public IllegalCoordinateFormatException() {
        super();
    }

    public IllegalCoordinateFormatException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public IllegalCoordinateFormatException(int x, int y, int width, int height) {
        super(
                "Passed fragment has wrong parameters(x := " +
                x +
                ",y:=" +
                y +
                ",width:=" +
                width +
                ",height:=" +
                height +
                "). " +
                "The result of (x + width) or (y + height) couldn't be negative"
        );
    }

    public IllegalCoordinateFormatException(final Throwable cause) {
        super(cause);
    }
}
