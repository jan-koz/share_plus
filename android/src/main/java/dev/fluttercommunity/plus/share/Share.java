// Copyright 2019 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package dev.fluttercommunity.plus.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.net.ConnectivityManager;
import android.content.IntentFilter;
import android.widget.Toast;

 class MyReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
      //super.onReceive(context, intent);
      String selectedAppPackage = String.valueOf(intent.getExtras().get(intent.EXTRA_CHOSEN_COMPONENT));
      ComponentName clickedComponent = intent.getParcelableExtra(intent.EXTRA_CHOSEN_COMPONENT);

      System.out.println("OnRECEIVEL");
      System.out.println(selectedAppPackage);
      System.out.println("selectedAppPackage on receive");
      // do something here
  }
  
}

/** Handles share intent. */
class Share {

  private final Context context;
  private Activity activity;

  private final String providerAuthority;

  /**
   * Constructs a Share object. The {@code context} and {@code activity} are used to start the share
   * intent. The {@code activity} might be null when constructing the {@link Share} object and set
   * to non-null when an activity is available using {@link #setActivity(Activity)}.
   */
  Share(Context context, Activity activity) {
    this.context = context;
    this.activity = activity;

    this.providerAuthority = getContext().getPackageName() + ".flutter.share_provider";
  }

  /**
   * Sets the activity when an activity is available. When the activity becomes unavailable, use
   * this method to set it to null.
   */
  void setActivity(Activity activity) {
    this.activity = activity;
  }

  void share(String text, String subject) {
    if (text == null || text.isEmpty()) {
      throw new IllegalArgumentException("Non-empty text expected :)(");
    }
    BroadcastReceiver br = new MyReceiver();
    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    filter.addAction(Intent.ACTION_SEND);
    context.registerReceiver(br, filter);

    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.putExtra(Intent.EXTRA_TEXT, text);
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
    shareIntent.setType("text/plain");
    PendingIntent pi = PendingIntent.getBroadcast(this.context, 0,
        new Intent(this.context, MyReceiver.class),
        PendingIntent.FLAG_UPDATE_CURRENT);
    shareIntent = Intent.createChooser(shareIntent, null, pi.getIntentSender());
    this.context.sendBroadcast(shareIntent);
    //Intent chooserIntent = Intent.createChooser(shareIntent, null, pi.getIntentSender());
    activity.startActivity(shareIntent);

  }



  void shareFiles(List<String> paths, List<String> mimeTypes, String text, String subject)
      throws IOException {
    if (paths == null || paths.isEmpty()) {
      throw new IllegalArgumentException("Non-empty path expected");
    }

    clearShareCacheFolder();
    ArrayList<Uri> fileUris = getUrisForPaths(paths);

    Intent shareIntent = new Intent();
    if (fileUris.isEmpty()) {
      share(text, subject);
      return;
    } else if (fileUris.size() == 1) {
      shareIntent.setAction(Intent.ACTION_SEND);
      shareIntent.putExtra(Intent.EXTRA_STREAM, fileUris.get(0));
      shareIntent.setType(
          !mimeTypes.isEmpty() && mimeTypes.get(0) != null ? mimeTypes.get(0) : "*/*");
    } else {
      shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
      shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris);
      shareIntent.setType(reduceMimeTypes(mimeTypes));
    }
    if (text != null) shareIntent.putExtra(Intent.EXTRA_TEXT, text);
    if (subject != null) shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    Intent chooserIntent = Intent.createChooser(shareIntent, null /* dialog title optional */);

    List<ResolveInfo> resInfoList =
        getContext()
            .getPackageManager()
            .queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY);
    for (ResolveInfo resolveInfo : resInfoList) {
      String packageName = resolveInfo.activityInfo.packageName;
      for (Uri fileUri : fileUris) {
        getContext()
            .grantUriPermission(
                packageName,
                fileUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
      }
    }

    startActivity(chooserIntent);
  }

  private void startActivity(Intent intent) {
    if (activity != null) {
      activity.startActivity(intent);
    } else if (context != null) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);
    } else {
      throw new IllegalStateException("Both context and activity are null");
    }
  }

  private ArrayList<Uri> getUrisForPaths(List<String> paths) throws IOException {
    ArrayList<Uri> uris = new ArrayList<>(paths.size());

    for (String path : paths) {
      File file = new File(path);
      if (fileIsInShareCache(file)) {
        // If file was saved in '.../caches/share_plus' it will have been erased by 'clearShareCacheFolder();'
        throw new IOException(
            "File to share not allowed to be located in '"
                + getShareCacheFolder().getCanonicalPath()
                + "'");
      }
      file = copyToShareCacheFolder(file);

      uris.add(FileProvider.getUriForFile(getContext(), providerAuthority, file));
    }

    return uris;
  }

  private String reduceMimeTypes(List<String> mimeTypes) {
    if (mimeTypes.size() > 1) {
      String reducedMimeType = mimeTypes.get(0);
      for (int i = 1; i < mimeTypes.size(); i++) {
        String mimeType = mimeTypes.get(i);
        if (!reducedMimeType.equals(mimeType)) {
          if (getMimeTypeBase(mimeType).equals(getMimeTypeBase(reducedMimeType))) {
            reducedMimeType = getMimeTypeBase(mimeType) + "/*";
          } else {
            reducedMimeType = "*/*";
            break;
          }
        }
      }
      return reducedMimeType;
    } else if (mimeTypes.size() == 1) {
      return mimeTypes.get(0);
    } else {
      return "*/*";
    }
  }

  @NonNull
  private String getMimeTypeBase(String mimeType) {
    if (mimeType == null || !mimeType.contains("/")) {
      return "*";
    }

    return mimeType.substring(0, mimeType.indexOf("/"));
  }

  private boolean fileIsInShareCache(File file) {
    try {
      String filePath = file.getCanonicalPath();
      return filePath.startsWith(getShareCacheFolder().getCanonicalPath());
    } catch (IOException e) {
      return false;
    }
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private void clearShareCacheFolder() {
    File folder = getShareCacheFolder();
    final File[] files = folder.listFiles();
    if (folder.exists() && files != null) {
      for (File file : files) {
        file.delete();
      }
      folder.delete();
    }
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private File copyToShareCacheFolder(File file) throws IOException {
    File folder = getShareCacheFolder();
    if (!folder.exists()) {
      folder.mkdirs();
    }

    File newFile = new File(folder, file.getName());
    copy(file, newFile);
    return newFile;
  }

  @NonNull
  private File getShareCacheFolder() {
    return new File(getContext().getCacheDir(), "share_plus");
  }

  private Context getContext() {
    if (activity != null) {
      return activity;
    }
    if (context != null) {
      return context;
    }

    throw new IllegalStateException("Both context and activity are null");
  }

  private static void copy(File src, File dst) throws IOException {
    try (InputStream in = new FileInputStream(src)) {
      try (OutputStream out = new FileOutputStream(dst)) {
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
      }
    }
  }
}
