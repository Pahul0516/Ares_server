package com.ares.ares_server.controllers;

import com.ares.ares_server.exceptios.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(409).body(ex.getMessage());
    }

    @ExceptionHandler(UserDoesNotExistsException.class)
    public ResponseEntity<?> handleUserDoesNotExists(UserDoesNotExistsException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(401).body(ex.getMessage());
    }
    @ExceptionHandler(RunDoesNotExistException.class)
    public ResponseEntity<?> handleRunDoesNotExists(RunDoesNotExistException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(ZoneDoesNotExistException.class)
    public ResponseEntity<?> handleZoneDoesNotExistException(ZoneDoesNotExistException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

}
