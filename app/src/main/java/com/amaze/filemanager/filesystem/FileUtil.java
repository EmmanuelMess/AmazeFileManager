/*
 * Copyright (C) 2014-2020 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>,
 * Emmanuel Messulam<emmanuelbendavid@gmail.com>, Raymond Lai <airwave209gt at gmail.com> and Contributors.
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.amaze.filemanager.filesystem;

import static com.amaze.filemanager.filesystem.smb.CifsContexts.SMB_URI_PREFIX;
import static com.amaze.filemanager.filesystem.ssh.SshConnectionPool.SSH_URI_PREFIX;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.amaze.filemanager.R;
import com.amaze.filemanager.application.AppConfig;
import com.amaze.filemanager.database.CloudHandler;
import com.amaze.filemanager.exceptions.NotAllowedException;
import com.amaze.filemanager.exceptions.OperationWouldOverwriteException;
import com.amaze.filemanager.file_operations.filesystem.OpenMode;
import com.amaze.filemanager.filesystem.cloud.CloudUtil;
import com.amaze.filemanager.filesystem.files.GenericCopyUtil;
import com.amaze.filemanager.ui.activities.MainActivity;
import com.amaze.filemanager.ui.preference.PreferencesConstants;
import com.amaze.filemanager.utils.DataUtils;
import com.amaze.filemanager.utils.OTGUtil;
import com.amaze.filemanager.utils.SmbUtil;
import com.cloudrail.si.interfaces.CloudStorage;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;

import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import jcifs.smb.SmbFile;
import kotlin.NotImplementedError;

/** Utility class for helping parsing file systems. */
public abstract class FileUtil {

  private static final String LOG = "AmazeFileUtils";

  private static final Pattern FILENAME_REGEX =
      Pattern.compile("[\\\\\\/:\\*\\?\"<>\\|\\x01-\\x1F\\x7F]", Pattern.CASE_INSENSITIVE);

  /**
   * Determine the camera folder. There seems to be no Android API to work for real devices, so this
   * is a best guess.
   *
   * @return the default camera folder.
   */
  // TODO the function?

  @Nullable
  public static OutputStream getOutputStream(final File target, Context context)
      throws FileNotFoundException {
    OutputStream outStream = null;
    // First try the normal way
    if (FileProperties.isWritable(target)) {
      // standard way
      outStream = new FileOutputStream(target);
    } else {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        // Storage Access Framework
        DocumentFile targetDocument = getDocumentFile(target, false, context);
        if (targetDocument == null) return null;
        outStream = context.getContentResolver().openOutputStream(targetDocument.getUri());
      } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
        // Workaround for Kitkat ext SD card
        return MediaStoreHack.getOutputStream(context, target.getPath());
      }
    }
    return outStream;
  }

  /** Writes uri stream from external application to the specified path */
  public static final void writeUriToStorage(
      @NonNull final MainActivity mainActivity,
      @NonNull final ArrayList<Uri> uris,
      @NonNull final ContentResolver contentResolver,
      @NonNull final String currentPath) {

    MaybeOnSubscribe<List<String>> writeUri =
        (MaybeOnSubscribe<List<String>>)
            emitter -> {
              List<String> retval = new ArrayList<>();

              for (Uri uri : uris) {

                BufferedInputStream bufferedInputStream = null;
                try {
                  bufferedInputStream =
                      new BufferedInputStream(contentResolver.openInputStream(uri));
                } catch (FileNotFoundException e) {
                  emitter.onError(e);
                  return;
                }

                BufferedOutputStream bufferedOutputStream = null;

                try {
                  DocumentFile documentFile = DocumentFile.fromSingleUri(mainActivity, uri);
                  String filename = documentFile.getName();
                  if (filename == null) {
                    filename = uri.getLastPathSegment();

                    // For cleaning up slashes. Back in #1217 there is a case of
                    // Uri.getLastPathSegment() end up with a full file path
                    if (filename.contains("/"))
                      filename = filename.substring(filename.lastIndexOf('/') + 1);
                  }

                  String finalFilePath = currentPath + "/" + filename;
                  DataUtils dataUtils = DataUtils.getInstance();

                  HybridFile hFile = new HybridFile(OpenMode.UNKNOWN, currentPath);
                  hFile.generateMode(mainActivity);

                  switch (hFile.getMode()) {
                    case FILE:
                    case ROOT:
                      File targetFile = new File(finalFilePath);
                      if (!FileProperties.isWritableNormalOrSaf(
                          targetFile.getParentFile(), mainActivity.getApplicationContext())) {
                        emitter.onError(new NotAllowedException());
                        return;
                      }

                      DocumentFile targetDocumentFile =
                          getDocumentFile(targetFile, false, mainActivity.getApplicationContext());

                      // Fallback, in case getDocumentFile() didn't properly return a
                      // DocumentFile
                      // instance
                      if (targetDocumentFile == null) {
                        targetDocumentFile = DocumentFile.fromFile(targetFile);
                      }

                      // Lazy check... and in fact, different apps may pass in URI in different
                      // formats, so we could only check filename matches
                      // FIXME?: Prompt overwrite instead of simply blocking
                      if (targetDocumentFile.exists() && targetDocumentFile.length() > 0) {
                        emitter.onError(new OperationWouldOverwriteException());
                        return;
                      }

                      bufferedOutputStream =
                          new BufferedOutputStream(
                              contentResolver.openOutputStream(targetDocumentFile.getUri()));
                      retval.add(targetFile.getPath());
                      break;
                    case SMB:
                      SmbFile targetSmbFile = SmbUtil.create(finalFilePath);
                      if (targetSmbFile.exists()) {
                        emitter.onError(new OperationWouldOverwriteException());
                        return;
                      } else {
                        OutputStream outputStream = targetSmbFile.getOutputStream();
                        bufferedOutputStream = new BufferedOutputStream(outputStream);
                        retval.add(HybridFile.parseAndFormatUriForDisplay(targetSmbFile.getPath()));
                      }
                      break;
                    case SFTP:
                      // FIXME: implement support
                      AppConfig.toast(mainActivity, mainActivity.getString(R.string.not_allowed));
                      emitter.onError(new NotImplementedError());
                      return;
                    case DROPBOX:
                    case BOX:
                    case ONEDRIVE:
                    case GDRIVE:
                      OpenMode mode = hFile.getMode();

                      CloudStorage cloudStorage = dataUtils.getAccount(mode);
                      String path = CloudUtil.stripPath(mode, finalFilePath);
                      cloudStorage.upload(path, bufferedInputStream, documentFile.length(), true);
                      retval.add(path);
                      break;
                    case OTG:
                      DocumentFile documentTargetFile =
                          OTGUtil.getDocumentFile(finalFilePath, mainActivity, true);

                      if (documentTargetFile.exists()) {
                        emitter.onError(new OperationWouldOverwriteException());
                        return;
                      }

                      bufferedOutputStream =
                          new BufferedOutputStream(
                              contentResolver.openOutputStream(documentTargetFile.getUri()),
                              GenericCopyUtil.DEFAULT_BUFFER_SIZE);

                      retval.add(documentTargetFile.getUri().getPath());
                      break;
                    default:
                      return;
                  }

                  int count = 0;
                  byte[] buffer = new byte[GenericCopyUtil.DEFAULT_BUFFER_SIZE];

                  while (count != -1) {
                    count = bufferedInputStream.read(buffer);
                    if (count != -1) {

                      bufferedOutputStream.write(buffer, 0, count);
                    }
                  }
                  bufferedOutputStream.flush();

                } catch (IOException e) {
                  emitter.onError(e);
                  return;
                } finally {
                  try {
                    if (bufferedInputStream != null) {
                      bufferedInputStream.close();
                    }
                    if (bufferedOutputStream != null) {
                      bufferedOutputStream.close();
                    }
                  } catch (IOException e) {
                    emitter.onError(e);
                  }
                }
              }

              if (retval.size() > 0) {
                emitter.onSuccess(retval);
              } else {
                emitter.onError(new Exception());
              }
            };

    Maybe.create(writeUri)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            new MaybeObserver<List<String>>() {
              @Override
              public void onSubscribe(@NonNull Disposable d) {}

              @Override
              public void onSuccess(@NonNull List<String> paths) {
                if (paths.size() == 1) {
                  Toast.makeText(
                          mainActivity,
                          mainActivity.getString(R.string.saved_single_file, paths.get(0)),
                          Toast.LENGTH_LONG)
                      .show();
                } else {
                  Toast.makeText(
                          mainActivity,
                          mainActivity.getString(R.string.saved_multi_files, paths.size()),
                          Toast.LENGTH_LONG)
                      .show();
                }
              }

              @Override
              public void onError(@NonNull Throwable e) {
                if (e instanceof OperationWouldOverwriteException) {
                  AppConfig.toast(mainActivity, mainActivity.getString(R.string.cannot_overwrite));
                  return;
                }
                if (e instanceof NotAllowedException) {
                  AppConfig.toast(
                      mainActivity, mainActivity.getResources().getString(R.string.not_allowed));
                }

                Log.e(
                    getClass().getSimpleName(),
                    "Failed to write uri to storage due to " + e.getCause());
                e.printStackTrace();
              }

              @Override
              public void onComplete() {}
            });
  }

  /**
   * Get a list of external SD card paths. (Kitkat or higher.)
   *
   * @return A list of external SD card paths.
   */
  @TargetApi(Build.VERSION_CODES.KITKAT)
  private static String[] getExtSdCardPaths(Context context) {
    List<String> paths = new ArrayList<>();
    for (File file : context.getExternalFilesDirs("external")) {
      if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
        int index = file.getAbsolutePath().lastIndexOf("/Android/data");
        if (index < 0) {
          Log.w(LOG, "Unexpected external file dir: " + file.getAbsolutePath());
        } else {
          String path = file.getAbsolutePath().substring(0, index);
          try {
            path = new File(path).getCanonicalPath();
          } catch (IOException e) {
            // Keep non-canonical path.
          }
          paths.add(path);
        }
      }
    }
    if (paths.isEmpty()) paths.add("/storage/sdcard1");
    return paths.toArray(new String[0]);
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  public static String[] getExtSdCardPathsForActivity(Context context) {
    List<String> paths = new ArrayList<>();
    for (File file : context.getExternalFilesDirs("external")) {
      if (file != null) {
        int index = file.getAbsolutePath().lastIndexOf("/Android/data");
        if (index < 0) {
          Log.w(LOG, "Unexpected external file dir: " + file.getAbsolutePath());
        } else {
          String path = file.getAbsolutePath().substring(0, index);
          try {
            path = new File(path).getCanonicalPath();
          } catch (IOException e) {
            // Keep non-canonical path.
          }
          paths.add(path);
        }
      }
    }
    if (paths.isEmpty()) paths.add("/storage/sdcard1");
    return paths.toArray(new String[0]);
  }

  /**
   * Determine the main folder of the external SD card containing the given file.
   *
   * @param file the file.
   * @return The main folder of the external SD card containing this file, if the file is on an SD
   *     card. Otherwise, null is returned.
   */
  @TargetApi(Build.VERSION_CODES.KITKAT)
  private static String getExtSdCardFolder(final File file, Context context) {
    String[] extSdPaths = getExtSdCardPaths(context);
    try {
      for (int i = 0; i < extSdPaths.length; i++) {
        if (file.getCanonicalPath().startsWith(extSdPaths[i])) {
          return extSdPaths[i];
        }
      }
    } catch (IOException e) {
      return null;
    }
    return null;
  }

  /**
   * Determine if a file is on external sd card. (Kitkat or higher.)
   *
   * @param file The file.
   * @return true if on external sd card.
   */
  @TargetApi(Build.VERSION_CODES.KITKAT)
  public static boolean isOnExtSdCard(final File file, Context c) {
    return getExtSdCardFolder(file, c) != null;
  }

  /**
   * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If
   * the file is not existing, it is created.
   *
   * @param file The file.
   * @param isDirectory flag indicating if the file should be a directory.
   * @return The DocumentFile
   */
  public static DocumentFile getDocumentFile(
      final File file, final boolean isDirectory, Context context) {

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) return DocumentFile.fromFile(file);

    String baseFolder = getExtSdCardFolder(file, context);
    boolean originalDirectory = false;
    if (baseFolder == null) {
      return null;
    }

    String relativePath = null;
    try {
      String fullPath = file.getCanonicalPath();
      if (!baseFolder.equals(fullPath)) relativePath = fullPath.substring(baseFolder.length() + 1);
      else originalDirectory = true;
    } catch (IOException e) {
      return null;
    } catch (Exception f) {
      originalDirectory = true;
      // continue
    }
    String as =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PreferencesConstants.PREFERENCE_URI, null);

    Uri treeUri = null;
    if (as != null) treeUri = Uri.parse(as);
    if (treeUri == null) {
      return null;
    }

    // start with root of SD card and then parse through document tree.
    DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
    if (originalDirectory) return document;
    String[] parts = relativePath.split("\\/");
    for (int i = 0; i < parts.length; i++) {
      DocumentFile nextDocument = document.findFile(parts[i]);

      if (nextDocument == null) {
        if ((i < parts.length - 1) || isDirectory) {
          nextDocument = document.createDirectory(parts[i]);
        } else {
          nextDocument = document.createFile("image", parts[i]);
        }
      }
      document = nextDocument;
    }

    return document;
  }

  // Utility methods for Kitkat

  /**
   * Checks whether the target path exists or is writable
   *
   * @param f the target path
   * @return 1 if exists or writable, 0 if not writable
   */
  public static int checkFolder(final String f, Context context) {
    if (f == null) return 0;
    if (f.startsWith(SMB_URI_PREFIX)
        || f.startsWith(SSH_URI_PREFIX)
        || f.startsWith(OTGUtil.PREFIX_OTG)
        || f.startsWith(CloudHandler.CLOUD_PREFIX_BOX)
        || f.startsWith(CloudHandler.CLOUD_PREFIX_GOOGLE_DRIVE)
        || f.startsWith(CloudHandler.CLOUD_PREFIX_DROPBOX)
        || f.startsWith(CloudHandler.CLOUD_PREFIX_ONE_DRIVE)) return 1;

    File folder = new File(f);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        && FileUtil.isOnExtSdCard(folder, context)) {
      if (!folder.exists() || !folder.isDirectory()) {
        return 0;
      }

      // On Android 5, trigger storage access framework.
      if (FileProperties.isWritableNormalOrSaf(folder, context)) {
        return 1;
      }
    } else if (Build.VERSION.SDK_INT == 19 && FileUtil.isOnExtSdCard(folder, context)) {
      // Assume that Kitkat workaround works
      return 1;
    } else if (folder.canWrite()) {
      return 1;
    } else {
      return 0;
    }
    return 0;
  }

  /**
   * Validate given text is a valid filename.
   *
   * @param text
   * @return true if given text is a valid filename
   */
  public static boolean isValidFilename(String text) {
    // It's not easy to use regex to detect single/double dot while leaving valid values
    // (filename.zip) behind...
    // So we simply use equality to check them
    return (!FILENAME_REGEX.matcher(text).find()) && !".".equals(text) && !"..".equals(text);
  }
}
