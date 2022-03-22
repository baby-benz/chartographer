package ru.baby_benz.kontur.intern.chartographer.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class FragmentNegativePlaneException extends RuntimeException {
    public FragmentNegativePlaneException() {
        super();
    }

    public FragmentNegativePlaneException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public FragmentNegativePlaneException(int x, int y, int width, int height) {
        super(
                "Passed fragment has wrong parameters(x:=" +
                x +
                ",y:=" +
                y +
                ",width:=" +
                width +
                ",height:=" +
                height +
                "). Fragment located entirely in the negative plane"
        );
    }

    public FragmentNegativePlaneException(final Throwable cause) {
        super(cause);
    }
}
