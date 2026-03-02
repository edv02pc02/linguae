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
 * Exception thrown when a formatting or serialization process fails.
 *
 * <p>This exception indicates that a given input (e.g. String, Label, or
 * external representation) could not be formatted or converted into the
 * expected external format.</p>
 *
 * <p>Typical use cases include:</p>
 * <ul>
 *   <li>Invalid input syntax</li>
 *   <li>Unsupported format types</li>
 *   <li>Malformed translation strings</li>
 *   <li>Serialization/formatting pipeline errors</li>
 * </ul>
 *
 * <p>This is a {@link RuntimeException}, meaning it represents a
 * non-recoverable formatting error in most application flows.</p>
 *
 * <p>It is commonly used in serializer/formatter implementations
 * where formatting failures are considered programmer or data errors.</p>
 *
 * @since 1.2.0
 * @author Lennard <a href="mailto:leycm@proton.me">leycm@proton.me</a>
 */
public class FormatException extends RuntimeException {

    /**
     * Constructs a new {@code FormatException} with no detail message.
     */
    public FormatException() {
        super();
    }

    /**
     * Constructs a new {@code FormatException} with the specified detail message.
     *
     * @param message the detail message
     */
    public FormatException(final @NonNull String message) {
        super(message);
    }

    /**
     * Constructs a new {@code FormatException} with the specified detail message
     * and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public FormatException(final @NonNull String message, final @NonNull Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code FormatException} with the specified cause.
     *
     * @param cause the cause of this exception
     */
    public FormatException(final @NonNull Throwable cause) {
        super(cause);
    }

    /**
     * Advanced constructor allowing suppression and stack trace writability control.
     *
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    protected FormatException(final @NonNull String message,
                              final @NonNull Throwable cause,
                              final boolean enableSuppression,
                              final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
