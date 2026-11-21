/*-
 * !--
 * For support and inquiries regarding this library, please contact:
 *   soporte@kanopus.cl
 *
 * Project website:
 *   https://www.kanopus.cl
 * %%
 * Copyright (C) 2025 Pablo Díaz Saavedra
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * --!
 */
package cl.kanopus.avatars;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Read-only access to the avatar images bundled in this artifact.
 *
 * <p>
 * The catalog is built from {@code kanopus/avatars/index.txt}, which lists one classpath resource path per line (for example {@code kanopus/avatars/vikings/vikings_r1_c1.png}). The category of an avatar is the directory right below {@code kanopus/avatars/}.
 *
 * <pre>{@code
 * AvatarCatalog catalog = AvatarCatalog.load();
 * String path = catalog.pick("user-42");          // deterministic avatar for a user id
 * try (InputStream png = catalog.open(path)) { ... }
 * }</pre>
 */
public final class AvatarCatalog {

    /** Classpath location of the index file. */
    public static final String INDEX = "kanopus/avatars/index.txt";

    private static final String PREFIX = "kanopus/avatars/";

    private final ClassLoader classLoader;
    private final List<String> paths;
    private final Map<String, List<String>> byCategory;

    private AvatarCatalog(ClassLoader classLoader, List<String> paths) {
        this.classLoader = classLoader;
        this.paths = List.copyOf(paths);
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (String p : this.paths) {
            map.computeIfAbsent(categoryOf(p), k -> new ArrayList<>()).add(p);
        }
        map.replaceAll((k, v) -> List.copyOf(v));
        this.byCategory = Collections.unmodifiableMap(map);
    }

    /** Loads the catalog using this class's class loader. */
    public static AvatarCatalog load() {
        return load(AvatarCatalog.class.getClassLoader());
    }

    /** Loads the catalog from the given class loader. */
    public static AvatarCatalog load(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "classLoader");
        try (InputStream in = classLoader.getResourceAsStream(INDEX)) {
            if (in == null) {
                throw new IllegalStateException("Avatar index not found on classpath: " + INDEX);
            }
            List<String> paths = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String p = line.strip();
                    if (!p.isEmpty() && !p.startsWith("#") && p.startsWith(PREFIX) && !p.contains("..")) {
                        paths.add(p);
                    }
                }
            }
            return new AvatarCatalog(classLoader, paths);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + INDEX, e);
        }
    }

    /** All avatar resource paths, in index order. */
    public List<String> all() {
        return paths;
    }

    /** Category names (sorted). */
    public SortedSet<String> categories() {
        return Collections.unmodifiableSortedSet(new TreeSet<>(byCategory.keySet()));
    }

    /** Avatar paths of one category, empty if the category does not exist. */
    public List<String> byCategory(String category) {
        return byCategory.getOrDefault(category, List.of());
    }

    /** Number of avatars. */
    public int size() {
        return paths.size();
    }

    /**
     * Picks a stable avatar for a key (user id, e-mail, ...). The same key always returns the same path while the index does not change.
     */
    public String pick(String key) {
        return pick(key, paths);
    }

    /** Picks a stable avatar for a key within one category. */
    public String pick(String key, String category) {
        List<String> candidates = byCategory(category);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("Unknown avatar category: " + category);
        }
        return pick(key, candidates);
    }

    /** Opens an avatar resource listed in the catalog. The caller must close the stream. */
    public InputStream open(String path) {
        if (!paths.contains(path)) {
            throw new IllegalArgumentException("Not an avatar of this catalog: " + path);
        }
        InputStream in = classLoader.getResourceAsStream(path);
        if (in == null) {
            throw new IllegalStateException("Avatar listed in index but missing: " + path);
        }
        return in;
    }

    /** Reads an avatar fully into memory. */
    public byte[] readBytes(String path) {
        try (InputStream in = open(path)) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read avatar " + path, e);
        }
    }

    /** Looks up a path by its file name (e.g. {@code vikings_r1_c1.png}). */
    public Optional<String> findByFileName(String fileName) {
        return paths.stream().filter(p -> p.endsWith("/" + fileName)).findFirst();
    }

    private static String pick(String key, List<String> candidates) {
        Objects.requireNonNull(key, "key");
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Avatar catalog is empty");
        }
        return candidates.get(Math.floorMod(key.hashCode(), candidates.size()));
    }

    private static String categoryOf(String path) {
        String rest = path.substring(PREFIX.length());
        int slash = rest.indexOf('/');
        return slash < 0 ? "" : rest.substring(0, slash);
    }
}
