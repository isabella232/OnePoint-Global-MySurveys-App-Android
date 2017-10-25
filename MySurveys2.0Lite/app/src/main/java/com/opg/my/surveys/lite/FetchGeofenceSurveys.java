package com.opg.my.surveys.lite;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.SaveOPGObjects;
import com.opg.sdk.OPGSDK;
import com.opg.sdk.exceptions.OPGException;
import com.opg.sdk.models.OPGGeofenceSurvey;

import java.util.List;

public class FetchGeofenceSurveys extends Service {
    Context mContext;

    public FetchGeofenceSurveys() {
        mContext = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        double latitude =0.0 ,longitude = 0.0;
        if(intent != null){
            Bundle bundle = intent.getExtras();
            if( bundle!= null && bundle.getDouble(Util.LATITUDE_KEY ,0.0 ) != 0.0 && bundle.getDouble(Util.LONGITUDE_KEY ,0.0 ) != 0.0){
                latitude = intent.getExtras().getDouble(Util.LATITUDE_KEY);
                longitude = intent.getExtras().getDouble(Util.LONGITUDE_KEY);
            }
            new GetGeofenceSurveys(latitude,longitude).execute();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class GetGeofenceSurveys extends AsyncTask<Void,Void,List<OPGGeofenceSurvey>> {

        OPGSDK opgsdk;
        double latitude,longitude;
        OPGException exception;
        GetGeofenceSurveys(double longitude, double latitude) {
            opgsdk = Util.getOPGSDKInstance();
            this.longitude = longitude;
            this.latitude = latitude;
        }

        @Override
        protected List<OPGGeofenceSurvey> doInBackground(Void... voids) {
            try {
                return opgsdk.getGeofenceSurveys(mContext,(float) latitude, (float)longitude);
            } catch (OPGException e) {
                e.printStackTrace();
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<OPGGeofenceSurvey> opgGeofenceSurveys) {
            super.onPostExecute(opgGeofenceSurveys);
            if(opgGeofenceSurveys != null){
                // Get the geofences used. Geofence data is hard coded in this sample.
                for (OPGGeofenceSurvey opgGeofenceSurvey:opgGeofenceSurveys){
                    try {
                        SaveOPGObjects.storeGeofenceSurvey(opgGeofenceSurvey);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                opgGeofenceSurveys.clear();
                Util.sendBroadcastMessage(true,mContext,"",Util.BROADCAST_ACTION_GEOFENCES_UPDATED);
            }
            stopSelf();
        }
    }
}
