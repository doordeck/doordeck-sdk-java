package com.doordeck.sdk.util;

import com.google.common.io.CharStreams;
import com.google.common.io.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A set of helper method for fixture files.
 */
public class FixtureHelpers {
    private FixtureHelpers() { /* singleton */ }

    /**
     * Reads the given fixture file from the classpath (e. g. {@code src/test/resources})
     * and returns its contents as a UTF-8 string.
     *
     * @param filename the filename of the fixture file
     * @return the contents of {@code src/test/resources/{filename}}
     * @throws IllegalArgumentException if an I/O error occurs.
     */
    public static String fixture(String filename) {
        return fixture(filename, StandardCharsets.UTF_8);
    }

    /**
     * Returns a {@code URL} pointing to {@code resourceName} if the resource is found using the
     * {@linkplain Thread#getContextClassLoader() context class loader}. In simple environments, the
     * context class loader will find resources from the class path. In environments where different
     * threads can have different class loaders, for example app servers, the context class loader
     * will typically have been set to an appropriate loader for the current thread.
     *
     * <p>In the unusual case where the context class loader is null, the class loader that loaded
     * this class ({@code Resources}) will be used instead.
     *
     * @throws IllegalArgumentException if the resource is not found
     */
    public static URL getResource(String resourceName) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader loader = contextClassLoader == null ? Resources.class.getClassLoader() : contextClassLoader;
        final URL url = loader.getResource(resourceName);
        if (url == null) {
            throw new IllegalArgumentException("resource " + resourceName + " not found.");
        }
        return url;
    }

    /**
     * Reads all characters from a URL into a {@link String}, using the given character set.
     *
     * @param url     the URL to read from
     * @param charset the charset used to decode the input stream; see {@link java.nio.charset.StandardCharsets} for helpful
     *                predefined constants
     * @return a string containing all the characters from the URL
     * @throws IOException if an I/O error occurs.
     */
    public static String toString(URL url, Charset charset) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            return CharStreams.toString(new InputStreamReader(inputStream, charset));
        }
    }

    /**
     * Reads the given fixture file from the classpath (e. g. {@code src/test/resources})
     * and returns its contents as a string.
     *
     * @param filename the filename of the fixture file
     * @param charset  the character set of {@code filename}
     * @return the contents of {@code src/test/resources/{filename}}
     * @throws IllegalArgumentException if an I/O error occurs.
     */
    private static String fixture(String filename, Charset charset) {
        final URL resource = getResource(filename);
        try {
            return Resources.toString(resource, charset).trim();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}