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
import de.leycm.linguae.exeption.IncompatibleMatchException;
import de.leycm.linguae.label.LiteralLabel;
import de.leycm.linguae.label.LocaleLabel;
import de.leycm.linguae.mapping.MappingRule;
import de.leycm.linguae.serialize.LabelSerializer;
import de.leycm.linguae.source.LinguaeSource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;

import java.text.ParseException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Default implementation of {@link LinguaeProvider} providing core localization
 * and templating functionality.
 *
 * <p>This implementation features:</p>
 * <ul>
 *   <li>Thread-safe translation caching with {@link ConcurrentHashMap}</li>
 *   <li>Pluggable serialization via {@link LabelSerializer} registry</li>
 *   <li>Configurable placeholder mapping rules</li>
 *   <li>Automatic fallback to default locale</li>
 *   <li>Builder pattern for configuration</li>
 * </ul>
 *
 * <p>Translation caching is performed per locale and automatically falls back
 * to the configured default locale when a translation is missing.</p>
 *
 * @since 1.0.1
 * @author Lennard <a href="mailto:leycm@proton.me">leycm@proton.me</a>
 */
@Slf4j
public class CommonLinguaeProvider implements LinguaeProvider {

    /**
     * Creates a new builder for configuring and constructing a {@link CommonLinguaeProvider}.
     *
     * <p>The builder provides a fluent API for setting up the provider with custom
     * serializers, mapping rules, and locale configuration.</p>
     *
     * @return a new builder instance; never {@code null}
     */
    @Contract(value = " -> new", pure = true)
    public static @NonNull Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating configured {@link CommonLinguaeProvider} instances.
     *
     * <p>Provides a fluent API for configuring serializers, mapping rules,
     * and locale settings before building the provider.</p>
     */
    public static class Builder {
        private final Map<Class<?>, LabelSerializer<?>> serializerRegistry;
        private MappingRule mappingRule;
        private Locale locale;

        private Builder() {
            this.serializerRegistry = new ConcurrentHashMap<>();
            this.mappingRule = MappingRule.FSTRING;
            this.locale = Locale.US; // may use Locale.getDefault()
        }

        /**
         * Registers a custom serializer for the specified type.
         *
         * @param type the class type this serializer handles; must not be {@code null}
         * @param serializer the serializer implementation; must not be {@code null}
         * @return this builder instance for method chaining
         * @throws NullPointerException if {@code type} or {@code serializer} is {@code null}
         */
        public Builder withSerializer(final @NonNull Class<?> type,
                                      final @NonNull LabelSerializer<?> serializer) {
            this.serializerRegistry.put(type, serializer);
            return this;
        }

        /**
         * Sets the mapping rule for placeholder substitution.
         *
         * @param mappingRule the mapping rule to use; must not be {@code null}
         * @return this builder instance for method chaining
         * @throws NullPointerException if {@code mappingRule} is {@code null}
         */
        public Builder mappingRule(final @NonNull MappingRule mappingRule) {
            this.mappingRule = mappingRule;
            return this;
        }

        /**
         * Sets the default locale for the provider.
         *
         * @param locale the default locale; must not be {@code null}
         * @return this builder instance for method chaining
         * @throws NullPointerException if {@code locale} is {@code null}
         */
        public Builder locale(final @NonNull Locale locale) {
            this.locale = locale;
            return this;
        }

        /**
         * Builds a new {@link CommonLinguaeProvider} with the configured settings.
         *
         * @param source the source for loading translation data; must not be {@code null}
         * @return a new configured provider instance; never {@code null}
         * @throws NullPointerException if {@code source} is {@code null}
         */
        public CommonLinguaeProvider build(final @NonNull LinguaeSource source) {
            return new CommonLinguaeProvider(serializerRegistry, mappingRule, source, locale);
        }

        /**
         * Builds a new {@link CommonLinguaeProvider} with the configured settings.
         *
         * @param source the source for loading translation data; must not be {@code null}
         * @return a new configured provider instance; never {@code null}
         * @throws NullPointerException if {@code source} is {@code null}
         */
        public CommonLinguaeProvider buildWarm(final @NonNull LinguaeSource source, final @NonNull Locale... locales) {
            CommonLinguaeProvider provider = new CommonLinguaeProvider(serializerRegistry, mappingRule, source, locale);
            provider.warmUp(locales);
            return provider;
        }
    }

    private final Map<String, Map<String, String>> translationCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, LabelSerializer<?>> serializerRegistry = new ConcurrentHashMap<>();
    private final MappingRule mappingRule;
    private final LinguaeSource source;
    private final Locale locale;


    /**
     * Creates a new {@link CommonLinguaeProvider} with the specified configuration.
     *
     * @param serializers the map of type serializers; must not be {@code null}
     * @param mappingRule the mapping rule for placeholders; must not be {@code null}
     * @param source the source for loading translations; must not be {@code null}
     * @param locale the default locale; must not be {@code null}
     * @throws NullPointerException if any parameter is {@code null}
     */
    private CommonLinguaeProvider(
            final @NonNull Map<Class<?>, LabelSerializer<?>> serializers,
            final @NonNull MappingRule mappingRule,
            final @NonNull LinguaeSource source,
            final @NonNull Locale locale) {
        this.mappingRule = mappingRule;
        this.serializerRegistry.putAll(serializers);
        this.source = source;
        this.locale = locale;
    }

    /**
     * {@inheritDoc}
     *
     * @return the {@link LinguaeSource} associated with this provider; never {@code null}
     */
    @Override
    public @NonNull LinguaeSource getSource() {
        return source;
    }

    /**
     * {@inheritDoc}
     *
     * @return the default {@link Locale} for this provider; never {@code null}
     */
    @Override
    public @NonNull Locale getLocale() {
        return locale;
    }

    /**
     * {@inheritDoc}
     *
     * @return the default {@link MappingRule} used by this provider; never {@code null}
     */
    @Override
    public @NonNull MappingRule getMappingRule() {
        return mappingRule;
    }

    /**
     * {@inheritDoc}
     *
     * @param key the translation key used to look up localized text; must not be {@code null}
     * @param fallback the fallback function to generate text when translation is missing; must not be {@code null}
     * @return a new translatable label instance; never {@code null}
     * @throws NullPointerException if {@code key} or {@code fallback} is {@code null}
     */
    @Override
    public @NonNull Label createLabel(@NonNull String key,
                                      @NonNull Function<Locale, String> fallback) {
        return new LocaleLabel(this, key, fallback);
    }

    /**
     * {@inheritDoc}
     *
     * @param literal the static text content for the label; must not be {@code null}
     * @return a new literal label instance; never {@code null}
     * @throws NullPointerException if {@code literal} is {@code null}
     */
    @Override
    public @NonNull Label createLiteralLabel(@NonNull String literal) {
        return new LiteralLabel(this, literal);
    }

    /**
     * {@inheritDoc}
     *
     * @param key the translation key; must not be {@code null}
     * @param fallback the fallback function; must not be {@code null}
     * @param locale the target locale; must not be {@code null}
     * @return the translated string; never {@code null}
     * @throws NullPointerException if any parameter is {@code null}
     * @throws NullPointerException if any parameter is {@code null}
     */
    @Override
    public @NonNull String translate(final @NonNull String key,
                                     final @NonNull Function<Locale, String> fallback,
                                     final @NonNull Locale locale) {

        final AtomicReference<RuntimeException> exception = new AtomicReference<>();

        final Locale defaultLocale = getLocale();
        final String localeTag = locale.toLanguageTag();
        final String defaultTag = defaultLocale.toLanguageTag();

        Map<String, String> localeMap = translationCache.computeIfAbsent(
                localeTag,
                tag -> loadTranslationsSafe(locale, exception)
        );

        String value = localeMap.get(key);
        if (value != null) {
            if (exception.get() != null) {
                throwCachedException(localeTag, exception);
            }
            return value;
        }

        if (!locale.equals(defaultLocale)) {

            Map<String, String> defaultMap = translationCache.computeIfAbsent(
                    defaultTag,
                    tag -> loadTranslationsSafe(defaultLocale, exception)
            );

            String defaultValue = defaultMap.get(key);

            if (defaultValue != null) {
                localeMap.putIfAbsent(key, defaultValue);

                if (exception.get() != null) {
                    throwCachedException(localeTag, exception);
                }

                return defaultValue;
            }
        }

        String fallbackValue = fallback.apply(locale);
        localeMap.putIfAbsent(key, fallbackValue);

        if (exception.get() != null) {
            throwCachedException(localeTag, exception);
        }

        return fallbackValue;
    }

    @Contract("_, _ -> fail")
    private void throwCachedException(final @NonNull String localeTag,
                                      final @NonNull AtomicReference<RuntimeException> exception) {
        throw new RuntimeException("Fail to load and cache the language \"" + localeTag +
                        "\" this fail is cached and will not be retried until the cache gets cleared", exception.get());
    }

    /**
     * Loads translations for the specified locale, handling exceptions safely.
     *
     * <p>This method attempts to load translations from the source and captures
     * any exceptions in the provided {@link AtomicReference}. If loading fails,
     * an empty map is returned and the exception is stored for later throwing.</p>
     *
     * @param locale the locale to load translations for; must not be {@code null}
     * @param exception reference to store any loading exception; must not be {@code null}
     * @return a map of translations, empty if loading failed
     * @throws NullPointerException if {@code locale} or {@code exception} is {@code null}
     */
    @Contract("_, _ -> new")
    private @NonNull Map<String, String> loadTranslationsSafe(@NonNull Locale locale,
                                                              @NonNull AtomicReference<RuntimeException> exception) {
        try {
            Map<String, String> translations = source.loadLanguage(locale);
            return new ConcurrentHashMap<>(translations);
        } catch (Exception e) {
            exception.set(new RuntimeException("Failed to load translations for locale: " + locale.toLanguageTag(), e));
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param <T> the target type of serialization
     * @param label the label to serialize; must not be {@code null}
     * @param type the target type of the serialized object; must not be {@code null}
     * @return the serialized object; never {@code null}
     * @throws IllegalArgumentException if the specified type is not supported for serialization
     * @throws NullPointerException if {@code label} or {@code type} is {@code null}
     * @throws IncompatibleMatchException if the serializer returns an incompatible type
     */
    @Override
    @SuppressWarnings("unchecked") // because: we try catch it
    public @NonNull <T> T serialize(final @NonNull Label label,
                                    final @NonNull Class<T> type) {
        if (!serializerRegistry.containsKey(type))
            throw new IllegalArgumentException("Unsupported serialization type: " + type.getName());
        try {
            return (T) serializerRegistry.get(type).serialize(label);
        } catch (ClassCastException e) {
            throw new IncompatibleMatchException(type, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param <T> the type of the serialized object
     * @param serialized the serialized object to deserialize; must not be {@code null}
     * @return the deserialized label; never {@code null}
     * @throws ParseException if the serialized object cannot be parsed into a label
     * @throws IllegalArgumentException if the object type is not supported for deserialization
     * @throws NullPointerException if {@code serialized} is {@code null}
     * @throws IncompatibleMatchException if the serializer returns an incompatible type
     */
    @Override
    @SuppressWarnings("unchecked") // because: we try catch it
    public <T> @NonNull Label deserialize(final @NonNull T serialized)
            throws ParseException {
        if (!serializerRegistry.containsKey(serialized.getClass()))
            throw new IllegalArgumentException("Unsupported serialization type: " + serialized.getClass().getName());
        try {
            LabelSerializer<T> serializer = (LabelSerializer<T>) serializerRegistry.get(serialized.getClass());
            return serializer.deserialize(serialized);
        } catch (ClassCastException e) {
            throw new IncompatibleMatchException(serialized.getClass(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param <T> the target type of the formatted representation
     * @param input the raw string input to format; must not be {@code null}
     * @param type the target type of the formatted representation; must not be {@code null}
     * @return the formatted serialized representation; never {@code null}
     * @throws NullPointerException if {@code input} or {@code type} is {@code null}
     * @throws IllegalArgumentException if the specified type is not supported for formatting
     * @throws FormatException if the input cannot be parsed/formatted
     * @throws IncompatibleMatchException if the formatter returns an incompatible type
     */
    @Override
    @SuppressWarnings("unchecked") // cast is checked and handled with proper exception handling
    public @NonNull <T> T format(final @NonNull String input,
                                 final @NonNull Class<T> type)
            throws FormatException {
        if (!serializerRegistry.containsKey(type))
            throw new IllegalArgumentException("Unsupported serialization type: " + type.getName());
        try {
            return  (T) serializerRegistry.get(type).format(input);
        } catch (ClassCastException e) {
            throw new IncompatibleMatchException(type, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>This method forces the provider to reload translations from the source
     * on the next translation request, allowing for dynamic updates to translation data.</p>
     */
    @Override
    public void clearCache() {
        // note: we can clear sub maps for faster Garbage Collection
        translationCache.clear();
    }

    /**
     * Clears cached translations for the specified locale only.
     *
     * <p>If the locale has no cached translations, this method does nothing.
     * Other locales remain unaffected.</p>
     *
     * @param locale the {@link Locale} to clear cached translations for; must not be {@code null}
     * @throws NullPointerException if {@code locale} is {@code null}
     */
    @Override
    public void clearCache(@NonNull Locale locale) {
        if (translationCache.containsKey(locale.toLanguageTag()))
            translationCache.get(locale.toLanguageTag()).clear();
    }

}
