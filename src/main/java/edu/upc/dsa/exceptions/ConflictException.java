package edu.upc.dsa.exceptions;

// Maps to HTTP 409 Conflict (username or email already exists during registration).
public class ConflictException extends Exception {
    public ConflictException(String message) {
        super(message);
    }
}