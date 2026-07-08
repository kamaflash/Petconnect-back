package com.petconnect.auth.domain.valueobjects;

import com.petconnect.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public final class Email extends ValueObject {

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String value;

    protected Email() {
        this.value = null;
    }

    public Email(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email must not be empty");
        }
        if (!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
        this.value = value.toLowerCase();
    }

    public String value() {
        return value;
    }

    @Override
    protected boolean equalsCore(Object other) {
        if (!(other instanceof Email that))
            return false;
        return Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}