package com.opg.my.surveys.lite.common.db;

import android.util.Log;

import com.opg.cs2jnet.system.collections.lcc.CSList;
import com.opg.my.surveys.lite.BuildConfig;
import com.opg.my.surveys.lite.common.Util;
import com.opg.pom.model.Country;
import com.opg.pom.model.CountryFactory;
import com.opg.pom.model.ICountry;
import com.opg.pom.model.IPanel;
import com.opg.pom.model.IPanelPanellist;
import com.opg.pom.model.IPanellistProfile;
import com.opg.pom.model.ITheme;
import com.opg.pom.model.Panel;
import com.opg.pom.model.PanelFactory;
import com.opg.pom.model.PanelPanellist;
import com.opg.pom.model.PanelPanellistFactory;
import com.opg.pom.model.PanellistProfile;
import com.opg.pom.model.PanellistProfileFactory;
import com.opg.pom.model.Theme;
import com.opg.pom.model.ThemeFactory;
import com.opg.prom.model.AppNotification;
import com.opg.prom.model.AppNotificationFactory;
import com.opg.prom.model.GeofenceSurvey;
import com.opg.prom.model.GeofenceSurveyFactory;
import com.opg.prom.model.IAppNotification;
import com.opg.prom.model.IGeofenceSurvey;
import com.opg.prom.model.ISurvey;
import com.opg.prom.model.ISurveyPanel;
import com.opg.prom.model.Survey;
import com.opg.prom.model.SurveyFactory;
import com.opg.prom.model.SurveyPanel;
import com.opg.prom.model.SurveyPanelFactory;
import com.opg.sdk.models.OPGCountry;
import com.opg.sdk.models.OPGGeofenceSurvey;
import com.opg.sdk.models.OPGPanel;
import com.opg.sdk.models.OPGPanellistProfile;
import com.opg.sdk.models.OPGSurvey;
import com.opg.sdk.models.OPGSurveyPanel;
import com.opg.sdk.models.OPGTheme;
import com.opg.sdk.models.OPGPanelPanellist;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import OnePoint.Common.Utils;

/**
 * Created by Padmavathi on 27/10/2016.
 */

public class SaveOPGObjects {


    /**
     * Fetching the data from OPGSurvey and storing to database
     * @param opgSurvey
     * @return
     * @throws Exception
     */
    public static boolean storeSurvey(OPGSurvey opgSurvey) throws Exception {
        SurveyFactory factory = new SurveyFactory();
        Survey survey = new Survey();

        survey.setName((opgSurvey.getName().equalsIgnoreCase("null")) ? "" : opgSurvey.getName().replace("'", "''"));

        survey.setDescription((opgSurvey.getDescription().equalsIgnoreCase("null")) ? ""
                : opgSurvey.getDescription().replace("'", "''"));

        survey.setSurveyID((String.valueOf(opgSurvey.getSurveyID()).equalsIgnoreCase("null")) ? -1
                : opgSurvey.getSurveyID());

        survey.setIsGeofencing((String.valueOf(opgSurvey.isGeofencing()).equalsIgnoreCase("null")) ? false
                : opgSurvey.isGeofencing());

        survey.setScriptID((String.valueOf(opgSurvey.getScriptID()).equalsIgnoreCase("null")) ? -1
                : opgSurvey.getScriptID());

        survey.setIsOffline((String.valueOf(opgSurvey.isOffline()).equalsIgnoreCase("null")) ? false
                : opgSurvey.isOffline());

        survey.setEstimatedTime((String.valueOf(opgSurvey.getEstimatedTime()).equalsIgnoreCase("null")) ? -1
                : opgSurvey.getEstimatedTime());

        //Need to replace the searchTags with surveyreference
        try {
            survey.setsurveyReference((opgSurvey.getSurveyReference().equalsIgnoreCase("null")) ? ""
                    : opgSurvey.getSurveyReference().replace("'", "''"));
        } catch (Exception e) {
            survey.setSearchTags("");
        }

        Date dummyDate = null;
        try {
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            dummyDate = df.parse("01/01/0001");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //*Need to confirm and check
        if(opgSurvey.getStartDate() != null){
            survey.setCreatedDate(opgSurvey.getStartDate());
        }else{
            survey.setCreatedDate(dummyDate);
        }

        if(opgSurvey.getEndDate() != null){
            survey.setLastUpdatedDate(opgSurvey.getEndDate());
        }else{
            survey.setLastUpdatedDate(dummyDate);
        }

        survey.setDeadLine(opgSurvey.getDeadLine());


        //No values found in the OPGSurvey class so we arec hardcording these values
        survey.setOccurences(10);
        survey.setStatus(opgSurvey.getStatus());

        survey.setType((short) 1);
        survey.setUserID(232);
        survey.setIsDeleted(false);
        survey.setIsCapi(true);
        try {
            survey.setMediaID(256);
        } catch (Exception e) {
            survey.setMediaID(-1);
        }

        CSList<ISurvey> list ;
        try{
            list = factory.findBySurveyID(survey.getSurveyID());
        }catch (Exception e){
            list = factory.findBySurveyID(survey.getSurveyID());
        }
        if (list.size() > 0) {
            survey.setIsNew(false);
        } else {
            survey.setIsNew(true);
        }

        return factory.save(survey);
    }


    /**
     * Fetching the data from OPGTheme and storing to database
     * @param opgTheme
     * @return
     * @throws Exception
     */
    public static boolean storeTheme(OPGTheme opgTheme) throws Exception {
        Theme theme = new Theme();
        ThemeFactory themeFactory = new ThemeFactory();

        theme.setName(opgTheme.getName().equalsIgnoreCase("null") ? "" : opgTheme.getName().replace("'", "''"));

        theme.setThemeID(String.valueOf(opgTheme.getThemeID()).equalsIgnoreCase("null") ? -1
                : opgTheme.getThemeID());

        theme.setValue(opgTheme.getValue().equalsIgnoreCase("null") ? ""
                : opgTheme.getValue().replaceAll("'", "''"));

        theme.setThemeElementTypeID(String.valueOf(opgTheme.getThemeElementTypeID()).equalsIgnoreCase("null") ? -1
                : (int)opgTheme.getThemeElementTypeID());

        theme.setThemeTemplateID(String.valueOf(opgTheme.getThemeTemplateID()).equalsIgnoreCase("null") ? -1
                : opgTheme.getThemeTemplateID());

        theme.setIsDeleted(String.valueOf(opgTheme.isDeleted()).equalsIgnoreCase("null") ? false
                : opgTheme.isDeleted());

        theme.setCreatedDate(opgTheme.getCreatedDate());

        theme.setLastUpdatedDate(opgTheme.getLastUpdatedDate());

        CSList<ITheme> list;

        try{
            list = themeFactory.findByThemeID(theme.getThemeID());
        }catch(Exception ex){
            list = themeFactory.findByThemeID(theme.getThemeID());
        }

        if (list.size() > 0) {
            theme.setIsNew(false);
        } else {
            theme.setIsNew(true);
        }
        /*
        theme.setCreatedDate(Utils.convertToDateFromUTC(opgTheme.get("CreatedDate").toString()));
        theme.setLastUpdatedDate(Utils.convertToDateFromUTC(obj.get("LastUpdatedDate").toString()));
        */
        /*if (OPGSharedPreference.getCheckForUpdatesPlugin((Activity) OPGApplication.getActivityContext())) {
            LogManager.getLogger(OPGJsonToModelParser.class).debug("PARSER - Theme: CHECK FOR UPDATE.");
            // TODO: check the last updated date and insert or update.
            CSList<ITheme> list = themeFactory.findByThemeID(theme.getThemeID());
            if (list.size() > 0) {
                Date dbLastUpdated = list.get(0).getCreatedDate();
                Date nwLastUpdated = theme.getCreatedDate();
                boolean needUpdate = (dbLastUpdated.compareTo(nwLastUpdated) < 0);
                if (needUpdate) {
                    theme.setIsNew(false);
                    themeFactory.save(theme);
                }
            } else {
                themeFactory.save(theme);
            }
        } else {
            LogManager.getLogger(OPGJsonToModelParser.class).debug("PARSER - Theme: NORMAL");
            themeFactory.save(theme);
        }*/

        return themeFactory.save(theme);
    }

    /**
     * Fetching the data from OPGPanelPanellist and storing to database
     * @param opgPanelPanellist
     * @return
     * @throws Exception
     */
    public static boolean storePanelPanellist(OPGPanelPanellist opgPanelPanellist) throws Exception {
        PanelPanellist pp = new PanelPanellist();
        PanelPanellistFactory panelPanellistFactory = new PanelPanellistFactory();


        pp.setPanelPanellistID(String.valueOf(opgPanelPanellist.getPanelPanellistID()).equalsIgnoreCase("null") ? -1
                : opgPanelPanellist.getPanelPanellistID());

        pp.setPanellistID(String.valueOf(opgPanelPanellist.getPanellistID()).equalsIgnoreCase("null") ? -1
                : opgPanelPanellist.getPanellistID());

        pp.setPanelID(String.valueOf(opgPanelPanellist.getPanelID()).equalsIgnoreCase("null") ? -1
                : opgPanelPanellist.getPanelID());

        pp.setIncluded(String.valueOf(opgPanelPanellist.isIncluded()).equalsIgnoreCase("null") ? false
                : opgPanelPanellist.isIncluded());

        pp.setCreatedDate(opgPanelPanellist.getCreatedDate());
        pp.setLastUpdatedDate(opgPanelPanellist.getLastUpdatedDate());

        pp.setIsDeleted(opgPanelPanellist.isDeleted());
        CSList<IPanelPanellist> list;
        try {
            list = panelPanellistFactory.findByPanelPanellistID(pp.getPanelPanellistID());
        }catch (Exception e){
            list = panelPanellistFactory.findByPanelPanellistID(pp.getPanelPanellistID());
        }
        if(list.size()>0){
            pp.setIsNew(false);
        }else{
            pp.setIsNew(true);
        }


        return panelPanellistFactory.save(pp);
    }

    /**
     * Fetching the data from OPGPanel and storing to database
     * @param opgPanel
     * @return
     * @throws Exception
     */
    public static boolean storePanel(OPGPanel opgPanel) throws Exception {

        Panel panel = new Panel();
        PanelFactory panelFactory = new PanelFactory();

        panel.setName(opgPanel.getName().equalsIgnoreCase("null") ? "" :
                opgPanel.getName().replace("'", "''"));

        /********WorkAround to save isLogoIDSpecified,logoID,logoUrl,isMediaIDSpecified.mediaID,mediaUrl in POM*/
        //save media URl in Description column of panels Table
        if(opgPanel.getMediaUrl() != null)
        {
            panel.setDescription(opgPanel.getMediaUrl());
        }

        //save logo URl in SearchTag column of panels Table
        //if(opgPanel.getLogoUrl() != null)
        {
            panel.setSearchTag(opgPanel.getLogoUrl());
        }

        //Save logoID to remark
        if(opgPanel.getLogoID() != 0)
        {
            panel.setRemark(opgPanel.getLogoID()+"");
        }

        //Save mediaID to UserID
        if(opgPanel.getMediaID() != 0)
        {
            panel.setUserID(opgPanel.getMediaID());
        }

        // assign is isMediaIDSpecified to panelType as we're shortage of variables in POM
        if(opgPanel.isMediaIDSpecified())
        {
            panel.setPanelType(opgPanel.isMediaIDSpecified() ? 1 : 0);
        }
        // panel.setPanelType(String.valueOf(opgPanel.getPanelType()).equalsIgnoreCase("null") ? -1 : opgPanel.getPanelType());

        // assign is isLogoIDSpecified to isDeleted as we're shortage of variables in POM
        panel.setIsDeleted(opgPanel.isLogoIDSpecified());

        panel.setPanelID(String.valueOf(opgPanel.getPanelID()).equalsIgnoreCase("null") ? -1 : opgPanel.getPanelID());
        panel.setThemeTemplateID(String.valueOf(opgPanel.getThemeTemplateID()).equalsIgnoreCase("null") ? -1
                : opgPanel.getThemeTemplateID());
        panel.setCreatedDate(opgPanel.getCreatedDate());

        panel.setLastUpdatedDate(opgPanel.getLastUpdatedDate());


        CSList<IPanel> list;
        try{
            list = panelFactory.findByPanelID(panel.getPanelID());
        }catch (Exception e){
            list = panelFactory.findByPanelID(panel.getPanelID());
        }
        if (list.size() > 0) {
            panel.setIsNew(false);
        } else {
            panel.setIsNew(true);
        }

        return panelFactory.save(panel);
    }


    /**
     * Fetching the data from OPGSurveyPanel and storing to database
     * @param opgSurveyPanel
     * @return
     * @throws Exception
     */
    public static boolean storeSurveyPanel(OPGSurveyPanel opgSurveyPanel) throws Exception {
        SurveyPanel sp = new SurveyPanel();
        SurveyPanelFactory surveyPanelFactory = new SurveyPanelFactory();

        sp.setSurveyID(String.valueOf(opgSurveyPanel.getSurveyID()).equalsIgnoreCase("null") ? -1
                : opgSurveyPanel.getSurveyID());

        sp.setPanelID(String.valueOf(opgSurveyPanel.getPanelID()).equalsIgnoreCase("null") ? -1
                : opgSurveyPanel.getPanelID());

        sp.setCreatedDate(opgSurveyPanel.getCreatedDate());

        sp.setLastUpdatedDate(opgSurveyPanel.getLastUpdatedDate());

        sp.setIsDeleted(String.valueOf(opgSurveyPanel.isDeleted()).equalsIgnoreCase("null") ? false
                : opgSurveyPanel.isDeleted());

        sp.setExcluded(String.valueOf(opgSurveyPanel.isExcluded()).equalsIgnoreCase("null") ? false
                : opgSurveyPanel.isExcluded());

        sp.setExcludedSpecified(String.valueOf(opgSurveyPanel.isExcludedSpecified()).equalsIgnoreCase("null") ? false
                : opgSurveyPanel.isExcludedSpecified());

        sp.setSurveyPanelID(String.valueOf(opgSurveyPanel.getSurveyPanelID()).equalsIgnoreCase("null") ? -1
                : opgSurveyPanel.getSurveyPanelID());

        CSList<ISurveyPanel> list;
        try {
            list = surveyPanelFactory.findBySurveyPanelID(sp.getSurveyPanelID());
        }catch (Exception e){
            list = surveyPanelFactory.findBySurveyPanelID(sp.getSurveyPanelID());

        }
        if (list.size() > 0) {
            sp.setIsNew(false);
        } else {
            sp.setIsNew(true);
        }

        return surveyPanelFactory.save(sp);
    }

    /**
     * Fetching the data from OPGPanellistProfile and saving to db
     * @param opgPanellistProfile
     * @return
     * @throws Exception
     */
    public static boolean storePanellistProfile(OPGPanellistProfile opgPanellistProfile) throws Exception {
        PanellistProfile profile = new PanellistProfile();
        PanellistProfileFactory factory = new PanellistProfileFactory();

        profile.setFirstName((opgPanellistProfile.getFirstName().equalsIgnoreCase("null") ? ""
                : opgPanellistProfile.getFirstName().replace("'", "''")));

        profile.setLastName((opgPanellistProfile.getLastName().equalsIgnoreCase("null") ? ""
                : opgPanellistProfile.getLastName().replace("'", "''")));

        profile.setUserName((opgPanellistProfile.getUserName().equalsIgnoreCase("null") ? ""
                : opgPanellistProfile.getUserName().replace("'", "''")));


        profile.setEmail((opgPanellistProfile.getEmailID().equalsIgnoreCase("null") ? ""
                : opgPanellistProfile.getEmailID().replace("'", "''")));

        profile.setAddress1((opgPanellistProfile.getAddress1().equalsIgnoreCase("null") ? ""
                : opgPanellistProfile.getAddress1().replace("'", "''")));

        profile.setAddress2((opgPanellistProfile.getAddress2().equalsIgnoreCase("null") ? ""
                : opgPanellistProfile.getAddress2().replace("'", "''")));

        profile.setPostalCode((opgPanellistProfile.getPostalCode().equalsIgnoreCase("null") ? ""
                : opgPanellistProfile.getPostalCode()).replace("'", "''"));

        profile.setMobileNumber((opgPanellistProfile.getMobileNumber().equalsIgnoreCase("null") ? ""
                : opgPanellistProfile.getMobileNumber()));


        profile.setMediaID((opgPanellistProfile.getMediaID().equalsIgnoreCase("null") ? ""
                : opgPanellistProfile.getMediaID()));

        profile.setTitle((opgPanellistProfile.getTitle().equalsIgnoreCase("null") ? ""
                : opgPanellistProfile.getTitle().replace("'", "''")));




        profile.setGender((String.valueOf(opgPanellistProfile.getGender()).equalsIgnoreCase("null") ? -1
                : (short)opgPanellistProfile.getGender()));

        profile.setDOB(opgPanellistProfile.getDob());

        //values to be included in the OPGPanellistProfile class
        profile.setPassword("@xyze");

        profile.setCreatedDate(opgPanellistProfile.getDob());
        profile.setLastUpdatedDate(opgPanellistProfile.getDob());
        profile.setPasswordLastUpdated(opgPanellistProfile.getDob());

        profile.setCountryCode(Integer.parseInt(opgPanellistProfile.getStd().replace("'", "''")));//using the countrycode for storing the std value

        profile.setRemark("Remark");

        profile.setPanellistID(opgPanellistProfile.getPanellistID());

        profile.setSearchTag(opgPanellistProfile.getCountryName().replace("'", "''"));//using the searchtag for storing the countryname

        profile.setWebsite("www.www.com".replace("'", "''"));

        profile.setGeoLocation("location".replace("'", "''"));
        profile.setStatus(1);
        profile.setIsDeleted(false);


        profile.setMaritalStatus((short) 0);
        profile.setPasswordEncrypted("@LDJDKLJ");
        profile.setTermsCondition(true);

        CSList<IPanellistProfile> list;
        try{
            list = factory.findAllObjects();
        }catch (Exception e){
            list = factory.findAllObjects();
        }

        if( list.size() > 0 && list.get(0).getPanellistID() == profile.getPanellistID()){
            profile.setIsNew(false);
        }else{
            profile.setIsNew(true);
        }
        return  factory.save(profile);
    }

    /**
     * Fetching the data from OPGCountry and saving it to DB.
     * @param opgCountry
     * @return
     * @throws Exception
     */
    public static boolean storeCountry(OPGCountry opgCountry) throws Exception {
        Country country = new Country();
        CountryFactory factory = new CountryFactory();
        country.setCountryID(opgCountry.getCountryID());
        country.setCountryCode(opgCountry.getCountryCode());
        country.setName(opgCountry.getCountryName());
        country.setStd(opgCountry.getStd());
        country.setCreditRate((float) opgCountry.getCreditRate());
        country.setIsDeleted(opgCountry.isDeleted());
        country.setGmt(opgCountry.getGmt());
        CSList<ICountry> list;
        try{
            list = factory.findByCountryID(opgCountry.getCountryID());
        }catch (Exception e){
            list = factory.findByCountryID(opgCountry.getCountryID());
        }
        if(!list.isEmpty()){
            country.setIsNew(false);
        }else{
            country.setIsNew(true);
        }
        return factory.save(country);
    }

    public static boolean storeNotification(String json)
    {
        boolean isSaved = false;
        try {
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            sdf.setTimeZone(tz);
            String d = sdf.format(new Date());
            AppNotification appNotification  = new AppNotification();
            JSONObject jsonObject = new JSONObject(json);
            if( jsonObject.has("alert") && jsonObject.has("title"))
            {
                //appNotification.setAppNotificationID(NotificationID.getID());
                appNotification.setBody(jsonObject.getString("alert").replace("'", "''"));
                appNotification.setIsRead(false);
                appNotification.setLastUpdated(Utils.convertToDateFromUTC(d));
                if(jsonObject.has("SurveyRef")){
                    appNotification.setTitle(jsonObject.getString("title").replace("'", "''")+"~"+jsonObject.getString("SurveyRef").replace("'", "''"));
                    appNotification.setType((short)100);
                }else {
                    appNotification.setTitle(jsonObject.getString("title").replace("'", "''"));
                    appNotification.setType((short)101);
                }
            }


            AppNotificationFactory notificationFactory = new AppNotificationFactory();
            CSList<IAppNotification> list = notificationFactory.findByAppNotificationID(appNotification.getAppNotificationID());
            if(!list.isEmpty()){
                appNotification.setIsNew(false);
            }else{
                appNotification.setIsNew(true);
            }
            isSaved = notificationFactory.save(appNotification);
        }
        catch (Exception  ex)
        {
            Log.i(Util.TAG,ex.getMessage());
        }
        return isSaved;

    }


    /**
     *
     * @param survey
     * @return
     * @throws Exception
     */
    public static boolean storeGeofenceSurvey(OPGGeofenceSurvey survey) throws Exception {

        GeofenceSurvey geofenceSurvey = new GeofenceSurvey();
        GeofenceSurveyFactory factory = new GeofenceSurveyFactory();

        geofenceSurvey.setSurveyName(survey.getSurveyName().replace("'","''"));
        geofenceSurvey.setSurveyID(survey.getSurveyID());
        geofenceSurvey.setSurveyReference(survey.getSurveyReference().replace("'","''"));
        geofenceSurvey.setAddress(survey.getAddress().replace("'","''"));
        geofenceSurvey.setAddressID((int) survey.getAddressID());
        geofenceSurvey.setLatitude((float) survey.getLatitude());
        geofenceSurvey.setLongitude((float) survey.getLongitude());
        geofenceSurvey.setGeoCode(survey.getGeocode());
        geofenceSurvey.setCreatedDate(survey.getCreatedDate());
        geofenceSurvey.setLastUpdatedDate(survey.getLastUpdatedDate());
        geofenceSurvey.setRange(survey.getRange());
        geofenceSurvey.setDistance((long)survey.getDistance());
        geofenceSurvey.setIsEntered(survey.isEntered());
        geofenceSurvey.setIsExit(survey.isExit());
        geofenceSurvey.setIsEnter(survey.isEnter());
        if(!survey.isEnter() && !survey.isExit() )
        {
            geofenceSurvey.setIsEnter(true);
        }
        geofenceSurvey.setGeofenceTimeInterval(survey.getTimeInterval());
        geofenceSurvey.setIsNew(true);

        Boolean status = factory.save(geofenceSurvey);
        return status;
    }

}
