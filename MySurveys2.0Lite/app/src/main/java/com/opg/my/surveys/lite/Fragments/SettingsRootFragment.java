package com.opg.my.surveys.lite.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.opg.my.surveys.lite.R;
import com.opg.my.surveys.lite.common.Util;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsRootFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsRootFragment extends Fragment {

    private static SettingsRootFragment settingsRootFragment;

    public SettingsRootFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment SettingsRootFragment.
     */
// TODO: Rename and change types and number of parameters
    public static SettingsRootFragment newInstance() {
        if(settingsRootFragment == null){
            settingsRootFragment = new SettingsRootFragment();
        }
        return settingsRootFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_settings_root, container, false);
        try
        {
            FragmentManager childManger = getChildFragmentManager();
            FragmentTransaction transaction = childManger.beginTransaction();
            SettingsFragment settingsFragment = (SettingsFragment)childManger.findFragmentByTag("SettingsFragment");
            if(settingsFragment == null)
            {
                transaction.replace(R.id.settings_root_fragment, SettingsFragment.newInstance(),"SettingsFragment");
            }
            else
            {
                transaction.replace(R.id.settings_root_fragment, settingsFragment,"SettingsFragment");
            }
            transaction.addToBackStack(null);
            transaction.commit();
        }catch (Exception ex)
        {

        }

        return  view;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter iff= new IntentFilter();
        iff.addAction(Util.BROADCAST_ACTION_REFRESH_FRAGMENT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, iff);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equalsIgnoreCase(Util.BROADCAST_ACTION_REFRESH_FRAGMENT)) {
                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                PanelFragment panelFragment = (PanelFragment) fragmentManager.findFragmentByTag("PanelFragment");
                if(panelFragment != null)
                {
                    transaction.remove(panelFragment);
                }
                ChangePasswordFragment changePasswordFragment = (ChangePasswordFragment) fragmentManager.findFragmentByTag("ChangePasswordFragment");
                if(changePasswordFragment != null)
                {
                    transaction.remove(changePasswordFragment);
                }
                WebViewFragment webViewFragment = (WebViewFragment) fragmentManager.findFragmentByTag("WebViewFragment");
                if(webViewFragment != null)
                {
                    transaction.remove(webViewFragment);
                }
                SettingsFragment settingsFragment = (SettingsFragment)fragmentManager.findFragmentByTag("SettingsFragment");
                if(settingsFragment != null)
                {
                    transaction.replace(R.id.settings_root_fragment, settingsFragment,"SettingsFragment");
                }
                transaction.commit();
            }
        }
    };

}
