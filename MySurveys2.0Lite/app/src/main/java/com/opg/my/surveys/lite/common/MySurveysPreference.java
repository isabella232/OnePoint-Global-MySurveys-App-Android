package com.opg.my.surveys.lite.common;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.location.Geofence;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.opg.my.surveys.lite.BuildConfig;
import com.opg.sdk.models.OPGGeofenceSurvey;
import com.opg.sdk.models.OPGScript;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kiran on 07-11-2016.
 */
public class MySurveysPreference
{
    private final static String PREFS_NAME = "OnePointPreference";
    private final static String SCRIPT_PREFS_NAME = "OnePointScriptPreference";

    private final static String IS_USER_LOGGED_IN ="isUserLoggedIn";
    private final static String LOGIN_TYPE  ="loginType";
    private final static String LOGIN_USERNAME  ="loginUsername";
    private final static String LOGIN_PASSWORD ="loginPassword";
    private final static String TOKEN_ID ="tokenID";

    private final static String CURRENT_PANEL_ID = "currentPanelID";
    private final static String CURRENT_PANEL_NAME = "currentPanelName";

    private final static String IS_DOWNLOADED = "isDownloaded";
    private final static String IS_DB_CREATED  = "isDBCreated";
    /*private static String SCRIPT_DATA= "scriptData";*/
    private final static String IS_GEOFENCING_ENABLED = "isGeofencingEnabled";

    private final static String PREFS_GEOFENCING_SURVEYS = "geoFencingTriggered";
    private final static String THEME_ACTION_COLOR = "themeActionBtnColor";
    private final static String THEME_HEADER_LOGO_PATH = "themeHeaderLogoPath";
    private final static String THEME_LOGIN_BG_IMAGE_PATH = "themeThemeLoginBgImagePath";
    //private final static String PANELLIST_PASSWORD_NAME = "panellistPasswordName";
    //private final static String PANELLIST_USER_NAME = "panellistUserName";

    private final static String PANELLIST_PASSWORD_NAME = "panellistPasswordName";
    private final static String PANELLIST_USER_NAME = "panellistUserName";
    private final static String PANELLIST_ID= "panellistID";
    private final static String HEADER_MEDIA_ID = "HeaderMediaID";
    private final static String LOGO_TEXT = "LogoText";
    private final static String LAST_LOCATION_KNOWN = "lastLocationKnown";



    /*for storing String Value*/
    private static synchronized void setProperty(final Context context, final String name, final String value)
    {
        if(context != null)
        {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(name, value).commit();
        }

    }

    private static synchronized String getProperty(final Context context, final String name, final String defaultValue)
    {
        try
        {
            final String value = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(name, null);
            if (value == null)
            {
                setProperty(context, name, defaultValue);
                return defaultValue;
            }
            else
            {
                return value;
            }
        }
        catch (final ClassCastException e)
        {
            setProperty(context, name, defaultValue);
            return defaultValue;
        }catch(Exception ex){
            if(BuildConfig.DEBUG){
                System.out.println(ex.toString());
            }
            return defaultValue;
        }
    }

    /*for storing boolean value*/
    private static synchronized void setProperty(final Context context, final String name, final boolean value)
    {
        if(context != null)
        {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(name, value).commit();
        }

    }

    /*for storing long value*/
    private static synchronized void setProperty(final Context context, final String name, final long value)
    {
        if(context != null)
        {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putLong(name, value).commit();
        }
    }

    private static synchronized boolean getProperty(final Context context, final String name, final boolean defaultValue)
    {
        try
        {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(name, defaultValue);
        }
        catch (final ClassCastException e)
        {
            setProperty(context, name, defaultValue);
            return defaultValue;
        }catch(Exception ex){
            if(BuildConfig.DEBUG){
                System.out.println(ex.toString());
            }
            return defaultValue;
        }
    }

    private static synchronized long getProperty(final Context context, final String name, final long defaultValue)
    {
        try
        {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getLong(name,defaultValue);

        }
        catch (final ClassCastException e)
        {
            setProperty(context, name, defaultValue);
            return defaultValue;
        }catch(Exception ex){
            if(BuildConfig.DEBUG){
                System.out.println(ex.toString());
            }
            return defaultValue;
        }
    }


    /*for storing int value*/
    private static synchronized void setProperty(final Context context, final String name, final int value)
    {
        if(context != null)
        {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putInt(name, value).commit();
        }

    }


    private static synchronized int getProperty(final Context context, final String name, final int defaultValue)
    {
        try
        {
            int value = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(name, -100);
            if (value == -100)
            {
                setProperty(context, name, defaultValue);
                return defaultValue;
            }
            else
            {
                return value;
            }
        }
        catch (final ClassCastException e)
        {
            setProperty(context, name, defaultValue);
            return defaultValue;
        }catch(Exception ex){
            if(BuildConfig.DEBUG){
                System.out.println(ex.toString());
            }
            return defaultValue;
        }
    }

    /**
     * Gives the status that the user is logged in or not
     *
     * @param context the context
     * @return boolean boolean
     */
    public static boolean isUserLoggedIn(Context context)
    {
        boolean isUserLoggedIn = getProperty(context, IS_USER_LOGGED_IN, false);
        return isUserLoggedIn;
    }

    /**
     * Sets the login status of the user
     *
     * @param context    the context
     * @param isUserLoggedIn the is logged in
     */
    public static void setIsUserLoggedIn(Context context, boolean isUserLoggedIn)
    {
        setProperty(context, IS_USER_LOGGED_IN,isUserLoggedIn);
    }

    /**
     * Sets the logintype
     * @param context
     * @param loginType
     */
    public static void setLoginType(Context context ,int loginType){
        setProperty(context,LOGIN_TYPE,loginType);
    }


    /**
     * Gets the login type
     * @param context
     * @return
     */
    public static int getLoginType(Context context){
        return getProperty(context, LOGIN_TYPE, -1);
    }

    /**
     * Sets LoginUserName
     * @param context
     * @param userName
     */
    public static void setLoginUserName(Context context,String userName){
        setProperty(context,LOGIN_USERNAME,userName);
    }

    /**
     * Gets LoginUserName
     * @param context
     * @return
     */
    public static String getLoginUserName(Context context){
        return getProperty(context,LOGIN_USERNAME,"");
    }

    /**
     * Sets LoginPassword
     * @param context
     * @param password
     */
    public static void setLoginPassword(Context context,String password){
        setProperty(context,LOGIN_PASSWORD,password);
    }

    /**
     * Gets LoginPassword
     * @param context
     * @return
     */
    public static String getLoginPassword(Context context){
        return getProperty(context,LOGIN_PASSWORD,"");
    }

    /**
     * Sets Token ID
     * @param context
     * @param tokenID
     */
    public static void setTokenID(Context context,String tokenID){
        setProperty(context,TOKEN_ID,tokenID);
    }

    /**
     * Gets Token ID
     * @param context
     * @return
     */
    public static String getTokenID(Context context){
        return getProperty(context,TOKEN_ID,"");
    }


    /**
     * Set the currentPanelID
     *
     * @param context        the context
     * @param currentPanelID the current panel id
     */
    public static void setCurrentPanelID(Context context,long currentPanelID)
    {
        setProperty(context,CURRENT_PANEL_ID,currentPanelID);
    }

    /**
     * Gets the currentPanelID
     *
     * @param context the context
     * @return current panel id
     */
    public static long getCurrentPanelID(Context context)
    {
        return getProperty(context,CURRENT_PANEL_ID,(long)0);
    }


    /**
     * Set the currentPanelName
     *
     * @param context        the context
     * @param currentPanelName the current panel id
     */
    public static void setCurrentPanelName(Context context,String currentPanelName)
    {
        setProperty(context,CURRENT_PANEL_NAME,currentPanelName);
    }

    /**
     * Gets the currentPanelName
     *
     * @param context the context
     * @return current panel name
     */
    public static String getCurrentPanelName(Context context)
    {
        return getProperty(context,CURRENT_PANEL_NAME,"");
    }

    /**
     * Gives the status data is stored or not
     *
     * @param context the context
     * @return boolean boolean
     */
    public static boolean isDownloaded(Context context)
    {
        boolean isDownloaded = getProperty(context, IS_DOWNLOADED, false);
        return isDownloaded;
    }

    /**
     * Sets the status of data stored or not
     *
     * @param context      the context
     * @param isDownloaded the is data stored
     */
    public static void setIsDownloaded(Context context, boolean isDownloaded)
    {
        setProperty(context, IS_DOWNLOADED,isDownloaded);
    }


    /**
     * Is db created boolean.
     *
     * @param context the context
     * @return the boolean
     */
    public static boolean isDBCreated(Context context)
    {
        boolean isDBCreated = getProperty(context, IS_DB_CREATED, false);
        return isDBCreated;
    }

    /**
     * Sets is db created.
     *
     * @param context     the context
     * @param isDBCreated the is db created
     */
    public static void setIsDBCreated(Context context, boolean isDBCreated)
    {
        setProperty(context,IS_DB_CREATED,isDBCreated);
    }

    /**
     * Clears the preferences of the app.
     * @param context
     */
    public static void clearPreference(Context context)
    {
        context.getSharedPreferences(PREFS_NAME, 0).edit().clear().commit();
    }
    public static void clearThemePreference(Context context)
    {
        context.getSharedPreferences(PREFS_NAME,0).edit().remove(THEME_ACTION_COLOR).commit();
        context.getSharedPreferences(PREFS_NAME,0).edit().remove(THEME_LOGIN_BG_IMAGE_PATH).commit();
    }

    public static void setPanellistUserName(Context context, String panellistUserName)
    {
        setProperty(context, PANELLIST_USER_NAME, panellistUserName);
    }

    public static String getPanellistUserName(Context context)
    {
        return getProperty(context, PANELLIST_USER_NAME, "");
    }


    public static void setPanellistPasswordName(Context context, String panellistPasswordName)
    {
        setProperty(context, PANELLIST_PASSWORD_NAME, panellistPasswordName);
    }

    public static String getPanellistPasswordName(Context context)
    {
        return getProperty(context, PANELLIST_PASSWORD_NAME, "");
    }

    /**
     * Saving the panellistID
     * @param context
     * @param panellistID
     */
    public static void setPanellistID(Context context, long panellistID)
    {
        setProperty(context, PANELLIST_ID, panellistID);
    }

    /**
     * Retriving the panellistID
     * @param context
     * @return
     */
    public static long getPanellistID(Context context)
    {
        return getProperty(context, PANELLIST_ID, (long)0);
    }



    /**
     * Gets the  geofencing status.
     *
     * @param context the context
     * @return the boolean
     */
    public static boolean getIsGeofencingEnabled(Context context)
    {
        boolean isGeofencingEnabled = getProperty(context, IS_GEOFENCING_ENABLED, false);
        return isGeofencingEnabled;
    }

    /**
     * Sets the geofencing status.
     *
     * @param context     the context
     * @param isGeofencingEnabled the is db created
     */
    public static void setIsGeofencingEnabled(Context context, boolean isGeofencingEnabled)
    {
        setProperty(context,IS_GEOFENCING_ENABLED ,isGeofencingEnabled);
    }


    /**
     *Gets the List of geofences List<Geofence> from the sharedpreferences
     * @param context
     * @return
     */
    public static List<Geofence> getGeofenceList(Context context)
    {
        List<OPGGeofenceSurvey> resultGeofenceSurveys = new ArrayList<>();
        List<Geofence> geofenceList = new ArrayList<>();
        String jsonString = getProperty(context, PREFS_GEOFENCING_SURVEYS, null);
        if(jsonString != null){
            Gson gson = new Gson();
            Type listType = new TypeToken<List<OPGGeofenceSurvey>>()
            {
            }.getType();
            resultGeofenceSurveys = gson.fromJson(jsonString, listType);
        }
        if(resultGeofenceSurveys!= null && resultGeofenceSurveys.size() > 0){
            for (OPGGeofenceSurvey entry : resultGeofenceSurveys) {
                geofenceList.add(new Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(entry.getSurveyReference())

                        // Set the circular region of this geofence.
                        .setCircularRegion(
                                entry.getLatitude(),
                                entry.getLongitude(),
                                entry.getRange()
                        )

                        // Set the expiration duration of the geofence. This geofence gets automatically
                        // removed after this period of time.
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setLoiteringDelay(Util.LOITERINGDELAY)
                        // Set the transition types of interest. Alerts are only generated for these
                        // transition. We track entry and exit transitions in this sample.
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL )

                        // Create the geofence.
                        .build());
            }
        }
        return geofenceList;
    }

    /**
     * We are saving the scriptdata
     * @param context
     * @param key
     * @param data
     */
    public static void saveScriptData(Context context, String key, String data)
    {
        SharedPreferences preferences = context.getSharedPreferences(SCRIPT_PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(key,data).commit();
        /*setProperty(context,key,data);*/
    }

    public static OPGScript getScriptData(Context context, String key)
    {
        SharedPreferences preferences = context.getSharedPreferences(SCRIPT_PREFS_NAME, Context.MODE_PRIVATE);
        OPGScript opgScript = null;
        try {
            Gson gson   = new Gson();
            String data = preferences.getString(key,"");
           /* String data = getProperty(context,SCRIPT_DATA+":"+key,"");*/
            opgScript = gson.fromJson(data,OPGScript.class);
        }
        catch (Exception ex)
        {

        }

        return opgScript;
    }

    /**
     * Checks wheather the script file is downloaded or not for a particular surveys
     * @param context
     * @param key
     * @return
     */
    public static boolean isScriptDataPresent(Context context,String key){
        SharedPreferences preferences = context.getSharedPreferences(SCRIPT_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.contains(key);
       /* boolean isPresent = false;
        try {
            String data = preferences.getString(SCRIPT_DATA+":"+key,"");
            if(!data.isEmpty()){
                isPresent = true;
            }
        }
        catch (Exception ex)
        {  }
        return isPresent;*/
    }

    public static void clearScriptData(Context  context)
    {
        try
        {
            context.getSharedPreferences(SCRIPT_PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit();
       /*     List<OPGSurvey> surveyList = RetriveOPGObjects.getAllSurveys(getCurrentPanelID(context));
            for(OPGSurvey opgSurvey : surveyList)
            {
                if(opgSurvey.isOffline() && MySurveysPreference.getScriptData(context,opgSurvey.getSurveyID()+"") != null)
                {
                    context.getSharedPreferences(PREFS_NAME,0).edit().remove(SCRIPT_DATA+":"+opgSurvey.getSurveyID()).commit();
                }
            }
*/
        }catch (Exception e){

        }
    }

    public static void setThemeActionBtnColor(Context context,String colorCode)
    {
        setProperty(context, THEME_ACTION_COLOR, colorCode);
    }

    public static String getThemeActionBtnColor(Context context)
    {
        return getProperty(context, THEME_ACTION_COLOR,"#F79137");//default primary color
    }

    public static void setLoginBgMediaId(Context context, String path)
    {
        setProperty(context, THEME_LOGIN_BG_IMAGE_PATH, path);
    }

    public static String getLoginBgMediaId(Context context)
    {
        return getProperty(context, THEME_LOGIN_BG_IMAGE_PATH, null);
    }
    public static void setHeaderMediaId(Context context, String id)
    {
        setProperty(context, HEADER_MEDIA_ID, id);
    }

    public static String getHeaderMediaId(Context context) {
        return getProperty(context, HEADER_MEDIA_ID,null);
    }
    public static void setLogoText(Context context, String logotext)
    {
         setProperty(context,LOGO_TEXT,logotext);
    }

    public static String getLogoText(Context context)
    {
       return getProperty(context,LOGO_TEXT,null);
    }

    public static void setLastLocationKnown(Context context, String lastLocationKnow)
    {
        setProperty(context,LAST_LOCATION_KNOWN,lastLocationKnow);
    }

    public static String getLastLocationKnown(Context context)
    {
        return getProperty(context,LAST_LOCATION_KNOWN,"");
    }

}
