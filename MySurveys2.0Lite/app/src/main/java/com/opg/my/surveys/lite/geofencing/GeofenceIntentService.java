package com.opg.my.surveys.lite.geofencing;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.opg.my.surveys.lite.BuildConfig;
import com.opg.my.surveys.lite.HomeActivity;
import com.opg.my.surveys.lite.LoginActivity;
import com.opg.my.surveys.lite.R;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.SaveOPGObjects;
import com.opg.my.surveys.lite.common.db.RetriveOPGObjects;
import com.opg.sdk.models.OPGGeofenceSurvey;

import org.json.JSONObject;

import java.util.List;
import java.util.Random;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class GeofenceIntentService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_ENTER = "com.opg.my.surveys.lite.transition.enter";
    public static final String ACTION_EXIT  = "com.opg.my.surveys.lite.transition.exit";
    public static final String ACTION_DWELL = "com.opg.my.surveys.lite.transition.dwell";

    Handler handler;
    NotificationManager notificationManager;

    public GeofenceIntentService() {
        super("GeofenceIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_ENTER.equals(action) || ACTION_EXIT.equals(action) /*|| ACTION_DWELL.equals(action)*/) {
                if(BuildConfig.DEBUG) {
                    Log.i("GeofenceIntentTriggered", "com.opg.sdk.transistion.enter");
                }
               /* String transistionType = "";
                if(ACTION_ENTER.equals(action)){
                    transistionType = "Entered survey location of ";
                }else  if(ACTION_EXIT.equals(action)){
                    transistionType = "Exited survey location of ";
                }else  if(ACTION_DWELL.equals(action)){
                    transistionType = "Present in survey location of ";
                }*/

                List<OPGGeofenceSurvey> newSurveys = Util.convertStringToOPGGeofenceList(intent.getStringExtra("triggeredGeofences"));
                if(BuildConfig.DEBUG) {
                    Log.i("GeofenceIntentTriggered", "TriggeredGeofences"+newSurveys.size());
                }
                StringBuilder stringBuilder = new StringBuilder();
                for (OPGGeofenceSurvey opgGeofenceSurvey:newSurveys){
                    stringBuilder.setLength(0);
                    try {
                        //  stringBuilder.append("Action").append(" : ").append(action).append("\n");

                        if((ACTION_ENTER.equals(action) && opgGeofenceSurvey.isEnter())||(ACTION_EXIT.equals(action) && opgGeofenceSurvey.isExit()))
                        {
                            stringBuilder.append(opgGeofenceSurvey.getSurveyName()).append("\n");
                            if(ACTION_ENTER.equals(action) && opgGeofenceSurvey.isEnter())
                            {
                                stringBuilder.append(getString(R.string.welcome_to)).append(" ").append(opgGeofenceSurvey.getAddress()).append("\n");
                                stringBuilder.append(getString(R.string.geofence_noti_msg));
                            }else if(ACTION_EXIT.equals(action))
                            {
                                stringBuilder.append(getString(R.string.thank_you_for_visiting)).append(" ").append(opgGeofenceSurvey.getAddress()).append("\n");
                                stringBuilder.append(getString(R.string.survey_available));
                            }
                            String notification_message = stringBuilder.toString();
                            JSONObject notificationJSON = new JSONObject();
                            notificationJSON.put("title",stringBuilder.toString());
                            // stringBuilder.append("\n\nAddress:").append(opgGeofenceSurvey.getAddress());
                            notificationJSON.put("alert",stringBuilder.toString());
                            notificationJSON.put("SurveyRef",opgGeofenceSurvey.getSurveyReference());
                            if(BuildConfig.DEBUG) {
                                Log.i("GeofenceIntentTriggered", "NotificationMessage"+notification_message);
                            }
                            updateDb(notificationJSON);
                            pushNotification(notification_message);

                        }


                        /*if(ACTION_ENTER.equals(action) && opgGeofenceSurvey.isEnter())
                        {
                            pushNotification(notification_message);
                            updateDb(notificationJSON);
                        }else if(ACTION_EXIT.equals(action) && opgGeofenceSurvey.isExit())
                        {
                            pushNotification(notification_message);
                            updateDb(notificationJSON);
                        }
                        notification_message = null;*/
                    } catch (Exception e) {
                        if(BuildConfig.DEBUG) {
                            e.printStackTrace();
                            Log.e("GeofenceIntentTriggered", "Exception2:" + e.toString());
                        }
                    }
                }
            }
        }
        GeofenceBroadcastReciever.completeWakefulIntent(intent);
    }
    private void updateDb(JSONObject notificationJSON)
    {
        boolean status ;
        try {
            status = SaveOPGObjects.storeNotification(notificationJSON.toString());
        }catch (Exception exception){
            try {
                status = SaveOPGObjects.storeNotification(notificationJSON.toString());
            }catch (Exception exc){
                if(BuildConfig.DEBUG)
                    Log.e("GeofenceIntentTriggered","Exception1:"+exc.toString());
            }
        }
    }
    public void pushNotification(final String message)
    {
        if(BuildConfig.DEBUG) {
            Log.i("GeofenceIntentTriggered", "TriggeredNotification"+message);
        }
        Intent intent = null;
        if(MySurveysPreference.isUserLoggedIn(this))
        {
            intent = new Intent(this, HomeActivity.class);
            intent.putExtra(Util.POSITION,1);
        }
        else
        {
            intent = new Intent(this, LoginActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = null;
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.status_bar_icon)
                    .setContentTitle("Geofence Alert")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentText(message).setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_SOUND |Notification.DEFAULT_LIGHTS)
                    .setColor(Color.TRANSPARENT).setContentIntent(pendingIntent);


        }
        else
        {
            mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.status_bar_icon)
                    .setContentTitle("Geofence Alert")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentText(message).setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_SOUND |Notification.DEFAULT_LIGHTS).setColor(Color.TRANSPARENT).setContentIntent(pendingIntent);
        }
        Notification myNotification = mBuilder.build();
        notificationManager.notify(((new Random()).nextInt(10) + 1), myNotification);
        Util.sendBroadcastMessage(true, getApplicationContext(), "", Util.BROADCAST_ACTION_NOTIFICATION);
    }

}
