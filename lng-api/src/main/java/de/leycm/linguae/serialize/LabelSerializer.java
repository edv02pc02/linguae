/**
 * LECP-LICENSE NOTICE
 * <br><br>
 * This Sourcecode is under the LECP-LICENSE. <br>
 * License at: <a href="https://github.com/leycm/leycm/blob/main/LICENSE">GITHUB</a>
 * <br><br>
 * Copyright (c) LeyCM <a href="mailto:leycm@proton.me">leycm@proton.me</a> l <br>
 * Copyright (c) maintainers <br>
 * Copyright (c) contributors
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
     * @param label the Label to serialize
     * @return the external serialized representation
     * @throws NullPointerException if label is null
     */
    @NonNull T serialize(@NonNull Label label);

    /**
     * Deserializes an external representation back into a {@link Label}.
     *
     * @param serialized the external representation to deserialize
     * @return the deserialized Label instance
     * @throws ParseException if the serialized representation cannot be parsed into a Label
     * @throws NullPointerException if serialized is null
     */
    @NonNull Label deserialize(@NonNull T serialized)
            throws ParseException;

    /**
     * Formats a translated raw string into an external serialized representation.
     *
     * <p>This method behaves similar to {@link #serialize(Label)}, but
     * takes a raw translated string as input instead of a Label instance.</p>
     *
     * @param input the translated/raw input string to format
     * @return the formatted external representation
     * @throws NullPointerException if translated is null
     */
    @NonNull T format(@NonNull String input)
            throws FormatException;
}
