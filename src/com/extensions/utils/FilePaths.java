package com.extensions.utils;

public enum FilePaths {
    JSON_THINGS_REPO("src\\main\\java\\com\\extension\\input\\things");

    private String filepath;

    private FilePaths(String filepath) {
        this.filepath = filepath;
    }

    public String getFilepath() {
        return filepath;
    }
}
