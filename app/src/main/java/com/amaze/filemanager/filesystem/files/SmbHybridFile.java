package com.amaze.filemanager.filesystem.files;

public class SmbHybridFile extends AbstractHybridFile {
    public SmbHybridFile(String path) {
        super(path);
    }

    public SmbHybridFile(String path, String name, boolean isDirectory) {
        super(path, name, isDirectory);
    }
}
