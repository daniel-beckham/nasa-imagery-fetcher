package com.beckhamd.nasaimageryfetcher.util;

import java.io.File;

public class FileUtils {
    public static boolean createDirectoryIfNotExists(File directory) {
        if (!directory.exists()) {
            return directory.mkdirs();
        }

        return true;
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
