package com.amaze.filemanager.filesystem.files;

import android.content.Context;
import com.amaze.filemanager.exceptions.ShellNotRunningException;
import com.amaze.filemanager.filesystem.RootHelper;
import com.amaze.filemanager.utils.RootUtils;

import java.io.File;

public class RootHybridFile extends FileHybridFile {
    public RootHybridFile(String path) {
        super(path);
    }

    public RootHybridFile(String path, String name, boolean isDirectory) {
        super(path, name, isDirectory);
    }

    @Override
    public long lastModified() {
        HybridFileParcelable baseFile = generateBaseFileFromParent();
        if (baseFile != null)
            return baseFile.getDate();
        return new File("/").lastModified();
    }

    @Override
    public long length() {
        HybridFileParcelable baseFile = generateBaseFileFromParent();
        if (baseFile != null) return baseFile.getSize();
        return 0L;
    }

    @Override
    public long length(Context context) {
        HybridFileParcelable baseFile=generateBaseFileFromParent();
        if(baseFile!=null) return baseFile.getSize();
        return 0L;
    }

    @Override
    public boolean isDirectory() {
        try {
            return RootHelper.isDirectory(path, true, 5);
        } catch (ShellNotRunningException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isDirectory(Context context) {
        try {
            return RootHelper.isDirectory(path,true,5);
        } catch (ShellNotRunningException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public long folderSize() {
        HybridFileParcelable baseFile = generateBaseFileFromParent();
        if (baseFile != null) return baseFile.getSize();
        return 0L;
    }

    @Override
    public long folderSize(Context context) {
        HybridFileParcelable baseFile=generateBaseFileFromParent();
        if(baseFile!=null) return baseFile.getSize();
        return 0L;
    }

    @Override
    public boolean exists() {
        return RootHelper.fileExists(path);
    }

    @Override
    public boolean isSimpleFile() {
        return false;
    }

    @Override
    public boolean delete(Context context, boolean rootmode) throws ShellNotRunningException {
        if(rootmode) {
            RootUtils.delete(getPath());
            return !exists();
        } else {
            return super.delete(context, false);
        }
    }
}
