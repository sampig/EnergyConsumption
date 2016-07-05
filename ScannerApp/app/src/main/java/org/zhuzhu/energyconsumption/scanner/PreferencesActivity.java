/**
 * Copyright (C) 2016 Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.scanner;

import android.app.Activity;
import android.os.Bundle;

/**
 * This is an activity for preferences setting.
 *
 * @author Chenfeng Zhu
 */
@Deprecated
public class PreferencesActivity extends Activity {
    //TODO: useless for now.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PreferencesFragment()).commit();
    }
}
