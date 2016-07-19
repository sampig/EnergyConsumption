/*
 * Copyright (C) 2008 ZXing authors
 * Copyright (C) 2016 Chenfeng Zhu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zhuzhu.energyconsumption.scanner.result;

import android.app.Activity;

import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;

/**
 * A base class for the Android-specific barcode handlers.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 * @author Chenfeng Zhu
 */
public abstract class ResultHandler {

    private final ParsedResult parsedResult;
    private final Activity activity;
    private final Result rawResult;

    ResultHandler(Activity activity, ParsedResult parsedResult) {
        this(activity, parsedResult, null);
    }

    ResultHandler(Activity activity, ParsedResult parsedResult, Result rawResult) {
        this.parsedResult = parsedResult;
        this.activity = activity;
        this.rawResult = rawResult;
    }

    public final ParsedResult getParsedResult() {
        return parsedResult;
    }

    public final Result getRawResult() {
        return rawResult;
    }

    final Activity getActivity() {
        return activity;
    }

    /**
     * Indicates how many buttons the derived class wants shown.
     *
     * @return The integer button count.
     */
//    public abstract int getButtonCount();

    /**
     * The text of the nth action button.
     *
     * @param index From 0 to getButtonCount() - 1
     * @return The button text as a resource ID
     */
//    public abstract int getButtonText(int index);

    /**
     * Execute the action which corresponds to the nth button.
     *
     * @param index The button that was clicked.
     */
//    public abstract void handleButtonPress(int index);

    /**
     * Create a possibly styled string for the contents of the current barcode.
     *
     * @return The text to be displayed.
     */
    public CharSequence getDisplayContents() {
        String contents = parsedResult.getDisplayResult();
        return contents.replace("\r", "");
    }

}
