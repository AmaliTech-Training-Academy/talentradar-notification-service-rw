package com.talentradar.talentradarnotificationservicerw.controllers;

import com.talentradar.talentradarnotificationservicerw.domain.dtos.ApiErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorController {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .success(false)
                .message("An unexpected error occurred")
                .errors(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFoundException(Exception ex) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .success(false)
                .message("Resource not found")
                .errors(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

}
