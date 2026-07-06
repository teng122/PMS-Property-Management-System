package com.smarthotel.housekeeping_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CleaningTaskNotFoundException extends RuntimeException {
    public CleaningTaskNotFoundException(String message) {
        super(message);
    }
}
