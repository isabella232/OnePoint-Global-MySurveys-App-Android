package com.opg.my.surveys.lite;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.RetriveOPGObjects;
import com.opg.sdk.models.OPGSurvey;

import java.text.SimpleDateFormat;

import static com.opg.my.surveys.lite.common.Util.DOWNLOAD_STATUS_KEY;
import static com.opg.my.surveys.lite.common.Util.NEW_STATUS_KEY;

public class SurveyDetailActivity extends RootActivity implements View.OnClickListener
{
    private TextView tvSurveyTitle;
    private TextView tvSurveyStatus;
    private OPGSurvey opgSurvey;
    private TextView tvFromToDate, tvTakeTrail, tvUploadSurveyResults;
    private TextView tvApproxTime,tvCounter;
    private ImageView btnBack;
    private ImageView btnTakeSurvey;
    private Context mContext;
    private LinearLayout counterLinearLayout;
    private LinearLayout container;
    private ProgressBar survey_progress_bar;
    private Toast toast;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(Util.BROADCAST_ACTION_REFRESH_UPLOAD)){
                if(opgSurvey!=null && opgSurvey.getSurveyID() == Long.parseLong(intent.getStringExtra("message"))){
                    onResumeActivity();
                }
            }else  if(intent.getAction().equals(Util.BROADCAST_ACTION_SURVEY_UPLOADED)){
                //This broadcast we will get from the Upload offline result async task...
                if(opgSurvey!=null){
                    if(opgSurvey.getSurveyID() == intent.getLongExtra("surveyID",0)){
                        if(intent.getBooleanExtra(Util.KEY_UPLOAD_STATUS,false)){
                            displayMessage(getString(R.string.success_results_uploaded));
                            hideProgressBar();
                        }else {
                            if(intent.getStringExtra(Util.MESSAGE_KEY).length() != 0){
                                displayMessage(intent.getStringExtra(Util.MESSAGE_KEY));
                            }else {
                                displayMessage(getString(R.string.err_upload_survey_failed));
                            }
                        }
                    }
                }
            }
            else if(intent.getAction().equals(Util.ACTION_SESSION_EXPIRED))
            {
                Util.launchLoginActivity((Activity) mContext);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_detail);

        mContext         = this;
        if(!Util.isTablet(mContext)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        tvSurveyTitle = (TextView) findViewById(R.id.tv_title_survey);
        tvSurveyStatus = (TextView) findViewById(R.id.tv_survey_status);
        tvFromToDate = (TextView)findViewById(R.id.tv_from_to_date);
        tvApproxTime = (TextView)findViewById(R.id.tv_approx_time);
        tvCounter = (TextView)findViewById(R.id.tv_counter);
        tvTakeTrail = (TextView)findViewById(R.id.btn_take_trial);
        tvUploadSurveyResults = (TextView)findViewById(R.id.btn_upload_survey_result);
        counterLinearLayout =(LinearLayout)findViewById(R.id.counter_layout);
        container = (LinearLayout)findViewById(R.id.container_approx_status_counter);
        survey_progress_bar = (ProgressBar)findViewById(R.id.survey_progress_bar);
        btnBack = (ImageView)findViewById(R.id.btn_back);
        btnTakeSurvey = (ImageView)findViewById(R.id.btn_take_survey);
        tvTakeTrail.setOnClickListener(this);
        tvTakeTrail.setVisibility(View.GONE);
        tvUploadSurveyResults.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        setTextViewFont();
        Drawable background = btnTakeSurvey.getBackground();
        if (background instanceof GradientDrawable)
        {
            // cast to 'GradientDrawable'
            GradientDrawable gradientDrawable = (GradientDrawable) background;
            gradientDrawable.setColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(mContext)));
        }
        tvUploadSurveyResults.setTextColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(mContext)));
        tvTakeTrail.setTextColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(mContext)));
        survey_progress_bar.getProgressDrawable().setColorFilter(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(mContext)), PorterDuff.Mode.SRC_IN);
        if(getIntent() != null && getIntent().getExtras() != null)
        {
            opgSurvey = getIntent().getParcelableExtra(Util.OPGSURVEY_KEY);
            tvSurveyTitle.setText(opgSurvey.getName());
            try
            {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
                StringBuffer stringBuffer = new StringBuffer();
                if(!dateFormat.format(opgSurvey.getCreatedDate()).equalsIgnoreCase("01 Jan 0001")) {
                    stringBuffer.append(dateFormat.format(opgSurvey.getCreatedDate())).append("\n-");
                }
                if(!dateFormat.format(opgSurvey.getLastUpdatedDate()).equalsIgnoreCase("01 Jan 0001")) {
                    stringBuffer.append(dateFormat.format(opgSurvey.getLastUpdatedDate()));
                }
                if(stringBuffer.toString().trim().length()==0)
                    stringBuffer.append(getString(R.string.unscheduled_str));
                tvFromToDate.setText(stringBuffer.toString());
            }catch (Exception ex)
            {
                Log.i(Util.TAG,ex.getMessage());
            }


            StringBuffer buffer = new StringBuffer();
            buffer.append(getResources().getString(R.string.approx_time)).append("\n");
            buffer.append(opgSurvey.getEstimatedTime()).append(" ").append(getResources().getString(R.string.min_time));
            tvApproxTime.setText(buffer);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter iff = new IntentFilter();
        iff.addAction(Util.BROADCAST_ACTION_REFRESH_UPLOAD);
        iff.addAction(Util.BROADCAST_ACTION_SURVEY_UPLOADED);
        iff.addAction(Util.ACTION_SESSION_EXPIRED);
        registerReceiver(mReceiver, iff);
        onResumeActivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private void displayMessage(String message){
        try {
            Util.showMessageDialog(mContext,message);
        }catch (Exception e){}
    }

    private void onResumeActivity()
    {
        try
        {
            if(opgSurvey != null )
            {
                opgSurvey = RetriveOPGObjects.getSurvey(opgSurvey.getSurveyID());
                setSurveyStatus(opgSurvey);
                if(opgSurvey.isOffline())
                {
                    container.setWeightSum(3.0f);
                    counterLinearLayout.setVisibility(View.VISIBLE);
                    setOfflineViews(opgSurvey);
                    tvUploadSurveyResults.setVisibility(View.VISIBLE);
                }
                else
                {
                    container.setWeightSum(2.0f);
                    counterLinearLayout.setVisibility(View.GONE);
                    tvUploadSurveyResults.setVisibility(View.GONE);
                }
            }
        }catch (Exception ex)
        {
            Log.i(SurveyDetailActivity.class.getCanonicalName(),ex.getMessage());
        }
    }

    private void hideProgressBar()
    {
        if(survey_progress_bar != null)
        {
            survey_progress_bar.setVisibility(View.GONE);
        }
    }
    private void setOfflineViews(OPGSurvey opgSurvey) throws Exception{
    }

    /**
     * Updating the status
     * @param survey
     */
    private void setSurveyStatus( OPGSurvey survey)
    {
        try
        {
            if(survey.getStatus() != null)
            {
                if(opgSurvey.getStatus().equalsIgnoreCase(DOWNLOAD_STATUS_KEY)
                        && (MySurveysPreference.isScriptDataPresent(mContext,String.valueOf(opgSurvey.getSurveyID())))) {
                    opgSurvey.setStatus(NEW_STATUS_KEY);
                }
                if(survey.getStatus().equals(Util.COMPLETED_STATUS_KEY)) {
                    tvSurveyStatus.setText(getString(R.string.survey_status_completed));
                }
                else if(survey.getStatus().equals(Util.PENDING_STATUS_KEY)) {
                    tvSurveyStatus.setText(getString(R.string.survey_status_pending));
                }
                else if(survey.getStatus().equals(Util.NEW_STATUS_KEY)) {
                    tvSurveyStatus.setText(getString(R.string.survey_status_new));
                }
            }
        }catch (Exception ex)
        {
            Log.i(Util.TAG,ex.getMessage());
        }
    }

    private void setTextViewFont(){
        Util.setTypeface(mContext, tvSurveyTitle,"font/roboto_regular.ttf");
        Util.setTypeface(mContext, tvSurveyStatus,"font/roboto_regular.ttf");
        Util.setTypeface(mContext, tvFromToDate,"font/roboto_regular.ttf");
        Util.setTypeface(mContext, tvTakeTrail,"font/roboto_regular.ttf");
        Util.setTypeface(mContext, tvUploadSurveyResults,"font/roboto_regular.ttf");
    }


    public void onStartSurvey(View view)
    {
        if(Util.isOnline(mContext))
        {
            if(opgSurvey != null && !opgSurvey.isOffline())
            {
                Intent intent = new Intent(SurveyDetailActivity.this, BrowseActivity.class);
                intent.putExtra(Util.SURVEY_REF,opgSurvey.getSurveyReference());
                startActivity(intent);
            }
        }
        else
        {
            displayMessage(getString(R.string.err_no_internet));
        }

    }

    private  void onBackBtnPressed()
    {
        super.onBackPressed();
        if(toast != null)
        {
            toast.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(toast != null)
        {
            toast.cancel();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btn_back :
                onBackBtnPressed();
                break;
            case R.id.btn_take_trial :
                takeTrial();
                break;
            case R.id.btn_upload_survey_result :
                uploadSurveyResults();
                break;
        }
    }
    private void takeTrial()
    {
        showToast("No Trial");
    }

    private void uploadSurveyResults(){
    }

    private void showToast(String message)
    {
        if(toast != null)
        {
            toast.cancel();
        }
        toast = Toast.makeText(SurveyDetailActivity.this,message,Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        tvSurveyTitle = null;
        tvSurveyStatus = null;
        opgSurvey = null;
        tvFromToDate= null; tvTakeTrail= null; tvUploadSurveyResults= null;
        tvApproxTime= null;tvCounter= null;
        btnBack= null;
        mContext= null;
        counterLinearLayout = null;
        container= null;
        if(toast != null)
        {
            toast.cancel();
        }
        super.onDestroy();
    }
}
