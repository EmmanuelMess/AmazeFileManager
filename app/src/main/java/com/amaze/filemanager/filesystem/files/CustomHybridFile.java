package com.amaze.filemanager.filesystem.files;

public class CustomHybridFile extends AbstractHybridFile {
    public CustomHybridFile(String path) {
        super(path);
    }

    public CustomHybridFile(String path, String name, boolean isDirectory) {
        super(path, name, isDirectory);
    }

    @Override
    public boolean isCustomPath() {
        return true;
    }
}
