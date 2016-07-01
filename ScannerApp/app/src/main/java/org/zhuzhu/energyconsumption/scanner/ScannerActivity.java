/**
 * Copyright (C) 2016 Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
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

import org.zhuzhu.energyconsumption.scanner.model.DataModel;
import org.zhuzhu.energyconsumption.scanner.result.ResultButtonListener;
import org.zhuzhu.energyconsumption.scanner.result.ResultHandler;
import org.zhuzhu.energyconsumption.scanner.result.ResultHandlerFactory;
import org.zhuzhu.energyconsumption.scanner.result.ResultModel;
import org.zhuzhu.energyconsumption.scanner.utils.HTMLGenerator;
import org.zhuzhu.energyconsumption.scanner.utils.HTTPUtils;
import org.zhuzhu.energyconsumption.scanner.utils.Intents;
import org.zhuzhu.energyconsumption.scanner.utils.ScreenManager;
import org.zhuzhu.energyconsumption.scanner.view.ViewfinderView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This is the Scanner activity.
 *
 * @author Chenfeng ZHU
 */
public class ScannerActivity extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = ScannerActivity.class.getSimpleName();

    private static final int CAMERA_OFFSET = 100;
    private static final long SCAN_DELAY_MS = 10000L;
    private static final double SCALE_GRAPH = 1.5;

    private CameraManager cameraManager;
    private ViewfinderView viewfinderView;
    private ScannerActivityHandler handler;
    private InactivityTimer inactivityTimer;

    private boolean hasSurface;
    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;
    private String characterSet;

    private TextView statusView;
    private View resultView;
//    private Result lastResult;
    private List<Result> listResult;
    private List<ResultModel> listResultModel;
    private RelativeLayout resultGraphView;

    private HTTPUtils httpUtils;

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
        httpUtils = new HTTPUtils();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraManager = new CameraManager(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);
        resultView = findViewById(R.id.result_view);
        statusView = (TextView) findViewById(R.id.status_view);
        resultGraphView = (RelativeLayout) findViewById(R.id.result_graph_view);
        handler = null;
//        lastResult = null;
        listResult.clear();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        resetResultView(true);
        inactivityTimer.onResume();
        Intent intent = getIntent();
        decodeFormats = null;
        characterSet = null;

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
                ScannerActivity.this.finish();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                ScannerActivity.this.finish();
            }
        });
        builder.show();
    }

    /**
     * @param rawResult
     * @param barcode
     * @param scaleFactor
     */
    public void handleDecode(Result[] rawResult, Bitmap barcode, float scaleFactor) {
        inactivityTimer.onActivity();
        if (rawResult.length < 1) {
            resetResultView(true);
            return;
        }
        listResult.clear();
        for (Result result : rawResult) {
//            lastResult = result;
            ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, result);
            if (resultHandler == null) {
                continue;
            }
            handleDecodeInternally(result, resultHandler, barcode);
            if (result!=null) {
                listResult.add(result);
            }
        }

        if (listResult.size()>0) {
            restartPreviewAfterDelay(SCAN_DELAY_MS);
        } else {
            restartPreviewAfterDelay(SCAN_DELAY_MS);
        }
    }

    /**
     * @param rawResult
     * @param resultHandler
     * @param barcode
     */
    private void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
        CharSequence displayContents = resultHandler.getDisplayContents();
        statusView.setVisibility(View.GONE);
        viewfinderView.setVisibility(View.GONE);
        resultView.setVisibility(View.VISIBLE);

        ImageView barcodeImageView = (ImageView) findViewById(R.id.barcode_image_view);
        if (barcode == null) {
            barcodeImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        } else {
            barcodeImageView.setImageBitmap(barcode);
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TextView timeTextView = (TextView) findViewById(R.id.time_text_view);
        timeTextView.setText(formatter.format(new Date(rawResult.getTimestamp())));
        TextView rawTextView = (TextView) findViewById(R.id.raw_text_view);
        // TODO: change
        rawTextView.setText(rawResult.getText());
        TextView contentsTextView = (TextView) findViewById(R.id.contents_text_view);
        contentsTextView.setText(displayContents);
        int scaledSize = Math.max(12, 32 - displayContents.length() / 4);
        contentsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize);

//        WebView webview = (WebView) findViewById(R.id.webView1);
        String deviceID = resultHandler.getDisplayContents().toString();

        ResultPoint[] points = rawResult.getResultPoints();
        ResultPoint position = points[1];
        int bitmapWidth = (int) (points[2].getX() - position.getX());
        int bitmapHeight = (int) (points[0].getY() - position.getY());

        WebView webview = new WebView(this);
        webview.setBackgroundColor(getResources().getColor(R.color.transparent));
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.requestFocusFromTouch();
        DataModel dm = DataModel.getRandomInstance("D5905EFFBA27634C");
        Map<String,Double> data = dm.data;
        String content = null;
        if (data.size()==0) {
             content = HTMLGenerator.getHTMLContent(httpUtils.parseJsonData(displayContents.toString()), bitmapWidth, bitmapHeight);
        } else {
            content = HTMLGenerator.getHTMLContent(data, bitmapWidth, bitmapHeight);
        }
        webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webview.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);
//        rawTextView.setText("size: "+findViewById(R.id.result_view_center).getLayoutParams().width+", "+findViewById(R.id.result_view_center).getLayoutParams().height);
//        rawTextView.setText(points.length+": "+points[0]);
        RelativeLayout webviewLayout = new RelativeLayout(getApplicationContext());
        webviewLayout.setBackground(getResources().getDrawable(R.drawable.graph_window));
        webviewLayout.addView(webview, new RelativeLayout.LayoutParams((int) (bitmapWidth*SCALE_GRAPH), (int) (bitmapHeight*SCALE_GRAPH)));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (bitmapWidth*SCALE_GRAPH), (int) (bitmapHeight*SCALE_GRAPH));
        RelativeLayout queueLayout = new RelativeLayout(getApplicationContext());
        params.addRule(RelativeLayout.BELOW, 20);
        params.leftMargin = (int) position.getX();
        params.topMargin = (int) position.getY();
//        params.width=405;
//        params.height=230;
        queueLayout.addView(webviewLayout, params);
        resultGraphView.addView(queueLayout);

        final Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
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

        // generate the button after viewing result
        int buttonCount = resultHandler.getButtonCount();
        ViewGroup buttonView = (ViewGroup) findViewById(R.id.result_button_view);
        buttonView.requestFocus();
        for (int x = 0; x < ResultHandler.MAX_BUTTON_COUNT; x++) {
            TextView button = (TextView) buttonView.getChildAt(x);
            if (x < buttonCount) {
                button.setVisibility(View.VISIBLE);
                button.setText(resultHandler.getButtonText(x));
                button.setOnClickListener(new ResultButtonListener(resultHandler, x));
            } else {
                button.setVisibility(View.GONE);
            }
        }

        // Hide all useless information
        findViewById(R.id.result_view_left).setVisibility(View.GONE);
        findViewById(R.id.result_view_center).setVisibility(View.GONE);
        findViewById(R.id.result_view_right).setVisibility(View.GONE);
//        findViewById(R.id.layout_raw_text).setVisibility(View.GONE);
        buttonView.setVisibility(View.GONE);
    }

    /**
     * Restart scanner.
     *
     * @param delayMS
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
            resultView.setVisibility(View.GONE);
            for (ResultModel result : listResultModel) {
                WebView webView = result.getWebView();
                if (webView.getParent() != null) {
                    ViewGroup view = (ViewGroup) webView.getParent();
                    if (view.getParent() !=null) {
                        ((ViewGroup) view.getParent()).removeView(view);
//                        ((ViewGroup) webView.getParent()).removeView(webView);
                    }
                }
            }
        }
        statusView.setText(R.string.msg_scan_default);
        statusView.setVisibility(View.VISIBLE);
        viewfinderView.setVisibility(View.VISIBLE);
//        lastResult = null;
        listResult.clear();
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (listResult.size() > 0 || resultView.getVisibility() != View.GONE) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //noinspection deprecation
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        switch (item.getItemId()) {
            case R.id.menu_settings:
                intent.setClassName(this, PreferencesActivity.class.getName());
                startActivity(intent);
                break;
            case R.id.menu_help:
                intent.setClassName(this, HelpActivity.class.getName());
                startActivity(intent);
                break;
            case R.id.menu_test:
                WebView webview = new WebView(this);
                webview.setBackgroundColor(getResources().getColor(R.color.transparent));
//                webview.setBackgroundColor(getResources().getColor(R.color.result_graph_background));
//                webview.setBackground(getResources().getDrawable(R.drawable.custom));
                WebSettings webSettings = webview.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webview.requestFocusFromTouch();
                String content = HTMLGenerator.getHTMLContent(null);
                webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                webview.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);
                RelativeLayout webviewLayout = new RelativeLayout(getApplicationContext());
                webviewLayout.addView(webview, new RelativeLayout.LayoutParams(405, 230));
                webviewLayout.setBackground(getResources().getDrawable(R.drawable.graph_window));
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(405, 230);
                RelativeLayout queueLayout = new RelativeLayout(getApplicationContext());
//                params.addRule(RelativeLayout.BELOW, 20);
                params.leftMargin = 100;
                params.topMargin = 100;
                queueLayout.addView(webviewLayout, params);
                resultGraphView.addView(queueLayout);
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
