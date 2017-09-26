package com.ft.common;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {

    public static String readFile(final String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(FileUtils.class.getClassLoader().getResource(path).toURI())), "UTF-8");
        } catch (IOException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
}
