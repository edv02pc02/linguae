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

import java.util.regex.Pattern;

/**
 * Defines a pattern for placeholder mapping with prefix and suffix delimiters.
 *
 * <p>Provides commonly used placeholder patterns as static constants and compiles
 * efficient regex patterns for placeholder detection and replacement.</p>
 *
 * <p>Instances are immutable and thread-safe.</p>
 *
 * @since 1.0.1
 * @author Lennard <a href="mailto:leycm@proton.me">leycm@proton.me</a>
 */
public class MappingRule {

    /**
     * Dollar-style placeholder pattern: {@code ${variable}}
     */
    public static final @NonNull MappingRule DOLLAR = new MappingRule("${", "}");

    /**
     * Percent-style placeholder pattern: {@code %variable%}
     */
    public static final @NonNull MappingRule PERCENT = new MappingRule("%", "%");

    /**
     * F-string style placeholder pattern: {@code %variable}
     */
    public static final @NonNull MappingRule FSTRING = new MappingRule("%", "");

    /**
     * Curly brace placeholder pattern: {@code {{variable}}}
     */
    public static final @NonNull MappingRule CURLY = new MappingRule("{{", "}}");

    /**
     * MiniMessage style placeholder pattern: {@code <var:variable>}
     */
    public static final @NonNull MappingRule MINI_MESSAGE = new MappingRule("<var:", ">");

    private final String prefix;
    private final String suffix;
    private final Pattern pattern;

    /**
     * Constructs a new {@link MappingRule} with specified prefix and suffix.
     *
     * <p>The pattern is compiled to efficiently match placeholders in the format:
     * {@code prefix + content + suffix}. The content is captured as a group and
     * cannot contain the first character of the suffix for proper termination.</p>
     *
     * @param prefix the prefix delimiter for placeholders; must not be {@code null}
     * @param suffix the suffix delimiter for placeholders; must not be {@code null}
     * @throws NullPointerException if {@code prefix} or {@code suffix} is {@code null}
     */
    public MappingRule(final @NonNull String prefix, final @NonNull String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
        if (suffix.isEmpty()) {
            this.pattern = Pattern.compile(Pattern.quote(prefix) + "([A-Za-z0-9_]+)");
        } else {
            this.pattern = Pattern.compile(Pattern.quote(prefix)
                            + "([^" + Pattern.quote(suffix.substring(0, 1)) + "]+)"
                            + Pattern.quote(suffix));
        }
    }

    /**
     * Returns the prefix delimiter for this mapping rule.
     *
     * @return the prefix delimiter; never {@code null}
     */
    public @NonNull String getPrefix() {
        return prefix;
    }

    /**
     * Returns the suffix delimiter for this mapping rule.
     *
     * @return the suffix delimiter; never {@code null}
     */
    public @NonNull String getSuffix() {
        return suffix;
    }

    /**
     * Returns the compiled regex pattern for this mapping rule.
     *
     * <p>The pattern is optimized for matching placeholders that follow the
     * prefix-content-suffix structure defined by this rule.</p>
     *
     * @return the compiled regex pattern; never {@code null}
     */
    public @NonNull Pattern getPattern() {
        return pattern;
    }
}