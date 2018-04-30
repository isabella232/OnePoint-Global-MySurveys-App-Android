package com.opg.my.surveys.lite;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.facebook.AccessToken;
import com.google.firebase.iid.FirebaseInstanceId;
import com.opg.my.surveys.lite.common.LoginType;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.SaveOPGObjects;
import com.opg.sdk.exceptions.OPGException;
import com.opg.sdk.models.OPGAuthenticate;
import com.opg.sdk.models.OPGCountry;
import com.opg.sdk.models.OPGPanel;
import com.opg.sdk.models.OPGPanelPanellist;
import com.opg.sdk.models.OPGPanellistPanel;
import com.opg.sdk.models.OPGPanellistProfile;
import com.opg.sdk.models.OPGSurvey;
import com.opg.sdk.models.OPGSurveyPanel;
import com.opg.sdk.models.OPGTheme;

import java.util.ArrayList;
import java.util.List;

public class FetchDataService extends Service implements LoginListener{

    Context mContext;
    public FetchDataService() {
        mContext = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Getting data from server
        new FetchAPI().execute();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onLoginProcessStarted() {

    }

    @Override
    public void onLoginProcessCompleted(OPGAuthenticate opgAuthenticate) {
        if (opgAuthenticate.isSuccess()) {
            MySurveysPreference.setIsUserLoggedIn(getApplicationContext(), true);
            new FetchAPI().execute();
        } else {
            //Perform Logout Operation ........
            //Need to write to place the logout code in the homeactivity .........
          /*  Util.sendBroadcastMessage(false,mContext,"UniqueID does not exist.",Util.BROADCAST_ACTION_SAVE_DATA);*/
            throwExceptionMsg(false,"UniqueID does not exist.");
        }
    }

    /**
     * This is used to fetch the data from the sdk/online and store to the database
     */
    public class FetchAPI extends AsyncTask<Void,String,Boolean> {
        public String exceptionMsg = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                OPGPanellistPanel opgPanellistPanel = Util.getOPGSDKInstance().getPanellistPanel(mContext);
                if(opgPanellistPanel.isSuccess())
                {
                    if(FirebaseInstanceId.getInstance() != null && FirebaseInstanceId.getInstance().getToken() != null &&  FirebaseInstanceId.getInstance().getToken().length()!=0){
                        String response =  Util.getOPGSDKInstance().registerNotifications(mContext,FirebaseInstanceId.getInstance().getToken());
                        if(response.equals(Util.SESSION_TIME_OUT_ERROR))
                        {
                            throw new Exception(Util.SESSION_TIME_OUT_ERROR);
                        }
                        if(BuildConfig.DEBUG) {
                            Log.e("Server Response:", response);
                        }
                    }
                    boolean panelsSaved = getPanels(opgPanellistPanel);
                    if(!panelsSaved)
                        return false;

                    boolean themesSaved = getThemes(opgPanellistPanel);
                    if(!themesSaved)
                        return false;

                    boolean surveyPanelsSaved = getSurveyPanels(opgPanellistPanel);
                    if(!surveyPanelsSaved)
                        return false;

                    boolean panelPanellistsSaved = getPanelPanellists(opgPanellistPanel);
                    if(!panelPanellistsSaved)
                        return false;

                }
                else
                {
                    if(BuildConfig.DEBUG)
                        System.out.print("Failed to fetch the  opgPanellistPanel ");
                    exceptionMsg = opgPanellistPanel.getStatusMessage();
                    return false;
                }

                boolean surveysSaved = getSurveys();
                if(!surveysSaved)
                    return false;

                boolean panelllistProfileSaved = getPanellistProfile();
                if(!panelllistProfileSaved)
                    return false;

                boolean countriesSaved = getCountries();
                if(!countriesSaved)
                    return false;

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                exceptionMsg = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            try{
                MySurveysPreference.setIsDownloaded(mContext,aBoolean);
                if(!aBoolean || !exceptionMsg.isEmpty())
                {
                    if(exceptionMsg.contains(Util.SESSION_TIME_OUT_ERROR))
                    {
                        Util.sendBroadcastMessage(false,mContext,Util.SESSION_TIME_OUT_ERROR,Util.BROADCAST_ACTION_SAVE_DATA);
                    }
                    else{
                        if(exceptionMsg.contains(Util.NO_INTERNET_ERROR))
                        {
                            exceptionMsg = "Network is unreachable.";
                        }
                        throwExceptionMsg(aBoolean,exceptionMsg);
                    }
                }
                else
                {
                    Util.sendBroadcastMessage(aBoolean,mContext,"",Util.BROADCAST_ACTION_SAVE_DATA);
                    stopSelf();
                }
            }catch (Exception e){
                throwExceptionMsg(aBoolean,e.getMessage());
            }
        }



        public boolean getPanels(OPGPanellistPanel opgPanellistPanel) throws Exception{
            List<OPGPanel> opgPanels = opgPanellistPanel.getPanelArray();
            int i =0;
            boolean panelPresent = false;
            for (OPGPanel panel : opgPanels){
                try {
                    boolean status = SaveOPGObjects.storePanel(panel);
                    if(status){
                        if( MySurveysPreference.getCurrentPanelID(mContext)!= 0 && MySurveysPreference.getCurrentPanelID(mContext) == panel.getPanelID()){
                            panelPresent = true;
                        }
                    }else{
                        return false;
                    }
                    i++;
                } catch (Exception e) {
                    e.printStackTrace();
                    if(BuildConfig.DEBUG)
                        System.out.print("Failed to save the panel : "+panel.getName()+"\n Reason");
                    exceptionMsg = e.getMessage();
                    return  false;
                }
            }
            if(opgPanels.size()>0 && !panelPresent){//if the theme is not already set
                MySurveysPreference.setCurrentPanelID(mContext, opgPanels.get(0).getPanelID());
                MySurveysPreference.setCurrentPanelName(mContext,opgPanels.get(0).getName());
            }
            return true;
        }

        public boolean getThemes(OPGPanellistPanel opgPanellistPanel) throws Exception{
            List<OPGTheme> opgThemes = opgPanellistPanel.getThemeArray();
            for (OPGTheme theme : opgThemes){
                try {
                    boolean status = SaveOPGObjects.storeTheme(theme);
                    if(!status)
                        return status;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.print("Failed to save the theme : "+theme.getName()+"\n Reason");
                    exceptionMsg = e.getMessage();
                    return  false;
                }
            }
            return true;
        }

        public boolean getSurveyPanels(OPGPanellistPanel opgPanellistPanel) throws Exception{
            List<OPGSurveyPanel> opgSurveyPanels = opgPanellistPanel.getSurveyPanelArray();
            for (OPGSurveyPanel surveyPanel : opgSurveyPanels){
                try {
                    boolean status = SaveOPGObjects.storeSurveyPanel(surveyPanel);
                    if(!status)
                        return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    if(BuildConfig.DEBUG)
                        System.out.print("Failed to save the surveyPanel : "+surveyPanel.getSurveyPanelID()+"\n Reason");
                    exceptionMsg = e.getMessage();
                    return  false;
                }
            }
            return true;
        }


        public boolean getPanelPanellists(OPGPanellistPanel opgPanellistPanel) throws Exception{

            List<OPGPanelPanellist> opgPanelPanellists = opgPanellistPanel.getPanelPanellistArray();
            for (OPGPanelPanellist panelPanellist : opgPanelPanellists){
                try {
                    boolean status = SaveOPGObjects.storePanelPanellist(panelPanellist);
                    if(!status)
                        return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    if(BuildConfig.DEBUG)
                        System.out.print("Failed to save the panelPanellist : "+panelPanellist.getPanelPanellistID()+"\n Reason");
                    exceptionMsg = e.getMessage();
                    return  false;
                }
            }
            return true;
        }

        public boolean getSurveys() throws OPGException {
            ArrayList<OPGSurvey> opgSurveys = Util.getOPGSDKInstance().getUserSurveyList(mContext);
            for (OPGSurvey survey:opgSurveys){
                try {
                    boolean status = SaveOPGObjects.storeSurvey(survey);
                    if(!status)
                        return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.print("Failed to save the survey : "+survey.getName()+"\n Reason");
                    exceptionMsg = e.getMessage();
                    return  false;
                }
            }

            return true;
        }

        public boolean getCountries() throws OPGException {
            List<OPGCountry> opgCountries = Util.getOPGSDKInstance().getCountries(mContext);
            for (OPGCountry country : opgCountries){
                try {
                    boolean status = SaveOPGObjects.storeCountry(country);
                    if(!status)
                        return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.print("Failed to save the country : "+country.getCountryName()+"\n Reason");
                    exceptionMsg = e.getMessage();
                    return  false;
                }
            }
            return true;
        }

        public boolean getPanellistProfile() throws Exception {
            OPGPanellistProfile opgPanellistProfile = Util.getOPGSDKInstance().getPanellistProfile(mContext);
            if(opgPanellistProfile.isSuccess())
            {
                MySurveysPreference.setPanellistID(mContext,opgPanellistProfile.getPanellistID());
                return  SaveOPGObjects.storePanellistProfile(opgPanellistProfile);
            }
            else if(opgPanellistProfile.getStatusMessage().contains(Util.SESSION_TIME_OUT_ERROR))
            {
                throw new Exception(opgPanellistProfile.getStatusMessage());
            }
            else
            {
                return false;
            }

        }
    }

    public void throwExceptionMsg(boolean aBoolean,String msg){
        Util.sendBroadcastMessage(aBoolean,mContext,msg,Util.BROADCAST_ACTION_SAVE_DATA);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        System.out.println("Service Stopped");
        super.onDestroy();
    }
}
