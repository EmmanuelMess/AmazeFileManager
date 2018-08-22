package com.amaze.filemanager.filesystem.files;

import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import com.amaze.filemanager.database.CloudHandler;
import com.amaze.filemanager.filesystem.files.cloud.BoxHybridFile;
import com.amaze.filemanager.filesystem.files.cloud.DropboxHybridFile;
import com.amaze.filemanager.filesystem.files.cloud.GdriveHybridFile;
import com.amaze.filemanager.filesystem.files.cloud.OnedriveHybridFile;
import com.amaze.filemanager.fragments.preference_fragments.PreferencesConstants;
import com.amaze.filemanager.utils.OTGUtil;

import java.io.File;

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
        } else if (path.startsWith(CloudHandler.CLOUD_PREFIX_ONE_DRIVE)) {
            return new OnedriveHybridFile(path);
        } else if (path.startsWith(CloudHandler.CLOUD_PREFIX_GOOGLE_DRIVE)) {
            return new GdriveHybridFile(path);
        } else if (path.startsWith(CloudHandler.CLOUD_PREFIX_DROPBOX)) {
            return new DropboxHybridFile(path);
        } else if(context == null) {
            return new FileHybridFile(path);
        } else {
            boolean rootmode = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesConstants.PREFERENCE_ROOTMODE, false);
            File file = new File(path);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                if (rootmode && !file.canRead()) {
                    return new RootHybridFile(path);
                }
                return new FileHybridFile(path);
            } else {
                if (FileUtil.isOnExtSdCard(file, context)) {
                    return new FileHybridFile(path);
                } else if (rootmode && !file.canRead()) {
                    return new RootHybridFile(path);
                }

                return new FileHybridFile(path);
            }
        }
    }

    private static boolean isCustomPath(String path) {
        return  path.equals("0") || path.equals("1") || path.equals("2") || path.equals("3") || path.equals("4") ||
                path.equals("5") || path.equals("6");
    }
}
