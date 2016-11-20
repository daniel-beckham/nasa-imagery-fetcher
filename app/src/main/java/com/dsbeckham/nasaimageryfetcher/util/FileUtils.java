package com.dsbeckham.nasaimageryfetcher.util;

import java.io.File;

class FileUtils {
    @SuppressWarnings("UnusedReturnValue")
    public static boolean createDirectoryIfNotExists(File directory) {
        return !directory.exists()
                && directory.mkdirs();
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean deleteDirectoryTree(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteDirectoryTree(child);
            }
        }

        return fileOrDirectory.delete();
    }
}
