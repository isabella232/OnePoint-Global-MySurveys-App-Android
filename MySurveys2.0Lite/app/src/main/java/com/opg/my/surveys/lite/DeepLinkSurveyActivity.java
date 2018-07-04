package com.opg.my.surveys.lite;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;


import com.opg.my.surveys.lite.common.Aes256;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;

import org.json.JSONObject;

public class DeepLinkSurveyActivity extends RootActivity {

    private String url;
    private Context mContext;
    private String SURVEY_REF_KEY = "SurveyRef=";
    private String DATA_KEY = "data=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_survey);
        mContext = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadSurvey();
        if(!MySurveysPreference.isUserLoggedIn(getApplicationContext())){
            try {
                sdkInitialize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the url from intent and split the surveyReference.
     * Loads the survey based on the surveyReference.
     */
    private void loadSurvey() {
        url = getIntent().getData().toString();
        try {
            if(Util.isOnline(mContext) && url!=null &&(url.contains(SURVEY_REF_KEY) || url.contains(DATA_KEY) )){
                String data;
                Intent startSurvey = new Intent(getApplicationContext(),BrowseActivity.class);
                if(url.contains(SURVEY_REF_KEY) && url.split(SURVEY_REF_KEY).length==2){
                    data = getAmpSepValue(url.split(SURVEY_REF_KEY)[1]);
                    startSurvey.putExtra(Util.SURVEY_REF,data);
                }else {
                    data = url.split(DATA_KEY)[1];
                    //JSONObject jsonObject = new JSONObject(com.opg.sdk.Aes256.decrypt(data));
                    JSONObject jsonObject = new JSONObject(Aes256.decrypt(data));
                    startSurvey.putExtra(Util.SURVEY_REF,jsonObject.getString(SURVEY_REF_KEY.replace("=","")));
                }
                startSurvey.putExtra(Util.DEEP_LINKING_KEY,true);
                startActivity(startSurvey);
            }else{
                if(!Util.isOnline(mContext)){
                    Toast.makeText(mContext,getString(R.string.err_no_internet),Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(mContext,getString(R.string.unknown_error),Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext,getString(R.string.unknown_error),Toast.LENGTH_LONG).show();
        }
        finish();
    }


    private String getAmpSepValue(String value){
        if(value.contains("&")){
            String[] seperatedList = value.split("&");
            return  seperatedList[0];
        }else{
            return value;
        }
    }
}
