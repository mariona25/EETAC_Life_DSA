package edu.upc.dsa.exceptions;

// Maps to HTTP 404 Not Found (user, cafeteria product, minigame, etc. does not exist).
public class NotFoundException extends Exception {
    public NotFoundException(String message) {
        super(message);
    }
}