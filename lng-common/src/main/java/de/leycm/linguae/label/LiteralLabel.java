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

/**
 * A literal (non-translatable) {@link Label} implementation that returns
 * the same text regardless of locale.
 *
 * <p>This record stores static text that is returned as-is without any
 * translation lookup. It still supports placeholder mapping and can be
 * converted to components through the provider's serialization system.</p>
 *
 * <p>Literal labels are useful for:</p>
 * <ul>
 *   <li>Dynamic or already-localized strings</li>
 *   <li>System messages that don't need translation</li>
 *   <li>Consistent handling with translatable labels in the same API</li>
 * </ul>
 *
 * <p>The {@code provider} and {@code literal} components of this record are
 * immutable references, but the associated {@link Mappings} instance is
 * mutable and is exposed via {@link #mappings()}. Mutating the mappings will
 * affect how this label is rendered. As a consequence, {@code LiteralLabel}
 * instances are not strictly immutable, and thread-safety depends on how the
 * underlying {@code Mappings} are shared and mutated by callers.</p>
 *
 * @since 1.0.1
 * @author Lennard <a href="mailto:leycm@proton.me">leycm@proton.me</a>
 * @param provider the provider for serialization; must not be {@code null}
 * @param mappings the mappings for placeholder substitution; must not be {@code null}
 * @param literal the static text content; must not be {@code null}
 */
public record LiteralLabel(
        @NonNull LinguaeProvider provider,
        @NonNull Mappings mappings,
        @NonNull String literal
) implements Label {

    /**
     * Compact constructor for validation.
     *
     * @throws NullPointerException if any parameter is {@code null}
     */
    public LiteralLabel { }

    /**
     * Creates a new {@link LiteralLabel} with default empty mappings.
     *
     * <p>This convenience constructor creates a new {@link Mappings} instance
     * associated with the provider, allowing the label to be used immediately
     * without explicit mapping configuration.</p>
     *
     * @param provider the provider for serialization; must not be {@code null}
     * @param literal the static text content; must not be {@code null}
     * @throws NullPointerException if {@code provider} or {@code literal} is {@code null}
     */
    public LiteralLabel(@NonNull LinguaeProvider provider,
                        @NonNull String literal) {
        this(provider, new Mappings(provider), literal);
    }

    /**
     * Returns the literal text for any locale.
     *
     * <p>Unlike {@link LocaleLabel}, this method ignores the locale parameter
     * and always returns the stored literal text, as literal labels are not
     * subject to translation lookup.</p>
     *
     * @param locale the target locale (ignored); must not be {@code null}
     * @return the literal text; never {@code null}
     * @throws NullPointerException if {@code locale} is {@code null}
     */
    @Override
    public @NonNull String in(@NonNull Locale locale) {
        return literal;
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
            return "LiteralLabel[" + literal + "]";
        }
    }

    /**
     * Compares this label to another object for equality.
     *
     * <p>Two {@link LiteralLabel} instances are considered equal when they have
     * the same literal text. The provider and mappings are not considered for
     * equality, as they represent implementation details rather than the
     * semantic identity of the label.</p>
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
        LiteralLabel that = (LiteralLabel) obj;
        return literal.equals(that.literal)
                && provider.equals(that.provider)
                && mappings.equals(that.mappings);
    }

    /**
     * Returns a hash code value for this label.
     *
     * <p>The hash code is based solely on the literal text, consistent with
     * {@link #equals(Object)}. This ensures that labels with the same literal
     * have the same hash code, regardless of their provider or mappings.</p>
     *
     * @return the hash code value for this label
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
        return literal.hashCode();
    }

}
