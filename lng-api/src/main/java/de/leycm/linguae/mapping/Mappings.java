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

import de.leycm.linguae.LinguaeProvider;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A thread-safe, immutable container for placeholder mappings.
 *
 * <p>Facilitates string transformation by replacing placeholders with their
 * corresponding values. Supports multiple mapping rules and provides a fluent
 * API for building mappings.</p>
 *
 * <p>Instances are immutable - all modification operations return new instances.</p>
 *
 * @since 1.0.1
 * @author Lennard <a href="mailto:leycm@proton.me">leycm@proton.me</a>
 * @param mappings a List of Mapping objects to start with
 */
public record Mappings(@NonNull List<Mapping> mappings,
                       @NonNull LinguaeProvider provider) {

    /**
     * Constructs an empty Mappings with no mappings.
     */
    public Mappings() {
        this(new ArrayList<>());
    }

    /**
     * Constructs an empty Mappings with no mappings but a special {@link LinguaeProvider}.
     *
     * <p>The provided list is copied to ensure immutability.</p>
     *
     * @param provider the provider to resolve Rules
     * @throws NullPointerException if mappings is null
     */
    public Mappings(final @NonNull LinguaeProvider provider) {
        this(new ArrayList<>(), provider);
    }

    /**
     * Constructs a Mappings with the specified mappings.
     *
     * <p>The provided list is copied to ensure immutability.</p>
     *
     * @param mappings the initial mappings to include
     * @throws NullPointerException if mappings is null
     */
    public Mappings(final @NonNull List<Mapping> mappings) {
        this(mappings, LinguaeProvider.getInstance());
    }

    /**
     * Constructs a Mappings with the specified mappings.
     *
     * <p>The provided list is copied to ensure immutability.</p>
     *
     * @param mappings the initial mappings to include
     * @param provider the provider to resolve Rules
     * @throws NullPointerException if mappings or provider is null
     */
    public Mappings(final @NonNull List<Mapping> mappings,
                    final @NonNull LinguaeProvider provider) {
        this.mappings = new ArrayList<>(mappings);
        this.provider = provider;
    }

    /**
     * Adds a new mapping using the default placeholder rule from the default provider.
     *
     * <p>This is a convenience method that uses the singleton LinguaeProvider instance
     * and its default placeholder rule.</p>
     *
     * @param key the placeholder key to replace
     * @param value the value to substitute
     * @return a new Mappings instance with the added mapping, never null
     * @throws NullPointerException if key or value is null
     */
    public @NonNull Mappings add(final @NonNull String key,
                                 final @NonNull Object value) {
        return add(provider, key, () -> value);
    }

    /**
     * Adds a new mapping using the default placeholder rule from the default provider.
     *
     * <p>This is a convenience method that uses the singleton LinguaeProvider instance
     * and its default placeholder rule.</p>
     *
     * @param key the placeholder key to replace
     * @param supplier the supplier providing the value to substitute
     * @return a new Mappings instance with the added mapping, never null
     * @throws NullPointerException if key or value is null
     */
    public @NonNull Mappings add(final @NonNull String key,
                                 final @NonNull Supplier<Object> supplier) {
        return add(provider, key, supplier);
    }

    /**
     * Adds a new mapping using the default placeholder rule from the specified provider.
     *
     * @param provider the {@link LinguaeProvider} to get the default placeholder rule from
     * @param key the placeholder key to replace
     * @param supplier the supplier providing the value to substitute
     * @return a new Mappings instance with the added mapping, never null
     * @throws NullPointerException if provider, key, or value is null
     */
    public @NonNull Mappings add(final @NonNull LinguaeProvider provider,
                                 final @NonNull String key,
                                 final @NonNull Supplier<Object> supplier) {
        return add(provider.getMappingRule(), key, supplier);
    }

    /**
     * Adds a new mapping with the specified rule, key, and value.
     *
     * <p>The value is converted to string using {@link String#valueOf(Object)}.
     * Returns a new Mappings instance, leaving the original unchanged.</p>
     *
     * @param rule the mapping rule to use for this placeholder
     * @param key the placeholder key to replace
     * @param supplier the supplier providing the value to substitute
     * @return a new Mappings instance with the added mapping, never null
     * @throws NullPointerException if rule, key, or value is null
     */
    public @NonNull Mappings add(final @NonNull MappingRule rule,
                                 final @NonNull String key,
                                 final @NonNull Supplier<Object> supplier) {
        return add(new Mapping(rule, key, () -> String.valueOf(supplier.get())));
    }

    /**
     * Adds a new mapping with the specified rule, key, and value.
     *
     * <p>The value is converted to string using {@link String#valueOf(Object)}.
     * Returns a new Mappings instance, leaving the original unchanged.</p>
     *
     * @param mapping the mapping to use
     * @return a new Mappings instance with the added mapping, never null
     * @throws NullPointerException if rule, key, or value is null
     */
    public @NonNull Mappings add(final @NonNull Mapping mapping) {
        mappings.add(mapping);
        return this;
    }

    /**
     * Applies all mappings to the input text, replacing placeholders with their values.
     *
     * <p>Mappings are applied in the order they were added. If no mappings are present,
     * the original text is returned unchanged.</p>
     *
     * @param text the input text containing placeholders
     * @return the text with all placeholders replaced by their mapped values, never null
     * @throws NullPointerException if text is null
     */
    public @NonNull String apply(final @NonNull String text) {
        if (mappings.isEmpty()) return text;

        String result = text;

        for (final Mapping mapping : mappings)
            result = mapping.apply(result);

        return result;
    }

    /**
     * Returns the number of mappings in this mapper.
     *
     * @return the number of mappings, never negative
     */
    public int size() {
        return mappings.size();
    }

    /**
     * Checks if this mapper contains no mappings.
     *
     * @return true if there are no mappings, false otherwise
     */
    public boolean isEmpty() {
        return mappings.isEmpty();
    }
}