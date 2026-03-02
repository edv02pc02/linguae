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
package de.leycm.linguae.mapping;

import lombok.NonNull;

import java.util.function.Supplier;
import java.util.regex.Matcher;

/**
 * Represents a single placeholder mapping rule.
 *
 * <p>This record defines a pattern to identify placeholders, the key to match,
 * and the value supplier for replacement.</p>
 *
 * @param rule the pattern used to identify placeholders
 * @param key the placeholder key to match
 * @param value the supplier providing the replacement value
 *
 * @since 1.0.1
 * @author Lennard <a href="mailto:leycm@proton.me">leycm@proton.me</a>
 */
public record Mapping(@NonNull MappingRule rule,
                      @NonNull String key,
                      @NonNull Supplier<String> value) {

    /**
     * Constructs a new Mapping with the specified rule, key, and value supplier.
     *
     * @param rule the pattern used to identify placeholders
     * @param key the placeholder key to match
     * @param value the supplier providing the replacement value
     */
    public Mapping { }

    /**
     * Applies this mapping to the input text, replacing matched placeholders.
     *
     * @param text the input text to process
     * @return the text with placeholders replaced by their mapped values, never null
     * @throws NullPointerException if text is null
     */
    public @NonNull String apply(final @NonNull String text) {
        final Matcher matcher = rule.getPattern().matcher(text);
        final StringBuilder result = new StringBuilder(text.length());

        int lastEnd = 0;
        while (matcher.find()) {
            if (!matcher.group(1).equals(key)) continue;
            result.append(text, lastEnd, matcher.start());
            result.append(value.get());
            lastEnd = matcher.end();
        }

        if (lastEnd == 0) return text;

        result.append(text, lastEnd, text.length());
        return result.toString();
    }
}