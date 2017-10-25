package com.opg.my.surveys.lite.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.opg.my.surveys.lite.BrowseActivity;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.UpdateOPGObjects;
import com.opg.my.surveys.lite.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationDetailFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private String mParam1;
    private String mParam2;
    private String mParam3;

    private ImageView img_notifi_start_survey;
    private TextView tvNotificationDetail ;
    private static NotificationDetailFragment fragment;
    public NotificationDetailFragment() {
        // Required empty public constructor
    }
    public static NotificationDetailFragment newInstance(long appNotificationID,String param1, String param2,String param3) throws Exception {
        if(fragment == null)
        {
            fragment = new NotificationDetailFragment();
            Bundle args = new Bundle();
            args.putString(ARG_PARAM1, param1);
            args.putString(ARG_PARAM2, param2);
            args.putString(ARG_PARAM3, param3);
            fragment.setArguments(args);
        }else{
            fragment.getArguments().putString(ARG_PARAM1, param1);
            fragment.getArguments().putString(ARG_PARAM2, param2);
            fragment.getArguments().putString(ARG_PARAM3, param3);
        }
        try {
            UpdateOPGObjects.updateAppNotificationStatus(appNotificationID, true);
        }catch (Exception e){
            Log.e("Notification Status","Failed to update");
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mParam3 =  getArguments().getString(ARG_PARAM3);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mParam3 = getArguments().getString(ARG_PARAM3);
        }
        View view = inflater.inflate(R.layout.fragment_notification_detail, container, false);
        img_notifi_start_survey =(ImageView) view.findViewById(R.id.img_notifi_start_survey);
        tvNotificationDetail = (TextView)view.findViewById(R.id.tv_notifi_detail);
        tvNotificationDetail.setText(mParam2);
        GradientDrawable gradientDrawable = (GradientDrawable) img_notifi_start_survey.getBackground();
        gradientDrawable.setColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity())));
        img_notifi_start_survey.setVisibility(View.GONE);
        img_notifi_start_survey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BrowseActivity.class);
                if(mParam3 != null && !mParam3.isEmpty())
                {
                    intent.putExtra(Util.SURVEY_REF,mParam3);
                }
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        img_notifi_start_survey = null;
        tvNotificationDetail= null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        fragment.getView().setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if( keyCode == KeyEvent.KEYCODE_BACK )
                {
                    getFragmentManager().popBackStack();
                    return true;
                }
                return false;
            }
        } );
    }
}
