package ru.baby_benz.kontur.intern.chartographer.controller.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class ApiError {
    private List<String> errors;
}

