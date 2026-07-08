package com.petconnect.shared.domain;

import java.util.Objects;

public abstract class ValueObject {

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return equalsCore(o);
    }

    protected abstract boolean equalsCore(Object other);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();
}