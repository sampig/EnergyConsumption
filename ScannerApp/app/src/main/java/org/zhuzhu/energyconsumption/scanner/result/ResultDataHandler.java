/*
 * Copyright (C) 2016 Chenfeng Zhu
 */
package org.zhuzhu.energyconsumption.scanner.result;

import android.app.Activity;
import android.view.KeyEvent;

import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;

import org.zhuzhu.energyconsumption.scanner.R;

/**
 * This is to handle the scanning result.
 *
 * @author Chenfeng Zhu
 */
public final class ResultDataHandler extends ResultHandler {

    private static final int[] buttons = {
            R.string.button_ok
    };

    public ResultDataHandler(Activity activity, ParsedResult result, Result rawResult) {
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
        // TODO: discard current result and start next scanning sooner.
        // String text = super.getParsedResult().getDisplayResult();
        switch (index) {
            case 0:
                super.getActivity().onKeyDown(KeyEvent.KEYCODE_BACK, null);
                break;
        }
    }

    /**
     * Lauch an activity to view the detail information of device.
     *
     * @param deviceId ID of device
     */
//    private void viewDetail(String deviceId) {
//        Intent intent = new Intent();
//        if (intent.getAction() == null) {
//            Toast.makeText(super.getActivity().getApplicationContext(),
//                    " Detail function has not been done: (" + deviceId + ')',
//                    Toast.LENGTH_SHORT).show();
//        }
//    }

    /**
     * Save the scanning result into history.
     *
     * @param deviceId ID of device
     */
//    private void saveToHistory(String deviceId) {
//        Toast.makeText(super.getActivity().getApplicationContext(),
//                " Save function has not been done: (" + deviceId + ')',
//                Toast.LENGTH_SHORT).show();
//    }
}
