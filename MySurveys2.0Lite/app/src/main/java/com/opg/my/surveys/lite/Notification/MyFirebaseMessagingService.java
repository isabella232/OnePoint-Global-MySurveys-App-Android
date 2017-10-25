package com.opg.my.surveys.lite.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.opg.my.surveys.lite.HomeActivity;
import com.opg.my.surveys.lite.LoginActivity;
import com.opg.my.surveys.lite.R;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.SaveOPGObjects;

import org.json.JSONObject;

import java.util.Random;

/**
 * Created by kiran on 21-11-2016.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService
{

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
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

        android.support.v4.app.NotificationCompat.Builder mBuilder = null;
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            mBuilder = new android.support.v4.app.NotificationCompat.Builder(this).setSmallIcon(R.drawable.status_bar_icon)
                               .setContentTitle(remoteMessage.getData().get("title"))
                               .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get("alert")))
                               .setContentText(remoteMessage.getData().get("alert")).setAutoCancel(true)
                               .setDefaults(Notification.DEFAULT_SOUND |Notification.DEFAULT_LIGHTS)
                               .setContentIntent(pendingIntent);

        }
        else
        {
            mBuilder = new android.support.v4.app.NotificationCompat.Builder(this).setSmallIcon(R.drawable.status_bar_icon)
                               .setContentTitle(remoteMessage.getData().get("title"))
                               .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get("alert")))
                               .setContentText(remoteMessage.getData().get("alert")).setAutoCancel(true)
                               .setDefaults(Notification.DEFAULT_SOUND |Notification.DEFAULT_LIGHTS)
                               .setContentIntent(pendingIntent);;
        }
        Notification myNotification = mBuilder.build();

        Util.sendBroadcastMessage(true, getApplicationContext(), "", Util.BROADCAST_ACTION_NOTIFICATION);

        if(!remoteMessage.getData().isEmpty())
        {
            SaveOPGObjects.storeNotification(new JSONObject(remoteMessage.getData()).toString()); //Saving Notification to be DB
        }
        Util.sendBroadcastMessage(false,this,"",Util.BROADCAST_ACTION_NOTIFICATION);//Sending broadcast to update view.

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(((new Random()).nextInt(10) + 1), myNotification);
    }
}
