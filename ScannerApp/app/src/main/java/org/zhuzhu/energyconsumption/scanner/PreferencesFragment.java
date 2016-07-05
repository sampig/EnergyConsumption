/**
 * Copyright (C) 2016 Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.scanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

/**
 * @author Chenfeng Zhu
 */
@Deprecated
public class PreferencesFragment
        extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    //TODO: useless for now.

    private CheckBoxPreference[] checkBoxPrefs;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceScreen preferences = getPreferenceScreen();
        preferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        ;
    }
}
