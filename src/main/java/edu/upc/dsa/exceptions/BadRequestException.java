package edu.upc.dsa.exceptions;

// Maps to HTTP 400 Bad Request (insufficient coins, not enough energy, invalid input data, etc.).
public class BadRequestException extends Exception {
    public BadRequestException(String message) {
        super(message);
    }
}