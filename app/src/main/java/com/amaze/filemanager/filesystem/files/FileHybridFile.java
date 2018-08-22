package com.amaze.filemanager.filesystem.files;

import android.content.Context;
import com.amaze.filemanager.adapters.data.LayoutElementParcelable;
import com.amaze.filemanager.filesystem.RootHelper;
import com.amaze.filemanager.utils.files.FileUtils;
import jcifs.smb.SmbException;

import java.io.File;

public class FileHybridFile extends AbstractHybridFile {
    public FileHybridFile(String path) {
        super(path);
    }

    public FileHybridFile(String path, String name, boolean isDirectory) {
        super(path, name, isDirectory);
    }

    @Override
    public long lastModified() {
        new File(path).lastModified();
        return new File("/").lastModified();
    }

    @Override
    public long length() {
        return new File(path).length();
    }

    @Override
    public long length(Context context) {
        return new File(path).length();
    }

    @Override
    public String getName() {
        return new File(path).getName();
    }

    @Override
    public String getName(Context context) {
        return new File(path).getName();
    }

    @Override
    public String getParent() {
        return new File(path).getParent();
    }

    @Override
    public String getParent(Context context) {
        return new File(path).getParent();
    }

    @Override
    public boolean isDirectory() {
        return new File(path).isDirectory();
    }

    @Override
    public boolean isDirectory(Context context) {
        return new File(path).isDirectory();
    }

    @Override
    public long folderSize() {
        return FileUtils.folderSize(new File(path), null);
    }

    @Override
    public long folderSize(Context context) {
        return FileUtils.folderSize(new File(path), null);
    }

    @Override
    public long getUsableSpace() {
        return new File(path).getUsableSpace();
    }

    @Override
    public long getTotal(Context context) {
        return new File(path).getTotalSpace();
    }

    /**
     * Generates a {@link LayoutElementParcelable} adapted compatible element.
     * Currently supports only local filesystem
     */
    public LayoutElementParcelable generateLayoutElement(boolean showThumbs) {
        File file = new File(path);
        if (isDirectory()) {
            return new LayoutElementParcelable(path, RootHelper.parseFilePermission(file),
                    "", folderSize() + "", 0, true, file.lastModified() + "",
                    false, showThumbs, mode);
        } else {
            return new LayoutElementParcelable(
                    file.getPath(), RootHelper.parseFilePermission(file),
                    file.getPath(), file.length() + "", file.length(), false, file.lastModified() + "",
                    false, showThumbs, mode);
        }
    }

}
