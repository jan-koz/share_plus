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

public class MyReceiver extends BroadcastReceiver {
    static boolean didGoToApp = false;
    @Override
    public void onReceive(Context context, Intent intent) {

        String selectedAppPackage = String.valueOf(intent.getExtras().get(intent.EXTRA_CHOSEN_COMPONENT));
        System.out.println(selectedAppPackage);
        if(selectedAppPackage != null){
            didGoToApp = true;
        }
        System.out.println(didGoToApp);
     
    }
    
  }