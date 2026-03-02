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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonFileSource implements LinguaeSource {

    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>(){}.getType();

    private final String basePath;
    private final Gson gson;
    private final HttpClient client;
    private final boolean remote;

    public JsonFileSource(@NonNull String basePath) {
        this.basePath = basePath.endsWith("/") ? basePath : basePath + "/";
        this.gson = new Gson();
        this.client = HttpClient.newHttpClient();
        this.remote = basePath.startsWith("http://") || basePath.startsWith("https://");
    }

    @Override
    public @NonNull List<Locale> getSupportedLanguages() {
        if (remote) return List.of();

        try {
            Path dir = Paths.get(basePath);
            if (!Files.exists(dir) || !Files.isDirectory(dir)) return List.of();

            try (var stream = Files.list(dir)) {
                return stream
                        .filter(p -> p.getFileName().toString().endsWith(".json"))
                        .map(p -> p.getFileName().toString().replace(".json", ""))
                        .map(name -> name.replace("_", "-"))
                        .map(Locale::forLanguageTag)
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {return List.of();}
    }

    @Override
    public @NonNull Map<String, String> loadLanguage(@NonNull Locale locale) throws Exception {
        String fileName = locale.toLanguageTag().replace("-", "_") + ".json";

        if (remote) {
            return loadRemote(fileName);
        } else {
            return loadLocal(fileName);
        }
    }

    private @NonNull Map<String, String> loadRemote(String fileName) throws Exception {
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

    private @NonNull Map<String, String> loadLocal(String fileName) throws Exception {
        Path path = Paths.get(basePath + fileName);

        if (!Files.exists(path)) {
            return Map.of();
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            Map<String, String> map = gson.fromJson(reader, MAP_TYPE);
            return map != null ? map : Map.of();
        }
    }


    @Override
    public boolean supportsLanguage(@NonNull Locale locale) {
        String fileName = locale.toLanguageTag().replace("-", "_") + ".json";

        if (remote) {
            try {HttpRequest request = HttpRequest.newBuilder()
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
