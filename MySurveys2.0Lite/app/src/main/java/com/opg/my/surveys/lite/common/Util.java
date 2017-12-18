package com.opg.my.surveys.lite.common;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.opg.dbhelper.OPGDBHelper;
import com.opg.my.surveys.lite.BuildConfig;
import com.opg.my.surveys.lite.Login;
import com.opg.my.surveys.lite.LoginActivity;
import com.opg.my.surveys.lite.LoginListener;
import com.opg.sdk.OPGSDK;
import com.opg.sdk.models.OPGGeofenceSurvey;
import com.opg.sdk.models.OPGPanellistPanel;
import com.opg.sdk.models.OPGScript;
import com.opg.my.surveys.lite.R;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import OnePoint.Common.Utils;

/**
 * Created by kiran on 24-10-2016.
 */

public class Util
{
    public static HashMap<String,OPGScript> scriptHashMap = new HashMap<>();
    public static ArrayList<Long> mediaDownloadingList = new ArrayList<>();
    private static OPGSDK opgsdk;
    public static final String TAG = "MySurveys";
    public static String db_name   = "Framework.db";
    public static String POSITION = "position";

    public static String SURVEY_REF = "SurveyReference";

    public static final String SESSION_TIME_OUT_ERROR = "Session expired.Please authenticate again";
    public static final String NO_INTERNET_ERROR="No address associated with hostname";


    public static final String DIRECTORY_NAME = "MySurveys2.0";
    public static final String PROFILE_PICS = "Profile";
    public static final String THEME_PICS = "Theme";
    public static final int COUNTRY_RESULT_CODE = 101;

    public static final String KEY_UPLOAD_STATUS = "uploadStatus";
    public static final String KEY_DOWNLOAD_STATUS = "downloadStatus";

    public static final String SURVEY_ID_KEY = "surveyID";
    public static final String MESSAGE_KEY = "message";

    public static String OPGSURVEY_KEY = "OPGSurvey";

    public static String UNIQUE_ID_ERROR = "UniqueID does not exist.";
    public static String INVALID_CREDENTIAL = "username and/or password are invalid";
    public static String EMAIL_ID_NOT_EXIST = "Email Id does not exist";


    /*****BROADCAST_ACTIONS*************************/
    public static String BROADCAST_ACTION_SAVE_DATA = "com.opg.my.surveys.saveddata";
    public static String BROADCAST_ACTION_REFRESH = "com.opg.my.surveys.refresh";
    public static String ACTION_UPLOAD_RESULT = "com.opg.my.surveys.uploadResults";
    public static String BROADCAST_ACTION_REFRESH_FRAGMENT = "com.opg.my.surveys.refresh.fragment";
    public static String ACTION_SESSION_EXPIRED = "com.opg.my.surveys.login";

    public static String BROADCAST_ACTION_REFRESH_UPLOAD = "com.opg.my.surveys.upload.refresh";

    public static String BROADCAST_ACTION_DOWNLOADING_STATUS = "com.opg.my.surveys.downloading";

    public static String BROADCAST_ACTION_UPLOADED_ALL   = "com.opg.my.surveys.uploaded.all";
    public static String BROADCAST_ACTION_SURVEY_UPLOADED = "com.opg.my.surveys.uploaded.survey";
    public static String BROADCAST_ACTION_GEOFENCE_UI      = "com.opg.my.surveys.geofence.ui";



    public static String BROADCAST_ACTION_NOTIFICATION      = "com.opg.my.surveys.notification";
    public static String BROADCAST_ACTION_GEOFENCES_UPDATED = "com.opg.my.surveys.geofence.updated";
    public static String BROADCAST_ACTION_GEOFENCE_START    = "com.opg.my.surveys.geofence.start";
    public static String BROADCAST_ACTION_GEOFENCE_STOP     = "com.opg.my.surveys.geofence.stop";



    public static String BROADCAST_GEOFENCE_TRANSITION_ENTER = "com.opg.my.surveys.lite.transition.enter";
    public static String BROADCAST_GEOFENCE_TRANSITION_EXIT  = "com.opg.my.surveys.lite.transition.exit";
    public static String BROADCAST_GEOFENCE_TRANSITION_DWELL = "com.opg.my.surveys.lite.transition.dwell";

    public static String RESTART_GEOFENCING = "restartGeofencing";

    /***********************************************/

    /**
     * Survey Statuses *********************************
     */
    public static String NEW_STATUS_KEY = "New";
    public static String PENDING_STATUS_KEY = "Pending";
    public static String COMPLETED_STATUS_KEY = "Completed";
    public static String DOWNLOAD_STATUS_KEY = "Download";
    public static String DOWNLOADING_STATUS_KEY = "Downloading";
    public static String DOWNLOADED_STATUS_KEY = "Downloaded";
    public static String UPLOAD_STATUS_KEY = "Upload";
    public static String UPLOADED_STATUS_KEY = "Uploaded";
    public static String UPLOADEDING_STATUS_KEY = "Uploading";
    /*******************************************************/

    /**
     * Keys used in the app.
     */
    public static String LATITUDE_KEY = "latitude";
    public static String LONGITUDE_KEY = "longitude";

    public static int CAMERA_ZOOM_VALUE = 15;

    /**
     * Location listener params
     */
    public static long INTERVAL = 1000*60*60; // 1 hour
    public static long FAST_INTERVAL = 1000 * 30; //30 seconds
    public static int LOITERINGDELAY = 1000*10 ;//10 seconds

    public static final int STORAGE_PERMISSION_REQUEST_CODE = 150;
    public static final int REQUEST_CODE_PROFILE = 151;
    public static final int REQUEST_CODE_LOCATION = 152;
    public static final int REQUEST_CODE_LOCATION_SETTINGS = 153;


    /**********URLs**********/
    /********Live*/
    public static final String PRIVACY_URL = "https://framework.onepointglobal.com/appwebsite/privacy?location=mobile&culture=";
    public static final String TERMS_CONDITION_URL = "https://framework.onepointglobal.com/appwebsite/termsofuse?location=mobile&culture=";
    public static final String ABOUT_US_URL = "https://framework.onepointglobal.com/appwebsite/About?location=mobile&culture=";



    private static OPGPanellistPanel opgPanellistPanel;

    public static OPGPanellistPanel getOpgPanellistPanel()
    {
        return opgPanellistPanel;
    }

    public static void setOpgPanellistPanel(OPGPanellistPanel opgPanellistPanel)
    {
        Util.opgPanellistPanel = opgPanellistPanel;
    }

    public static OPGSDK getOPGSDKInstance()
    {
        if(opgsdk == null){
            opgsdk = new OPGSDK();
        }
        return opgsdk;
    }

    public static  boolean isOnline(Context context)
    {
        ConnectivityManager cm =(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static void showSyncDialog(final Context context)
    {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_sync);
        Button btnOk = (Button) dialog.findViewById(R.id.btn_okay);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static void showMessageDialog(final Context context,String message)
    {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_sync);
        ((TextView)dialog.findViewById(R.id.tv_sync_msg)).setText(message);
        Button btnOk = (Button) dialog.findViewById(R.id.btn_okay);
        btnOk.setTextColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(context)));
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * Used to send a local broadcast message
     * @param aBoolean
     * @param mContext
     * @param message
     */
    public static void sendBroadcastMessage(boolean aBoolean,Context mContext,String message,String action){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(action);
        broadcastIntent.putExtra("status",aBoolean);
        if(!aBoolean && !message.isEmpty()){
            broadcastIntent.putExtra("message",message);
        }
        mContext.sendBroadcast(broadcastIntent);
    }

    public static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    /**
     * Method to set font for a particular view
     * @param context
     * @param view
     * @param type
     */
    public static void setTypeface(Context context,View view, String type)
    {
        Typeface myTypeface  =   Typeface.createFromAsset(context.getAssets(),type)  ;

        if(view instanceof TextView)
            ((TextView)view).setTypeface(myTypeface);

        if(view instanceof Button)
            ((Button)view).setTypeface(myTypeface);

        if(view instanceof EditText)
            ((EditText)view).setTypeface(myTypeface);

        if(view instanceof TextInputLayout)
            ((TextInputLayout)view).setTypeface(myTypeface);
    }
    /**
     * clears all the data from db
     */
    public static void clearDB(String tableName) {
        try
        {
            OPGDBHelper.setDatabaseName(OPGDBHelper.FRAMEWORK_DB);
            OPGDBHelper opgdbHelper = OPGDBHelper.getInstance();
            if(opgdbHelper!=null && opgdbHelper.openDataBase()) {
                for (String TABLE_NAME : OPGDBHelper.getAllTableNames()) {
                    if (!TABLE_NAME.equalsIgnoreCase(tableName) )
                        opgdbHelper.getWritableDatabase().execSQL("delete from " + TABLE_NAME);
                }
                opgdbHelper.close();
            }else{
                if(BuildConfig.DEBUG)
                    Log.i(TAG, "OPGDBHELPER IS null ");
            }
        }catch (Exception ex)
        {
            Log.i(TAG, ex.getMessage());

        }
    }

    /**
     * Verifies wheather the location services are enabled or not.....
     * @param mContext
     * @return
     */
    public static boolean locationServicesEnabled(final Context mContext) {
        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean net_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            Log.e(mContext.getPackageName(),"Exception gps_enabled");
        }

        try {
            net_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Log.e(mContext.getPackageName(),"Exception network_enabled");
        }
        return gps_enabled || net_enabled;
    }

    public static void showLocationServicesError(final Context mContext){
        final Dialog locationAlert = new Dialog(mContext);
        locationAlert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        locationAlert.setContentView(R.layout.dialog_logout);

        ((TextView)locationAlert.findViewById(R.id.tv_title_logout_dialog)).
                setText(mContext.getResources().getString(R.string.gps_network_not_enabled));

        Button btncancel = (Button) locationAlert.findViewById(R.id.btn_cancel_logout);
        Button btnTakeSurvey = (Button) locationAlert.findViewById(R.id.btn_confirm_logout);
        btnTakeSurvey.setText(mContext.getResources().getString(R.string.open_location_settings));
        btnTakeSurvey.setTextColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(mContext)));
        btncancel.setTextColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(mContext)));
        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationAlert.dismiss();
            }
        });
        btnTakeSurvey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationAlert.dismiss();
                Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(myIntent);
            }
        });
        locationAlert.show();
    }

    /**
     * To get the last known location
     * @param mContext
     * @param mGoogleApiClient
     * @return
     */
    public static Location getLastKnowLocation(Context mContext, GoogleApiClient mGoogleApiClient){
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        return LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

    }

    /**
     * Checks whether the given service class is running in background or not.
     * @param serviceClass
     * @return boolean
     */
    public static boolean isServiceRunning(Context context , Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts JSONString to list of OPGGeofenceSurveys
     * @param jsonString
     * @return
     */
    public static List<OPGGeofenceSurvey> convertStringToOPGGeofenceList(String jsonString){
        Gson gson = new Gson();
        Type listType = new TypeToken<List<OPGGeofenceSurvey>>()
        {
        }.getType();
        return gson.fromJson(jsonString, listType);
    }

    @SuppressWarnings("deprecation")
    public static int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= Build.VERSION_CODES.M ) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    @SuppressWarnings("deprecation")
    public static Drawable getDrawable(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getResources().getDrawable(id, context.getTheme());
        } else {
            return context.getResources().getDrawable(id);
        }
    }
    /*Find the current app version
    * */
    public static String getAppVersion(Context context)
    {
        String version ="";
        try
        {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        }
        catch (Exception ex)
        {
            if(BuildConfig.DEBUG)
                Log.i(Util.TAG,ex.getMessage());
        }
        return version;
    }

   /* public static int  getSurveyTakenCount(Context context,long surveyID)
    {
        return  Util.getOPGSDKInstance().getSurveyTakenCount(context,surveyID,MySurveysPreference.getCurrentPanelID(context), MySurveysPreference.getPanellistID(context));

    }*/

    public static boolean validateEditext(EditText editText, TextInputLayout inputLayout, String errMsg) {
        if(editText.getText().toString().trim().isEmpty()) {
            inputLayout.setError(errMsg);
            //editText.setBackgroundResource(R.drawable.singleline_et_err_bg);
            return false;
        }
        else {
            inputLayout.setErrorEnabled(false);
            //editText.setBackgroundResource(R.drawable.singleline_et_bg_white);
        }
        return true;
    }

    public static Dialog getProgressDialog(Context mContext){
        final Dialog progressDialog = new Dialog(mContext);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setContentView(R.layout.progress_dialog);
        ProgressBar progressBar4 = (ProgressBar) progressDialog.findViewById(R.id.progressBar4);
        progressBar4.setIndeterminate(true);
        progressBar4.getIndeterminateDrawable().setColorFilter(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(mContext)), android.graphics.PorterDuff.Mode.MULTIPLY);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }

    public static String  moveFile(Context context, String inputPath,String mediaID,String parentFolder)
    {
        String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+Utils.getApplicationName(context)+File.separator+parentFolder;
        String outputFilePath = outputPath + File.separator + mediaID + ".PNG";//+"/"+panellistProfile.getMediaID()+"/.jpg";

        InputStream in;
        OutputStream out;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }
            in = new FileInputStream(inputPath);
            File file = new File(outputFilePath);
            out = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file
            out.flush();
            out.close();

        }

        catch (FileNotFoundException fnfe1)
        {
            if(BuildConfig.DEBUG)
                Log.e("tag", fnfe1.getMessage());
            outputFilePath = null;
        }
        catch (Exception e)
        {
            Log.e("tag", e.getMessage());
            outputFilePath = null;
        }
        return outputFilePath;
    }
    public static String searchFile(Context context, String mediaID, String parentFolder)
    {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+ Utils.getApplicationName(context)+File.separator+parentFolder;
        File[] files = new File(filePath).listFiles();
        if(files != null)
        {
            for (File file : files)
            {
                if(FilenameUtils.removeExtension(file.getName()).equalsIgnoreCase(mediaID))
                {
                    return file.getAbsolutePath();
                }
            }
        }

        return null;
    }

    public static  boolean checkPermission(Context context,String permission)
    {

        //String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static void showPermissionDialog(final Context context,String msg)
    {
        final android.support.v7.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            builder = new android.support.v7.app.AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        }
        else
        {
            builder = new android.support.v7.app.AlertDialog.Builder(context);
        }
        builder.setTitle(context.getString(R.string.runtime_permission))
                .setMessage(msg)
                .setPositiveButton(context.getString(R.string.allow), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        goToSettingPage(context);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    private static void goToSettingPage(Context context){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static void launchLoginActivity(final Activity activity)
    {
        Toast.makeText(activity,activity.getString(R.string.session_expired_login_again),Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
    public static int manipulateColor(int color) {
        float factor = 0.8f; // multiply by <1.0 to get darker color and multiply by >1.0 to get light color
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }

    public static void setStatusBarColor(Activity activity)
    {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if(MySurveysPreference.getThemeActionBtnColor(activity).equals("#F79137"))
            {
                window.setStatusBarColor(Color.parseColor("#e67512"));
            }
            else
            {
                int color = Util.manipulateColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(activity)));
                window.setStatusBarColor(color);
            }
        }
        else
        {
            //not possible change the color of status bar below lollipop
        }
    }

    public static boolean isTablet(Context context){
        return !context.getResources().getBoolean(R.bool.portrait_only);
    }
}
