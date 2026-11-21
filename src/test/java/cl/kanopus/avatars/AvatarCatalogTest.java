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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AvatarCatalogTest {

    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    private final AvatarCatalog catalog = AvatarCatalog.load();

    @Test
    void everyIndexedAvatarExistsAndIsPng() throws IOException {
        assertTrue(catalog.size() > 0);
        for (String path : catalog.all()) {
            try (InputStream in = catalog.open(path)) {
                assertArrayEquals(PNG_SIGNATURE, in.readNBytes(8), path);
            }
        }
    }

    @Test
    void categoriesMatchDirectories() {
        assertTrue(catalog.categories().contains("vikings"));
        int total = catalog.categories().stream().mapToInt(c -> catalog.byCategory(c).size()).sum();
        assertEquals(catalog.size(), total);
        assertTrue(catalog.byCategory("does-not-exist").isEmpty());
    }

    @Test
    void pickIsDeterministic() {
        assertEquals(catalog.pick("user-42"), catalog.pick("user-42"));
        String viking = catalog.pick("user-42", "vikings");
        assertTrue(viking.startsWith("kanopus/avatars/vikings/"));
        assertThrows(IllegalArgumentException.class, () -> catalog.pick("x", "nope"));
        assertThrows(NullPointerException.class, () -> catalog.pick(null));
    }

    @Test
    void openRejectsPathsOutsideCatalog() {
        assertThrows(IllegalArgumentException.class, () -> catalog.open("kanopus/avatars/index.txt"));
        assertThrows(IllegalArgumentException.class, () -> catalog.open("../../etc/passwd"));
    }

    @Test
    void readBytesAndFindByFileName() {
        String first = catalog.all().get(0);
        String fileName = first.substring(first.lastIndexOf('/') + 1);
        assertEquals(first, catalog.findByFileName(fileName).orElseThrow());
        assertTrue(catalog.readBytes(first).length > PNG_SIGNATURE.length);
        assertFalse(catalog.findByFileName("missing.png").isPresent());
    }

    @Test
    void indexIgnoresCommentsBlankAndUnsafeLines(@TempDir Path dir) throws IOException {
        Path idx = dir.resolve(AvatarCatalog.INDEX);
        Files.createDirectories(idx.getParent());
        Files.writeString(idx, "# comment\n\n kanopus/avatars/a/x.png \nkanopus/avatars/../secret\nother/y.png\n", StandardCharsets.UTF_8);
        try (URLClassLoader cl = new URLClassLoader(new URL[]{dir.toUri().toURL()}, null)) {
            AvatarCatalog custom = AvatarCatalog.load(cl);
            assertEquals(1, custom.size());
            assertEquals("kanopus/avatars/a/x.png", custom.all().get(0));
            assertThrows(IllegalStateException.class, () -> custom.open("kanopus/avatars/a/x.png"));
        }
    }

    @Test
    void missingIndexFails(@TempDir Path dir) throws IOException {
        try (URLClassLoader cl = new URLClassLoader(new URL[]{dir.toUri().toURL()}, null)) {
            assertThrows(IllegalStateException.class, () -> AvatarCatalog.load(cl));
        }
    }
}
