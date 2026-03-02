/*
 * This file is part of the linguae Library.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (LGPL-3.0)
 * You should have received a copy of the license in LICENSE.LGPL
 * If not, see https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Copyright (c) leycm <leycm@proton.me>
 * Copyright (c) maintainers
 */
package de.leycm.linguae.exeption;

import lombok.NonNull;

/**
 * Exception thrown when a serializer returns an incompatible type during serialization.
 *
 * <p>This indicates a mismatch between the expected output type and the actual type returned
 * by the serializer, which may be due to a programming error or misconfiguration.</p>
 *
 * @since 1.2.4
 * @author Lennard <a href="mailto:leycm@proton.me">leycm@proton.me</a>
 */
public class IncompatibleMatchException extends IllegalArgumentException {

    /**
     * Constructs a new {@code IncompatibleMatchException} for the specified type.
     *
     * <p>This constructor creates an exception with a default message indicating
     * that a serializer for the given type returned an incompatible result.</p>
     *
     * @param type the expected type that was incompatible; must not be {@code null}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public IncompatibleMatchException(final @NonNull Class<?> type) {
        super("Serializer for type " + type.getName() + " returned incompatible type");
    }

    /**
     * Constructs a new {@code IncompatibleMatchException} for the specified type with cause.
     *
     * <p>This constructor creates an exception with a default message indicating
     * that a serializer for the given type returned an incompatible result,
     * and includes the underlying cause.</p>
     *
     * @param type the expected type that was incompatible; must not be {@code null}
     * @param cause the underlying cause of this exception; must not be {@code null}
     * @throws NullPointerException if {@code type} or {@code cause} is {@code null}
     */
    public IncompatibleMatchException(final @NonNull Class<?> type, final @NonNull Throwable cause) {
        super("Serializer for type " + type.getName() + " returned incompatible type", cause);
    }

}
