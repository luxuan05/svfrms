package org.example.svfrms.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Opens a config file. A copy in the working directory wins over the one bundled in the jar,
 * so crops can be added during the demo by editing crops.csv next to run.jar, with no rebuild.
 */
public final class ConfigReader {
    private ConfigReader() {}

    public static BufferedReader open(String name) throws IOException {
        Path external = Path.of(name);
        if (Files.isRegularFile(external)) {
            return Files.newBufferedReader(external, UTF_8);
        }
        InputStream bundled = ConfigReader.class.getClassLoader().getResourceAsStream(name);
        if (bundled == null) {
            throw new FileNotFoundException(name + " not found in working directory or classpath");
        }
        return new BufferedReader(new InputStreamReader(bundled, UTF_8));
    }

    /** Where {@link #open} will read from, for a startup log line. */
    public static String sourceOf(String name) {
        Path external = Path.of(name);
        return Files.isRegularFile(external) ? external.toAbsolutePath().toString() : "bundled " + name;
    }
}
