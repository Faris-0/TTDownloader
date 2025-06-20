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

import java.util.ArrayList;

public class MainActivity extends Activity {

    private WebView wvWeb;
    private WebSettings wsWeb;
    private ArrayList<String> stringArrayList = new ArrayList<>();
    private String sURL = "";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SmartVideoDownloader downloader = new SmartVideoDownloader(this);
        downloader.registerReceiver();

        wvWeb = findViewById(R.id.amWeb);
        wsWeb = wvWeb.getSettings();
        wsWeb.setJavaScriptEnabled(true);
        wvWeb.addJavascriptInterface(new JSBridge(this), "AndroidBridge");
        wsWeb.setMediaPlaybackRequiresUserGesture(false);
        wsWeb.setDomStorageEnabled(true);
        wsWeb.setLoadWithOverviewMode(true);
        wsWeb.setUseWideViewPort(true);
        wvWeb.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (stringArrayList.size() == 1) wvWeb.loadUrl(stringArrayList.get(0));
                String js = "(async function () {\n" +
                        "  const video = document.querySelector('video');\n" +
                        "\n" +
                        "  if (!video || !video.src && !video.querySelector('source')) {\n" +
                        "    alert(\"Video tidak ditemukan\");\n" +
                        "    return;\n" +
                        "  }\n" +
                        "\n" +
                        "  const url = video.src || video.querySelector('source').src;\n" +
                        "\n" +
                        "  const response = await fetch(url);\n" +
                        "  const blob = await response.blob();\n" +
                        "\n" +
                        "  const reader = new FileReader();\n" +
                        "  reader.onloadend = function () {\n" +
                        "    const base64 = reader.result.split(',')[1];\n" +
                        "    AndroidBridge.saveBase64File(base64, 'TikTok_" + (System.currentTimeMillis()) + ".mp4');\n" +
                        "  };\n" +
                        "  reader.readAsDataURL(blob);\n" +
                        "})();\n";

                view.evaluateJavascript(js, null);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (request.getUrl().toString().contains("-webapp-prime.tiktok.com") && sURL.startsWith("https://www.tiktok.com/")) {
                    stringArrayList.add(request.getUrl().toString());
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
                .setTitle("Input URL Tiktok\n(ex. https://www.tiktok.com/.../video/7513176888089513222)")
                .setView(flURL)
                .setPositiveButton("Save", (dialogInterface, i) -> {
                    stringArrayList.clear();
                    sURL = etURL.getText().toString();
                    wvWeb.loadUrl(sURL);
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    @Override
    public void onBackPressed() {
        dialogURL();
    }
}