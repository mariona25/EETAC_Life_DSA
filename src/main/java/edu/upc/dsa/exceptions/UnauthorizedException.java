package edu.upc.dsa.exceptions;

// Maps to HTTP 401 Unauthorized (wrong password during login).
public class UnauthorizedException extends Exception {
    public UnauthorizedException(String message) {
        super(message);
    }
}
