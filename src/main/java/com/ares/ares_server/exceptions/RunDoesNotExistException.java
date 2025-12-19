package com.ares.ares_server.exceptions;

public class RunDoesNotExistException extends RuntimeException {
    public RunDoesNotExistException(String message) {
        super(message);
    }
}
