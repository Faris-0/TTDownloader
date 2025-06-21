package com.yuuna.ttdownloader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

    private WebView wvWebHide, wvVideo;
    private SmartVideoDownloader downloader;
    private String sURL = "";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloader = new SmartVideoDownloader(this);
        downloader.registerReceiver();

        wvWebHide = findViewById(R.id.amWebHide);
        wvVideo = findViewById(R.id.amVideo);

        WebSettings wsWeb = wvWebHide.getSettings();
        wsWeb.setJavaScriptEnabled(true);
        wsWeb.setMediaPlaybackRequiresUserGesture(false);
        wsWeb.setDomStorageEnabled(true);
        wsWeb.setLoadWithOverviewMode(true);
        wsWeb.setUseWideViewPort(true);
        wvWebHide.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (request.getUrl().toString().contains("-webapp-prime.tiktok.com") && sURL.startsWith("https://www.tiktok.com/")) {
                    runOnUiThread(() -> {
                        wvVideo.loadUrl(request.getUrl().toString());
                        wvVideo.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
                            downloader.downloadVideo(url, userAgent);
                        });
                    });
                }
                return super.shouldInterceptRequest(view, request);
            }
        });

        dialogURL();
    }

    private void dialogURL() {
        EditText etURL = new EditText(this);
        etURL.setSingleLine();
        FrameLayout flURL = new FrameLayout(this);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._15sdp);
        params.rightMargin = getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._15sdp);
        etURL.setLayoutParams(params);
        flURL.addView(etURL);
        new AlertDialog.Builder(this)
                .setTitle("Input URL Tiktok\n(ex. https://www.tiktok.com/../video/...)")
                .setView(flURL)
                .setPositiveButton("Open", (dialogInterface, i) -> {
                    sURL = etURL.getText().toString();
                    wvWebHide.loadUrl(sURL);
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    @Override
    public void onBackPressed() {
        dialogURL();
    }
}