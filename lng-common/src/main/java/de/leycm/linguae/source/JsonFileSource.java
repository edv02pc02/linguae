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
package de.leycm.linguae.source;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.NonNull;

import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;

/**
 * A {@link LinguaeSource} implementation that loads translations from JSON files.
 *
 * <p>This implementation supports both local file system and remote HTTP sources:
 * <ul>
 *   <li>Local: {@code /path/to/translations/}</li>
 *   <li>Remote: {@code https://example.com/translations/}</li>
 * </ul>
 * </p>
 *
 * <p>Translation files must be named according to the locale language tag,
 * for example: {@code en_US.json}, {@code de_DE.json}, {@code zh_CN.json}.</p>
 *
 * <p>Each JSON file should contain a flat map of translation keys to values:</p>
 * <pre>{@code
 * {
 *   "welcome": "Welcome!",
 *   "goodbye": "Goodbye!",
 *   "user.count": "Users: %count%"
 * }
 * }</pre>
 *
 * <p>For remote sources, only the {@link #loadLanguage(Locale)} and {@link #supportsLanguage(Locale)}
 * methods are supported.</p>
 *
 * @since 1.2.0
 * @author Lennard <a href="mailto:leycm@proton.me">leycm@proton.me</a>
 */
public class JsonFileSource implements LinguaeSource {

    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>(){}.getType();

    private final String basePath;
    private final Gson gson;
    private final HttpClient client;
    private final boolean remote;

    /**
     * Constructs a new {@link JsonFileSource} with the specified base path.
     *
     * <p>The base path can be either a local directory path or a remote HTTP URL.
     * If the path does not end with {@code /}, it will be automatically appended.</p>
     *
     * @param basePath the base path to translation files; must not be {@code null}
     * @throws NullPointerException if {@code basePath} is {@code null}
     */
    public JsonFileSource(@NonNull String basePath) {
        this.basePath = basePath.endsWith("/") ? basePath : basePath + "/";
        this.gson = new Gson();
        this.client = HttpClient.newHttpClient();
        this.remote = basePath.startsWith("http://") || basePath.startsWith("https://");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Loads translations from either a local JSON file or remote HTTP endpoint.
     * The filename is derived from the locale language tag with dashes replaced
     * by underscores (e.g. {@code zh-CN} → {@code zh_CN.json}).</p>
     *
     * @param locale the {@link Locale} to load translations for; must not be {@code null}.
     * @return a map of translation keys to localized strings; empty if the file is not found.
     * @throws Exception if loading fails due to I/O errors, network issues, or JSON parsing errors.
     * @throws NullPointerException if {@code locale} is {@code null}
     */
    @Override
    public @NonNull Map<String, String> loadLanguage(@NonNull Locale locale) throws Exception {
        String fileName = locale.toLanguageTag().replace("-", "_") + ".json";

        if (remote) {return loadRemote(fileName);
        } else {return loadLocal(fileName);}
    }

    /**
     * Loads translations from a remote HTTP endpoint.
     *
     * <p>Sends a GET request to {@code basePath + fileName} and parses the response
     * as JSON. Returns an empty map if the response status is not 200 or the body is empty.</p>
     *
     * @param fileName the JSON filename to load; must not be {@code null}
     * @return a map of translation keys to values; empty if request fails or returns invalid data
     * @throws Exception if the HTTP request fails or JSON parsing encounters an error
     * @throws NullPointerException if {@code fileName} is {@code null}
     */
    private @NonNull Map<String, String> loadRemote(@NonNull String fileName) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(basePath + fileName))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 || response.body() == null || response.body().isEmpty()) {
            return Map.of();
        }

        Map<String, String> map = gson.fromJson(response.body(), MAP_TYPE);
        return map != null ? map : Map.of();
    }

    /**
     * Loads translations from a local JSON file.
     *
     * <p>Reads the file from {@code basePath + fileName} and parses it as JSON.
     * Returns an empty map if the file does not exist.</p>
     *
     * @param fileName the JSON filename to load; must not be {@code null}
     * @return a map of translation keys to values; empty if file not found
     * @throws Exception if file reading fails or JSON parsing encounters an error
     * @throws NullPointerException if {@code fileName} is {@code null}
     */
    private @NonNull Map<String, String> loadLocal(@NonNull String fileName) throws Exception {
        Path path = Paths.get(basePath + fileName);

        if (!Files.exists(path)) {
            return Map.of();
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            Map<String, String> map = gson.fromJson(reader, MAP_TYPE);
            return map != null ? map : Map.of();
        }
    }


    /**
     * {@inheritDoc}
     *
     * <p>For remote sources, sends a HEAD request to check if the file exists.
     * For local sources, checks if the file exists on the file system.</p>
     *
     * @param locale the {@link Locale} to check; must not be {@code null}
     * @return {@code true} if the translation file exists; {@code false} otherwise
     * @throws NullPointerException if {@code locale} is {@code null}
     */
    @Override
    public boolean supportsLanguage(@NonNull Locale locale) {
        String fileName = locale.toLanguageTag().replace("-", "_") + ".json";

        if (remote) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(basePath + fileName))
                        .method("HEAD", HttpRequest.BodyPublishers.noBody())
                        .build();

                HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

                return response.statusCode() == 200;
            } catch (Exception e) {
                return false;
            }
        }

        Path path = Paths.get(basePath + fileName);
        return Files.exists(path);
    }
}
