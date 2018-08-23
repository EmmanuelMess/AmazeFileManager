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
        return new File("/").lastModified();
    }

    /**
     * @deprecated use {@link #length(Context)} to handle content resolvers
     */
    public long length() {
        return 0L;
    }

    /**
     * Helper method to find length
     */
    public long length(Context context) {
        return 0L;
    }

    public String getPath() {
        return path;
    }

    /**
     * @deprecated use {@link #getName(Context)}
     */
    public String getName() {
        StringBuilder builder = new StringBuilder(path);
        return builder.substring(builder.lastIndexOf("/") + 1, builder.length());
    }

    public String getName(Context context) {
        StringBuilder builder = new StringBuilder(path);
        return builder.substring(builder.lastIndexOf("/") + 1, builder.length());
    }

    public boolean isCustomPath() {
        return false;
    }

    /**
     * Returns a path to parent for various {@link #mode}
     * @deprecated use {@link #getParent(Context)} to handle content resolvers
     */
    public String getParent() {
        StringBuilder builder = new StringBuilder(path);
        return builder.substring(0, builder.length() - (getName().length() + 1));
    }

    /**
     * Helper method to get parent path
     */
    public String getParent(Context context) {
        StringBuilder builder = new StringBuilder(path);
        StringBuilder parentPathBuilder = new StringBuilder(builder.substring(0,
                builder.length() - (getName(context).length() + 1)));
        return parentPathBuilder.toString();
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
        return new File(path).isDirectory();
    }

    public boolean isDirectory(Context context) {
        return new File(path).isDirectory();
    }

    /**
     * @deprecated use {@link #folderSize(Context)}
     */
    public long folderSize() {
        return 0L;
    }

    /**
     * Helper method to get length of folder in an otg
     */
    public long folderSize(Context context) {
        return 0L;
    }


    /**
     * Gets usable i.e. free space of a device
     */
    public long getUsableSpace() {
        return 0L;
    }

    /**
     * Gets total size of the disk
     */
    public long getTotal(Context context) {
        return 0l;
    }

    /**
     * Helper method to list children of this file
     */
    public void forEachChildrenFile(Context context, boolean isRoot, OnFileFound onFileFound) {
        RootHelper.getFiles(path, isRoot, true, null, onFileFound);
    }

    /**
     * Helper method to list children of this file
     * @deprecated use forEachChildrenFile()
     */
    public ArrayList<HybridFileParcelable> listFiles(Context context, boolean isRoot) {
        return RootHelper.getFilesList(path, isRoot, true, null);
    }

    public String getReadablePath(String path) {
        return path;
    }

    /**
     * Handles getting input stream for various {@link OpenMode}
     * @deprecated use {@link #getInputStream(Context)} which allows handling content resolver
     */
    public InputStream getInputStream() {
        try {
            return new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public InputStream getInputStream(Context context) {
        try {
            return new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public OutputStream getOutputStream(Context context) {
        try {
            return FileUtil.getOutputStream(new File(path), context);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean exists() {
        return false;
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
        return false;
    }

    public boolean setLastModified(final long date) {
        File f = new File(path);
        return f.setLastModified(date);
    }

    public void mkdir(Context context) {
        FileUtil.mkdir(new File(path), context);
    }

    public boolean delete(Context context, boolean rootmode) throws ShellNotRunningException {
        FileUtil.deleteFile(new File(path), context);
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
        return null;
    }
}
