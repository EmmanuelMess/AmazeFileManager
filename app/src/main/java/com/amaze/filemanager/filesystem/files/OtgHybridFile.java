package com.amaze.filemanager.filesystem.files;

import android.content.ContentResolver;
import android.content.Context;
import android.support.v4.provider.DocumentFile;
import com.amaze.filemanager.utils.OTGUtil;
import com.amaze.filemanager.utils.OnFileFound;
import com.amaze.filemanager.utils.files.FileUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class OtgHybridFile extends AbstractHybridFile {
    public OtgHybridFile(String path) {
        super(path);
    }

    public OtgHybridFile(String path, String name, boolean isDirectory) {
        super(path, name, isDirectory);
    }

    @Override
    public long length(Context context) {
        return OTGUtil.getDocumentFile(path, context, false).length();
    }

    @Override
    public String getName(Context context) {
        return OTGUtil.getDocumentFile(path, context, false).getName();
    }

    @Override
    public boolean isDirectory() {
        // TODO: support for this method in OTG on-the-fly
        // you need to manually call {@link RootHelper#getDocumentFile() method
        return false;
    }

    @Override
    public boolean isDirectory(Context context) {
        return OTGUtil.getDocumentFile(path, context, false).isDirectory();
    }

    @Override
    public long folderSize(Context context) {
        return FileUtils.otgFolderSize(path, context);
    }

    @Override
    public long getUsableSpace() {
        // TODO: Get free space from OTG when {@link DocumentFile} API adds support
        return super.getUsableSpace();
    }

    @Override
    public long getTotal(Context context) {
        // TODO: Find total storage space of OTG when {@link DocumentFile} API adds support
        DocumentFile documentFile = OTGUtil.getDocumentFile(path, context, false);
        documentFile.length();
        return super.getTotal(context);
    }

    @Override
    public void forEachChildrenFile(Context context, boolean isRoot, OnFileFound onFileFound) {
        OTGUtil.getDocumentFiles(path, context, onFileFound);
    }

    @Override
    public ArrayList<HybridFileParcelable> listFiles(Context context, boolean isRoot) {
        return OTGUtil.getDocumentFilesList(path, context);
    }

    @Override
    public InputStream getInputStream(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        DocumentFile documentSourceFile = OTGUtil.getDocumentFile(path,
                context, false);
        try {
            return contentResolver.openInputStream(documentSourceFile.getUri());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        DocumentFile documentSourceFile = OTGUtil.getDocumentFile(path,
                context, true);
        try {
            return contentResolver.openOutputStream(documentSourceFile.getUri());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean exists(Context context) {
        DocumentFile fileToCheck = OTGUtil.getDocumentFile(path, context, false);
        return fileToCheck != null;
    }

    @Override
    public void mkdir(Context context) {
        if (!exists(context)) {
            DocumentFile parentDirectory = OTGUtil.getDocumentFile(getParent(context), context, false);
            if (parentDirectory.isDirectory()) {
                parentDirectory.createDirectory(getName(context));
            }
        }
    }
}
