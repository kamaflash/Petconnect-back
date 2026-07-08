package com.petconnect.pets.domain.valueobjects;

import com.petconnect.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public final class PetName extends ValueObject {

    @Column(name = "name", nullable = false, length = 100)
    private String value;

    protected PetName() {
        this.value = null;
    }

    public PetName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Pet name must not be empty");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("Pet name must not exceed 100 characters");
        }
        this.value = value.trim();
    }

    public String value() {
        return value;
    }

    @Override
    protected boolean equalsCore(Object other) {
        if (!(other instanceof PetName that))
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