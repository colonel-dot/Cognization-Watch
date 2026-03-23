package com.example.bridge.geofence.map.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class MapWebView extends WebView {
    public MapWebView(Context context) {
        this(context, null);
    }

    public MapWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBridgeWebView(context, attrs);
    }

    public MapWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBridgeWebView(context, attrs);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initBridgeWebView(Context context, AttributeSet attrs) {
        WebSettings settings = getSettings();

        // 允许使用js
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 设置适应 Html5
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // 设置 WebView 属性以执行 Javascript 脚本
        settings.setDefaultTextEncodingName("utf-8");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            setWebContentsDebuggingEnabled(true);
        }

        // android 4.1
        //允许 webview 对文件的操作
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        settings.setAllowFileAccess(true);

        // android 4.1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(true);
        }
        settings.setAllowContentAccess(true);
        settings.setDatabaseEnabled(true);

        // 允许 blob 请求
        settings.setAllowFileAccessFromFileURLs(true);

        // 允许本地缓存
        settings.setAllowUniversalAccessFromFileURLs(true);

        setWebChromeClient(new WebChromeClient());
    }

}
