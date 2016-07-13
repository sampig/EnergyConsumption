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

import android.view.View;

/**
 * Handles the result of barcode decoding in the context of the Android platform.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Chenfeng Zhu
 */
@Deprecated
public final class ResultButtonListener implements View.OnClickListener {
    //TODO: useless for now.

    private final ResultHandler resultHandler;
    private final int index;

    public ResultButtonListener(ResultHandler resultHandler, int index) {
        this.resultHandler = resultHandler;
        this.index = index;
    }

    @Override
    public void onClick(View view) {
//        resultHandler.handleButtonPress(index);
    }
}
