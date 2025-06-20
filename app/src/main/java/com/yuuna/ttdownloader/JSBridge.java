package com.yuuna.ttdownloader;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class JSBridge {
    private Context context;

    public JSBridge(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public void saveBase64File(String base64, String filename) {
        try {
            byte[] data = Base64.decode(base64, Base64.DEFAULT);
            File appDirectory;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) appDirectory = new File(context.getExternalFilesDir(null), "TTDownloader");
            else appDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "TTDownloader");
            if (!appDirectory.exists()) if (!appDirectory.mkdir()) return;
            File currDir = new File(appDirectory, filename);
            FileOutputStream fos = new FileOutputStream(currDir.getPath());
            fos.write(data);
            fos.close();
            Log.d("JSBridge", "File disimpan di: " + currDir.getAbsolutePath());
            Toast.makeText(context, "Disimpan: " + currDir.getName(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("JSBridge", "Gagal simpan file: " + e.getMessage(), e);
        }
    }
}