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


public record LocaleLabel(
        @NonNull LinguaeProvider provider,
        @NonNull Mappings mappings,
        @NonNull String key,
        @NonNull Function<Locale, String> fallback
        ) implements Label {

    public LocaleLabel(@NonNull LinguaeProvider provider, @NonNull String key,
                       @NonNull Function<Locale, String> fallback) {
        this(provider, new Mappings(provider), key, fallback);
    }

    @Override
    public @NonNull String in(@NonNull Locale locale) {
        return provider().translate(key(), fallback, locale);
    }

    @Override
    public @NonNull String toString() {
        return provider().serialize(this, String.class);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LocaleLabel that = (LocaleLabel) obj;
        return key().equals(that.key());
    }

    @Override
    public int hashCode() {
        return key().hashCode();
    }

}
