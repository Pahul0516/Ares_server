package com.ares.ares_server.exceptios;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) { super(message);
    }
}
