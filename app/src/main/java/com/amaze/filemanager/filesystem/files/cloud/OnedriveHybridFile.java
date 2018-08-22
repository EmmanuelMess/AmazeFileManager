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

public class OnedriveHybridFile extends AbstractHybridFile {
    public OnedriveHybridFile(String path) {
        super(path);
    }

    public OnedriveHybridFile(String path, String name, boolean isDirectory) {
        super(path, name, isDirectory);
    }

    @Override
    public long length(Context context) {
        return dataUtils.getAccount(OpenMode.ONEDRIVE)
                .getMetadata(CloudUtil.stripPath(OpenMode.ONEDRIVE, getPath())).getSize();
    }

    @Override
    public boolean isDirectory(Context context) {
        return dataUtils.getAccount(OpenMode.ONEDRIVE)
                .getMetadata(CloudUtil.stripPath(OpenMode.ONEDRIVE, getPath())).getFolder();
    }

    @Override
    public long folderSize(Context context) {
        return FileUtils.folderSizeCloud(OpenMode.ONEDRIVE,
                dataUtils.getAccount(OpenMode.ONEDRIVE).getMetadata(CloudUtil.stripPath(OpenMode.ONEDRIVE, getPath())));
    }

    @Override
    public long getUsableSpace() {
        SpaceAllocation spaceAllocation = dataUtils.getAccount(OpenMode.ONEDRIVE).getAllocation();
        return spaceAllocation.getTotal() - spaceAllocation.getUsed();
    }

    @Override
    public long getTotal(Context context) {
        SpaceAllocation spaceAllocation = dataUtils.getAccount(OpenMode.ONEDRIVE).getAllocation();
        return spaceAllocation.getTotal();
    }

    @Override
    public void forEachChildrenFile(Context context, boolean isRoot, OnFileFound onFileFound) {
        try {
            CloudUtil.getCloudFiles(getPath(), dataUtils.getAccount(OpenMode.ONEDRIVE), OpenMode.ONEDRIVE, onFileFound);
        } catch (CloudPluginException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<HybridFileParcelable> listFiles(Context context, boolean isRoot) {
        try {
            return CloudUtil.listFiles(getPath(), dataUtils.getAccount(OpenMode.ONEDRIVE), OpenMode.ONEDRIVE);
        } catch (CloudPluginException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public InputStream getInputStream(Context context) {
        CloudStorage cloudStorageOneDrive = dataUtils.getAccount(OpenMode.ONEDRIVE);
        return cloudStorageOneDrive.download(CloudUtil.stripPath(OpenMode.ONEDRIVE, getPath()));
    }

    @Override
    public boolean exists() {
        CloudStorage cloudStorageOneDrive = dataUtils.getAccount(OpenMode.ONEDRIVE);
        return cloudStorageOneDrive.exists(CloudUtil.stripPath(OpenMode.ONEDRIVE, getPath()));
    }

    @Override
    public void mkdir(Context context) {
        CloudStorage cloudStorageOneDrive = dataUtils.getAccount(OpenMode.ONEDRIVE);
        try {
            cloudStorageOneDrive.createFolder(CloudUtil.stripPath(OpenMode.ONEDRIVE, getPath()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
