/*
 * Copyright (C) 2016 Chenfeng Zhu
 */
package org.zhuzhu.energyconsumption.scanner.result;

import android.app.Activity;

import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;

import org.zhuzhu.energyconsumption.scanner.R;

/**
 * This is to handle the scanning result of setting.
 *
 * @author Chenfeng Zhu
 */
public final class ResultSettingHandler extends ResultHandler {

    private static final int[] buttons = {
            R.string.button_confirm,
            R.string.button_discard
    };

    public ResultSettingHandler(Activity activity, ParsedResult result, Result rawResult) {
        super(activity, result, rawResult);
    }

    @Override
    public int getButtonCount() {
        return buttons.length;
    }

    @Override
    public int getButtonText(int index) {
        return buttons[index];
    }

    @Override
    public void handleButtonPress(int index) {
        // TODO: confirm the change or cancel the change.
        switch (index) {
            case 0:
                // accept the change.
                break;
            case 1:
                // cancel the change.
                break;
        }
    }

}
