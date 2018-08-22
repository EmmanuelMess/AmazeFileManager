package com.amaze.filemanager.filesystem.files;

import android.content.Context;
import com.amaze.filemanager.exceptions.ShellNotRunningException;
import com.amaze.filemanager.utils.OnFileFound;
import com.amaze.filemanager.utils.OpenMode;
import com.amaze.filemanager.utils.files.FileUtils;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;

public class SmbHybridFile extends AbstractHybridFile {
    public SmbHybridFile(String path) {
        super(path);
    }

    public SmbHybridFile(String path, String name, boolean isDirectory) {
        super(path, name, isDirectory);
        if (!isDirectory) this.path = path + name;
        else if (!name.endsWith("/")) this.path = path + name + "/";
        else this.path = path + name;
    }

    @Override
    public long lastModified() throws SmbException {
        SmbFile smbFile = getSmbFile();
        if (smbFile != null)
            return smbFile.lastModified();
        return new File("/").lastModified();
    }

    @Override
    public long length() {
        SmbFile smbFile = getSmbFile();
        if (smbFile != null) {
            try {
                return smbFile.length();
            } catch (SmbException e) {
                return 0;
            }
        }

        return 0;
    }

    @Override
    public long length(Context context) {
        SmbFile smbFile=getSmbFile();
        if(smbFile!=null) {
            try {
                return smbFile.length();
            } catch (SmbException e) {
                return 0;
            }
        }
        return 0;
    }

    @Override
    public String getName() {
        SmbFile smbFile = getSmbFile();
        if (smbFile != null)
            return smbFile.getName();
        return null;
    }

    @Override
    public String getName(Context context) {
        SmbFile smbFile=getSmbFile();
        if(smbFile!=null)
            return smbFile.getName();
        return null;
    }

    @Override
    public String getParent() {
        try {
            return new SmbFile(path).getParent();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public String getParent(Context context) {
        try {
            return new SmbFile(path).getParent();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public boolean isDirectory() {
        try {
            return new SmbFile(path).isDirectory();
        } catch (SmbException e) {
            e.printStackTrace();
            return false;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isDirectory(Context context) {
        try {
            return new SmbFile(path).isDirectory();
        } catch (SmbException e) {
            e.printStackTrace();
            return false;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public long folderSize() {
        try {
            return FileUtils.folderSize(new SmbFile(path));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    @Override
    public long folderSize(Context context) {
        try {
            return FileUtils.folderSize(new SmbFile(path));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return 0l;
        }
    }

    @Override
    public long getUsableSpace() {
        try {
            return (new SmbFile(path).getDiskFreeSpace());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return 0L;
        } catch (SmbException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    @Override
    public long getTotal(Context context) {
        // TODO: Find total storage space of SMB when JCIFS adds support
        try {
            return new SmbFile(path).getDiskFreeSpace();
        } catch (SmbException e) {
            e.printStackTrace();
            return 0;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void forEachChildrenFile(Context context, boolean isRoot, OnFileFound onFileFound) {
        try {
            SmbFile smbFile = new SmbFile(path);
            for (SmbFile smbFile1 : smbFile.listFiles()) {
                HybridFileParcelable baseFile=new HybridFileParcelable(smbFile1.getPath());
                baseFile.setName(smbFile1.getName());
                baseFile.setMode(OpenMode.SMB);
                baseFile.setDirectory(smbFile1.isDirectory());
                baseFile.setDate(smbFile1.lastModified());
                baseFile.setSize(baseFile.isDirectory()?0:smbFile1.length());
                onFileFound.onFileFound(baseFile);
            }
        } catch (MalformedURLException | SmbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<HybridFileParcelable> listFiles(Context context, boolean isRoot) {
        ArrayList<HybridFileParcelable> arrayList = new ArrayList<>();

        try {
            SmbFile smbFile = new SmbFile(path);
            for (SmbFile smbFile1 : smbFile.listFiles()) {
                HybridFileParcelable baseFile=new HybridFileParcelable(smbFile1.getPath());
                baseFile.setName(smbFile1.getName());
                baseFile.setMode(OpenMode.SMB);
                baseFile.setDirectory(smbFile1.isDirectory());
                baseFile.setDate(smbFile1.lastModified());
                baseFile.setSize(baseFile.isDirectory()?0:smbFile1.length());
                arrayList.add(baseFile);
            }
        } catch (MalformedURLException e) {
            arrayList.clear();
            e.printStackTrace();
        } catch (SmbException e) {
            arrayList.clear();
            e.printStackTrace();
        }

        return arrayList;
    }

    @Override
    public String getReadablePath(String path) {
        if (path.contains("@"))
            return "smb://" + path.substring(path.indexOf("@") + 1, path.length());
        else return path;
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new SmbFile(path).getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public InputStream getInputStream(Context context) {
        try {
            return new SmbFile(path).getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream(Context context) {
        try {
            return new SmbFile(path).getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean exists() {
        try {
            SmbFile smbFile = getSmbFile(2000);
            return smbFile != null && smbFile.exists();
        } catch (SmbException e) {
            return false;
        }
    }

    @Override
    public boolean setLastModified(long date) {
        try {
            new SmbFile(path).setLastModified(date);
            return true;
        } catch (SmbException e) {
            e.printStackTrace();
            return false;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void mkdir(Context context) {
        try {
            new SmbFile(path).mkdirs();
        } catch (SmbException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean delete(Context context, boolean rootmode) throws ShellNotRunningException {
        try {
            new SmbFile(path).delete();
        } catch (SmbException | MalformedURLException e) {
            e.printStackTrace();
        }

        return !exists();
    }

    public SmbFile getSmbFile(int timeout) {
        try {
            SmbFile smbFile = new SmbFile(path);
            smbFile.setConnectTimeout(timeout);
            return smbFile;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public SmbFile getSmbFile() {
        try {
            return new SmbFile(path);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
