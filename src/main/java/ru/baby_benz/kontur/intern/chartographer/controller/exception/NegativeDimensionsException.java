package ru.baby_benz.kontur.intern.chartographer.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class NegativeDimensionsException extends RuntimeException {
    public NegativeDimensionsException(String imageType, final int width, final int height) {
        super(
                imageType + "'s dimensions (width:"
                        + width
                        + "px,height:"
                        +  height
                        + "px) contain negative value"
        );
    }
}
