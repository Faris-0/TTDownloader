package com.yuuna.ttdownloader;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;

public class SmartVideoDownloader {

    private final Context context;
    private BroadcastReceiver receiver;
    private long lastDownloadId = -1;

    public SmartVideoDownloader(Context ctx) {
        this.context = ctx.getApplicationContext();
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
            context.registerReceiver(
                    receiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_NOT_EXPORTED
            );
        } else {
            context.registerReceiver(
                    receiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            );
        }
    }

    public void downloadFromWebView(WebView webView) {
        webView.evaluateJavascript(
                "(function() {" +
                        "  var src = document.querySelector('video source')?.src;" +
                        "  return src || '';" +
                        "})()",
                value -> {
                    String url = value.replace("\"", "");
                    if (!url.isEmpty()) {
                        String ua = webView.getSettings().getUserAgentString();
                        downloadVideo(url, ua);
                    } else {
                        Log.w("VideoDownload", "Tidak ditemukan <source> video.");
                    }
                }
        );
    }

    public void downloadVideo(String url, String userAgent) {
        try {
            Uri uri = Uri.parse(url);
            String filename = uri.getLastPathSegment();
            if (filename == null || filename.isEmpty()) {
                filename = "video_" + System.currentTimeMillis() + ".mp4";
            }

            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
            request.addRequestHeader("User-Agent", userAgent);

            String cookie = CookieManager.getInstance().getCookie(url);
            if (cookie != null) {
                request.addRequestHeader("Cookie", cookie);
            }

            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            lastDownloadId = dm.enqueue(request);
        } catch (Exception e) {
            Log.e("VideoDownload", "Gagal menginisiasi download: " + e.getMessage(), e);
        }
    }
}