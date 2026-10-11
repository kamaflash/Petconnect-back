package com.petconnect.users.domain.exceptions;

public class InvalidProfileCredentialException extends RuntimeException {

    private final String field;

    public InvalidProfileCredentialException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
