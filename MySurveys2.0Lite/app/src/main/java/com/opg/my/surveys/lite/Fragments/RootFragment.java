package com.opg.my.surveys.lite.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.opg.my.surveys.lite.R;
import com.opg.my.surveys.lite.common.Util;

/**
 * A simple {@link Fragment} subclass.
 */
public class RootFragment extends Fragment {

    private static RootFragment rootFragment;
    public RootFragment() {
        // Required empty public constructor
    }


    public static RootFragment newInstance() {
        if(rootFragment == null){
            rootFragment = new RootFragment();
        }
        return rootFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_root, container, false);

        try
        {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		/*
		 * When this container fragment is created, we fill it with our first
		 * "real" fragment
		 */
            transaction.replace(R.id.root_frame, NotificationsFragment.newInstance());

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
        getActivity().registerReceiver(mReceiver, iff);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equalsIgnoreCase(Util.BROADCAST_ACTION_REFRESH_FRAGMENT)) {
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		        /*
		        * When this container fragment is created, we fill it with our first
		        * "real" fragment
		        */
                transaction.replace(R.id.root_frame, new NotificationsFragment());
                transaction.commit();
            }
        }
    };

}
