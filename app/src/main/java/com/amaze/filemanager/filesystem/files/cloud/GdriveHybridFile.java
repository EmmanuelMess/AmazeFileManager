package com.amaze.filemanager.filesystem.files.cloud;

import android.content.Context;
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

public class GdriveHybridFile extends AbstractHybridFile {
    public GdriveHybridFile(String path) {
        super(path);
    }

    public GdriveHybridFile(String path, String name, boolean isDirectory) {
        super(path, name, isDirectory);
    }

    @Override
    public long length(Context context) {
        return dataUtils.getAccount(OpenMode.GDRIVE)
                .getMetadata(CloudUtil.stripPath(OpenMode.GDRIVE, getPath())).getSize();
    }

    @Override
    public boolean isDirectory(Context context) {
        return dataUtils.getAccount(OpenMode.GDRIVE)
                .getMetadata(CloudUtil.stripPath(OpenMode.GDRIVE, getPath())).getFolder();
    }

    @Override
    public long folderSize(Context context) {
        return FileUtils.folderSizeCloud(OpenMode.GDRIVE,
                dataUtils.getAccount(OpenMode.GDRIVE).getMetadata(CloudUtil.stripPath(OpenMode.GDRIVE, getPath())));
    }

    @Override
    public long getUsableSpace() {
        SpaceAllocation spaceAllocation = dataUtils.getAccount(OpenMode.GDRIVE).getAllocation();
        return spaceAllocation.getTotal() - spaceAllocation.getUsed();
    }

    @Override
    public long getTotal(Context context) {
        SpaceAllocation spaceAllocation = dataUtils.getAccount(OpenMode.GDRIVE).getAllocation();
        return spaceAllocation.getTotal();
    }

    @Override
    public void forEachChildrenFile(Context context, boolean isRoot, OnFileFound onFileFound) {
        try {
            CloudUtil.getCloudFiles(getPath(), dataUtils.getAccount(OpenMode.GDRIVE), OpenMode.GDRIVE, onFileFound);
        } catch (CloudPluginException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<HybridFileParcelable> listFiles(Context context, boolean isRoot) {
        try {
            return CloudUtil.listFiles(getPath(), dataUtils.getAccount(OpenMode.GDRIVE), OpenMode.GDRIVE);
        } catch (CloudPluginException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public InputStream getInputStream(Context context) {
        CloudStorage cloudStorageGDrive = dataUtils.getAccount(OpenMode.GDRIVE);
        return cloudStorageGDrive.download(CloudUtil.stripPath(OpenMode.GDRIVE, getPath()));
    }

    @Override
    public boolean exists() {
        CloudStorage cloudStorageGoogleDrive = dataUtils.getAccount(OpenMode.GDRIVE);
        return cloudStorageGoogleDrive.exists(CloudUtil.stripPath(OpenMode.GDRIVE, getPath()));
    }

    @Override
    public void mkdir(Context context) {
        CloudStorage cloudStorageGdrive = dataUtils.getAccount(OpenMode.GDRIVE);
        try {
            cloudStorageGdrive.createFolder(CloudUtil.stripPath(OpenMode.GDRIVE, getPath()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
