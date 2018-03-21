package com.opg.my.surveys.lite.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.opg.my.surveys.lite.BuildConfig;
import com.opg.my.surveys.lite.R;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.RetriveOPGObjects;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SurveysFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SurveysFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SurveysFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private static SurveysFragment surveysFragment;
    private TabLayout tabLayout;

    private SurveyByListFragment surveyByListFragment;
    private SurveyByLocationFragment surveyByLocationFragment;

    public SurveysFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment SurveysFragment.
     */
    public static SurveysFragment newInstance() {
        if(surveysFragment== null){
            surveysFragment = new SurveysFragment();
        }
        return surveysFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_surveys, container, false);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        setTabTitleColor();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setCurrentTabFragment(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int unselectedPosition = tab.getPosition();
                if(BuildConfig.DEBUG) {
                    Log.e("Unselected Position", String.valueOf(unselectedPosition));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        setTabs();

        return view;
    }

    private void setTabs() {
        if(surveyByListFragment==null)
            surveyByListFragment = new SurveyByListFragment();
        if(surveyByLocationFragment == null)
            surveyByLocationFragment = new SurveyByLocationFragment();
        tabLayout.addTab(tabLayout.newTab().setText(getActivity().getString(R.string.by_list)));
        tabLayout.addTab(tabLayout.newTab().setText(getActivity().getString(R.string.by_location)));
        setCurrentTabFragment(0);
    }

    private void setCurrentTabFragment(int tabPosition)
    {
        switch (tabPosition)
        {
            case 0 :
                replaceFragment(surveyByListFragment);
                break;
            case 1 :
                replaceFragment(surveyByLocationFragment);
                break;
        }
    }

    public void replaceFragment(Fragment fragment) {
        try {
            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.child_container, fragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
        }catch(Exception e){
            if(BuildConfig.DEBUG) {
                Log.e(getActivity().getPackageName(), "Error in replacing the fragments");
            }
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

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


    @Override
    public void onResume() {
        super.onResume();
        refreshViewpager();
        IntentFilter iff = new IntentFilter();
        iff.addAction(Util.BROADCAST_ACTION_SAVE_DATA);
        iff.addAction(Util.BROADCAST_ACTION_REFRESH);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, iff);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }
    private void setTabTitleColor()
    {
        if(tabLayout != null)
        {
            tabLayout.setTabTextColors(Color.GRAY, Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity())));
            tabLayout.setSelectedTabIndicatorColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity())));
        }
    }
    /**
     * We will change the viewpager based on the geofencing surveys.
     * i.e if there are no geofencing surveys we will disable the tabs and also hide the geofencing tab
     */
    public void refreshViewpager() {
        try {
            if(MySurveysPreference.isDownloaded(getActivity())) {
                int size = RetriveOPGObjects.getGeofencingSurveys(MySurveysPreference
                        .getCurrentPanelID
                                (getActivity()))
                        .size();
                if(size == 0) {
                    hideViews();
                }
                else {
                    tabLayout.setVisibility(View.VISIBLE);
                }
            }
            else {
                hideViews();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideViews() {
        tabLayout.setVisibility(View.GONE);
        tabLayout.getTabAt(0).select();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(Util.BROADCAST_ACTION_SAVE_DATA) || intent.getAction().equals(Util.BROADCAST_ACTION_REFRESH)) {
                if(MySurveysPreference.isDownloaded(getActivity()))
                {
                    setTabTitleColor();
                }
                refreshViewpager();
            }
        }
    };
}
