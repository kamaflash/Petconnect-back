package com.petconnect.marketplace.domain.exceptions;

public class ProductNotOwnedException extends RuntimeException {

    public ProductNotOwnedException(String message) {
        super(message);
    }

    public ProductNotOwnedException(String message, Throwable cause) {
        super(message, cause);
    }
}