package com.dsbeckham.nasaimageryfetcher.util;

import java.io.File;

public class FileUtils {
    public static boolean createDirectoryIfNotExists(File directory) {
        return !directory.exists()
                && directory.mkdirs();
    }

    public static boolean deleteDirectoryTree(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteDirectoryTree(child);
            }
        }

        return fileOrDirectory.delete();
    }
}
