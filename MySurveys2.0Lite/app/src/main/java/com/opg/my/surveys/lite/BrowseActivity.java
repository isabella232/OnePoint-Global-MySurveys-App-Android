package com.opg.my.surveys.lite;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.UpdateOPGObjects;
import com.opg.sdk.OPGActivity;
import com.opg.sdk.OPGSurveyInterface;
import com.opg.sdk.models.OPGSurvey;

public class BrowseActivity extends OPGActivity implements OPGSurveyInterface {

    private OPGSurvey opgSurvey;
    Context mContext;
    private ProgressDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Get SurveyId from Intent and pass it loadOnePointWebView as a params
         */
        mContext = this;
        if(!Util.isTablet(mContext)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(getString(R.string.loading_survey));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        try {
            if(getIntent() != null && getIntent().hasExtra(Util.OPGSURVEY_KEY))
            {
                opgSurvey = getIntent().getParcelableExtra(Util.OPGSURVEY_KEY);
                if(opgSurvey != null)
                {
                    //OnlineSurvey
                    super.loadOnlineSurvey(this, opgSurvey.getSurveyReference(), MySurveysPreference .getCurrentPanelID(mContext) , MySurveysPreference.getPanellistID(mContext));
                }
            }
            else if(getIntent() != null && getIntent().hasExtra(Util.SURVEY_REF))
            {
                String surveyRef = getIntent().getStringExtra(Util.SURVEY_REF);
                // super.loadOnlineSurvey(this, surveyRef);
                super.loadOnlineSurvey(this, surveyRef, MySurveysPreference .getCurrentPanelID(mContext), MySurveysPreference.getPanellistID(mContext));
            }
            else {
                Toast.makeText(mContext, getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                finish();
            }
        } catch (Exception ex) {
            if(BuildConfig.DEBUG) {
                Log.i(Util.TAG, ex.getMessage());
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    public void closeProgressDialog() {
        if(mProgressDialog != null && mProgressDialog.isShowing()) { mProgressDialog.dismiss(); }
    }

    /**
     * didSurveyCompleted  method called when done with all the questions.
     * User can do required action steps after survey is completed.
     */

    public void didSurveyCompleted() {
        closeProgressDialog();
        if(opgSurvey != null) {
            if(opgSurvey.isOffline()) {
                setOPGSurveyStatus(Util.UPLOAD_STATUS_KEY);
            }
            else {
                setOPGSurveyStatus(Util.COMPLETED_STATUS_KEY);
            }
        }
        finish();
    }

    /**
     * didSurveyFinishLoad called when webpage finished loading.
     */

    public void didSurveyFinishLoad() {
        closeProgressDialog();
    }

    /**
     * didSurveyStartLoad called when webpage is stated loading.
     */
    public void didSurveyStartLoad() {
        if(mProgressDialog != null)
        {
            mProgressDialog.show();
        }
        setOPGSurveyStatus(Util.PENDING_STATUS_KEY);
    }

    /**
     * used to change the status of the survey
     *
     * @param status
     */
    private void setOPGSurveyStatus(String status) {
        if(opgSurvey != null && !opgSurvey.getStatus().equalsIgnoreCase(status)) {
            opgSurvey.setStatus(status);
            try {
                boolean updateStatus = UpdateOPGObjects.updateSurveyStatus(opgSurvey.getSurveyID(), status);
                System.out.print(updateStatus);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        closeProgressDialog();
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStop() {
       closeProgressDialog();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        opgSurvey = null;
        mContext= null;
        mProgressDialog = null;
        super.onDestroy();
    }
}
