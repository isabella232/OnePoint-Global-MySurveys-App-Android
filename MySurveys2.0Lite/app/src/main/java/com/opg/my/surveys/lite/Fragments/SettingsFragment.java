package com.opg.my.surveys.lite.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.opg.my.surveys.lite.HomeActivity;
import com.opg.my.surveys.lite.R;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;


import static com.opg.my.surveys.lite.common.Util.REQUEST_CODE_LOCATION_SETTINGS;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener{

    private LinearLayout layoutChangePanel;
    private LinearLayout layoutChangePassword;
    private LinearLayout layoutPrivacy;
    private LinearLayout layoutTermCondition;
    private LinearLayout layoutAboutUs;
    private SwitchCompat switchGeolocation;
    private TextView tvVersion;

    private OnFragmentInteractionListener mListener;

    private static SettingsFragment settingsFragment;

    public SettingsFragment() {
        // Required empty public constructor
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance() {
        if(settingsFragment == null)
        {
            settingsFragment = new SettingsFragment();
        }
        return settingsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        layoutChangePanel = (LinearLayout)view.findViewById(R.id.container_change_panel);
        layoutChangePassword = (LinearLayout)view.findViewById(R.id.container_change_password);
        layoutPrivacy = (LinearLayout)view.findViewById(R.id.container_privacy);
        layoutTermCondition = (LinearLayout)view.findViewById(R.id.container_terms_condition);
        layoutAboutUs = (LinearLayout)view.findViewById(R.id.container_about_us);
        switchGeolocation =(SwitchCompat) view.findViewById(R.id.switch_geo_location);
        tvVersion = (TextView)view.findViewById(R.id.tv_version);

        layoutChangePanel.setOnClickListener(this);
        layoutChangePassword.setOnClickListener(this);
        layoutPrivacy.setOnClickListener(this);
        layoutTermCondition.setOnClickListener(this);
        layoutAboutUs.setOnClickListener(this);

        switchGeolocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isPressed())
                {
                    if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) &&
                                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
                            ActivityCompat.requestPermissions(getActivity(), new String []{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_LOCATION_SETTINGS);
                        }else{
                            Util.showPermissionDialog(getActivity(),getString(R.string.location_permission));
                        }
                        updateGeofenceSwitch();
                        return;
                    }
                    if (Util.locationServicesEnabled(getActivity()) && MySurveysPreference.isDownloaded(getActivity())) {
                        MySurveysPreference.setIsGeofencingEnabled(getActivity(), b);
                        updateGeofenceSwitch();
                        if (b) {
                            if (Util.isOnline(getActivity())) {
                                Util.sendBroadcastMessage(true, getActivity(), "", Util.BROADCAST_ACTION_GEOFENCE_START);
                            } else {
                                MySurveysPreference.setIsGeofencingEnabled(getActivity(), !b);
                                updateGeofenceSwitch();
                                ((HomeActivity) getActivity()).showSnackBar(getString(R.string.no_network_msg), Toast.LENGTH_SHORT);
                            }
                        } else {
                            Util.sendBroadcastMessage(true, getActivity(), "", Util.BROADCAST_ACTION_GEOFENCE_STOP);
                        }
                    } else {
                        if (!Util.locationServicesEnabled(getActivity())) {
                            Util.showLocationServicesError(getActivity());
                        } else if (!MySurveysPreference.isDownloaded(getActivity())) {
                            Util.showSyncDialog(getActivity());
                        }
                        updateGeofenceSwitch();
                    }


                }
            }
        });

        tvVersion.setText(getString(R.string.version)+" "+Util.getAppVersion(getActivity()));
        return view;
    }
    private void switchColor(boolean checked) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            switchGeolocation.getThumbDrawable().setColorFilter(checked ? Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity())) : Color.GRAY, PorterDuff.Mode.MULTIPLY);
            switchGeolocation.getTrackDrawable().setColorFilter(Color.LTGRAY , PorterDuff.Mode.MULTIPLY);
        }
    }
    @Override
    public void onDestroyView() {
        layoutChangePanel = null;
        layoutChangePassword = null;
        layoutPrivacy = null;
        layoutTermCondition = null;
        layoutAboutUs = null;
        switchGeolocation = null;
        tvVersion = null;
        super.onDestroyView();
    }

    @Override
    public void onClick(View view)
    {
        FragmentManager fragmentManager = getFragmentManager()                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              ;
        FragmentTransaction ft = fragmentManager.beginTransaction();
        try
        {
            switch (view.getId())
            {
                case R.id.container_change_panel :
                    ft.replace(R.id.settings_root_fragment, PanelFragment.newInstance(1),"PanelFragment");
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.addToBackStack(null);
                    ft.commit();
                    break;

                case R.id.container_geo_location :
                    Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(viewIntent);
                    break;

                case R.id.container_change_password :
                    ft.replace(R.id.settings_root_fragment, ChangePasswordFragment.newInstance("1","1"),"ChangePasswordFragment");
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.addToBackStack(null);
                    ft.commit();
                    break;

                case R.id.container_privacy :
                    openWebView(ft,1);
                    break;

                case R.id.container_terms_condition :
                    openWebView(ft,2);
                    break;

                case R.id.container_about_us :
                    openWebView(ft,3);
                    break;
            }
        }
        catch (Exception ex)
        {
            Log.i(SettingsFragment.class.getName(),ex.getMessage());
        }


    }
    private void openWebView(FragmentTransaction ft,int param)
    {
        if(Util.isOnline(getActivity()))
        {
            ft.replace(R.id.settings_root_fragment, WebViewFragment.newInstance(param),"WebViewFragment");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.addToBackStack(null);
            ft.commit();
        }
        else
        {
            ((HomeActivity)getActivity()).showSnackBar(getString(R.string.no_network_msg), Snackbar.LENGTH_LONG);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateGeofenceSwitch();
        IntentFilter iff= new IntentFilter();
        iff.addAction(Util.BROADCAST_ACTION_GEOFENCE_UI);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, iff);
    }

    private void updateGeofenceSwitch() {
        if(switchGeolocation != null)
        {
            switchGeolocation.setChecked(MySurveysPreference.getIsGeofencingEnabled(getActivity()));
            switchColor(MySurveysPreference.getIsGeofencingEnabled(getActivity()));
        }
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Util.BROADCAST_ACTION_GEOFENCE_UI))
            {
                if (switchGeolocation != null) {
                    updateGeofenceSwitch();
                }
            }
        }
    };


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
