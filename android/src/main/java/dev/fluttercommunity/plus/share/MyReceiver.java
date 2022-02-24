package dev.fluttercommunity.plus.share;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodChannel;

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

        // final flutterEngine = new FlutterEngine(context, null);
        // DartExecutor executor = flutterEngine.getDartExecutor();
        // final backgroundMethodChannel = new MethodChannel(executor.getBinaryMessenger(), "your channel name");
        // backgroundMethodChannel.setMethodCallHandler(this);
        // // Get and launch the users app isolate manually:
        // executor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault());

        // flutterEngine.getBroadcastReceiverControlSurface().attachToBroadcastReceiver(this, null); 

        String selectedAppPackage = String.valueOf(intent.getExtras().get(intent.EXTRA_CHOSEN_COMPONENT));
        System.out.println(selectedAppPackage);
        if(selectedAppPackage != null){
            didGoToApp = true;
        }
        System.out.println(didGoToApp);
    }
    
  }