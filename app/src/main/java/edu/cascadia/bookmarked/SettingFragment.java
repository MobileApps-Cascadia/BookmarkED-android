package edu.cascadia.bookmarked;


import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragment {


//    public SettingFragment() {
//        // Required empty public constructor
//    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
    }


}
