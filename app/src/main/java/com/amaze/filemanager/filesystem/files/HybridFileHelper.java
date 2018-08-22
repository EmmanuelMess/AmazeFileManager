package com.amaze.filemanager.filesystem.files;

import android.content.Context;
import com.amaze.filemanager.database.CloudHandler;
import com.amaze.filemanager.filesystem.files.cloud.BoxHybridFile;
import com.amaze.filemanager.utils.OTGUtil;

public final class HybridFileHelper {

    public static AbstractHybridFile getHybridFile(Context context, String path) {
        if (path.startsWith("smb://")) {
            return new SmbHybridFile(path);
        } else if (path.startsWith("ssh://")) {
            return new SftpHybridFile(path);
        } else if (path.startsWith(OTGUtil.PREFIX_OTG)) {
            return new OtgHybridFile(path);
        } else if (isCustomPath(path)) {
            return new CustomHybridFile(path);
        } else if (path.startsWith(CloudHandler.CLOUD_PREFIX_BOX)) {
            return new BoxHybridFile(path);
        } /*else if (path.startsWith(CloudHandler.CLOUD_PREFIX_ONE_DRIVE)) {
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
        }*/
        throw new IllegalStateException();
    }

    private static boolean isCustomPath(String path) {
        return  path.equals("0") || path.equals("1") || path.equals("2") || path.equals("3") || path.equals("4") ||
                path.equals("5") || path.equals("6");
    }
}
