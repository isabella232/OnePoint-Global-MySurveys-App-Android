package com.opg.my.surveys.lite.Notification;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.opg.my.surveys.lite.BuildConfig;

/**
 * Created by kiran on 21-11-2016.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService
{
    @Override
    public void onTokenRefresh()
    {
        // super.onTokenRefresh();
        String regToken = FirebaseInstanceId.getInstance().getToken();
        if(BuildConfig.DEBUG)
            Log.i("FireBaseID",regToken);
    }
}
