/*
 * Copyright (C) 2016 Chenfeng Zhu
 */
package org.zhuzhu.energyconsumption.scanner.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * This is to get the information about Screen.
 *
 * @author Chenfeng Zhu
 */
public abstract class ScreenManager {

    /**
     * Get the Screen Resolution.
     *
     * @param context Context of App.
     * @return the size of the screen resolution
     */
    public static Point getScreenResolution(final Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point screenResolution = new Point();
        display.getSize(screenResolution);
        return screenResolution;
    }
}
