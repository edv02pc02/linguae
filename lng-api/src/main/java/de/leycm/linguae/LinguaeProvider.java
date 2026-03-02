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

import de.leycm.linguae.exeption.FormatException;
import de.leycm.linguae.mapping.MappingRule;

import de.leycm.linguae.source.LinguaeSource;
import de.leycm.neck.instance.Initializable;
import lombok.NonNull;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;

import java.text.ParseException;
import java.util.Locale;
import java.util.function.Function;

/**
 * Core interface for the Linguae localization and templating system.
 *
 * <p>Provides a singleton instance via {@link #getInstance()} and defines a standard
 * initialization contract. Serves as the main entry point for creating
 * and managing localizable labels and text components.</p>
 *
 * <p>The provider supports multiple placeholder syntaxes, text parsing, and
 * integration with Adventure components. Implementations should be thread-safe
 * and properly initialized before use.</p>
 *
 * @since 1.0.1
 * @author Lennard <a href="mailto:leycm@proton.me">leycm@proton.me</a>
 */
public interface LinguaeProvider extends Initializable {

    /**
     * Returns the singleton instance of the {@code LinguaeProvider}.
     *
     * <p>This method relies on the {@link Initializable#getInstance(Class)}
     * mechanism to retrieve the registered implementation.</p>
     *
     * <p>The provider must be initialized via {@link #onInstall()} before first use
     * to ensure proper configuration and resource loading.</p>
     *
     * @return the singleton instance of {@code LinguaeProvider}; never {@code null}
     * @throws NullPointerException if no implementation is registered
     */
    @Contract(pure = true)
    static @NonNull LinguaeProvider getInstance() {
        return Initializable.getInstance(LinguaeProvider.class);
    }

    /**
     * Returns the source of this provider, which is responsible for loading translation data.
     *
     * <p>The source provides access to translation resources, supports dynamic loading, reloading
     * and serves as the central point for loading localization data within the provider.</p>
     *
     * @return the {@link LinguaeSource} associated with this provider; never {@code null}
     */
    @NonNull LinguaeSource getSource();

    /**
     * Returns the default locale used by this provider for translation and formatting.
     *
     * <p>The default locale is used as a fallback when no specific locale is provided
     * during label resolution or translation requests. It should be a valid locale supported
     * by the provider's source.</p>
     *
     * @return the default {@link Locale} for this provider; never {@code null}
     */
    @NonNull Locale getLocale();

    /**
     * Returns the placeholder {@link MappingRule} used by this provider.
     *
     * <p>This rule is used when adding mappings without specifying an explicit rule,
     * providing a consistent default placeholder syntax across the provider.</p>
     *
     * @return the default mapping rule; never {@code null}
     */
    @NonNull MappingRule getMappingRule();

    /**
     * Parses a string representation into a label instance.
     *
     * <p>The parsing format is implementation-dependent but typically
     * supports both translatable and predefined label syntaxes.
     * This allows for flexible label creation from configuration files or user input.</p>
     *
     * @deprecated since 1.2.0. Use the {@link #deserialize(Object)} method instead.
     * @param parsable the string to parse into a label
     * @return the parsed label instance; never {@code null}
     * @throws ParseException if the string cannot be parsed as a valid label
     * @throws NullPointerException if parsable is {@code null}
     */
    @Deprecated(since = "1.2.0")
    default @NonNull Label createFromString(final @NonNull String parsable)
            throws ParseException {
        return deserialize(parsable);
    }

    /**
     * Creates a default fallback function for the given translation key.
     *
     * <p>The fallback generates a placeholder string in the format {@code [locale.key]}
     * where {@code locale} is the language tag in lowercase and {@code key} is the
     * translation key. This provides a clear indication when a translation is missing.</p>
     *
     * @param key the translation key to create a fallback for; must not be {@code null}
     * @return a fallback function that generates locale-specific placeholder text; never {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    default @NonNull Function<Locale, String> createFallback(final @NonNull String key) {
        return locale -> "[" + locale.toLanguageTag().toLowerCase() + "." + key + "]";
    }

    /**
     * Creates a new translatable label with the specified translation key.
     *
     * <p>Translatable labels resolve their content at runtime based on the current
     * locale and available translation resources. The actual translation lookup
     * is performed when {@link Label#in(Locale)} is called.</p>
     *
     * @param key the translation key used to look up localized text; must not be {@code null}
     * @param fallback the fallback function to generate text when translation is missing; must not be {@code null}
     * @return a new translatable label instance; never {@code null}
     * @throws NullPointerException if {@code key} or {@code fallback} is {@code null}
     */
    @NonNull Label createLabel(@NonNull String key,
                               @NonNull Function<Locale, String> fallback);

    /**
     * Creates a new predefined label with the specified static text.
     *
     * <p>Literal labels use the provided text as-is without translation lookup.
     * They still support placeholder mapping and can be converted to components.</p>
     *
     * @param literal the static text content for the label; must not be {@code null}
     * @return a new predefined label instance; never {@code null}
     * @throws NullPointerException if {@code literal} is {@code null}
     */
    @NonNull Label createLiteralLabel(@NonNull String literal);

    /**
     * Translates a key to a string in the specified locale.
     *
     * @param key the translation key; must not be {@code null}
     * @param fallback the fallback function to generate text when translation is missing; must not be {@code null}
     * @param locale the target locale; must not be {@code null}
     * @return the translated string; never {@code null}
     * @throws NullPointerException if key, fallback, or locale is {@code null}
     */
    @NonNull String translate(@NonNull String key,
                              @NonNull Function<Locale, String> fallback,
                              @NonNull Locale locale);

    /**
     * Serializes a Label into another external type.
     *
     * <p>The serialization format is implementation-dependent and may
     * vary based on the target type and implementation of this type.</p>
     *
     * @param <T> the target type of serialization
     * @param label the label to serialize into another type; must not be {@code null}
     * @param type the target type of the serialized Object; must not be {@code null}
     * @return the serialized object; never {@code null}
     * @throws IllegalArgumentException if the specified type is not supported for serialization
     * @throws NullPointerException if label or type is null
     */
    <T> @NonNull T serialize(@NonNull Label label,
                             @NonNull Class<T> type);

    /**
     * Deserializes a previously serialized Object back into a Label instance.
     *
     * <p>The deserialization format is implementation-dependent and may 
     * vary based on the target type and implementation of this type.</p>
     *
     * @param <T> the type of the serialized object
     * @param serialized the serialized object to deserialize into a Label; must not be {@code null}
     * @return the deserialized label; never {@code null}
     * @throws ParseException if the serialized object cannot be parsed into a Label
     * @throws IllegalArgumentException if the object is not supported for deserialization
     * @throws NullPointerException if serialized is null
     */
    <T> @NonNull Label deserialize(@NonNull T serialized)
            throws ParseException;

    /**
     * Formats a raw string into a serialized external representation.
     *
     * <p>This method behaves similar to {@link #serialize(Label, Class)}, but
     * takes a raw string as input instead of a Label. It's used to provide
     * translations in any format, like {@link Component} and more.</p>
     *
     * @param <T> the target type of the formatted representation
     * @param input the raw string input to format; must not be {@code null}
     * @param type the target type of the formatted representation; must not be {@code null}
     * @return the formatted serialized representation; never {@code null}
     * @throws NullPointerException if {@code input} or {@code type} is {@code null}
     * @throws IllegalArgumentException if the specified type is not supported for formatting
     * @throws FormatException if the input cannot be parsed/formatted
     */
    <T> @NonNull T format(@NonNull String input,
                          @NonNull Class<T> type
    ) throws FormatException;

    /**
     * Clears all cached translations for all languages.
     *
     * <p>This method forces the provider to reload translations from the source
     * on the next translation request, allowing for dynamic updates to translation data.</p>
     */
    void clearCache();

    /**
     * Clears cached translations for the specified language.
     *
     * <p>This method forces the provider to reload translations for the given locale
     * from the source on the next translation request, allowing for dynamic updates
     * to translation data for specific languages.</p>
     *
     * @param locale the {@link Locale} to clear cached translations for, must not be {@code null}
     */
    void clearCache(@NonNull Locale locale);
}