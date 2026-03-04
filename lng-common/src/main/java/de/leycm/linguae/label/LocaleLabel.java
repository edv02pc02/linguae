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
package de.leycm.linguae.label;

import de.leycm.linguae.Label;
import de.leycm.linguae.LinguaeProvider;
import de.leycm.linguae.mapping.Mappings;
import lombok.NonNull;

import java.util.Locale;
import java.util.function.Function;


/**
 * A translatable {@link Label} implementation that resolves text at runtime
 * based on the current locale and available translation resources.
 *
 * <p>This record stores the translation key, mappings, and fallback function,
 * performing the actual translation lookup when {@link #in(Locale)} is called.
 * The fallback function is used when no translation is found for the requested
 * locale.</p>
 *
 * <p>The record components themselves are final, so the association between
 * provider, mappings, key and fallback does not change once constructed.
 * However, the {@link Mappings} instance referenced by this label is mutable
 * and is shared with the caller. Mutating that {@code Mappings} will affect
 * the behavior of this label, and concurrent modifications must be externally
 * synchronized if thread-safety is required.</p>
 *
 * @since 1.0.1
 * @author Lennard <a href="mailto:leycm@proton.me">leycm@proton.me</a>
 */
public record LocaleLabel(
        @NonNull LinguaeProvider provider,
        @NonNull Mappings mappings,
        @NonNull String key,
        @NonNull Function<Locale, String> fallback
        ) implements Label {

    /**
     * Creates a new {@link LocaleLabel} with default empty mappings.
     *
     * <p>This convenience constructor creates a new {@link Mappings} instance
     * associated with the provider, allowing the label to be used immediately
     * without explicit mapping configuration.</p>
     *
     * @param provider the provider for translation lookup; must not be {@code null}
     * @param key the translation key; must not be {@code null}
     * @param fallback the fallback function for missing translations; must not be {@code null}
     * @throws NullPointerException if any parameter is {@code null}
     */
    public LocaleLabel(@NonNull LinguaeProvider provider, @NonNull String key,
                       @NonNull Function<Locale, String> fallback) {
        this(provider, new Mappings(provider), key, fallback);
    }

    /**
     * Renders this label for the specified locale by performing translation lookup.
     *
     * <p>This method delegates to {@link LinguaeProvider#translate(String, Function, Locale)}
     * to perform the actual translation. The provider will attempt to find a translation
     * for the stored key, falling back to the configured fallback function if needed.</p>
     *
     * @param locale the target locale for translation; must not be {@code null}
     * @return the translated text for the given locale; never {@code null}
     * @throws NullPointerException if {@code locale} is {@code null}
     * @see LinguaeProvider#translate(String, Function, Locale)
     */
    @Override
    public @NonNull String in(@NonNull Locale locale) {
        return provider().translate(key(), fallback, locale);
    }

    /**
     * Returns the serialized string representation of this label.
     *
     * <p>This method delegates to {@link LinguaeProvider#serialize(Label, Class)}
     * to obtain a string representation suitable for storage or transmission.
     * The exact format depends on the provider's serialization configuration.</p>
     *
     * @return the serialized string representation; never {@code null}
     * @see LinguaeProvider#serialize(Label, Class)
     */
    @Override
    public @NonNull String toString() {
        try {
            return provider().serialize(this, String.class);
        } catch (Exception e) {
            return "LocaleLabel[" + key + "]";
        }
    }

    /**
     * Compares this label to another object for equality.
     *
     * <p>Two {@link LocaleLabel} instances are considered equal when they have
     * the same translation key. The provider, mappings, and fallback function
     * are not considered for equality, as they represent implementation details
     * rather than the semantic identity of the label.</p>
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if the given object represents an equivalent label;
     *         {@code false} otherwise
     * @see #hashCode()
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LocaleLabel that = (LocaleLabel) obj;
        return key().equals(that.key())
                && provider.equals(that.provider)
                && mappings.equals(that.mappings);
    }

    /**
     * Returns a hash code value for this label.
     *
     * <p>The hash code is based solely on the translation key, consistent with
     * {@link #equals(Object)}. This ensures that labels with the same key
     * have the same hash code, regardless of their provider or fallback function.</p>
     *
     * @return the hash code value for this label
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
        return key().hashCode();
    }

}
