package com.opg.my.surveys.lite;

import android.content.Context;
import android.os.AsyncTask;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.opg.dbhelper.DBHelperApplication;
import com.opg.dbhelper.OPGDBHelper;
import com.opg.logging.LogManager;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.RetriveOPGObjects;
import com.opg.my.surveys.lite.common.db.UpdateOPGObjects;
import com.opg.sdk.models.OPGGeofenceSurvey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Padmavathi on 13/10/2016.
 */

public class MySurveys extends MultiDexApplication {

    public static List<OPGGeofenceSurvey> avaliableOPGGeofenceSurveys;


    public static List<OPGGeofenceSurvey> getAvaliableOPGGeofenceSurveys() {
        return avaliableOPGGeofenceSurveys;
    }

    public static void setAvaliableOPGGeofenceSurveys(List<OPGGeofenceSurvey> avaliableOPGGeofenceSurveys) {
        MySurveys.avaliableOPGGeofenceSurveys = avaliableOPGGeofenceSurveys;
    }

    private static Context appContext;
    /**
     * This method is used to add OPGGeofenceSurveys to a list of already existing OPGGeofenceSurveys.
     * It will check whether the OPGGeofenceSurvey is exists or not.
     * If not then onlyit will add.
     * @param addOPGGeofenceSurveys
     */
    public static List<OPGGeofenceSurvey> addAvaliableOPGGeofenceSurveys(List<OPGGeofenceSurvey> avaliableOPGGeofenceSurveys,List<OPGGeofenceSurvey> addOPGGeofenceSurveys){
        if(avaliableOPGGeofenceSurveys.size() == 0 && addOPGGeofenceSurveys != null){
            avaliableOPGGeofenceSurveys = addOPGGeofenceSurveys;
        }else {
            for (OPGGeofenceSurvey opgGeofenceSurvey : addOPGGeofenceSurveys){

                boolean addStatus = true;
                for (OPGGeofenceSurvey opgGeofenceSurvey1 : avaliableOPGGeofenceSurveys){
                    if(opgGeofenceSurvey1.getAddressID() == opgGeofenceSurvey.getAddressID() && opgGeofenceSurvey1.getSurveyID() == opgGeofenceSurvey.getSurveyID()) {
                        addStatus = false;
                        break;
                    }
                }
                if(addStatus)
                {  avaliableOPGGeofenceSurveys.add(opgGeofenceSurvey); }
            }
        }
        return avaliableOPGGeofenceSurveys;
    }



    /**
     *  This method is used to remove OPGGeofenceSurveys from a list of already existing OPGGeofenceSurveys.
     * It will check whether the OPGGeofenceSurvey is exists or not.
     * If exists then only it will remove from the existing list.
     * @param removeOPGGeofenceSurveys
     */
    public static List<OPGGeofenceSurvey> removeOPGGeofenceSurveys(List<OPGGeofenceSurvey> avaliableOPGGeofenceSurveys ,List<OPGGeofenceSurvey> removeOPGGeofenceSurveys){
        List<OPGGeofenceSurvey> updatedGeofenceSurveys = avaliableOPGGeofenceSurveys;
        if(avaliableOPGGeofenceSurveys.size() != 0){
            for (OPGGeofenceSurvey opgGeofenceSurvey : removeOPGGeofenceSurveys){
                for (OPGGeofenceSurvey opgGeofenceSurvey1 : avaliableOPGGeofenceSurveys){
                    if(opgGeofenceSurvey1.getAddressID() == opgGeofenceSurvey.getAddressID() && opgGeofenceSurvey1.getSurveyID() == opgGeofenceSurvey.getSurveyID()) {
                        updatedGeofenceSurveys.remove(opgGeofenceSurvey1);
                        break ;
                    }
                }

            }
            avaliableOPGGeofenceSurveys = updatedGeofenceSurveys;
        }
        return avaliableOPGGeofenceSurveys;
    }

    /**
     * It will check the given list has the opggeofencesurvey object or not.If it there we will change the status is deleted to the assigned value.
     * Here isdeleted is used as isEntered variable.It says whether the user entered the survey location or not.
     * @param updatedList
     * @param enterStatus
     * @return
     */
    public static List<OPGGeofenceSurvey> updateOPGGeofenceSurveys(List<OPGGeofenceSurvey> updatedList, boolean enterStatus) throws Exception {
        List<OPGGeofenceSurvey> opgGeofenceSurveys = RetriveOPGObjects.getOPGGeofenceSurveys();//Getting the main geofence list from db
        if(opgGeofenceSurveys != null && opgGeofenceSurveys.size() >= 0  ){

            for (OPGGeofenceSurvey opgGeofenceSurvey : updatedList){

                for (OPGGeofenceSurvey opgGeofenceSurvey1 : opgGeofenceSurveys){

                    if(opgGeofenceSurvey1.getAddressID() == opgGeofenceSurvey.getAddressID() && opgGeofenceSurvey.getSurveyID() == opgGeofenceSurvey1.getSurveyID()) {
                        opgGeofenceSurvey1.setDeleted(enterStatus);
                        Boolean status = UpdateOPGObjects.updateOPGGeofenceSurveyStatus(opgGeofenceSurvey1.getAddressID(),opgGeofenceSurvey1.getSurveyID(),enterStatus);
                        if(BuildConfig.DEBUG)
                            System.out.println("Update Status: "+enterStatus);
                    }
                }
            }
        }
        return opgGeofenceSurveys;
    }

    public static void clearHashMaps(){

    }
    @Override
    public void onCreate() {
        super.onCreate();
        avaliableOPGGeofenceSurveys = new ArrayList<>();
        appContext = this;
        //Setting up DB
        try{
            DBHelperApplication.setAppContext(appContext);
            OPGDBHelper.mContext = appContext;
            setUpDB();
        }catch (Exception e){
            Log.e(appContext.getPackageName(),e.toString());
        }
    }

    private void setUpDB() {
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                if (!MySurveysPreference.isDBCreated(getApplicationContext())) {
                    OPGDBHelper.setDatabaseName(Util.db_name);
                    if(BuildConfig.DEBUG)
                        LogManager.getLogger(getClass()).error(Util.db_name + " SETUP STARTED!");
                    OPGDBHelper.getInstance().getWritableDatabase();
                    if(BuildConfig.DEBUG)
                        LogManager.getLogger(getClass()).error(Util.db_name + " SETUP ENDED!");
                    MySurveysPreference.setIsDBCreated(appContext, true);
                }
                return null;
            }
        };
    }
}
