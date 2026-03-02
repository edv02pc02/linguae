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
package de.leycm.linguae.serialize;

import de.leycm.linguae.Label;
import de.leycm.linguae.exeption.FormatException;
import lombok.NonNull;

import java.text.ParseException;

/**
 * Generic serializer interface for converting between {@link Label}
 * instances and external representations.
 *
 * <p>This interface defines a bidirectional transformation model:</p>
 *
 * <ul>
 *   <li>{@link #serialize(Label)}: {@code Label -> external format}</li>
 *   <li>{@link #deserialize(Object)}: {@code external format -> Label}</li>
 *   <li>{@link #format(String)}: {@code String -> external format}</li>
 * </ul>
 *
 * <p>The concrete external format is implementation-dependent and may be
 * represented as a String, JSON structure, binary data, or any other type.</p>
 *
 * <p>Implementations are expected to be deterministic, thread-safe if used
 * in concurrent contexts, and consistent between serialization and
 * deserialization.</p>
 *
 * @since 1.2.0
 * @author Lennard <a href="mailto:leycm@proton.me">leycm@proton.me</a>
 * @param <T> the external representation type (e.g. String, byte[], JsonObject)
 */
public interface LabelSerializer<T> {

    /**
     * Serializes a {@link Label} into an external representation.
     *
     * <p>The resulting format is implementation-dependent and may vary
     * depending on the concrete serializer implementation.</p>
     *
     * @param label the {@link Label} to serialize; must not be {@code null}
     * @return the external serialized representation; never {@code null}
     * @throws NullPointerException if {@code label} is {@code null}
     */
    @NonNull T serialize(@NonNull Label label);

    /**
     * Deserializes an external representation back into a {@link Label}.
     *
     * @param serialized the external representation to deserialize; must not be {@code null}
     * @return the deserialized {@link Label} instance; never {@code null}
     * @throws ParseException if the serialized representation cannot be parsed into a {@link Label}
     * @throws NullPointerException if {@code serialized} is {@code null}
     */
    @NonNull Label deserialize(@NonNull T serialized)
            throws ParseException;

    /**
     * Formats a translated raw string into an external serialized representation.
     *
     * <p>This method behaves similarly to {@link #serialize(Label)}, but
     * takes a raw translated string as input instead of a {@link Label} instance.</p>
     *
     * @param input the translated/raw input string to format; must not be {@code null}
     * @return the formatted external representation; never {@code null}
     * @throws NullPointerException if {@code input} is {@code null}
     * @throws FormatException if the input cannot be formatted
     */
    @NonNull T format(@NonNull String input)
            throws FormatException;
}
