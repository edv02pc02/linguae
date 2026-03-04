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
package de.leycm.linguae;

import de.leycm.linguae.mapping.Mapping;
import de.leycm.linguae.mapping.MappingRule;
import de.leycm.linguae.mapping.Mappings;

import lombok.NonNull;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A localizable text element that can be rendered into a string for a given {@link Locale}.
 *
 * <p>Labels support translation via resource bundles and placeholder mapping.
 * Use {@link #of(String)} for translatable labels or {@link #literal(String)} for static text.</p>
 *
 * @see LinguaeProvider
 * @see Mappings
 * @since 1.0.1
 * @author Lennard <a href="mailto:leycm@proton.me">leycm@proton.me</a>
 */
public interface Label {

    /**
     * Creates a translatable label for the given key using the default {@link LinguaeProvider}.
     *
     * <p>When no translation is found for the current locale, the provider's default
     * fallback strategy is applied (see {@link LinguaeProvider#createFallback(String)}).</p>
     *
     * @param key the translation key; must not be {@code null}
     * @return a translatable label; never {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    static @NonNull Label of(final @NonNull String key) {
        return of(LinguaeProvider.getInstance(), key);
    }

    /**
     * Creates a translatable label for the given key using the default {@link LinguaeProvider},
     * with a static string used as the fallback when no translation is available.
     *
     * @param key      the translation key; must not be {@code null}
     * @param fallback the text returned when no translation is found; must not be {@code null}
     * @return a translatable label; never {@code null}
     * @throws NullPointerException if {@code key} or {@code fallback} is {@code null}
     */
    static @NonNull Label of(final @NonNull String key,
                             final @NonNull String fallback) {
        return of(LinguaeProvider.getInstance(), key, fallback);
    }

    /**
     * Creates a translatable label for the given key using the default {@link LinguaeProvider},
     * with a locale-aware function used as the fallback when no translation is available.
     *
     * @param key      the translation key; must not be {@code null}
     * @param fallback a function that receives the requested {@link Locale} and returns
     *                 the fallback text; must not be {@code null}
     * @return a translatable label; never {@code null}
     * @throws NullPointerException if {@code key} or {@code fallback} is {@code null}
     */
    static @NonNull Label of(final @NonNull String key,
                             final @NonNull Function<Locale, String> fallback) {
        return of(LinguaeProvider.getInstance(), key, fallback);
    }

    /**
     * Creates a translatable label for the given key using the specified {@link LinguaeProvider}.
     *
     * <p>The provider's default fallback strategy is applied when no translation is found
     * (see {@link LinguaeProvider#createFallback(String)}).</p>
     *
     * @param provider the provider responsible for translation lookup; must not be {@code null}
     * @param key      the translation key; must not be {@code null}
     * @return a translatable label; never {@code null}
     * @throws NullPointerException if {@code provider} or {@code key} is {@code null}
     */
    static @NonNull Label of(final @NonNull LinguaeProvider provider,
                             final @NonNull String key) {
        return provider.createLabel(key, provider.createFallback(key));
    }

    /**
     * Creates a translatable label for the given key using the specified {@link LinguaeProvider},
     * with a static string used as the fallback when no translation is available.
     *
     * @param provider the provider responsible for translation lookup; must not be {@code null}
     * @param key      the translation key; must not be {@code null}
     * @param fallback the text returned when no translation is found; must not be {@code null}
     * @return a translatable label; never {@code null}
     * @throws NullPointerException if {@code provider}, {@code key}, or {@code fallback} is {@code null}
     */
    static @NonNull Label of(final @NonNull LinguaeProvider provider,
                             final @NonNull String key,
                             final @NonNull String fallback) {
        return provider.createLabel(key, locale -> fallback);
    }

    /**
     * Creates a translatable label for the given key using the specified {@link LinguaeProvider},
     * with a locale-aware function used as the fallback when no translation is available.
     *
     * @param provider the provider responsible for translation lookup; must not be {@code null}
     * @param key      the translation key; must not be {@code null}
     * @param fallback a function that receives the requested {@link Locale} and returns
     *                 the fallback text; must not be {@code null}
     * @return a translatable label; never {@code null}
     * @throws NullPointerException if {@code provider}, {@code key}, or {@code fallback} is {@code null}
     */
    static @NonNull Label of(final @NonNull LinguaeProvider provider,
                             final @NonNull String key,
                             final @NonNull Function<Locale, String> fallback) {
        return provider.createLabel(key, fallback);
    }

    /**
     * Creates a literal (non-translatable) label with the given static text,
     * using the default {@link LinguaeProvider}.
     *
     * <p>Literal labels are returned as-is and are never looked up in a resource bundle.
     * They are useful for dynamic or already-localized strings that should still
     * participate in the {@link Label} abstraction (e.g. for consistent component handling).</p>
     *
     * @param literal the static text content; must not be {@code null}
     * @return a literal label; never {@code null}
     * @throws NullPointerException if {@code literal} is {@code null}
     */
    static @NonNull Label literal(final @NonNull String literal) {
        return literal(LinguaeProvider.getInstance(), literal);
    }

    /**
     * Creates a literal (non-translatable) label with the given static text,
     * using the specified {@link LinguaeProvider}.
     *
     * @param provider the provider to associate with this label; must not be {@code null}
     * @param literal  the static text content; must not be {@code null}
     * @return a literal label; never {@code null}
     * @throws NullPointerException if {@code provider} or {@code literal} is {@code null}
     */
    static @NonNull Label literal(final @NonNull LinguaeProvider provider,
                                  final @NonNull String literal) {
        return provider.createLiteralLabel(literal);
    }

    /**
     * Returns the {@link LinguaeProvider} associated with this label.
     *
     * @return the provider that created this label; never {@code null}
     */
    @NonNull LinguaeProvider provider();

    /**
     * Returns the {@link Mappings} currently registered on this label.
     *
     * <p>Mappings define placeholder substitutions applied by {@link #mapped(Locale)}
     * and related methods.</p>
     *
     * @return the current mappings; never {@code null}
     */
    @NonNull Mappings mappings();

    /**
     * Registers a placeholder mapping using the provider's default {@link MappingRule}.
     *
     * <p>The supplied {@code value} is converted to a string via {@link String#valueOf(Object)}
     * and substituted wherever the placeholder for {@code key} appears in the rendered text.</p>
     *
     * @param key   the placeholder key to replace; must not be {@code null}
     * @param value the substitution value; must not be {@code null}
     * @return this label instance; never {@code null}
     * @throws NullPointerException if {@code key} or {@code value} is {@code null}
     */
    default @NonNull Label mapTo(final @NonNull String key,
                                       final @NonNull Object value) {
        return mapTo(key, () -> value);
    }

    /**
     * Registers a placeholder mapping using the provider's default {@link MappingRule},
     * with the substitution value supplied lazily.
     *
     * <p>The supplier is invoked each time the label is rendered, allowing dynamic values.</p>
     *
     * @param key      the placeholder key to replace; must not be {@code null}
     * @param supplier a supplier for the substitution value; must not be {@code null}
     * @return this label instance; never {@code null}
     * @throws NullPointerException if {@code key} or {@code supplier} is {@code null}
     */
    default @NonNull Label mapTo(final @NonNull String key,
                                       final @NonNull Supplier<Object> supplier) {
        return mapTo(provider().getMappingRule(), key, supplier);
    }

    /**
     * Registers a placeholder mapping with an explicit {@link MappingRule} and a lazy supplier.
     *
     * <p>Use this overload when a non-default placeholder syntax is required (e.g. a custom
     * delimiter pattern).</p>
     *
     * @param rule     the rule that defines how placeholders are matched and replaced;
     *                 must not be {@code null}
     * @param key      the placeholder key to replace; must not be {@code null}
     * @param supplier a supplier for the substitution value; must not be {@code null}
     * @return this label instance; never {@code null}
     * @throws NullPointerException if {@code rule}, {@code key}, or {@code supplier} is {@code null}
     */
    default @NonNull Label mapTo(final @NonNull MappingRule rule,
                                       final @NonNull String key,
                                       final @NonNull Supplier<Object> supplier) {
        return mapTo(new Mapping(rule, key, () -> String.valueOf(supplier.get())));
    }

    /**
     * Registers a pre-built {@link Mapping} on this label.
     *
     * @param mapping the mapping to add; must not be {@code null}
     * @return this label instance; never {@code null}
     * @throws NullPointerException if {@code mapping} is {@code null}
     */
    default @NonNull Label mapTo(final @NonNull Mapping mapping) {
        mappings().add(mapping);
        return this;
    }

    /**
     * Renders this label using the provider's current locale, <em>without</em> applying mappings.
     *
     * <p>For translatable labels this performs a resource-bundle lookup.
     * For literal labels it returns the static text.</p>
     *
     * @return the raw rendered string; never {@code null}
     * @see #mapped()
     */
    default @NonNull String in() {
        return in(provider().getLocale());
    }

    /**
     * Renders this label for the given locale, <em>without</em> applying mappings.
     *
     * <p>For translatable labels this performs a resource-bundle lookup.
     * For literal labels it returns the static text.</p>
     *
     * @param locale the target locale; must not be {@code null}
     * @return the raw rendered string; never {@code null}
     * @throws NullPointerException if {@code locale} is {@code null}
     * @see #mapped(Locale)
     */
    @NonNull String in(@NonNull Locale locale);

    /**
     * Renders this label using the provider's current locale and converts the result
     * to the specified type via {@link LinguaeProvider#format(String, Class)},
     * <em>without</em> applying mappings.
     *
     * @param <T>  the target type
     * @param type the class to convert the rendered string into; must not be {@code null}
     * @return the rendered and converted value; never {@code null}
     * @throws NullPointerException if {@code type} is {@code null}
     * @see #mapped(Class)
     */
    default <T> @NonNull T in(final @NonNull Class<T> type) {
        return in(provider().getLocale(), type);
    }

    /**
     * Renders this label for the given locale and converts the result to the specified type
     * via {@link LinguaeProvider#format(String, Class)}, <em>without</em> applying mappings.
     *
     * @param <T>    the target type
     * @param locale the target locale; must not be {@code null}
     * @param type   the class to convert the rendered string into; must not be {@code null}
     * @return the rendered and converted value; never {@code null}
     * @throws NullPointerException if {@code locale} or {@code type} is {@code null}
     * @see #mapped(Locale, Class)
     */
    default <T> @NonNull T in(@NonNull Locale locale, final @NonNull Class<T> type) {
        return provider().format(in(locale), type);
    }

    /**
     * Renders this label using the provider's current locale and applies all registered mappings.
     *
     * <p>Equivalent to calling {@link #mapped(Locale)} with {@link LinguaeProvider#getLocale()}.</p>
     *
     * @return the rendered string with placeholder substitutions applied; never {@code null}
     * @see #in()
     */
    default @NonNull String mapped() {
        return mapped(provider().getLocale());
    }

    /**
     * Renders this label for the given locale and applies all registered mappings.
     *
     * <p>The rendering process is: translate (or return literal) → apply {@link Mappings}.</p>
     *
     * @param locale the target locale; must not be {@code null}
     * @return the rendered string with placeholder substitutions applied; never {@code null}
     * @throws NullPointerException if {@code locale} is {@code null}
     * @see #in(Locale)
     */
    default @NonNull String mapped(final @NonNull Locale locale) {
        return mappings().apply(in(locale));
    }

    /**
     * Renders this label using the provider's current locale, applies all registered mappings,
     * and converts the result to the specified type via {@link LinguaeProvider#format(String, Class)}.
     *
     * @param <T>  the target type
     * @param type the class to convert the rendered string into; must not be {@code null}
     * @return the rendered, mapped, and converted value; never {@code null}
     * @throws NullPointerException if {@code type} is {@code null}
     * @see #in(Class)
     */
    default <T> @NonNull T mapped(final @NonNull Class<T> type) {
        return mapped(provider().getLocale(), type);
    }

    /**
     * Renders this label for the given locale, applies all registered mappings,
     * and converts the result to the specified type via {@link LinguaeProvider#format(String, Class)}.
     *
     * @param <T>    the target type
     * @param locale the target locale; must not be {@code null}
     * @param type   the class to convert the rendered string into; must not be {@code null}
     * @return the rendered, mapped, and converted value; never {@code null}
     * @throws NullPointerException if {@code locale} or {@code type} is {@code null}
     * @see #in(Locale, Class)
     */
    default <T> @NonNull T mapped(final @NonNull Locale locale, final @NonNull Class<T> type) {
        return provider().format(mappings().apply(in(locale)), type);
    }

    // -------------------------------------------------------------------------
    // Instance API – serialization & object contract
    // -------------------------------------------------------------------------

    /**
     * Serializes this label to a {@link String} using the provider's serialization strategy
     * (see {@link LinguaeProvider#serialize(Label, Class)}).
     *
     * <p>This differs from {@link #in()} in that the provider may apply additional
     * post-processing (e.g. escaping, wrapping) beyond a plain locale lookup.</p>
     *
     * @return the serialized string representation; never {@code null}
     */
    default @NonNull String asString() {
        return provider().serialize(this, String.class);
    }

    /**
     * Returns a human-readable representation of this label, useful for debugging.
     *
     * <p>The format is implementation-defined but should convey the label's type
     * (translatable vs. literal) and its key or content.</p>
     *
     * @return a non-{@code null} string representation
     */
    @Override
    @NonNull String toString();

    /**
     * Indicates whether this label is equal to another object.
     *
     * <p>Two labels are considered equal when they share the same provider, key (or literal
     * content), and registered mappings. Implementations must be consistent with
     * {@link #hashCode()}.</p>
     *
     * @param obj the reference object with which to compare; may be {@code null}
     * @return {@code true} if the given object represents an equivalent label
     */
    @Override
    boolean equals(Object obj);

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     *
     * <p>Equal labels must produce equal hash codes.</p>
     *
     * @return the hash code value for this label
     */
    @Override
    int hashCode();
}