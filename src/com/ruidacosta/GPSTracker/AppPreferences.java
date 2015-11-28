package com.ruidacosta.GPSTracker;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by ruidacosta on 11/10/2015.
 */
public class AppPreferences extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
