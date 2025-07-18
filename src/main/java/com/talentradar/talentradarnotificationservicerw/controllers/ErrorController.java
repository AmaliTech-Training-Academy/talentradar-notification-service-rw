package com.talentradar.talentradarnotificationservicerw.controllers;

import com.talentradar.talentradarnotificationservicerw.domain.dtos.ApiErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ErrorController {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex) {
        log.error(ex.getMessage());
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .success(false)
                .message("An unexpected error occurred")
                .errors(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFoundException(Exception ex) {
        log.error(ex.getMessage());
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .success(false)
                .message("Resource not found")
                .errors(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

}
