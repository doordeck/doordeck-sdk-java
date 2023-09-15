package com.doordeck.sdk.util;

import java.util.Optional;

import java.util.Objects;

/**
 * Used with @Value.Immutable to specify if field was set to null, unspecified or set to a value
 *
 * To function, a default value of OptionalUpdate.preserve() must be used along
 * with @JsonInclude(JsonInclude.Include.NON_DEFAULT) and the OptionalUpdateModule registered
 *
 * @param <T>
 */
public class OptionalUpdate<T> {

    // Tri-state optional
    // null = delete
    // empty = preserve
    // set = update
    private final Optional<T> value;

    private OptionalUpdate(Optional<T> value) {
        this.value = value;
    }

    public boolean isDelete() {
        return value == null;
    }

    public boolean isUpdate() {
        return value != null && value.isPresent();
    }

    public boolean isPreserve() {
        return value != null && !value.isPresent();
    }

    public T get() {
        return value.get();
    }

    public static <T> OptionalUpdate<T> preserve() {
        return new OptionalUpdate<>(Optional.empty());
    }

    public static <T> OptionalUpdate<T> update(T value) {
        return new OptionalUpdate<>(Optional.of(value));
    }

    public static <T> OptionalUpdate<T> delete() {
        return new OptionalUpdate<>(null);
    }

    @Override
    public String toString() {
        if (isUpdate()) {
            return String.format("OptionalUpdate.update[%s]", value);
        } else if (isDelete()) {
            return "OptionalUpdate.delete";
        } else {
            return "OptionalUpdate.preserve";
        }
    }

    /**
     * Returns the hash code value of the present value, if any, or 0 (zero) if
     * no value is present.
     *
     * @return hash code value of the present value or 0 if no value is present
     */
    @Override
    public int hashCode() {
        return Objects.hash(isUpdate(), isDelete(), isPreserve(), value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OptionalUpdate)) {
            return false;
        }

        OptionalUpdate<?> other = (OptionalUpdate<?>) obj;
        return Objects.equals(value, other.value);
    }

}
