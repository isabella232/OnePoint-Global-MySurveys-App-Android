package com.opg.my.surveys.lite;

import com.opg.sdk.models.OPGAuthenticate;

/**
 * Created by Dinesh-opg on 1/19/2017.
 */

public interface LoginListener {
    public void onLoginProcessStarted();
    public void onLoginProcessCompleted(OPGAuthenticate opgAuthenticate);
}
