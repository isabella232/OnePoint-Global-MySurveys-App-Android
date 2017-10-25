package com.opg.my.surveys.lite.geofencing;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.opg.my.surveys.lite.LocationService;
import com.opg.my.surveys.lite.common.MySurveysPreference;

import static com.opg.my.surveys.lite.common.Util.RESTART_GEOFENCING;

public class GeofenceBroadcastReciever extends WakefulBroadcastReceiver {
    public GeofenceBroadcastReciever() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equalsIgnoreCase(GeofenceIntentService.ACTION_ENTER)){
            ComponentName comp = new ComponentName(context.getPackageName(),
                    GeofenceIntentService.class.getName());

            // Start the service, keeping the device awake while it is launching.
            startWakefulService(context, (intent.setComponent(comp)));
        }else if(intent.getAction().equalsIgnoreCase("android.intent.action.BOOT_COMPLETED")){
            if( MySurveysPreference.getIsGeofencingEnabled(context)){
                Intent locationIntent = new Intent(context, LocationService.class);
                locationIntent.putExtra(RESTART_GEOFENCING,RESTART_GEOFENCING);
                context.startService(locationIntent);
            }
        }
    }
}
