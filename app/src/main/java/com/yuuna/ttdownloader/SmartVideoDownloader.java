package com.yuuna.ttdownloader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SmartVideoDownloader {

    private final Activity activity;
    private BroadcastReceiver receiver;
    private long lastDownloadId = -1;

    public SmartVideoDownloader(Activity act) {
        this.activity = act;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public void registerReceiver() {
        if (receiver != null) return;

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId != lastDownloadId) return;

                DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
                DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                Cursor cursor = dm.query(query);

                if (cursor.moveToFirst()) {
                    @SuppressLint("Range") int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    @SuppressLint("Range") int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));

                    switch (status) {
                        case DownloadManager.STATUS_SUCCESSFUL:
                            Log.d("VideoDownload", "Berhasil!");
                            break;
                        case DownloadManager.STATUS_FAILED:
                            Log.e("VideoDownload", "Gagal. Alasan: " + reason);
                            break;
                        default:
                            Log.d("VideoDownload", "Status: " + status);
                    }
                }
                cursor.close();
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.registerReceiver(
                    receiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_NOT_EXPORTED
            );
        } else {
            activity.registerReceiver(
                    receiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            );
        }
    }

    public void downloadVideo(String url, String userAgent) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", userAgent)
                        .addHeader("Referer", "https://www.tiktok.com/")
                        .addHeader("Origin", "https://www.tiktok.com")
                        .addHeader("Cookie", CookieManager.getInstance().getCookie(url))
                        .build();

                Response response = client.newCall(request).execute();
                Log.i("VideoDownload", "Response code: " + response.code());

                if (response.isSuccessful()) {
                    byte[] data = response.body().bytes();
                    String label = activity.getApplicationInfo().loadLabel(activity.getPackageManager()).toString();
                    File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), label);
                    if (!dir.exists()) dir.mkdir();
                    File file = new File(dir, "TikTok_" + System.currentTimeMillis() + ".mp4");
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(data);
                    fos.close();
                    Log.i("VideoDownload", "Berhasil simpan: " + file.getAbsolutePath());
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity, "Disimpan: " + file.getName(), Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e("VideoDownload", "Gagal download: " + e.getMessage(), e);
            }
        }).start();
    }
}