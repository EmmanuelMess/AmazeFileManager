package com.amaze.filemanager.filesystem.files;

import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.NoCopySpan;
import com.amaze.filemanager.database.CloudHandler;
import com.amaze.filemanager.fragments.preference_fragments.PreferencesConstants;
import com.amaze.filemanager.utils.OTGUtil;
import com.amaze.filemanager.utils.OpenMode;

public final class HybridFileHelper {

    public static HybridFile getHybridFile(Context context, String path) {
        /*if (path.startsWith("smb://")) {
            mode = OpenMode.SMB;
        } else*/ if (path.startsWith("ssh://")) {
            return new SftpHybridFile(path);
        } /*else if (path.startsWith(OTGUtil.PREFIX_OTG)) {
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
        }*/
        throw new IllegalStateException();
    }
}
