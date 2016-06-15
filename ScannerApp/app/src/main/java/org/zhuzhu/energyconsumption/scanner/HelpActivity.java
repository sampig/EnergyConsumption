/**
 * Copyright (C) 2016 Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.scanner;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;

import org.zhuzhu.energyconsumption.scanner.utils.HTMLGenerator;

/**
 * This is an activity to open the HELP page.
 *
 * @author Chenfeng ZHU
 */
public class HelpActivity extends Activity {

    private static final String BASE_URL = "file:///android_asset/";

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        webView = (WebView) findViewById(R.id.help_contents);
        if (savedInstanceState == null) {
            String content = HTMLGenerator.getHTMLContent(null);
//            WebSettings webSettings = webView.getSettings();
//            webSettings.setJavaScriptEnabled(true);
//            webView.requestFocusFromTouch();
//            webView.loadDataWithBaseURL( "file:///android_asset/", content, "text/html", "utf-8", null);
            webView.loadUrl(BASE_URL + "index.html");
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
