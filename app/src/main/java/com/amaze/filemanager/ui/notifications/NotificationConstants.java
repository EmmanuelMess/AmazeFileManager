package com.amaze.filemanager.ui.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.amaze.filemanager.R;

/**
 * @author Emmanuel
 *         on 17/9/2017, at 13:34.
 */

public class NotificationConstants {

    public static final int NORMAL = 0, FTP = 1;

    public static final String CHANNEL_NORMAL_ID = "normalChannel";
    public static final String CHANNEL_FTP_ID = "ftpChannel";

    /**
     * This creates a channel (API >= 26) or applies the correct metadata to a notification (API < 26)
     */
    public static void setMetadata(Context context, NotificationCompat.Builder notification, int type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            switch (type) {
                case NORMAL:
                    createNormalChannel(context);
                    break;
                case FTP:
                    createFtpChannel(context);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized type:" + type);
            }
        } else {
            switch (type) {
                case NORMAL:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        notification.setCategory(Notification.CATEGORY_SERVICE);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        notification.setPriority(Notification.PRIORITY_MIN);
                    }
                    break;
                case FTP:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        notification.setCategory(Notification.CATEGORY_SERVICE);
                        notification.setVisibility(Notification.VISIBILITY_PUBLIC);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        notification.setPriority(Notification.PRIORITY_MAX);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized type:" + type);
            }
        }
    }

    /**
     * You CANNOT call this from android < O.
     * THis channel is set so it doesn't bother the user, but it has importance.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void createFtpChannel(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(mNotificationManager.getNotificationChannel(CHANNEL_FTP_ID) == null) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_FTP_ID,
                    context.getString(R.string.channelname_ftp), NotificationManager.IMPORTANCE_HIGH);
            // Configure the notification channel.
            mChannel.setDescription(context.getString(R.string.channeldescription_ftp));
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    /**
     * You CANNOT call this from android < O.
     * THis channel is set so it doesn't bother the user, with the lowest importance.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void createNormalChannel(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(mNotificationManager.getNotificationChannel(CHANNEL_NORMAL_ID) == null) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_NORMAL_ID,
                    context.getString(R.string.channelname_normal), NotificationManager.IMPORTANCE_MIN);
            // Configure the notification channel.
            mChannel.setDescription(context.getString(R.string.channeldescription_normal));
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

}
