package com.ares.ares_server.exceptios;

public class RunDoesNotExistException extends RuntimeException {
    public RunDoesNotExistException(String message) {
        super(message);
    }
}
