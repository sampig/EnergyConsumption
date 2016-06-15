package org.zhuzhu.energyconsumption.scanner.utils;

/**
 * @author Chenfeng Zhu
 */
public final class Intents {

    private Intents() { }

    public static final class Scan {
        /**
         * Send this intent to open the Barcodes app in scanning mode, find a barcode, and return
         * the results.
         */
        public static final String ACTION = "com.google.zxing.client.android.SCAN";
        /**
         * By default, sending this will decode all barcodes that we understand. However it
         * may be useful to limit scanning to certain formats. Use
         * {@link android.content.Intent#putExtra(String, String)} with one of the values below.
         *
         * Setting this is effectively shorthand for setting explicit formats with {@link #FORMATS}.
         * It is overridden by that setting.
         */
        public static final String MODE = "SCAN_MODE";
        /**
         * Decode only QR codes.
         */
        public static final String QR_CODE_MODE = "QR_CODE_MODE";
        /**
         * Comma-separated list of formats to scan for. The values must match the names of
         * {@link com.google.zxing.BarcodeFormat}s, e.g. {@link com.google.zxing.BarcodeFormat#EAN_13}.
         * Example: "EAN_13,EAN_8,QR_CODE". This overrides {@link #MODE}.
         */
        public static final String FORMATS = "SCAN_FORMATS";
        /**
         * Optional parameter to specify the id of the camera from which to recognize barcodes.
         * Overrides the default camera that would otherwise would have been selected.
         * If provided, should be an int.
         */
        public static final String CAMERA_ID = "SCAN_CAMERA_ID";
        /**
         * @see com.google.zxing.DecodeHintType#CHARACTER_SET
         */
        public static final String CHARACTER_SET = "CHARACTER_SET";
        /**
         * Optional parameters to specify the width and height of the scanning rectangle in pixels.
         * The app will try to honor these, but will clamp them to the size of the preview frame.
         * You should specify both or neither, and pass the size as an int.
         */
        public static final String WIDTH = "SCAN_WIDTH";
        public static final String HEIGHT = "SCAN_HEIGHT";
        /**
         * Prompt to show on-screen when scanning by intent. Specified as a {@link String}.
         */
        public static final String PROMPT_MESSAGE = "PROMPT_MESSAGE";
    }
}
