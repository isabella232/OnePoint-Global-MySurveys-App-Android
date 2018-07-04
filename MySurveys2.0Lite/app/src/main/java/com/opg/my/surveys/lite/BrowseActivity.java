package com.opg.my.surveys.lite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.UpdateOPGObjects;
import com.opg.sdk.OPGActivity;
import com.opg.sdk.OPGSurveyInterface;
import com.opg.sdk.models.OPGSurvey;

import java.util.ArrayList;
import java.util.List;

public class BrowseActivity extends OPGActivity implements OPGSurveyInterface {

    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private OPGSurvey opgSurvey;
    Context mContext;
    private ProgressDialog mProgressDialog;
    private List<String> listPermissionsNeeded;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private String[] permissions = new String[]{
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };

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

        listPermissionsNeeded = new ArrayList<>();
        sharedPreferences = mContext.getSharedPreferences(getString(R.string.app_name),MODE_PRIVATE);
        editor = sharedPreferences.edit();


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
                if(getIntent().hasExtra(Util.DEEP_LINKING_KEY)){
                    super.loadOnlineSurvey(this, surveyRef);
                }else{
                    super.loadOnlineSurvey(this, surveyRef, MySurveysPreference
                                    .getCurrentPanelID(mContext)
                            , MySurveysPreference.getPanellistID(mContext));
                }
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
        if(getIntent().hasExtra(Util.DEEP_LINKING_KEY)) {
            checkPermission(true);
        }
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
        goToHome();
    }

    private void goToHome(){
        if(getIntent().hasExtra(Util.DEEP_LINKING_KEY)) {
            Intent startMain = new Intent(mContext, SplashScreenActivity.class);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
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

    public boolean checkPermission(boolean requestPermission) {
        if(Build.VERSION.SDK_INT >= 23 && !MySurveysPreference.isPermissionsRequested(mContext)) {
            MySurveysPreference.setPermissionsRequested(mContext,true);
            int result;
            for (String p : permissions) {
                result = ContextCompat.checkSelfPermission(mContext, p);
                if(BuildConfig.DEBUG)
                    Log.d("Result", "" + result);
                if(result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(p);
                }
            }
            if(!listPermissionsNeeded.isEmpty()) {
                if(requestPermission) {
                    ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                }
                return false;
            }
            return true;
        }
        else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                for (int i = 0; i < permissions.length; i++) {
                    if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        if(listPermissionsNeeded.contains(grantResults[i])) {
                            listPermissionsNeeded.remove(grantResults);
                        }
                        if(BuildConfig.DEBUG)
                            Log.d("Permissions", "Permission Granted: " + permissions[i]);
                    }
                    else if(grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        if(BuildConfig.DEBUG)
                            Log.d("Permissions", "Permission Denied: " + permissions[i]);
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }
}
