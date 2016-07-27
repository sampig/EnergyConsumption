/**
 * Energy Consumption ( https://github.com/sampig/EnergyConsumption ) - This file is part of Energy Consumption.
 * Copyright (C) 2016 - Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.scanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.decode.DecodeFormatManager;
import com.google.zxing.client.android.decode.DecodeHintManager;
import com.google.zxing.client.android.decode.InactivityTimer;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.client.result.ResultParser;

import org.zhuzhu.energyconsumption.scanner.db.SettingsManager;
import org.zhuzhu.energyconsumption.scanner.model.DataModel;
import org.zhuzhu.energyconsumption.scanner.model.DeviceModel;
import org.zhuzhu.energyconsumption.scanner.model.SettingsModel;
import org.zhuzhu.energyconsumption.scanner.result.ResultHandler;
import org.zhuzhu.energyconsumption.scanner.result.ResultHandlerFactory;
import org.zhuzhu.energyconsumption.scanner.result.ResultModel;
import org.zhuzhu.energyconsumption.scanner.utils.HTMLGenerator;
import org.zhuzhu.energyconsumption.scanner.utils.HTTPUtils;
import org.zhuzhu.energyconsumption.scanner.utils.Intents;
import org.zhuzhu.energyconsumption.scanner.utils.ScreenManager;
import org.zhuzhu.energyconsumption.scanner.view.ViewfinderView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is the Scanner activity.
 *
 * @author Chenfeng ZHU
 */
public class ECScannerMainActivity extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = ECScannerMainActivity.class.getSimpleName();

    private static final int CAMERA_OFFSET = 10;
    private static final long SCAN_DELAY_MS = 10000L;
    private double scaleGraph = 1.5;

    private static final int SETTING_WINDOW_WIDTH = 600;
    private static final int SETTING_WINDOW_HEIGHT = 200;

    private Point resolution;

    private CameraManager cameraManager;
    private ViewfinderView viewfinderView;
    private ScannerActivityHandler handler;
    private InactivityTimer inactivityTimer;

    private boolean hasSurface;
    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;
    private String characterSet;

    private TextView statusView;
    private List<Result> listResult;
    private List<ResultModel> listResultModel;
    private RelativeLayout resultGraphView;
    private WebView settingView;

    private SettingsManager settingsManager;
    private HTTPUtils httpUtils;

    private String localURL;
    private int localQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_scanner);

        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        listResult = new ArrayList<>(0);
        listResultModel = new ArrayList<>(0);
        httpUtils = new HTTPUtils(this);
        settingsManager = new SettingsManager(this);

        resolution = ScreenManager.getScreenResolution(this.getApplicationContext());

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // init
        cameraManager = new CameraManager(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);
        statusView = (TextView) findViewById(R.id.status_view);
        resultGraphView = (RelativeLayout) findViewById(R.id.result_graph_view);
        handler = null;
        listResult.clear();

        resetResultView(true);
        inactivityTimer.onResume();
        Intent intent = getIntent();
        decodeFormats = null;
        characterSet = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            //noinspection WrongConstant
            setRequestedOrientation(getCurrentOrientation());
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        Point point = ScreenManager.getScreenResolution(this.getApplicationContext());
        cameraManager.setManualFramingRect(point.x - CAMERA_OFFSET, point.y - CAMERA_OFFSET);

        if (intent != null) {
            String action = intent.getAction();
            if (Intents.Scan.ACTION.equals(action)) {
                // Scan the formats the intent requested, and return the result to the calling activity.
                decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
                decodeHints = DecodeHintManager.parseDecodeHints(intent);
                if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
                    int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
                    int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
                    if (width > 0 && height > 0) {
                        cameraManager.setManualFramingRect(width, height);
                    }
                }
                if (intent.hasExtra(Intents.Scan.CAMERA_ID)) {
                    int cameraId = intent.getIntExtra(Intents.Scan.CAMERA_ID, -1);
                    if (cameraId >= 0) {
                        cameraManager.setManualCameraId(cameraId);
                    }
                }
                String customPromptMessage = intent.getStringExtra(Intents.Scan.PROMPT_MESSAGE);
                if (customPromptMessage != null) {
                    statusView.setText(customPromptMessage);
                }
            }
            characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);
        }

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
        }

        localURL = settingsManager.getWebServer();
        localQuantity = settingsManager.getQuantity();
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            if (handler == null) {
                handler = new ScannerActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.msg_error));
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ECScannerMainActivity.this.finish();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                ECScannerMainActivity.this.finish();
            }
        });
        builder.show();
    }

    /**
     * @param rawResult a list of raw result
     * @param qrCode    the QR Code bitmap
     */
    public void handleDecode(Result[] rawResult, Bitmap qrCode) {
        inactivityTimer.onActivity();
        if (rawResult.length < 1) {
            resetResultView(true);
            return;
        }
        listResult.clear();

        this.showProcessing(true);

        for (Result result : rawResult) {
            ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, result);
            if (resultHandler == null) {
                continue;
            }
            handleDecodeInternally(result, resultHandler, qrCode);
            if (result != null) {
                listResult.add(result);
            }
        }

        if (listResult.size() > 0 || settingView != null) {
            restartPreviewAfterDelay(SCAN_DELAY_MS);
        } else {
            restartPreviewAfterDelay(SCAN_DELAY_MS);
        }

        this.showProcessing(false);
    }

    private void showProcessing(boolean flag) {
        if (flag) {
            statusView.setText(R.string.msg_scan_process);
            statusView.setVisibility(View.VISIBLE);
        } else {
            statusView.setVisibility(View.GONE);
        }
    }

    /**
     * @param rawResult     the raw result
     * @param resultHandler the result handler
     * @param qrCode        the QR Code bitmap
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap qrCode) {
        // check the content in the QR code
        Log.d(TAG, qrCode.toString());
        boolean flagError = false;
        CharSequence displayContents = resultHandler.getDisplayContents();
        if (displayContents == null) {
            return;
        }
        String contents = displayContents.toString();
        System.out.println("***Activity.Contents: " + contents);
        String requestURL = null;
            String deviceID = null;
            if (ResultParser.parseResult(rawResult).getType() == ParsedResultType.URI
                    || contents.toLowerCase().startsWith("http")) {
                // if it is a URL
                requestURL = contents;
                deviceID = contents;
            } else if (contents.startsWith("{")) {
                // if it is a JSON data
                DeviceModel dm = DeviceModel.getInstance(contents);
                if (dm.deviceID == null) {
                    flagError = true;
                } else {
                    deviceID = dm.deviceID;
                    if (dm.direct && dm.webserver != null) {
                        requestURL = dm.webserver;
                    } else {
                        String tmpURL = dm.webserver;
                        int tmpQuantity = dm.quantity;
                        if (dm.webserver == null) {
                            if (localURL.endsWith("/")) {
                                tmpURL = localURL;
                            } else {
                                tmpURL = localURL + "/";
                            }
                        }
                        if (dm.quantity == 0) {
                            tmpQuantity = localQuantity;
                        }
                        requestURL = tmpURL + dm.deviceID + "/" + tmpQuantity;
                    }
                }
            } else if (contents.toUpperCase().startsWith("SETTINGS={")) {
                // if it is configuration
                this.changeSetting(contents);
                return;
            } else if (contents.length() < 32) {
                // if it is a text, it will be considered as a device ID.
                String webserver = settingsManager.getWebServer();
                deviceID = resultHandler.getDisplayContents().toString();
                if (webserver.endsWith("/")) {
                    requestURL = webserver + deviceID + "/" + localQuantity;
                } else {
                    requestURL = webserver + "/" + deviceID + "/" + localQuantity;
                }
            } else {
                flagError = true;
            }

        viewfinderView.setVisibility(View.GONE);

        String responseData = httpUtils.getRequestGET(requestURL);
        System.out.println("***Activity.RequestURL: " + requestURL);
        DataModel dm = DataModel.getInstance(responseData);

        // get the position and size of the QR Code
        ResultPoint[] points = rawResult.getResultPoints();
        ResultPoint position = points[1];
        int bitmapWidth = (int) (points[2].getX() - position.getX());
        int bitmapHeight = (int) (points[0].getY() - position.getY());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scaleGraph = 0.7;;
        } else {
            scaleGraph = 1.5;
        }
        WebView webview = new WebView(this);
        webview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent));
        //(getResources().getColor(R.color.transparent));
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.requestFocusFromTouch();
        String contentHTML;
        if (flagError) {
            contentHTML = HTMLGenerator.getErrorPage((int) (bitmapWidth / scaleGraph), (int) (bitmapHeight / scaleGraph));
        } else if (responseData == null || "".equalsIgnoreCase(responseData) || dm == null || dm.data == null || dm.data.size() == 0) {
            contentHTML = HTMLGenerator.getHTMLContent(null, (int) (bitmapWidth / scaleGraph), (int) (bitmapHeight / scaleGraph));
        } else {
            contentHTML = HTMLGenerator.getHTMLContent(dm.data, bitmapWidth, bitmapHeight);
        }
        webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webview.loadDataWithBaseURL("file:///android_asset/", contentHTML, "text/html", "utf-8", null);
        RelativeLayout webviewLayout = new RelativeLayout(getApplicationContext());
        webviewLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.settings_result_window));
        //(getResources().getDrawable(R.drawable.graph_window));
        webviewLayout.addView(webview, new RelativeLayout.LayoutParams((int) (bitmapWidth * scaleGraph), (int) (bitmapHeight * scaleGraph)));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (bitmapWidth * scaleGraph), (int) (bitmapHeight * scaleGraph));
        RelativeLayout queueLayout = new RelativeLayout(getApplicationContext());
        params.addRule(RelativeLayout.BELOW, 20);
        params.leftMargin = (int) position.getX();
        params.topMargin = (int) position.getY();
        queueLayout.addView(webviewLayout, params);
        resultGraphView.addView(queueLayout);

        final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        webview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                vibrator.vibrate(1000);
                return true;
            }
        });

        ResultModel model = new ResultModel(deviceID);
        if (listResultModel.contains(model)) {
            int i = listResultModel.indexOf(model);
            model = listResultModel.get(i);
            WebView webView = model.getWebView();
            if (webView.getParent() != null) {
                ((ViewGroup) webView.getParent()).removeView(webView);
            }
        }
        model.setWebView(webview);
        listResultModel.add(model);

    }

    /**
     * Change the settings.
     *
     * @param contents the contents in JSON format
     */
    private void changeSetting(String contents) {
        SettingsModel settingsModel = SettingsModel.getInstance(contents.substring(9));

        // update the settings in local database.
        if (settingsModel.quantity > 0) {
            settingsManager.updateQuantity(settingsModel.quantity);
//            System.out.println("***Settings changed: " + settingsManager.getQuantity());
        }
        if (settingsModel.webserver != null) {
            settingsManager.updateWebServer(settingsModel.webserver);
//            System.out.println("***Settings changed: " + settingsManager.getWebServer());
        }
        settingsModel.webserver = settingsManager.getWebServer();
        settingsModel.quantity = settingsManager.getQuantity();
//        System.out.println("Settings current: " + settingsManager.getWebServer() + ", " + settingsManager.getQuantity());
//        statusView.setText("Settings current: " + settingsManager.getWebServer() + ", " + settingsManager.getQuantity());
//        statusView.setVisibility(View.VISIBLE);

        int width = 600;
        int height = 200;

        settingView = new WebView(this);
        settingView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent));
        settingView.requestFocusFromTouch();
        String content = HTMLGenerator.getSettingsPage(settingsModel);
        settingView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        settingView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);
        RelativeLayout webviewLayout = new RelativeLayout(getApplicationContext());
        webviewLayout.addView(settingView, new RelativeLayout.LayoutParams(width, height));
        webviewLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.settings_result_window));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        RelativeLayout queueLayout = new RelativeLayout(getApplicationContext());
        params.leftMargin = (resolution.x - width) / 2;
        params.topMargin = (resolution.y - height) / 2;
        queueLayout.addView(webviewLayout, params);
        resultGraphView.addView(queueLayout);

        settingView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clearWebView(settingView);
                return true;
            }
        });

    }

    /**
     * Restart scanner.
     *
     * @param delayMS the ms for delay
     */
    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
        resetResultView(delayMS == 0L);
    }

    /**
     * Reset the Result View.
     *
     * @param isBack true if clicking back button
     */
    public void resetResultView(boolean isBack) {
        if (isBack) {
            // if clicking back button, remove all the result graphs.
            for (ResultModel result : listResultModel) {
                WebView webView = result.getWebView();
                if (webView.getParent() != null) {
                    ViewGroup view = (ViewGroup) webView.getParent();
                    if (view.getParent() != null) {
                        ((ViewGroup) view.getParent()).removeView(view);
                    }
                }
            }
            // remove
            if (settingView != null) {
                clearWebView(settingView);
            }
        }
        // statusView.setText(R.string.msg_scan_default);
        // statusView.setVisibility(View.GONE);
        viewfinderView.setVisibility(View.VISIBLE);
        listResult.clear();
    }

    /**
     * Clear the content in webview.
     *
     * @param webView the webview
     */
    private void clearWebView(WebView webView) {
        if (webView.getParent() != null) {
            ViewGroup view = (ViewGroup) webView.getParent();
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
                webView.destroy();
            }
        }
        statusView.setVisibility(View.GONE);
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    private int getCurrentOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_90:
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            default:
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        }
//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            switch (rotation) {
//                case Surface.ROTATION_0:
//                case Surface.ROTATION_90:
//                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
//                default:
//                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
//            }
//        } else {
//            switch (rotation) {
//                case Surface.ROTATION_0:
//                case Surface.ROTATION_270:
//                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
//                default:
//                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
//            }
//        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (listResult.size() > 0) {
                    restartPreviewAfterDelay(0L);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                cameraManager.setTorch(false);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                cameraManager.setTorch(true);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_scanner, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //noinspection deprecation
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        switch (item.getItemId()) {
            case R.id.menu_settings:
                settingView = new WebView(this);
                settingView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent));
                WebSettings webSettings = settingView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                settingView.requestFocusFromTouch();
                SettingsModel sm = new SettingsModel();
                sm.webserver = settingsManager.getWebServer();
                sm.quantity = settingsManager.getQuantity();
                String content = HTMLGenerator.getSettingsPage(sm);
                settingView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                settingView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);
                RelativeLayout webviewLayout = new RelativeLayout(getApplicationContext());
                webviewLayout.addView(settingView, new RelativeLayout.LayoutParams(SETTING_WINDOW_WIDTH, SETTING_WINDOW_HEIGHT));
                webviewLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.settings_result_window));
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(SETTING_WINDOW_WIDTH, SETTING_WINDOW_HEIGHT);
                RelativeLayout queueLayout = new RelativeLayout(getApplicationContext());
                params.leftMargin = (resolution.x - SETTING_WINDOW_WIDTH) / 2;
                params.topMargin = (resolution.y - SETTING_WINDOW_HEIGHT) / 2;
                queueLayout.addView(webviewLayout, params);
                resultGraphView.addView(queueLayout);
                settingView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        clearWebView(settingView);
                        return true;
                    }
                });
                String text = sm.webserver + ", " + sm.quantity;
                statusView.setText(text);
                statusView.setVisibility(View.VISIBLE);
                break;
            case R.id.menu_help:
                intent.setClassName(this, HelpActivity.class.getName());
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public Handler getHandler() {
        return handler;
    }

}
