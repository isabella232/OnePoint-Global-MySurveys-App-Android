package com.opg.my.surveys.lite;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.RetriveOPGObjects;
import com.opg.my.surveys.lite.common.db.SaveOPGObjects;
import com.opg.my.surveys.lite.common.db.UpdateOPGObjects;
import com.opg.sdk.OPGSDK;
import com.opg.sdk.exceptions.OPGException;
import com.opg.sdk.geofence.OPGGeofenceTriggerEvents;
import com.opg.sdk.models.OPGGeofenceStatus;
import com.opg.sdk.models.OPGGeofenceSurvey;
import com.opg.sdk.models.OPGSurvey;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.app.AlarmManager.ELAPSED_REALTIME;
import static android.os.SystemClock.elapsedRealtime;
import static com.opg.my.surveys.lite.common.Util.RESTART_GEOFENCING;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener, OPGGeofenceTriggerEvents {

    private static final String TAG = LocationService.class.getSimpleName();

    private String LOCATION_ENABLED_ACTION = "android.location.PROVIDERS_CHANGED";
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private LocationRequest mLocationRequest;

    private Location mLastLocation;
    private NotificationManager notificationManager;
    public AsyncTask<Void,Void,List<OPGGeofenceSurvey>> getGeofenceSurveys;
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 30 * 60 *1000/*300000*/; // 30 minutes
    private static int FATEST_INTERVAL = 5 * 60 * 1000/*300000*/; // 5 minutes
    private static int DISPLACEMENT = 500; // 500 meters
    private String GEOFENCE_ENTER_TRANSISTION = "com.opg.my.surveys.lite.transition.enter";
    private String GEOFENCE_EXIT_TRANSISTION = "com.opg.my.surveys.lite.transition.exit";
    private String GEOFENCE_DWELL_TRANSISTION = "com.opg.my.surveys.lite.transition.dwell";

    private List<OPGGeofenceSurvey> opgGeofenceSurveys;
    private StringBuilder stringBuilder ;
    private LocationEnabledReceiver locationEnabledReceiver;
    private OPGSDK opgsdk;
    private boolean restartGeofencing = false;

    public LocationService() {

    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        opgsdk = Util.getOPGSDKInstance();
        sharedPreferences = mContext.getSharedPreferences(getString(R.string.app_name),MODE_PRIVATE);
        editor = sharedPreferences.edit();
        opgGeofenceSurveys = new ArrayList<>();
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        stringBuilder = new StringBuilder();
        locationEnabledReceiver = new LocationEnabledReceiver();
        if(BuildConfig.DEBUG) {
            Log.e(TAG, "OnCreate Called");
        }
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(LOCATION_ENABLED_ACTION);
        filter.addAction(GEOFENCE_ENTER_TRANSISTION);
        filter.addAction(GEOFENCE_EXIT_TRANSISTION);
        filter.addAction(GEOFENCE_DWELL_TRANSISTION);
        this.registerReceiver(locationEnabledReceiver, filter);


        String lastLocation = MySurveysPreference.getLastLocationKnown(mContext);
        if(!lastLocation.isEmpty()){
            mLastLocation = new Gson().fromJson(lastLocation, Location.class);
        }
        // First we need to check availability of play services
        if (checkPlayServices()) {
            createLocationRequest();
            // Building the GoogleApi client
            buildGoogleApiClient();
        }else{
            stopServiceSelf();
        }
    }

    private void stopServiceSelf() {
        MySurveysPreference.setIsGeofencingEnabled(mContext,false);
        unregisterLocationChangeReceiver();
        stopSelf();
    }

    private void unregisterLocationChangeReceiver() {
        if(locationEnabledReceiver != null) {
            this.unregisterReceiver(locationEnabledReceiver);
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }


    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            stopServiceSelf();
            return;
        }
        if(mLocationRequest!=null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }


    /**
     * This class used to monitor SMS
     */
    class LocationEnabledReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(LOCATION_ENABLED_ACTION) && Util.locationServicesEnabled(mContext)) {
                startLocationUpdates();
            }else if(intent.getAction().equalsIgnoreCase(GEOFENCE_ENTER_TRANSISTION)){
                List<OPGGeofenceSurvey> enteredSurveys = Util.convertStringToOPGGeofenceList(intent.getStringExtra("triggeredGeofences"));
                enteredSurveysRecieved(enteredSurveys);
            }else if(intent.getAction().equalsIgnoreCase(GEOFENCE_EXIT_TRANSISTION)){
                List<OPGGeofenceSurvey> exitedSurveys = Util.convertStringToOPGGeofenceList(intent.getStringExtra("triggeredGeofences"));
                exitedSurveysRecieved(exitedSurveys);
            }else if(intent.getAction().equalsIgnoreCase(GEOFENCE_DWELL_TRANSISTION)){
                List<OPGGeofenceSurvey> dwellSurveys = Util.convertStringToOPGGeofenceList(intent.getStringExtra("triggeredGeofences"));
                //dwellSurveysRecieved(dwellSurveys);
            }
        }
    }

    private void enteredSurveysRecieved(List<OPGGeofenceSurvey> list){
        if(BuildConfig.DEBUG) {
            Log.i("com.opg.sdk.enter","surveys Size:" + list.size());

        }
        try {
            for(OPGGeofenceSurvey triggeredSurvey : list)
            {
                if(triggeredSurvey.isEnter() && triggeredSurvey.isExit())
                {
                    MySurveysPreference.setGeofenceSurveyEnabled(this,triggeredSurvey.getSurveyReference(),true);
                    MySurveys.updateOPGGeofenceSurveys(triggeredSurvey,true);
                }
                else if(triggeredSurvey.isEnter() && !triggeredSurvey.isExit())
                {
                    MySurveysPreference.setGeofenceSurveyEnabled(this,triggeredSurvey.getSurveyReference(),true);
                    MySurveys.updateOPGGeofenceSurveys(triggeredSurvey,true);
                }
                else if(!triggeredSurvey.isEnter() && triggeredSurvey.isExit() && !isAlreadyEntered(triggeredSurvey) )
                {
                    MySurveysPreference.setGeofenceSurveyEnabled(this,triggeredSurvey.getSurveyReference(),false);
                    MySurveys.updateOPGGeofenceSurveys(triggeredSurvey,true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void exitedSurveysRecieved(List<OPGGeofenceSurvey> list){
        if(BuildConfig.DEBUG) {
            Log.i("com.opg.sdk.exit","Surveys Size:" + list.size());
        }
        try {
            for(OPGGeofenceSurvey triggeredSurvey : list)
            {
                if(triggeredSurvey.isEntered() && triggeredSurvey.isExit())
                {
                    MySurveys.updateOPGGeofenceSurveys(triggeredSurvey,true);
                    MySurveysPreference.setGeofenceSurveyEnabled(this,triggeredSurvey.getSurveyReference(),true);
                }else if(triggeredSurvey.isEnter() && !triggeredSurvey.isExit())
                {
                    MySurveys.updateOPGGeofenceSurveys(triggeredSurvey,true);
                    MySurveysPreference.setGeofenceSurveyEnabled(this,triggeredSurvey.getSurveyReference(),false);
                }
                else if(!triggeredSurvey.isEnter() && triggeredSurvey.isExit() && isAlreadyEntered(triggeredSurvey))
                {
                    MySurveys.updateOPGGeofenceSurveys(triggeredSurvey,true);
                    MySurveysPreference.setGeofenceSurveyEnabled(this,triggeredSurvey.getSurveyReference(),true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isAlreadyEntered(OPGGeofenceSurvey triggeredSurvey) throws Exception
    {
        List<OPGGeofenceSurvey> opgGeofenceSurveys = RetriveOPGObjects.getOPGGeofenceSurveys();
        for(OPGGeofenceSurvey survey : opgGeofenceSurveys)
        {
            if(survey.getAddressID() == triggeredSurvey.getAddressID() && survey.getSurveyID() == triggeredSurvey.getSurveyID() && survey.isEntered())
            {
                return survey.isEntered();
            }
        }
        return false;
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(BuildConfig.DEBUG) {
            Log.i(TAG, "Received start id " + startId + ": " + intent);
        }
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        if(intent.hasExtra(RESTART_GEOFENCING)){
            restartGeofencing = true;
        }else{
            restartGeofencing = false;
        }
        return START_REDELIVER_INTENT;
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(mContext);
        if (status != ConnectionResult.SUCCESS) {
            return false;
        }
        return (status == ConnectionResult.SUCCESS ? true :false );
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        restartGeofencingMonitoring();
        startLocationUpdates();
    }

    private void restartGeofencingMonitoring() {
        if(restartGeofencing){
            if(mContext!=null && mGoogleApiClient!=null && mGoogleApiClient.isConnected() && MySurveysPreference.getIsGeofencingEnabled(mContext)){
                List<OPGGeofenceSurvey> opgGeofenceSurveys = opgsdk.getOPGGeofenceSurveys(mContext);
                try {
                    opgsdk.startGeofencingMonitor(mContext,mGoogleApiClient,opgGeofenceSurveys,this);
                } catch (OPGException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        stopLocationUpdates();
        if(mGoogleApiClient!=null){
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        stopLocationUpdates();
        if(BuildConfig.DEBUG) {
            Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                    + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        if(mLastLocation == null){
            mLastLocation = location;
            storeLastLocation(mLastLocation);
            updateLocation(location,0 ,0);
        }else {
            int distance1 = (int) mLastLocation.distanceTo(location);
            int distance2 = (int) distanceInMts(mLastLocation.getLatitude(),mLastLocation.getLongitude(),location.getLatitude(),location.getLongitude());
            if(distance1>DISPLACEMENT && distance2 >= DISPLACEMENT){
                storeLastLocation(mLastLocation);
                updateLocation(location,distance1,distance2);
            }
        }
    }

    public void storeLastLocation(Location lastKnowLocation){
        try
        {
            if(lastKnowLocation!=null){
                String jsonLocation = mLastLocation.toString();
                //  if(jsonLocation.startsWith("{"))
                {
                    String json = lastKnowLocation == null ? null : new Gson().toJson(mLastLocation);
                    MySurveysPreference.setLastLocationKnown(mContext,json);
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


    public static float distanceInMts(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    private void updateLocation(Location location,int distance1 ,int distance2){
        mLastLocation = location;
        pushNotification(stringBuilder.toString(),true);
    }

    public void pushNotification(final String message,boolean fetchGeofences)
    {
        if(fetchGeofences) {
            fetchGeofenceSurveys();
        }
    }

    public String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return "Current Time : " + mdformat.format(calendar.getTime());
    }


    @Override
    public void onDestroy() {
        unregisterLocationChangeReceiver();
        stopLocationUpdates();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        PendingIntent restartServicePendingIntent = PendingIntent.getService(
                getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmService.set(ELAPSED_REALTIME, elapsedRealtime() + 1000,
                restartServicePendingIntent);
        opgsdk = Util.getOPGSDKInstance();
        super.onTaskRemoved(rootIntent);
    }


    public boolean isOnline(Context context)
    {
        ConnectivityManager cm =(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void fetchGeofenceSurveys() {
        if(isOnline((mContext))){
            if (getGeofenceSurveys == null || (getGeofenceSurveys != null && getGeofenceSurveys.getStatus() == AsyncTask.Status.FINISHED)) {
                getGeofenceSurveys = new GetGeofenceSurveys(this,mLastLocation.getLongitude(), mLastLocation.getLatitude());
                getGeofenceSurveys.execute();
                if(BuildConfig.DEBUG)
                    Log.e(TAG,"geofence async request started");
            }else{
                if(BuildConfig.DEBUG) {
                    if ((getGeofenceSurveys != null && getGeofenceSurveys.getStatus() != AsyncTask.Status.RUNNING)) {
                        Log.e(TAG, "geofence async is in running state");
                    } else if ((getGeofenceSurveys != null && getGeofenceSurveys.getStatus() != AsyncTask.Status.PENDING)) {
                        Log.e(TAG, "geofence async is in pending state");
                    } else if (getGeofenceSurveys != null) {
                        Log.e(TAG, "geofence async is in " + getGeofenceSurveys.getStatus() + "state");
                    } else {
                        Log.e(TAG, "geofence async is null");
                    }
                }
            }

        }
    }

    @Override
    public void onResult(OPGGeofenceStatus opgGeofenceStatus) {
        MySurveysPreference.setIsGeofencingEnabled(mContext, opgGeofenceStatus.isMonitoring());
        if(!opgGeofenceStatus.isSuccess())
            Toast.makeText(mContext, opgGeofenceStatus.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void didEnterSurveyRegion(Location location, List<OPGGeofenceSurvey> list) {
        //enteredSurveysRecieved(list);
    }

    @Override
    public void didExitSurveyRegion(Location location, List<OPGGeofenceSurvey> list) {
        //exitedSurveysRecieved(list);
    }

    /*@Override
    public void didDwellSurveyRegion(Location location, List<OPGGeofenceSurvey> list) {
        //dwellSurveysRecieved(list);
    }*/


    private class GetGeofenceSurveys extends AsyncTask<Void,Void,List<OPGGeofenceSurvey>> {

        OPGSDK opgsdk;
        double latitude, longitude;
        OPGException exception;
        Service service;

        GetGeofenceSurveys(Service activity, double longitude, double latitude) {
            opgsdk = Util.getOPGSDKInstance();
            this.longitude = longitude;
            this.latitude = latitude;
            this.service = activity;
        }

        @Override
        protected List<OPGGeofenceSurvey> doInBackground(Void... voids) {
            try {
                return opgsdk.getGeofenceSurveys(mContext, (float) latitude, (float) longitude);
            } catch (OPGException e) {
                e.printStackTrace();
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<OPGGeofenceSurvey> opgGeofenceSurveys) {
            super.onPostExecute(opgGeofenceSurveys);
            try {
                if (mContext != null) {

                    if (opgGeofenceSurveys != null) {
                        if(opgGeofenceSurveys.size() >0)
                        {
                            try {
                                UpdateOPGObjects.clearOPGGeofencesDB();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //Toast.makeText(mContext,"Geofence Service Stopped \n Geosurveys Size:"+opgGeofenceSurveys.size(),Toast.LENGTH_SHORT).show();
                            //pushNotification("Geosurveys Size:"+opgGeofenceSurveys.size()+"\n"+getCurrentTime(),false);
                            if(BuildConfig.DEBUG){
                                Log.d(TAG,"Geosurveys Size:"+opgGeofenceSurveys.size()+"\n"+getCurrentTime());
                            }
                            for (OPGGeofenceSurvey opgGeofenceSurvey : opgGeofenceSurveys) {
                                try {
                                    SaveOPGObjects.storeGeofenceSurvey(opgGeofenceSurvey);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            try
                            {
                                opgGeofenceSurveys = RetriveOPGObjects.getOPGGeofenceSurveys();
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            if (MySurveysPreference.getIsGeofencingEnabled(mContext) && opgGeofenceSurveys.size() > 0) {
                                opgsdk.startGeofencingMonitor(mContext, mGoogleApiClient, opgGeofenceSurveys, (OPGGeofenceTriggerEvents) service);
                            } else {
                                opgsdk.stopGeofencingMonitor(mContext, mGoogleApiClient, (OPGGeofenceTriggerEvents) service);
                            }
                        }
                        /*else{
                            //Toast.makeText(mContext,"Geofence Service Stopped\n No Surveys found",Toast.LENGTH_SHORT).show();
                        }*/
                    } /*else {
                        if (exception != null && exception.getMessage().contains(Util.SESSION_TIME_OUT_ERROR)) {
                            *//*Util.launchLoginActivity(activity);*//*
                        } else {
                            //MySurveysPreference.setIsGeofencingEnabled(mContext, false);
                            if (exception != null) {
                                Util.sendBroadcastMessage(true, mContext, "", Util.BROADCAST_ACTION_GEOFENCE_UI);
                                *//*if (exception.getMessage().equalsIgnoreCase("No address associated with hostname")) {
                                    showSnackBar(getString(R.string.err_no_internet), Snackbar.LENGTH_SHORT);
                                } else {
                                    showSnackBar(getString(R.string.unknown_error), Snackbar.LENGTH_SHORT);
                                }*//*
                                if (BuildConfig.DEBUG) {
                                    Log.e(TAG, exception.getMessage());
                                }
                            }
                        }
                    }*/
                }
            } catch (OPGException e) {
                //MySurveysPreference.setIsGeofencingEnabled(mContext, false);
                e.printStackTrace();
            }

        }
    }

}
