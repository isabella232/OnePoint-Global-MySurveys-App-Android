package com.opg.my.surveys.lite.Fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.opg.my.surveys.lite.BuildConfig;
import com.opg.my.surveys.lite.DividerItemDecoration;
import com.opg.my.surveys.lite.FetchDataService;
import com.opg.my.surveys.lite.HomeActivity;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.R;
import com.opg.my.surveys.lite.ShimmerFrameLayout;
import com.opg.my.surveys.lite.SurveyDetailActivity;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.RetriveOPGObjects;
import com.opg.sdk.models.OPGSurvey;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.opg.my.surveys.lite.common.Util.DOWNLOAD_STATUS_KEY;
import static com.opg.my.surveys.lite.common.Util.NEW_STATUS_KEY;
import static com.opg.my.surveys.lite.common.Util.TAG;


public class SurveyByListFragment extends Fragment {
    private List<OPGSurvey> surveysList = null;

    private RecyclerView recyclerView;
    private SurveysAdapter surveysAdapter;
    private ShimmerFrameLayout mShimmerViewContainer;
    private TextView no_surveys_tv;
    private HashMap<Long, Integer> surveysPositions;
    private boolean fetchData = true;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Util.BROADCAST_ACTION_SAVE_DATA) || intent.getAction().equals(Util.BROADCAST_ACTION_REFRESH) || intent.getAction().equals(Util.BROADCAST_ACTION_UPLOADED_ALL)) {
                setSurveyList();
            } else if (intent.getAction().equals(Util.ACTION_UPLOAD_RESULT)) {
                ((HomeActivity) getActivity()).showSnackBar(intent.getStringExtra("message"), Snackbar.LENGTH_LONG);
            }/* else if (intent.getAction().equals(Util.BROADCAST_ACTION_REFRESH_UPLOAD)) {
            }*/
        }
    };

    public SurveyByListFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surveysPositions = new HashMap<>();
        surveysList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_survey_by_list, container, false);
        surveysPositions.clear();
        surveysList.clear();
        surveysAdapter = new SurveysAdapter(getActivity(), surveysList);
        recyclerView = (RecyclerView) view.findViewById(R.id.surveys_rv);
        mShimmerViewContainer = (ShimmerFrameLayout) view.findViewById(R.id.shimmer_view_container);
        no_surveys_tv = (TextView) view.findViewById(R.id.no_surveys_tv);
        setAdapter();
        return view;
    }

    private void setAdapter() {
        try {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), R.drawable.divider));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(surveysAdapter);
            recyclerView.setHasFixedSize(true);
        } catch (Exception ex) {
            Log.i(SurveyByListFragment.class.getName(), ex.getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        if(surveysAdapter!=null){
            surveysAdapter.clearAdapterData();
        }
        recyclerView = null;
        mShimmerViewContainer = null;
        no_surveys_tv = null;
        surveysAdapter = null;
        surveysList.clear();
        surveysPositions.clear();
        super.onDestroyView();
    }


    @Override
    public void onResume() {
        super.onResume();
        IntentFilter iff = new IntentFilter();
        iff.addAction(Util.BROADCAST_ACTION_SAVE_DATA);
        iff.addAction(Util.BROADCAST_ACTION_REFRESH);
        iff.addAction(Util.BROADCAST_ACTION_REFRESH_UPLOAD);
        iff.addAction(Util.ACTION_UPLOAD_RESULT);
        iff.addAction(Util.BROADCAST_ACTION_UPLOADED_ALL);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, iff);
        setSurveyList();
    }

    @Override
    public void onPause() {
        super.onPause();
        fetchData = false;
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onStop() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG,"OnStopCalledSurveyByList");
        }
        super.onStop();
    }

    private void setSurveyList() {
        fetchData = true;
        if (Util.isServiceRunning(getActivity(), FetchDataService.class) || MySurveysPreference.isDownloaded(getActivity())) {
            surveysList = createDummySurveyList();
            if (Util.isServiceRunning(getActivity(), FetchDataService.class))
                ((HomeActivity) getActivity()).showSnackBar(getString(R.string.sync_msg), Snackbar.LENGTH_INDEFINITE);
            mShimmerViewContainer.startShimmerAnimation();
            surveysAdapter.swap(surveysList, mShimmerViewContainer);
        } else {
            surveysList.clear();
            if (!Util.isOnline(getActivity()) && !MySurveysPreference.isDownloaded(getActivity()))
                ((HomeActivity) getActivity()).showSnackBar(getString(R.string.no_network_msg), Snackbar.LENGTH_LONG);
        }
        surveysAction();
    }



    /**
     * Actions like getting the surveys from the db and
     * Also filtering geofence surveys
     * Also starting the download of all the offline surveys present under the panel are performed under this method.
     */
    private void surveysAction() {
        try {
            final long panelID = MySurveysPreference.getCurrentPanelID(getActivity());
            if (MySurveysPreference.isDownloaded(getActivity()) && panelID != 0) {
                Thread dataThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (fetchData) {
                            Exception exception = null;
                            List<OPGSurvey> opgAllSurveys = null;
                            try {
                                opgAllSurveys = getSurveysFromDB(panelID);
                            } catch (Exception e) {
                                exception = e;
                            }
                            final List<OPGSurvey> finalOpgAllSurveys = opgAllSurveys;
                            final Exception finalException = exception;
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onGetSurveysPostExecute(finalOpgAllSurveys, finalException);
                                    }
                                });
                            }
                        }
                    }
                });
                dataThread.setPriority(Thread.MIN_PRIORITY);
                dataThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<OPGSurvey> getSurveysFromDB(long panelID) throws Exception {
        Exception exception = null;
        boolean retryRetrivingData = true;
                           /*Retrieving the surveys from the db.We are retrieving the data in for loop
                            as there may be chance of db connection issue.So we are trying multiple times*/
        int count = 0;
        for (; count < 10; count++) {
            if (retryRetrivingData && fetchData) {
                try {
                    return RetriveOPGObjects.getAllSurveys(panelID);
                } catch (Exception e) {
                    retryRetrivingData = true;
                    exception = e;
                }
            } else {
                break;
            }
        }
        if (exception != null) {
            throw exception;
        }
        return null;
    }

    /**
     * This method will filter all the geofence surveys  and give remaining surveys and it also starts downloading all the offline surveys.
     *
     * @param opgAllSurveys
     * @return
     */
    private List<OPGSurvey> getGeofenceFilteredSurveys(List<OPGSurvey> opgAllSurveys) {
        OPGSurvey survey = null;
        List<OPGSurvey> filteredSurveys = new ArrayList<>();
        for (int i = 0; i < opgAllSurveys.size(); i++) {
            survey = opgAllSurveys.get(i);
            if (!survey.isGeofencing()) {
                filteredSurveys.add(survey);
            }
        }

        return filteredSurveys;
    }

    private void onGetSurveysPostExecute(List<OPGSurvey> opgSurveys, Exception exception) {
        if(no_surveys_tv!=null && recyclerView!=null && mShimmerViewContainer!=null && surveysAdapter!=null && MySurveysPreference.isDownloaded(getActivity())){
            try {
                if (opgSurveys != null) {
                    surveysList = getGeofenceFilteredSurveys(opgSurveys);
                    if (surveysList.size() > 0) {
                        no_surveys_tv.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        mShimmerViewContainer.startShimmerAnimation();
                    } else {
                        no_surveys_tv.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        mShimmerViewContainer.stopShimmerAnimation();
                    }
                    surveysAdapter.swap(surveysList, mShimmerViewContainer);
                } else {
                    mShimmerViewContainer.stopShimmerAnimation();
                    if (exception != null) {
                        ((HomeActivity) getActivity()).showSnackBar("Failed to fetch the surveys", Snackbar.LENGTH_LONG);
                    }
                }
            } catch (Exception e) {
                if(BuildConfig.DEBUG)
                    Log.e(TAG,"error on onGetSurveysPostExecute:"+e.getMessage());
                ((HomeActivity) getActivity()).showSnackBar("Failed to fetch the surveys", Snackbar.LENGTH_LONG);
                mShimmerViewContainer.stopShimmerAnimation();
            }
        }else{
            if(BuildConfig.DEBUG)
                Log.e(TAG,"related views are null onGetSurveysPostExecute:");
        }
    }

    private List<OPGSurvey> createDummySurveyList() {
        surveysList.clear();
        OPGSurvey opgSurvey = new OPGSurvey();
        for (int i = 0; i < 5; i++) {
            opgSurvey.setName("");
            opgSurvey.setOffline(false);
            surveysList.add(opgSurvey);
        }
        return surveysList;
    }

    @Override
    public void onDestroy() {
        surveysPositions = null;
        surveysList = null;
        surveysAdapter = null;
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private class SurveysAdapter extends RecyclerView.Adapter<SurveysAdapter.SurveyViewHolder> {

        List<OPGSurvey> opgSurveys;

        public SurveysAdapter(Activity context, List<OPGSurvey> surveyList) {
            opgSurveys = surveyList;
        }

        @Override
        public SurveyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_survey, parent,
                    false);
            return new SurveyViewHolder(v);
        }


        public  void refresh(){
            notifyDataSetChanged();
        }

        public void refresh(int position,String status){
            if(opgSurveys.size()>position){
                OPGSurvey opgSurvey = opgSurveys.get(position);
                opgSurvey.setStatus(status);
                opgSurveys.set(position,opgSurvey);
            }
            if(recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE){
                //notifyItemChanged(position);
                notifyDataSetChanged();
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public void onBindViewHolder(final SurveyViewHolder holder, int position) {
            try
            {
                final OPGSurvey survey = opgSurveys.get(position);
                if(survey.getName().isEmpty()){//if the view is dummy view
                    setDummyView(holder);
                }else{//if the view is not dummy view

                    holder.progressBar.setVisibility(View.INVISIBLE);
                    setDataToView(survey,holder);
                    setSurveyStatus(holder,survey);
                    holder.surveyDetail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startSurveyDetailActivity(survey);
                        }
                    });
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startSurveyDetailActivity(survey);
                        }
                    });

                    holder.surveyStatus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onClickSurveyStatus(survey,holder);
                        }
                    });
                }

            }catch (Exception ex)
            {
                Log.i("My Survyes",ex.getMessage());
            }
        }


        private void onClickSurveyStatus(OPGSurvey survey,SurveyViewHolder holder) {
        }


        private void setDataToView(OPGSurvey survey,SurveyViewHolder holder){
            holder.surveyTitle.setText(survey.getName());
            holder.surveyStatus.setBackgroundColor(getResources().getColor(android.R.color
                    .transparent));
            holder.surveyTitle.setBackgroundColor(getResources().getColor(android.R.color
                    .transparent));
            holder.surveyDetail.setBackgroundResource(R.drawable.roundedbtn_active);
            holder.surveyDetail.setEnabled(true);
            holder.itemView.setEnabled(true);
            setFont(holder);
            holder.tvCounter.setVisibility(View.GONE);
            GradientDrawable gradientDrawable = (GradientDrawable) holder.surveyDetail.getBackground();
            gradientDrawable.setColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity())));
        }


        private void setDummyView(SurveyViewHolder holder){
            holder.progressBar.setVisibility(View.INVISIBLE);
            holder.surveyStatus.setText("                                    ");
            holder.surveyTitle.setText("                       ");
            holder.surveyStatus.setBackgroundColor(getResources().getColor(android.R.color
                    .darker_gray));
            holder.surveyTitle.setBackgroundColor(getResources().getColor(android.R.color
                    .darker_gray));
            holder.surveyDetail.setBackgroundResource(R.drawable.roundedbtn_inactive);
            holder.surveyDetail.setEnabled(false);
            holder.itemView.setEnabled(false);
            GradientDrawable gradientDrawable = (GradientDrawable) holder.surveyDetail.getBackground();
            gradientDrawable.setColor(getResources().getColor(android.R.color
                    .darker_gray));
        }
        private void setFont(SurveyViewHolder holder){
            Util.setTypeface(getActivity(), holder.surveyTitle, "font/roboto_regular.ttf");
            Util.setTypeface(getActivity(), holder.surveyStatus, "font/roboto_regular.ttf");
        }

        private void setSurveyStatus(SurveyViewHolder holder, OPGSurvey opgSurvey)
        {
            try
            {
                if(opgSurvey.getStatus() != null)
                {
                    if(opgSurvey.getStatus().equalsIgnoreCase(DOWNLOAD_STATUS_KEY) && (MySurveysPreference.isScriptDataPresent(getActivity(),String.valueOf(opgSurvey.getSurveyID())))) {
                        opgSurvey.setStatus(NEW_STATUS_KEY);
                    }
                    if(opgSurvey.getStatus().equals(Util.COMPLETED_STATUS_KEY)) {
                        holder.surveyStatus.setText(getString(R.string.survey_status_completed));
                        holder.surveyStatus.setTextColor(getResources().getColor(R.color.sub_title_text_color));
                    }else if(opgSurvey.getStatus().equals(Util.PENDING_STATUS_KEY)) {
                        holder.surveyStatus.setText(getString(R.string.survey_status_pending));
                        holder.surveyStatus.setTextColor(getResources().getColor(R.color.sub_title_text_color));
                    } else if(opgSurvey.getStatus().equals(Util.NEW_STATUS_KEY)) {
                        holder.surveyStatus.setText(getString(R.string.survey_status_new));
                        holder.surveyStatus.setTextColor(getResources().getColor(R.color.sub_title_text_color));
                    }
                }
            }catch (Exception ex)
            {
                if(BuildConfig.DEBUG)
                    Log.i(Util.TAG,ex.getMessage());
            }

        }

        private void startSurveyDetailActivity(OPGSurvey survey) {
            Intent intent = new Intent(getActivity(), SurveyDetailActivity.class);
            intent.putExtra(Util.OPGSURVEY_KEY, survey);
            startActivity(intent);
        }

        @Override
        public int getItemCount() {
            return opgSurveys.size();
        }

        public void swap(List<OPGSurvey> opgSurveyList,ShimmerFrameLayout shimmerFrameLayout) {
            opgSurveys = opgSurveyList;
            notifyDataSetChanged();
            if(mShimmerViewContainer != null && surveysList.size() > 0) {
                if(!surveysList.get(0).getName().isEmpty())
                    mShimmerViewContainer.stopShimmerAnimation();
            }
            if((opgSurveys.size() == 0) || (opgSurveys.size()>0 && !(opgSurveys.get(0).getName().trim().length()==0))){
                ((HomeActivity) getActivity()).dismissSnackBar();
            }
        }

        public void clearAdapterData(){
            opgSurveys.clear();
            notifyDataSetChanged();
        }

        public class SurveyViewHolder extends RecyclerView.ViewHolder {

            TextView tvCounter;
            TextView surveyTitle;
            ImageView surveyDetail;
            TextView surveyStatus;
            View itemView;
            ProgressBar progressBar;

            public SurveyViewHolder(View itemView) {
                super(itemView);
                surveyTitle = (TextView) itemView.findViewById(R.id.survey_title_tv);
                surveyDetail = (ImageView) itemView.findViewById(R.id.iv_sur_det);
                surveyStatus = (TextView) itemView.findViewById(R.id.survey_status_tv);
                this.itemView = itemView;
                progressBar = (ProgressBar) itemView.findViewById(R.id.survey_progress_bar);
                tvCounter = (TextView)itemView.findViewById(R.id.tv_counter);
                progressBar.getProgressDrawable().setColorFilter(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity())), PorterDuff.Mode.SRC_IN);
            }
        }
    }

}
