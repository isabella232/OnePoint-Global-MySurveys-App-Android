package com.opg.my.surveys.lite;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.opg.dbhelper.OPGDBHelper;
import com.opg.logging.LogManager;
import com.opg.my.surveys.lite.common.LoginType;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.sdk.OPGSDK;
import com.opg.sdk.exceptions.OPGException;
import com.opg.sdk.models.OPGAuthenticate;
import com.opg.sdk.models.OPGDownloadMedia;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import OnePoint.Common.Utils;

public class LoginActivity extends RootActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener,LoginListener
{

    private static final int RC_SIGN_IN = 9001;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private TextInputLayout inputLayoutUsername, inputLayoutPassword;
    private TextView btnForgotPassword,btnLogin, btnFacebookLogin, btnGooglePlusLogin, tvHeaderText;
    private EditText txtUsername, txtPassword;
    private Context mContext;
    private String fbAccessToken, googleAuthCode;
    private static  GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;
    private LoginType loginType;
    private ImageView ivLogo;

    private String TAG = "LoginActivity";
    private Dialog pDialog;
    private CoordinatorLayout coordinatorLayout;
    private List<String> listPermissionsNeeded;
    private AsyncTask<Void, Void, OPGDownloadMedia> asyncTask = null;
    private String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        setContentView(R.layout.activity_login);
        mContext = this;
        if(!Util.isTablet(mContext)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        btnFacebookLogin = (TextView) findViewById(R.id.btn_facebook_login);
        btnGooglePlusLogin = (TextView) findViewById(R.id.btn_google_plus_login);
        inputLayoutUsername = (TextInputLayout) findViewById(R.id.input_layout_username);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.input_layout_password);
        btnForgotPassword = (TextView) findViewById(R.id.tv_forgot_password);
        btnLogin = (TextView) findViewById(R.id.btn_login);
        txtUsername = (EditText) findViewById(R.id.et_username);
        txtPassword = (EditText) findViewById(R.id.et_password);
        ivLogo = (ImageView)findViewById(R.id.iv_logo);
        tvHeaderText =(TextView)findViewById(R.id.tv_header_login);
        Util.setTypeface(this,tvHeaderText,"font/roboto_bold.ttf");
        txtPassword.setTransformationMethod(new PasswordTransformationMethod());
        listPermissionsNeeded = new ArrayList<>();
        checkPermission(true);
        setTypeface();
        setDrawable(ContextCompat.getDrawable(mContext,R.drawable.splash_nologo),coordinatorLayout);

        try {
            pDialog = Util.getProgressDialog(mContext);
        } catch (OPGException e) {
            e.printStackTrace();
        }

        //hiding the actionbar
        if(getActionBar() != null) {
            getActionBar().hide();
        }
        else if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setListeners();
        initialization();
        setLoginBtnBg();
    }
    private void setLoginBtnBg()
    {
        int state[] = new int[]{android.R.attr.state_pressed};
        int [] stateDefault = new int[]{android.R.attr.state_enabled};

        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setStroke(2,ContextCompat.getColor(this,R.color.white));
        gd.setColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(this)));
        gd.setCornerRadius(21.0f);

        GradientDrawable defaultGd = new GradientDrawable();
        defaultGd.setShape(GradientDrawable.RECTANGLE);
        defaultGd.setStroke(2,ContextCompat.getColor(this,R.color.white));
        defaultGd.setCornerRadius(21.0f);




        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(state,gd);
        stateListDrawable.addState(stateDefault,defaultGd);
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            btnLogin.setBackgroundDrawable(stateListDrawable);
        } else {
            btnLogin.setBackground(stateListDrawable);
        }
    }
    private void deleteOldSurveyResults()
    {
        String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+ Utils.getApplicationName(mContext)+File.separator +"Completed"+File.separator;
        File dir = new File (outputPath);
        if(dir.exists())
        {
            deleteRecursive(dir);
        }
    }
    private void deleteRecursive(File fileOrDirectory)
    {
        try
        {
            if (fileOrDirectory.isDirectory())
                for (File child : fileOrDirectory.listFiles())
                    deleteRecursive(child);

            fileOrDirectory.delete();
        }catch (Exception ex)
        {
            if(BuildConfig.DEBUG)
                Log.i(LoginActivity.class.getName(),ex.getMessage());
        }
    }

    private void setStatusBarColor()
    {
        final String mediaId = MySurveysPreference.getLoginBgMediaId(mContext);
        if (mediaId != null)
        {
            String filePath = Util.searchFile(mContext, mediaId, Util.THEME_PICS);
            if (filePath != null)
            {
                setBackGroundImage(filePath);
            }
            else
            {
                asyncTask = new AsyncTask<Void, Void, OPGDownloadMedia>() {
                    @Override
                    protected OPGDownloadMedia doInBackground(Void... voids) {
                        return Util.getOPGSDKInstance().downloadMediaFile(mContext, mediaId, "PNG");
                    }

                    @Override
                    protected void onPostExecute(OPGDownloadMedia opgDownloadMedia) {
                        super.onPostExecute(opgDownloadMedia);
                        if (opgDownloadMedia != null && opgDownloadMedia.isSuccess() && mContext != null) {
                            String path = Util.moveFile(mContext, opgDownloadMedia.getMediaPath(), mediaId, Util.THEME_PICS);
                            setBackGroundImage(path);
                        }
                    }
                };
                asyncTask.execute();
            }
        }
        else
        {
            setDrawable(ContextCompat.getDrawable(mContext, R.drawable.splash_nologo), coordinatorLayout);
            ivLogo.setVisibility(View.VISIBLE);
            tvHeaderText.setVisibility(View.GONE);
        }

    }

    private void setBackGroundImage(String filePath)
    {
        Bitmap imageBitmap = BitmapFactory.decodeFile(filePath);
        if(imageBitmap != null)
        {
            Drawable drawable = new BitmapDrawable(getResources(), imageBitmap);
            setDrawable(drawable,coordinatorLayout);
            setHeader();
        }
        else
        {
            setDrawable(ContextCompat.getDrawable(mContext,R.drawable.splash_nologo),coordinatorLayout);
        }
    }

    private void setHeader()
    {
        final String headerMediaID = MySurveysPreference.getHeaderMediaId(mContext);
        if(headerMediaID != null)
        {
            tvHeaderText.setVisibility(View.GONE);
            String filePath = Util.searchFile(mContext,headerMediaID, Util.THEME_PICS);
            if (filePath != null)
            {
                Bitmap imageBitmap = BitmapFactory.decodeFile(new File(filePath).getAbsolutePath());
                if (imageBitmap != null && ivLogo != null) {
                    ivLogo.setVisibility(View.VISIBLE);
                    ivLogo.setImageBitmap(imageBitmap);
                }
            } else {
                new AsyncTask<Void, Void, OPGDownloadMedia>() {
                    @Override
                    protected OPGDownloadMedia doInBackground(Void... voids) {
                        return Util.getOPGSDKInstance().downloadMediaFile(mContext, headerMediaID, "PNG");
                    }

                    @Override
                    protected void onPostExecute(OPGDownloadMedia opgDownloadMedia) {
                        super.onPostExecute(opgDownloadMedia);
                        if (opgDownloadMedia != null && opgDownloadMedia.isSuccess())
                        {
                            String filePath = opgDownloadMedia.getMediaPath();
                            filePath = Util.moveFile(mContext, filePath, headerMediaID, Util.THEME_PICS);
                            if (filePath != null) {
                                Bitmap imageBitmap = BitmapFactory.decodeFile(new File(filePath).getAbsolutePath());
                                if (imageBitmap != null && ivLogo != null) {
                                    ivLogo.setVisibility(View.VISIBLE);
                                    ivLogo.setImageBitmap(imageBitmap);
                                }
                            }
                        }
                        else
                        {
                            ivLogo.setVisibility(View.GONE);
                        }
                    }
                }.execute();
            }
        }
        else if(MySurveysPreference.getLogoText(mContext) != null)
        {
            tvHeaderText.setVisibility(View.VISIBLE);
            ivLogo.setVisibility(View.GONE);
            tvHeaderText.setText(MySurveysPreference.getLogoText(mContext));
        }
        else
        {
            ivLogo.setVisibility(View.GONE);
            tvHeaderText.setVisibility(View.GONE);
        }
    }
    private void initialization() {
        try
        {
            sdkInitialize();
            facebookInitialize();
            googleInitialize();
        }catch (Exception ex){
            Log.i(TAG,ex.getMessage());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient != null)
        {
            mGoogleApiClient.connect();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatusBarColor();
        if(getIntent() != null && getIntent().hasExtra("Splash"))
        {
            if(MySurveysPreference.isUserLoggedIn(getApplicationContext())) {//Checking the user is
                // previously logged in or not
                LaunchHomeActivity();
            }
            else
            {
                deleteOldSurveyResults();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void setListeners()
    {
        btnFacebookLogin.setOnClickListener(this);
        btnGooglePlusLogin.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnForgotPassword.setOnClickListener(this);
        txtUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Util.validateEditext(txtUsername, inputLayoutUsername, getString(R.string.err_username_msg));
            }
        });
        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Util.validateEditext(txtPassword, inputLayoutPassword, getString(R.string.err_password_msg));
            }
        });
    }
    private  void googleInitialize() throws Exception
    {

        //**************************************************************************************************
        //******google related signin code ********
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.server_client_id))
                .requestServerAuthCode(getString(R.string.server_client_id))
                .build();
        // ***************
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(LoginActivity.this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        //*****************************************************************************************************************
    }

    /**
     * //******facebook related signin code ********
     * @throws Exception
     */
    private void facebookInitialize() throws Exception
    {
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        if(BuildConfig.DEBUG)
                            Log.i("accessToken", loginResult.getAccessToken().getToken());
                        //storing the facebook accesstoken in  fbAccessToken string
                        fbAccessToken = loginResult.getAccessToken().getToken();
                        loginType = LoginType.FACEBOOK;
                        new Login("", "", fbAccessToken, LoginType.FACEBOOK,LoginActivity.this,mContext).execute();
                    }

                    @Override
                    public void onCancel() {
                        Log.i(mContext.getPackageName(), "facebook login is been cancelled");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(mContext, exception.getMessage(), Toast.LENGTH_SHORT).show();
                        if (exception instanceof FacebookAuthorizationException)
                        {
                            if (AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();
                            }
                        }
                    }
                }
        );
    }
    private void setTypeface()
    {
        Util.setTypeface(mContext,txtUsername,"font/roboto_regular.ttf");
        Util.setTypeface(mContext,txtPassword,"font/roboto_regular.ttf");
        Util.setTypeface(mContext,btnForgotPassword,"font/roboto_regular.ttf");
        Util.setTypeface(mContext,btnLogin,"font/roboto_bold.ttf");
        Util.setTypeface(mContext,btnFacebookLogin,"font/roboto_medium.ttf");
        Util.setTypeface(mContext,btnGooglePlusLogin,"font/roboto_medium.ttf");
    }

    private void sdkInitialize() throws Exception
    {
        //plz enter the admin name and shared key of your account on OnePoint website
        //https://account.onepointglobal.com/#/login
        OPGSDK.initialize("****", "****-****-****-****-****", getApplicationContext());
        //pass the required app version
        Util.getOPGSDKInstance().setAppVersion("your-app-version", getApplicationContext());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if(requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View view) {
        if(Util.isOnline(this)) {
            switch (view.getId()) {
                case R.id.btn_facebook_login:
                    loginWithFacebook();
                    break;
                case R.id.btn_google_plus_login:
                    loginWithGooglePlus();
                    break;
                case R.id.btn_login:
                    authenticate();
                    break;
                case R.id.tv_forgot_password:
                    forgotPasswordAction();
                    break;
                default:
                    break;
            }
        }
        else
        {
            //Displays the alert dialog
            showSnackBar(getString(R.string.no_network_msg));
        }

    }

    /**
     * This displays the forgot password dialog
     */
    private void forgotPasswordAction() {
        startActivity(new Intent(mContext,ForgotPasswordActivity.class));
    }

    //login with facebook
    private void loginWithFacebook() {
        try
        {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email","public_profile"));
        }
        catch (Exception ex)
        {
            Log.e(LoginActivity.class.getName(),ex.getMessage());
        }

    }

    //login with google
    private void loginWithGooglePlus() {
        try
        {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
        catch (Exception ex)
        {
            Log.e(LoginActivity.class.getName(),ex.getMessage());
        }

    }

    private void authenticate()
    {
        String username = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();
        if(!username.isEmpty() && !password.isEmpty())
        {
            loginType = LoginType.NORMAL;
            new Login(txtUsername.getText().toString(), txtPassword.getText().toString(), "", LoginType.NORMAL,this,mContext).execute();
        }
        else
        {
            Util.validateEditext(txtUsername, inputLayoutUsername, getString(R.string.err_username_msg));
            Util.validateEditext(txtPassword, inputLayoutPassword, getString(R.string.err_password_msg));
        }

    }

    private void handleSignInResult(GoogleSignInResult result) {
        if(BuildConfig.DEBUG)
            Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if(result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Name: ").append(acct.getDisplayName()).append("\n");
            stringBuilder.append("Email: ").append(acct.getEmail()).append("\n");
            stringBuilder.append("ServerAuthCode: ").append(acct.getServerAuthCode()).append("\n");
            stringBuilder.append("IdToken: ").append(acct.getIdToken()).append("\n");
            googleAuthCode = acct.getIdToken();
            if(BuildConfig.DEBUG)
                Log.d(TAG, stringBuilder.toString());
            loginType = LoginType.GOOGLE;
            new Login("", "", googleAuthCode, LoginType.GOOGLE,this,mContext).execute();
        }
        else {
            // Signed out, show unauthenticated UI.
            Log.d(TAG, "google signin failed");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }



    @Override
    protected void onPause() {
        super.onPause();
        closeProgressDialog();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(asyncTask != null)
        {
            asyncTask.cancel(true);
        }
    }

    private void closeProgressDialog() {
        if(pDialog!=null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                for (int i = 0; i < permissions.length; i++) {
                    if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        if(listPermissionsNeeded.contains(grantResults[i])) {
                            listPermissionsNeeded.remove(grantResults);
                        }
                        if(BuildConfig.DEBUG)
                            Log.d("Permissions", "Permission Granted: " + permissions[i]);
                    }
                    else if(grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        if(BuildConfig.DEBUG)
                            Log.d("Permissions", "Permission Denied: " + permissions[i]);
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    public boolean checkPermission(boolean requestPermission) {
        if(Build.VERSION.SDK_INT >= 23) {
            int result;
            for (String p : permissions) {
                result = ContextCompat.checkSelfPermission(mContext, p);
                if(BuildConfig.DEBUG)
                    Log.d("Result", "" + result);
                if(result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(p);
                }
            }
            if(!listPermissionsNeeded.isEmpty()) {
                if(requestPermission) {
                    ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                }
                return false;
            }
            return true;
        }
        else {
            return true;
        }
    }

    /**
     * It will launch the home activity
     */
    private void LaunchHomeActivity() {
        Intent intent = new Intent(mContext, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0,0);
        finish();
    }

    private void showSnackBar(String text)
    {
        final Snackbar snackbar = Snackbar.make(coordinatorLayout, text,Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    public void onLoginProcessStarted() {
        if(pDialog != null)
            pDialog.show();
    }

    @Override
    public void onLoginProcessCompleted(OPGAuthenticate opgAuthenticate) {
        if(pDialog != null && pDialog.isShowing()){
            pDialog.dismiss();
        }
        if(opgAuthenticate.isSuccess())
        {
            if(MySurveysPreference.isUserLoggedIn(mContext))
            {
                setDatabase();
                MySurveysPreference.setIsUserLoggedIn(getApplicationContext(), true);
            }
            LaunchHomeActivity();
        }
        else
        {
            if(opgAuthenticate.getHttpStatusCode() == 406)
            {
                showSnackBar(getString(R.string.invalid_credential));
            }
            else if(opgAuthenticate.getHttpStatusCode() == 401)
            {
                showSnackBar(getString(R.string.unauthorised_login));
            }
            else if(opgAuthenticate.getHttpStatusCode() == 500)
            {
                showSnackBar(getString(R.string.internal_server_error));
            }
            else if(loginType.equals(LoginType.FACEBOOK) || loginType.equals(LoginType.GOOGLE))
            {
                showSnackBar(getString(R.string.can_not_sign_in));
            }
            else
            {
                showSnackBar(getString(R.string.unknown_error));
            }
        }
        if(mGoogleApiClient!=null && mGoogleApiClient.isConnected()){
            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient);
        }
    }



    /**
     * This methods helps to setup the database
     */
    private boolean setDatabase() {
        if (!MySurveysPreference.isDBCreated(mContext)) {
            OPGDBHelper.setDatabaseName(Util.db_name);
            if(BuildConfig.DEBUG)
                LogManager.getLogger(getClass()).error(Util.db_name + " SETUP STARTED!");
            OPGDBHelper.getInstance().getWritableDatabase();
            if(BuildConfig.DEBUG)
                LogManager.getLogger(getClass()).error(Util.db_name + " SETUP ENDED!");
            MySurveysPreference.setIsDBCreated(mContext, true);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @Override
    protected void onDestroy() {
        inputLayoutUsername = null;
        inputLayoutPassword = null;
        btnForgotPassword = null;
        btnLogin = null;
        btnFacebookLogin = null;
        btnGooglePlusLogin = null;
        txtUsername  = null;
        txtPassword  = null;
        mContext = null;
        fbAccessToken  = null;
        googleAuthCode = null;
        mGoogleApiClient  = null;
        callbackManager = null;
        pDialog = null;
        super.onDestroy();
    }
}
