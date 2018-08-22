package com.amaze.filemanager.filesystem.files;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.amaze.filemanager.adapters.data.LayoutElementParcelable;
import com.amaze.filemanager.database.CloudHandler;
import com.amaze.filemanager.exceptions.CloudPluginException;
import com.amaze.filemanager.exceptions.ShellNotRunningException;
import com.amaze.filemanager.filesystem.RootHelper;
import com.amaze.filemanager.filesystem.ssh.Statvfs;
import com.amaze.filemanager.fragments.preference_fragments.PreferencesConstants;

import com.amaze.filemanager.filesystem.ssh.SFtpClientTemplate;
import com.amaze.filemanager.filesystem.ssh.SshClientTemplate;
import com.amaze.filemanager.filesystem.ssh.SshClientUtils;
import com.amaze.filemanager.utils.application.AppConfig;

import com.amaze.filemanager.utils.DataUtils;
import com.amaze.filemanager.utils.OTGUtil;
import com.amaze.filemanager.utils.OnFileFound;
import com.amaze.filemanager.utils.OpenMode;
import com.amaze.filemanager.utils.RootUtils;
import com.amaze.filemanager.utils.cloud.CloudUtil;
import com.amaze.filemanager.utils.files.FileUtils;
import com.cloudrail.si.interfaces.CloudStorage;
import com.cloudrail.si.types.SpaceAllocation;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.Buffer;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.xfer.FilePermission;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.EnumSet;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by Arpit on 07-07-2015.
 */
//Hybrid file for handeling all types of files
public abstract class AbstractHybridFile {

    private static final String TAG = "HFile";

    String path;
    //public static final int ROOT_MODE=3,LOCAL_MODE=0,SMB_MODE=1,UNKNOWN=-1;
    OpenMode mode = OpenMode.FILE;

    protected final DataUtils dataUtils = DataUtils.getInstance();

    public AbstractHybridFile(String path) {
        this.path = path;
    }

    public AbstractHybridFile(String path, String name, boolean isDirectory) {
        this.path = path + "/" + name;
    }

    public void generateMode(Context context) {
        if (path.startsWith("smb://")) {
            mode = OpenMode.SMB;
        } else if (path.startsWith("ssh://")) {
            mode = OpenMode.SFTP;
        } else if (path.startsWith(OTGUtil.PREFIX_OTG)) {
            mode = OpenMode.OTG;
        } else if (isCustomPath()) {
            mode = OpenMode.CUSTOM;
        } else if (path.startsWith(CloudHandler.CLOUD_PREFIX_BOX)) {
            mode = OpenMode.BOX;
        } else if (path.startsWith(CloudHandler.CLOUD_PREFIX_ONE_DRIVE)) {
            mode = OpenMode.ONEDRIVE;
        } else if (path.startsWith(CloudHandler.CLOUD_PREFIX_GOOGLE_DRIVE)) {
            mode = OpenMode.GDRIVE;
        } else if (path.startsWith(CloudHandler.CLOUD_PREFIX_DROPBOX)) {
            mode = OpenMode.DROPBOX;
        } else if(context == null) {
            mode = OpenMode.FILE;
        } else {
            boolean rootmode = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesConstants.PREFERENCE_ROOTMODE, false);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                mode = OpenMode.FILE;
                if (rootmode && !getFile().canRead()) {
                    mode = OpenMode.ROOT;
                }
            } else {
                if (FileUtil.isOnExtSdCard(getFile(), context)) {
                    mode = OpenMode.FILE;
                } else if (rootmode && !getFile().canRead()) {
                    mode = OpenMode.ROOT;
                }

                if (mode == OpenMode.UNKNOWN) {
                    mode = OpenMode.FILE;
                }
            }
        }

    }

    public void setMode(OpenMode mode) {
        this.mode = mode;
    }

    public OpenMode getMode() {
        return mode;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isLocal() {
        return mode == OpenMode.FILE;
    }

    public boolean isRoot() {
        return mode == OpenMode.ROOT;
    }

    public boolean isSmb() {
        return mode == OpenMode.SMB;
    }

    public boolean isSftp() { return mode == OpenMode.SFTP; }

    public boolean isOtgFile() {
        return mode == OpenMode.OTG;
    }

    public boolean isBoxFile() {
        return mode == OpenMode.BOX;
    }

    public boolean isDropBoxFile() {
        return mode == OpenMode.DROPBOX;
    }

    public boolean isOneDriveFile() {
        return mode == OpenMode.ONEDRIVE;
    }

    public boolean isGoogleDriveFile() {
        return mode == OpenMode.GDRIVE;
    }

    public File getFile() {
        return new File(path);
    }

    HybridFileParcelable generateBaseFileFromParent() {
        ArrayList<HybridFileParcelable> arrayList = RootHelper.getFilesList(getFile().getParent(), true, true, null);
        for (HybridFileParcelable baseFile : arrayList) {
            if (baseFile.getPath().equals(path))
                return baseFile;
        }
        return null;
    }

    public long lastModified() throws SmbException {
        switch (mode) {
            case FILE:
                new File(path).lastModified();
                break;
            case ROOT:
                HybridFileParcelable baseFile = generateBaseFileFromParent();
                if (baseFile != null)
                    return baseFile.getDate();
        }
        return new File("/").lastModified();
    }

    /**
     * @deprecated use {@link #length(Context)} to handle content resolvers
     */
    public long length() {
        long s = 0L;
        switch (mode) {
            case FILE:
                s = new File(path).length();
                return s;
            case ROOT:
                HybridFileParcelable baseFile = generateBaseFileFromParent();
                if (baseFile != null) return baseFile.getSize();
                break;
        }
        return s;
    }

    /**
     * Helper method to find length
     */
    public long length(Context context) {

        long s = 0l;
        switch (mode){
            case FILE:
                s = new File(path).length();
                return s;
            case ROOT:
                HybridFileParcelable baseFile=generateBaseFileFromParent();
                if(baseFile!=null) return baseFile.getSize();
                break;
            case DROPBOX:
                s = dataUtils.getAccount(OpenMode.DROPBOX)
                        .getMetadata(CloudUtil.stripPath(OpenMode.DROPBOX, path)).getSize();
                break;
            case ONEDRIVE:
                s = dataUtils.getAccount(OpenMode.ONEDRIVE)
                        .getMetadata(CloudUtil.stripPath(OpenMode.ONEDRIVE, path)).getSize();
                break;
            case GDRIVE:
                s = dataUtils.getAccount(OpenMode.GDRIVE)
                        .getMetadata(CloudUtil.stripPath(OpenMode.GDRIVE, path)).getSize();
                break;
            default:
                break;
        }
        return s;
    }

    public String getPath() {
        return path;
    }

    /**
     * @deprecated use {@link #getName(Context)}
     */
    public String getName() {
        String name = null;
        switch (mode) {
            case FILE:
                return new File(path).getName();
            case ROOT:
                return new File(path).getName();
            default:
                StringBuilder builder = new StringBuilder(path);
                name = builder.substring(builder.lastIndexOf("/") + 1, builder.length());
        }
        return name;
    }

    public String getName(Context context) {
        String name = null;
        switch (mode){
            case FILE:
                return new File(path).getName();
            case ROOT:
                return new File(path).getName();
            default:
                StringBuilder builder = new StringBuilder(path);
                name = builder.substring(builder.lastIndexOf("/")+1, builder.length());
        }
        return name;
    }

    public boolean isCustomPath() {
        return false;
    }

    /**
     * Returns a path to parent for various {@link #mode}
     * @deprecated use {@link #getParent(Context)} to handle content resolvers
     */
    public String getParent() {
        String parentPath = "";
        switch (mode) {
            case FILE:
            case ROOT:
                parentPath = new File(path).getParent();
                break;
            default:
                StringBuilder builder = new StringBuilder(path);
                return builder.substring(0, builder.length() - (getName().length() + 1));
        }
        return parentPath;
    }

    /**
     * Helper method to get parent path
     */
    public String getParent(Context context) {

        String parentPath = "";
        switch (mode) {
            case FILE:
            case ROOT:
                parentPath = new File(path).getParent();
                break;
            default:
                StringBuilder builder = new StringBuilder(path);
                StringBuilder parentPathBuilder = new StringBuilder(builder.substring(0,
                        builder.length()-(getName(context).length()+1)));
                return parentPathBuilder.toString();
        }
        return parentPath;
    }

    public String getParentName() {
        StringBuilder builder = new StringBuilder(path);
        StringBuilder parentPath = new StringBuilder(builder.substring(0,
                builder.length() - (getName().length() + 1)));
        String parentName = parentPath.substring(parentPath.lastIndexOf("/") + 1,
                parentPath.length());
        return parentName;
    }

    /**
     * Whether this object refers to a directory or file, handles all types of files
     * @deprecated use {@link #isDirectory(Context)} to handle content resolvers
     */
    public boolean isDirectory() {
        boolean isDirectory;
        switch (mode) {
            case FILE:
                isDirectory = new File(path).isDirectory();
                break;
            case ROOT:
                try {
                    isDirectory = RootHelper.isDirectory(path, true, 5);
                } catch (ShellNotRunningException e) {
                    e.printStackTrace();
                    isDirectory = false;
                }
                break;
            default:
                isDirectory = new File(path).isDirectory();
                break;

        }
        return isDirectory;
    }

    public boolean isDirectory(Context context) {

        boolean isDirectory;
        switch (mode) {
            case FILE:
                isDirectory = new File(path).isDirectory();
                break;
            case ROOT:
                try {
                    isDirectory = RootHelper.isDirectory(path,true,5);
                } catch (ShellNotRunningException e) {
                    e.printStackTrace();
                    isDirectory = false;
                }
                break;
            case DROPBOX:
                isDirectory = dataUtils.getAccount(OpenMode.DROPBOX)
                        .getMetadata(CloudUtil.stripPath(OpenMode.DROPBOX, path)).getFolder();
                break;
            case GDRIVE:
                isDirectory = dataUtils.getAccount(OpenMode.GDRIVE)
                        .getMetadata(CloudUtil.stripPath(OpenMode.GDRIVE, path)).getFolder();
                break;
            case ONEDRIVE:
                isDirectory = dataUtils.getAccount(OpenMode.ONEDRIVE)
                        .getMetadata(CloudUtil.stripPath(OpenMode.ONEDRIVE, path)).getFolder();
                break;
            default:
                isDirectory = new File(path).isDirectory();
                break;

        }
        return isDirectory;
    }

    /**
     * @deprecated use {@link #folderSize(Context)}
     */
    public long folderSize() {
        long size = 0L;

        switch (mode) {
            case FILE:
                size = FileUtils.folderSize(new File(path), null);
                break;
            case ROOT:
                HybridFileParcelable baseFile = generateBaseFileFromParent();
                if (baseFile != null) size = baseFile.getSize();
                break;
            default:
                return 0L;
        }
        return size;
    }

    /**
     * Helper method to get length of folder in an otg
     */
    public long folderSize(Context context) {

        long size = 0l;

        switch (mode){
            case FILE:
                size = FileUtils.folderSize(new File(path), null);
                break;
            case ROOT:
                HybridFileParcelable baseFile=generateBaseFileFromParent();
                if(baseFile!=null) size = baseFile.getSize();
                break;
            case DROPBOX:
            case GDRIVE:
            case ONEDRIVE:
                size = FileUtils.folderSizeCloud(mode,
                        dataUtils.getAccount(mode).getMetadata(CloudUtil.stripPath(mode, path)));
                break;
            default:
                return 0l;
        }
        return size;
    }


    /**
     * Gets usable i.e. free space of a device
     */
    public long getUsableSpace() {
        long size = 0L;
        switch (mode) {
            case FILE:
            case ROOT:
                size = new File(path).getUsableSpace();
                break;
            case DROPBOX:
            case GDRIVE:
            case ONEDRIVE:
                SpaceAllocation spaceAllocation = dataUtils.getAccount(mode).getAllocation();
                size = spaceAllocation.getTotal() - spaceAllocation.getUsed();
                break;

        }
        return size;
    }

    /**
     * Gets total size of the disk
     */
    public long getTotal(Context context) {
        long size = 0l;
        switch (mode) {
            case FILE:
            case ROOT:
                size = new File(path).getTotalSpace();
                break;
            case DROPBOX:
            case ONEDRIVE:
            case GDRIVE:
                SpaceAllocation spaceAllocation = dataUtils.getAccount(mode).getAllocation();
                size = spaceAllocation.getTotal();
                break;
        }
        return size;
    }

    /**
     * Helper method to list children of this file
     */
    public void forEachChildrenFile(Context context, boolean isRoot, OnFileFound onFileFound) {
        switch (mode) {
            case DROPBOX:
            case GDRIVE:
            case ONEDRIVE:
                try {
                    CloudUtil.getCloudFiles(path, dataUtils.getAccount(mode), mode, onFileFound);
                } catch (CloudPluginException e) {
                    e.printStackTrace();
                }
                break;
            default:
                RootHelper.getFiles(path, isRoot, true, null, onFileFound);

        }
    }

    /**
     * Helper method to list children of this file
     * @deprecated use forEachChildrenFile()
     */
    public ArrayList<HybridFileParcelable> listFiles(Context context, boolean isRoot) {
        ArrayList<HybridFileParcelable> arrayList = new ArrayList<>();
        switch (mode) {
            case DROPBOX:
            case GDRIVE:
            case ONEDRIVE:
                try {
                    arrayList = CloudUtil.listFiles(path, dataUtils.getAccount(mode), mode);
                } catch (CloudPluginException e) {
                    e.printStackTrace();
                    arrayList = new ArrayList<>();
                }
                break;
            default:
                arrayList = RootHelper.getFilesList(path, isRoot, true, null);

        }

        return arrayList;
    }

    public String getReadablePath(String path) {
        return path;
    }

    /**
     * Handles getting input stream for various {@link OpenMode}
     * @deprecated use {@link #getInputStream(Context)} which allows handling content resolver
     */
    public InputStream getInputStream() {
        InputStream inputStream;
            try {
                inputStream = new FileInputStream(path);
            } catch (FileNotFoundException e) {
                inputStream = null;
                e.printStackTrace();
            }
        return inputStream;
    }

    public InputStream getInputStream(Context context) {
        InputStream inputStream;

        switch (mode) {
            case DROPBOX:
                CloudStorage cloudStorageDropbox = dataUtils.getAccount(OpenMode.DROPBOX);
                Log.d(getClass().getSimpleName(), CloudUtil.stripPath(OpenMode.DROPBOX, path));
                inputStream = cloudStorageDropbox.download(CloudUtil.stripPath(OpenMode.DROPBOX, path));
                break;
            case GDRIVE:
                CloudStorage cloudStorageGDrive = dataUtils.getAccount(OpenMode.GDRIVE);
                inputStream = cloudStorageGDrive.download(CloudUtil.stripPath(OpenMode.GDRIVE, path));
                break;
            case ONEDRIVE:
                CloudStorage cloudStorageOneDrive = dataUtils.getAccount(OpenMode.ONEDRIVE);
                inputStream = cloudStorageOneDrive.download(CloudUtil.stripPath(OpenMode.ONEDRIVE, path));
                break;
            default:
                try {
                    inputStream = new FileInputStream(path);
                } catch (FileNotFoundException e) {
                    inputStream = null;
                    e.printStackTrace();
                }
                break;
        }
        return inputStream;
    }

    public OutputStream getOutputStream(Context context) {
        OutputStream outputStream;
        switch (mode) {
            default:
                try {
                    outputStream = FileUtil.getOutputStream(new File(path), context);
                } catch (Exception e) {
                    outputStream=null;
                    e.printStackTrace();
                }

        }
        return outputStream;
    }

    public boolean exists() {
        boolean exists = false;
        if (isDropBoxFile()) {
            CloudStorage cloudStorageDropbox = dataUtils.getAccount(OpenMode.DROPBOX);
            exists = cloudStorageDropbox.exists(CloudUtil.stripPath(OpenMode.DROPBOX, path));
        } else if (isGoogleDriveFile()) {
            CloudStorage cloudStorageGoogleDrive = dataUtils.getAccount(OpenMode.GDRIVE);
            exists = cloudStorageGoogleDrive.exists(CloudUtil.stripPath(OpenMode.GDRIVE, path));
        } else if (isOneDriveFile()) {
            CloudStorage cloudStorageOneDrive = dataUtils.getAccount(OpenMode.ONEDRIVE);
            exists = cloudStorageOneDrive.exists(CloudUtil.stripPath(OpenMode.ONEDRIVE, path));
        } else if (isLocal()) {
            exists = new File(path).exists();
        } else if (isRoot()) {
            return RootHelper.fileExists(path);
        }

        return exists;
    }

    /**
     * Helper method to check file existence in otg
     */
    public boolean exists(Context context) {
        return (exists());
    }

    /**
     * Whether file is a simple file (i.e. not a directory/smb/otg/other)
     *
     * @return true if file; other wise false
     */
    public boolean isSimpleFile() {
        return !isSmb() && !isOtgFile() && !isCustomPath()
                && !android.util.Patterns.EMAIL_ADDRESS.matcher(path).matches() &&
                !new File(path).isDirectory() && !isOneDriveFile() && !isGoogleDriveFile()
                && !isDropBoxFile() && !isBoxFile() && !isSftp();
    }

    public boolean setLastModified(final long date) {
        File f = new File(path);
        return f.setLastModified(date);
    }

    public void mkdir(Context context) {
        if (isDropBoxFile()) {
            CloudStorage cloudStorageDropbox = dataUtils.getAccount(OpenMode.DROPBOX);
            try {
                cloudStorageDropbox.createFolder(CloudUtil.stripPath(OpenMode.DROPBOX, path));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (isOneDriveFile()) {
            CloudStorage cloudStorageOneDrive = dataUtils.getAccount(OpenMode.ONEDRIVE);
            try {
                cloudStorageOneDrive.createFolder(CloudUtil.stripPath(OpenMode.ONEDRIVE, path));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (isGoogleDriveFile()) {
            CloudStorage cloudStorageGdrive = dataUtils.getAccount(OpenMode.GDRIVE);
            try {
                cloudStorageGdrive.createFolder(CloudUtil.stripPath(OpenMode.GDRIVE, path));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            FileUtil.mkdir(new File(path), context);
    }

    public boolean delete(Context context, boolean rootmode) throws ShellNotRunningException {
        if (isRoot() && rootmode) {
            setMode(OpenMode.ROOT);
            RootUtils.delete(getPath());
        } else {
            FileUtil.deleteFile(new File(path), context);
        }
        return !exists();
    }

    /**
     * Returns the name of file excluding it's extension
     * If no extension is found then whole file name is returned
     */
    public String getNameString(Context context) {
        String fileName = getName(context);

        int extensionStartIndex = fileName.lastIndexOf(".");
        return fileName.substring(0, extensionStartIndex == -1 ? fileName.length() : extensionStartIndex);
    }

    /**
     * Generates a {@link LayoutElementParcelable} adapted compatible element.
     * Currently supports only local filesystem
     */
    public LayoutElementParcelable generateLayoutElement(boolean showThumbs) {
        switch (mode) {
            case FILE:
            case ROOT:
                File file = new File(path);
                LayoutElementParcelable layoutElement;
                if (isDirectory()) {

                    layoutElement = new LayoutElementParcelable(path, RootHelper.parseFilePermission(file),
                            "", folderSize() + "", 0, true, file.lastModified() + "",
                            false, showThumbs, mode);
                } else {
                    layoutElement = new LayoutElementParcelable(
                            file.getPath(), RootHelper.parseFilePermission(file),
                            file.getPath(), file.length() + "", file.length(), false, file.lastModified() + "",
                            false, showThumbs, mode);
                }
                return layoutElement;
            default:
                return null;
        }
    }
}
