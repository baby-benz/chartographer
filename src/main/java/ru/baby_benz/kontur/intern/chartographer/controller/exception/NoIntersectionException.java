package ru.baby_benz.kontur.intern.chartographer.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class NoIntersectionException extends RuntimeException {
    public NoIntersectionException(int x, int y, int chartaWidth, int chartaHeight) {
        super(
                "Fragment's upper left corner (x,y) coordinate ("
                        + x
                        + ","
                        + y
                        + ") couldn't be more than charta's dimensions"
                        + "(width:"
                        + chartaWidth
                        + "px,height:"
                        + chartaHeight
                        + "px)"
        );
    }
}
