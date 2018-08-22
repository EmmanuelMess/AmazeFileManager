package com.amaze.filemanager.filesystem.files;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import com.amaze.filemanager.exceptions.ShellNotRunningException;
import com.amaze.filemanager.filesystem.ssh.SFtpClientTemplate;
import com.amaze.filemanager.filesystem.ssh.SshClientTemplate;
import com.amaze.filemanager.filesystem.ssh.SshClientUtils;
import com.amaze.filemanager.filesystem.ssh.Statvfs;
import com.amaze.filemanager.utils.OnFileFound;
import com.amaze.filemanager.utils.OpenMode;
import com.amaze.filemanager.utils.application.AppConfig;
import jcifs.smb.SmbException;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.Buffer;
import net.schmizz.sshj.sftp.*;
import net.schmizz.sshj.xfer.FilePermission;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumSet;

public class SftpHybridFile extends HybridFile {

    private static final String TAG = "SftpHybridFile";

    public SftpHybridFile(String path) {
        super(path);
    }

    public SftpHybridFile(String path, String name, boolean isDirectory) {
        super(path, name, isDirectory);

        this.path = path + "/" + name;
    }

    @Override
    public long lastModified() {
        SshClientUtils.execute(new SFtpClientTemplate(path) {
            @Override
            public Long execute(SFTPClient client) throws IOException {
                return client.mtime(SshClientUtils.extractRemotePathFrom(path));
            }
        });

        return new File("/").lastModified();// TODO: 22/08/18 check
    }

    @Override
    public long length() {
        return SshClientUtils.execute(new SFtpClientTemplate(path) {
            @Override
            public Long execute(SFTPClient client) throws IOException {
                return client.size(SshClientUtils.extractRemotePathFrom(path));
            }
        });
    }

    @Override
    public long length(Context context) {
        return length();// TODO: 22/08/18 check, was "((HybridFileParcelable)this).getSize();"
    }

    @Override
    public boolean isDirectory() {
        return isDirectory(AppConfig.getInstance());
    }

    @Override
    public boolean isDirectory(Context context) {
        return SshClientUtils.execute(new SFtpClientTemplate(path) {
            @Override
            public Boolean execute(SFTPClient client) throws IOException {
                try {
                    return client.stat(SshClientUtils.extractRemotePathFrom(path)).getType()
                            .equals(FileMode.Type.DIRECTORY);
                } catch (SFTPException notFound){
                    return false;
                }
            }
        });
    }

    @Override
    public long folderSize() {
        return folderSize(AppConfig.getInstance());
    }

    @Override
    public long folderSize(Context context) {
        return SshClientUtils.execute(new SFtpClientTemplate(path) {
            @Override
            public Long execute(SFTPClient client) throws IOException {
                return client.size(SshClientUtils.extractRemotePathFrom(path));
            }
        });
    }

    @Override
    public long getUsableSpace() {
        return SshClientUtils.execute(new SFtpClientTemplate(path) {
            @Override
            public Long execute(@NonNull SFTPClient client) throws IOException {
                try {
                    Statvfs.Response response = new Statvfs.Response(path,
                            client.getSFTPEngine().request(Statvfs.request(client, SshClientUtils.extractRemotePathFrom(path))).retrieve());
                    return response.diskFreeSpace();
                } catch (SFTPException e) {
                    Log.e(TAG, "Error querying server", e);
                    return 0L;
                } catch (Buffer.BufferException e) {
                    Log.e(TAG, "Error parsing reply", e);
                    return 0L;
                }
            }
        });
    }

    @Override
    public long getTotal(Context context) {
        return SshClientUtils.execute(new SFtpClientTemplate(path) {
            @Override
            public Long execute(@NonNull SFTPClient client) throws IOException {
                try {
                    Statvfs.Response response = new Statvfs.Response(path,
                            client.getSFTPEngine().request(Statvfs.request(client, SshClientUtils.extractRemotePathFrom(path))).retrieve());
                    return response.diskSize();
                } catch (SFTPException e) {
                    Log.e(TAG, "Error querying server", e);
                    return 0L;
                } catch (Buffer.BufferException e) {
                    Log.e(TAG, "Error parsing reply", e);
                    return 0L;
                }
            }
        });
    }

    @Override
    public void forEachChildrenFile(Context context, boolean isRoot, OnFileFound onFileFound) {
        try {
            SshClientUtils.execute(new SFtpClientTemplate(path) {
                @Override
                public Void execute(SFTPClient client) {
                    try {
                        for (RemoteResourceInfo info : client.ls(SshClientUtils.extractRemotePathFrom(path))) {
                            boolean isDirectory = info.isDirectory();
                            if(info.getAttributes().getType().equals(FileMode.Type.SYMLINK)){
                                FileAttributes symlinkAttrs = client.stat(info.getPath());
                                isDirectory = symlinkAttrs.getType().equals(FileMode.Type.DIRECTORY);
                            }
                            HybridFileParcelable f = new HybridFileParcelable(String.format("%s/%s", path, info.getName()));
                            f.setName(info.getName());
                            f.setMode(OpenMode.SFTP);
                            f.setDirectory(isDirectory);
                            f.setDate(info.getAttributes().getMtime() * 1000);
                            f.setSize(isDirectory ? 0 : info.getAttributes().getSize());
                            f.setPermission(Integer.toString(FilePermission.toMask(info.getAttributes().getPermissions()), 8));
                            onFileFound.onFileFound(f);
                        }
                    } catch (IOException e) {
                        Log.w("DEBUG.listFiles", "IOException", e);
                    }
                    return null;
                }
            });
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<HybridFileParcelable> listFiles(Context context, boolean isRoot) {
        ArrayList<HybridFileParcelable> arrayList = new ArrayList<>();

        try {
            arrayList = SshClientUtils.execute(new SFtpClientTemplate(path) {
                @Override
                public ArrayList<HybridFileParcelable> execute(SFTPClient client) {
                    ArrayList<HybridFileParcelable> retval = new ArrayList<HybridFileParcelable>();
                    try {
                        for (RemoteResourceInfo info : client.ls(SshClientUtils.extractRemotePathFrom(path))) {
                            HybridFileParcelable f = new HybridFileParcelable(String.format("%s/%s", path, info.getName()));
                            f.setName(info.getName());
                            f.setMode(OpenMode.SFTP);
                            f.setDirectory(info.isDirectory());
                            f.setDate(info.getAttributes().getMtime() * 1000);
                            f.setSize(f.isDirectory() ? 0 : info.getAttributes().getSize());
                            f.setPermission(Integer.toString(FilePermission.toMask(info.getAttributes().getPermissions()), 8));
                            retval.add(f);
                        }
                    } catch (IOException e) {
                        Log.w("DEBUG.listFiles", "IOException", e);
                    }
                    return retval;
                }
            });
        } catch(Exception e){
            e.printStackTrace();
            arrayList.clear();
        }

        return arrayList;
    }

    @Override
    public String getReadablePath(String path) {
        if (path.contains("@"))
            return "ssh://" + path.substring(path.indexOf("@") + 1, path.length());
        else return path;
    }

    @Override
    public InputStream getInputStream() {
        return SshClientUtils.execute(new SFtpClientTemplate(path) {
            @Override
            public InputStream execute(SFTPClient client) throws IOException {
                final RemoteFile rf = client.open(SshClientUtils.extractRemotePathFrom(path));
                return rf. new RemoteFileInputStream(){
                    @Override
                    public void close() throws IOException {
                        try
                        {
                            super.close();
                        }
                        finally
                        {
                            rf.close();
                        }
                    }
                };
            }
        });
    }

    @Override
    public InputStream getInputStream(Context context) {
        return SshClientUtils.execute(new SFtpClientTemplate(path, false) {
            @Override
            public InputStream execute(final SFTPClient client) throws IOException {
                final RemoteFile rf = client.open(SshClientUtils.extractRemotePathFrom(path));
                return rf. new RemoteFileInputStream(){
                    @Override
                    public void close() throws IOException {
                        try
                        {
                            super.close();
                        }
                        finally
                        {
                            rf.close();
                            client.close();
                        }
                    }
                };
            }
        });
    }

    @Override
    public OutputStream getOutputStream(Context context) {
        return SshClientUtils.execute(new SshClientTemplate(path, false) {
            @Override
            public OutputStream execute(final SSHClient ssh) throws IOException {
                final SFTPClient client = ssh.newSFTPClient();
                final RemoteFile rf = client.open(SshClientUtils.extractRemotePathFrom(path),
                        EnumSet.of(net.schmizz.sshj.sftp.OpenMode.WRITE,
                                net.schmizz.sshj.sftp.OpenMode.CREAT));
                return rf.new RemoteFileOutputStream(){
                    @Override
                    public void close() throws IOException {
                        try
                        {
                            super.close();
                        }
                        finally
                        {
                            rf.close();
                            client.close();
                        }
                    }
                };
            }
        });
    }

    @Override
    public boolean exists() {
        return SshClientUtils.execute(new SFtpClientTemplate(path) {
            @Override
            public Boolean execute(SFTPClient client) throws IOException {
                try {
                    return client.stat(SshClientUtils.extractRemotePathFrom(path)) != null;
                } catch (SFTPException notFound){
                    return false;
                }
            }
        });
    }

    @Override
    public void mkdir(Context context) {
        SshClientUtils.execute(new SFtpClientTemplate(path) {
            @Override
            public Void execute(SFTPClient client) {
                try {
                    client.mkdir(SshClientUtils.extractRemotePathFrom(path));
                } catch(IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    @Override
    public boolean delete(Context context, boolean rootmode) {
        SshClientUtils.execute(new SFtpClientTemplate(path) {
            @Override
            public Void execute(SFTPClient client) throws IOException {
                if(isDirectory(AppConfig.getInstance()))
                    client.rmdir(SshClientUtils.extractRemotePathFrom(path));
                else
                    client.rm(SshClientUtils.extractRemotePathFrom(path));
                return null;
            }
        });
        return true;
    }


}
