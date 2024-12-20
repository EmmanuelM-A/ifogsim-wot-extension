package com.extensions.utils;

public enum FilePaths {
    JSON_THINGS_REPO("src/com/extensions/input/things");

    private final String filepath;

    FilePaths(String filepath) {
        this.filepath = filepath;
    }

    public String getFilepath() {
        return filepath;
    }
}
