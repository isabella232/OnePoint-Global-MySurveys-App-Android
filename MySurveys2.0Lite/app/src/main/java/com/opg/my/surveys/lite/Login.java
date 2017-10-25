package com.opg.my.surveys.lite;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.opg.my.surveys.lite.common.LoginType;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.sdk.OPGSDK;
import com.opg.sdk.models.OPGAuthenticate;

/**
 * Created by Dinesh-opg on 1/19/2017.
 */

public class Login extends AsyncTask<String, String, OPGAuthenticate> {
    private String username, password, tokenID;
    private LoginType loginType;
    OPGSDK opgsdk ;
    LoginListener loginListener;
    Context context;

    public Login(String username, String password, String tokenID, LoginType loginType, LoginListener loginListener, Context context) {
        this.username = username;
        this.password = password;
        this.tokenID = tokenID;
        this.loginType = loginType;
        this.loginListener = loginListener;
        this.context = context;
        opgsdk = new OPGSDK();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        loginListener.onLoginProcessStarted();
    }

    @Override
    protected OPGAuthenticate doInBackground(String... strings) {
        try
        {
            if (loginType.equals(LoginType.NORMAL)) {
                return opgsdk.authenticate(username, password, context);
            } else if (loginType.equals(LoginType.GOOGLE)) {
                return opgsdk.authenticateWithGoogle(tokenID, context);
            } else if (loginType.equals(LoginType.FACEBOOK)) {
                return opgsdk.authenticateWithFacebook(tokenID, context);
            } else {
                return null;
            }
        }catch (Exception ex)
        {
            if(BuildConfig.DEBUG) {
                Log.i(context.getClass().getName(), ex.getMessage());
            }
            return null;
        }

    }

    @Override
    protected void onPostExecute(OPGAuthenticate opgAuthenticate) {
        super.onPostExecute(opgAuthenticate);
        if (opgAuthenticate.isSuccess()) {
            MySurveysPreference.setIsUserLoggedIn(context, true);
            int loginTypeInt = loginType.ordinal();
            MySurveysPreference.setLoginType(context,loginType.ordinal());
            if (loginType.equals(LoginType.NORMAL)) {
                MySurveysPreference.setLoginUserName(context,username);
                MySurveysPreference.setLoginPassword(context,password);
            } else if (loginType.equals(LoginType.GOOGLE) || loginType.equals(LoginType.FACEBOOK)) {
                MySurveysPreference.setTokenID(context,tokenID);
            }
        }
        loginListener.onLoginProcessCompleted(opgAuthenticate);
    }
}
