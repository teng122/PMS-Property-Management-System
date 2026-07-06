package com.smarthotel.housekeeping_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCleaningTaskStateException extends RuntimeException {
    public InvalidCleaningTaskStateException(String message) {
        super(message);
    }
}
