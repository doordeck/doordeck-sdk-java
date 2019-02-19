package com.doordeck.sdk.core.util;

import com.google.common.base.Optional;

import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ManifestUtils {

    private ManifestUtils() { /* static class */ }

    public static Optional<String> getManfiestVersion() {
        try {
            Class clazz = ManifestUtils.class;
            String className = clazz.getSimpleName() + ".class";
            String classPath = clazz.getResource(className).toString();
            if (!classPath.startsWith("jar")) {
                // Class not from JAR
                return Optional.absent();
            }

            String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
            Manifest manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attr = manifest.getMainAttributes();
            return Optional.fromNullable(attr.getValue("Manifest-Version"));
        } catch (Exception e) {
            return Optional.absent();
        }
    }
}
