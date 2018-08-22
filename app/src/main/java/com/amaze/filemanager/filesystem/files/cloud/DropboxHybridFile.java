package com.amaze.filemanager.filesystem.files.cloud;

import android.content.Context;
import android.util.Log;
import com.amaze.filemanager.exceptions.CloudPluginException;
import com.amaze.filemanager.filesystem.files.AbstractHybridFile;
import com.amaze.filemanager.filesystem.files.HybridFileParcelable;
import com.amaze.filemanager.utils.OnFileFound;
import com.amaze.filemanager.utils.OpenMode;
import com.amaze.filemanager.utils.cloud.CloudUtil;
import com.amaze.filemanager.utils.files.FileUtils;
import com.cloudrail.si.interfaces.CloudStorage;
import com.cloudrail.si.types.SpaceAllocation;

import java.io.InputStream;
import java.util.ArrayList;

public class DropboxHybridFile extends AbstractHybridFile {
    public DropboxHybridFile(String path) {
        super(path);
    }

    public DropboxHybridFile(String path, String name, boolean isDirectory) {
        super(path, name, isDirectory);
    }

    @Override
    public long length(Context context) {
        return dataUtils.getAccount(OpenMode.DROPBOX)
                .getMetadata(CloudUtil.stripPath(OpenMode.DROPBOX, getPath())).getSize();
    }

    @Override
    public boolean isDirectory(Context context) {
        return dataUtils.getAccount(OpenMode.DROPBOX)
                .getMetadata(CloudUtil.stripPath(OpenMode.DROPBOX, getPath())).getFolder();
    }

    @Override
    public long folderSize(Context context) {
        return FileUtils.folderSizeCloud(OpenMode.DROPBOX,
                dataUtils.getAccount(OpenMode.DROPBOX).getMetadata(CloudUtil.stripPath(OpenMode.DROPBOX, getPath())));
    }

    @Override
    public long getUsableSpace() {
        SpaceAllocation spaceAllocation = dataUtils.getAccount(OpenMode.DROPBOX).getAllocation();
        return spaceAllocation.getTotal() - spaceAllocation.getUsed();
    }

    @Override
    public long getTotal(Context context) {
        SpaceAllocation spaceAllocation = dataUtils.getAccount(OpenMode.DROPBOX).getAllocation();
        return spaceAllocation.getTotal();
    }

    @Override
    public void forEachChildrenFile(Context context, boolean isRoot, OnFileFound onFileFound) {
        try {
            CloudUtil.getCloudFiles(getPath(), dataUtils.getAccount(OpenMode.DROPBOX), OpenMode.DROPBOX, onFileFound);
        } catch (CloudPluginException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<HybridFileParcelable> listFiles(Context context, boolean isRoot) {
        try {
            return CloudUtil.listFiles(getPath(), dataUtils.getAccount(OpenMode.DROPBOX), OpenMode.DROPBOX);
        } catch (CloudPluginException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public InputStream getInputStream(Context context) {
        CloudStorage cloudStorageDropbox = dataUtils.getAccount(OpenMode.DROPBOX);
        Log.d(getClass().getSimpleName(), CloudUtil.stripPath(OpenMode.DROPBOX, getPath()));
        return cloudStorageDropbox.download(CloudUtil.stripPath(OpenMode.DROPBOX, getPath()));
    }

    @Override
    public boolean exists() {
        CloudStorage cloudStorageDropbox = dataUtils.getAccount(OpenMode.DROPBOX);
        return cloudStorageDropbox.exists(CloudUtil.stripPath(OpenMode.DROPBOX, getPath()));
    }

    @Override
    public void mkdir(Context context) {
        CloudStorage cloudStorageDropbox = dataUtils.getAccount(OpenMode.DROPBOX);
        try {
            cloudStorageDropbox.createFolder(CloudUtil.stripPath(OpenMode.DROPBOX, getPath()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
