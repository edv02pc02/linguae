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

@Slf4j
public class CommonLinguaeProvider implements LinguaeProvider {

    @Contract(value = " -> new", pure = true)
    public static @NonNull Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<Class<?>, LabelSerializer<?>> serializerRegistry;
        private MappingRule mappingRule;
        private Locale locale;

        private Builder() {
            this.serializerRegistry = new ConcurrentHashMap<>();
            this.mappingRule = MappingRule.FSTRING;
            this.locale = Locale.US; // may use Locale.getDefault()
        }

        public Builder withSerializer(final @NonNull Class<?> type,
                                      final @NonNull LabelSerializer<?> serializer) {
            this.serializerRegistry.put(type, serializer);
            return this;
        }

        public Builder mappingRule(final @NonNull MappingRule mappingRule) {
            this.mappingRule = mappingRule;
            return this;
        }

        public Builder locale(final @NonNull Locale locale) {
            this.locale = locale;
            return this;
        }

        public CommonLinguaeProvider build(final @NonNull LinguaeSource source) {
            return new CommonLinguaeProvider(serializerRegistry, mappingRule, source, locale);
        }
    }

    private final Map<String, Map<String, String>> translationCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, LabelSerializer<?>> serializerRegistry = new ConcurrentHashMap<>();
    private final MappingRule mappingRule;
    private final LinguaeSource source;
    private final Locale locale;


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

    @Override
    public @NonNull LinguaeSource getSource() {
        return source;
    }

    @Override
    public @NonNull Locale getLocale() {
        return locale;
    }

    @Override
    public @NonNull MappingRule getMappingRule() {
        return mappingRule;
    }

    @Override
    public @NonNull Label createLabel(@NonNull String key, @NonNull Function<Locale, String> fallback) {
        return new LocaleLabel(this, key, fallback);
    }

    @Override
    public @NonNull Label createLiteralLabel(@NonNull String literal) {
        return new LiteralLabel(this, literal);
    }

    @Override
    public @NonNull String translate(final @NonNull String key,
                                     final @NonNull Function<Locale, String> fallback,
                                     final @NonNull Locale locale) {

        final AtomicReference<RuntimeException> exception = new AtomicReference<>();
        final String localeTag = locale.toLanguageTag();
        final Locale defaultLocale = getLocale();
        final String defaultTag = defaultLocale.toLanguageTag();

        Map<String, String> localeMap = translationCache.computeIfAbsent(localeTag, tag ->
                loadTranslationsSafe(locale, exception)
        );

        String value = localeMap.computeIfAbsent(key, k -> {

            if (!locale.equals(defaultLocale)) {
                Map<String, String> defaultMap = translationCache.computeIfAbsent(defaultTag, tag ->
                        loadTranslationsSafe(defaultLocale, exception)
                );

                String defaultValue = defaultMap.get(k);
                if (defaultValue != null) {
                    localeMap.put(k, defaultValue);
                    return defaultValue;
                }
            }

            return fallback.apply(locale);
        });

        if (exception.get() != null) {
            throw exception.get();
        }

        return value;
    }

    @Contract("_, _ -> new")
    private @NonNull Map<String, String> loadTranslationsSafe(Locale locale,
                                                                                       AtomicReference<RuntimeException> exception) {
        try {
            Map<String, String> translations = source.loadLanguage(locale);
            return new ConcurrentHashMap<>(translations);
        } catch (Exception e) {
            exception.set(new RuntimeException(
                    "Failed to load translations for locale: " + locale.toLanguageTag(), e
            ));
            return new ConcurrentHashMap<>();
        }
    }

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

    @Override
    @SuppressWarnings("unchecked") // because: we try catch it wow what a surprise
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

    @Override
    public void clearCache() {
        // note: we can clear sub maps for faster Garbage Collection
        translationCache.clear();
    }

    @Override
    public void clearCache(@NonNull Locale locale) {
        if (translationCache.containsKey(locale.toLanguageTag()))
            translationCache.get(locale.toLanguageTag()).clear();
    }

}
