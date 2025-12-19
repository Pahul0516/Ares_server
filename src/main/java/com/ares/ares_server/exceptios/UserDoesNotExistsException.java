package com.ares.ares_server.exceptios;

public class UserDoesNotExistsException extends RuntimeException {
    public UserDoesNotExistsException(String message) { super(message);}
}
