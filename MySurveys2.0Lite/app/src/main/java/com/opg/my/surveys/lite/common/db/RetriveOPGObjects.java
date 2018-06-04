package com.opg.my.surveys.lite.common.db;

import com.opg.cs2jnet.system.collections.lcc.CSList;
import com.opg.my.surveys.lite.BuildConfig;
import com.opg.my.surveys.lite.common.Aes256;
import com.opg.my.surveys.lite.common.Util;
import com.opg.pom.model.CountryFactory;
import com.opg.pom.model.ICountry;
import com.opg.pom.model.IPanel;
import com.opg.pom.model.IPanellistProfile;
import com.opg.pom.model.ITheme;
import com.opg.pom.model.PanelFactory;
import com.opg.pom.model.PanellistProfile;
import com.opg.pom.model.PanellistProfileFactory;
import com.opg.pom.model.ThemeFactory;
import com.opg.prom.model.AppNotification;
import com.opg.prom.model.AppNotificationFactory;
import com.opg.prom.model.GeofenceSurvey;
import com.opg.prom.model.GeofenceSurveyFactory;
import com.opg.prom.model.IAppNotification;
import com.opg.prom.model.IGeofenceSurvey;
import com.opg.prom.model.ISurvey;
import com.opg.prom.model.ISurveyPanel;
import com.opg.prom.model.SurveyFactory;
import com.opg.prom.model.SurveyPanelFactory;
import com.opg.sdk.models.OPGCountry;
import com.opg.sdk.models.OPGGeofenceSurvey;
import com.opg.sdk.models.OPGPanel;
import com.opg.sdk.models.OPGPanellistProfile;
import com.opg.sdk.models.OPGSurvey;
import com.opg.sdk.models.OPGTheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Padmavathi on 27/10/2016.
 */

public class RetriveOPGObjects {

    public synchronized static OPGPanellistProfile getPanellistProfile() throws Exception {
        OPGPanellistProfile opgPanellistProfile = new OPGPanellistProfile();
        PanellistProfileFactory factory = new PanellistProfileFactory();
        CSList<IPanellistProfile> list = factory.findAllObjects();
        if (list != null && list.size() > 0) {
            PanellistProfile panellistProfile = ((PanellistProfile) list.get(0));
            opgPanellistProfile.setPostalCode(Aes256.decrypt(panellistProfile.getPostalCode()));
            opgPanellistProfile.setAddress1(Aes256.decrypt(panellistProfile.getAddress1()));
            opgPanellistProfile.setAddress2(Aes256.decrypt(panellistProfile.getAddress2()));
            opgPanellistProfile.setEmailID(Aes256.decrypt(panellistProfile.getEmail()));
            opgPanellistProfile.setTitle(Aes256.decrypt(panellistProfile.getTitle()));
            opgPanellistProfile.setFirstName(Aes256.decrypt(panellistProfile.getFirstName()));
            opgPanellistProfile.setLastName(Aes256.decrypt(panellistProfile.getLastName()));
            opgPanellistProfile.setUserName(Aes256.decrypt(panellistProfile.getUserName()));
            opgPanellistProfile.setMobileNumber(Aes256.decrypt(panellistProfile.getMobileNumber()));
            opgPanellistProfile.setCountryName(Aes256.decrypt(panellistProfile.getSearchTag()));//stored countryname as searchtag
            /*opgPanellistProfile.setPostalCode(panellistProfile.getPostalCode());
            opgPanellistProfile.setAddress1(panellistProfile.getAddress1());
            opgPanellistProfile.setAddress2(panellistProfile.getAddress2());
            opgPanellistProfile.setEmailID(panellistProfile.getEmail());
            opgPanellistProfile.setTitle(panellistProfile.getTitle());
            opgPanellistProfile.setFirstName(panellistProfile.getFirstName());
            opgPanellistProfile.setLastName(panellistProfile.getLastName());
            opgPanellistProfile.setUserName(panellistProfile.getUserName());
            opgPanellistProfile.setMobileNumber(panellistProfile.getMobileNumber());
            opgPanellistProfile.setCountryName(panellistProfile.getSearchTag());//stored countryname as searchtag*/

            opgPanellistProfile.setDob(panellistProfile.getDOB());
            opgPanellistProfile.setGender(panellistProfile.getGender());

            opgPanellistProfile.setMediaID(panellistProfile.getMediaID());

            opgPanellistProfile.setStd(String.valueOf(panellistProfile.getCountryCode()));//stored stdcode as countrycode
            opgPanellistProfile.setStatusMessage("Success");
            opgPanellistProfile.setSuccess(true);
            opgPanellistProfile.setPanellistID(panellistProfile.getPanellistID());
         /*   json.put("FirstName", panellistProfile.getFirstName());
            json.put("LastName", panellistProfile.getLastName());
            json.put("UserName", panellistProfile.getUserName());
            json.put("Email", panellistProfile.getEmail());
            json.put("Address1", panellistProfile.getAddress1());
            json.put("Address2", panellistProfile.getAddress2());
            json.put("PostalCode", panellistProfile.getPostalCode());
            json.put("MobileNumber", panellistProfile.getMobileNumber());
            json.put("CountryCode", panellistProfile.getCountryCode());
            json.put("MediaID", panellistProfile.getMediaID());
            json.put("Title", panellistProfile.getTitle());
            json.put("Remark", panellistProfile.getRemark());
            json.put("PanellistID", panellistProfile.getPanellistID());
            json.put("SearchTag", panellistProfile.getSearchTag());
            json.put("Website", panellistProfile.getWebsite());
            json.put("GeoLocation", panellistProfile.getGeoLocation());
            json.put("Status", panellistProfile.getStatus());
            json.put("Gender", panellistProfile.getGender());
            json.put("MaritalStatus", panellistProfile.getMaritalStatus());
            json.put("TermsCondition", panellistProfile.getTermsCondition());
            Log.e("DOB 1", panellistProfile.getDOB().toGMTString());
            Log.e("DOB 2", Utils.convertToISOFromDate(panellistProfile.getDOB()));
            json.put("DOB", Utils.convertToISOFromDate(panellistProfile.getDOB()));
            json.put("IsDeleted", panellistProfile.getIsDeleted());
            json.put("LastUpdatedDate", Utils.convertToISOFromDate(panellistProfile.getLastUpdatedDate()));
            json.put("CreatedDate", Utils.convertToISOFromDate(panellistProfile.getCreatedDate()));*/

        } else {
            opgPanellistProfile.setStatusMessage("Error : No profile found in DB!");
            opgPanellistProfile.setSuccess(false);
        }

        return opgPanellistProfile;
    }

    /**
     * Retrive all the surveys from the database
     * @return
     * @throws Exception
     */
    public synchronized static List<OPGSurvey> getAllSurveys() throws Exception {
        ArrayList<OPGSurvey> opgSurveyList = new ArrayList<>();
        SurveyFactory factory = new SurveyFactory();
        if (factory.findAllObjects().size() > 0) {
            CSList<ISurvey> surveysList = factory.findAllObjects();
            if(BuildConfig.DEBUG)
                System.out.println("No Surveys " + surveysList.size());

            if (surveysList.size() > 0) {
                for (ISurvey survey : surveysList) {
                       /* if (temp.get(0).getIsGeofencing() != false) {*/
                    OPGSurvey opgSurvey = new OPGSurvey();
                    opgSurvey.setGeofencing(survey.getIsGeofencing());
                    opgSurvey.setName(survey.getName());
                    opgSurvey.setDescription(survey.getDescription());
                    opgSurvey.setScriptID(survey.getScriptID());
                    opgSurvey.setStatus(survey.getStatus());
                    opgSurvey.setSurveyReference(survey.getsurveyReference());
                    opgSurvey.setSurveyID(survey.getSurveyID());
                    opgSurvey.setCreatedDate(survey.getCreatedDate());
                    opgSurvey.setLastUpdatedDate(survey.getLastUpdatedDate());
                    opgSurvey.setOffline(survey.getIsOffline());
                    opgSurvey.setEstimatedTime(survey.getEstimatedTime());
                    opgSurvey.setDeadLine(survey.getDeadLine());
                    opgSurvey.setErrorMessage("");
                    opgSurveyList.add(opgSurvey);
                }
            }


        }
        return opgSurveyList;
    }


    /**
     * Retrive all the surveys from database based on the panelid
     * @param panelID
     * @return
     * @throws Exception
     */
    public synchronized static List<OPGSurvey> getAllSurveys(long panelID) throws Exception {
        List<OPGSurvey> opgSurveyList = new ArrayList<>();
        SurveyFactory factory = new SurveyFactory();
        if (factory.findAllObjects().size() > 0) {
            SurveyPanelFactory surveyPanelFactory = new SurveyPanelFactory();
            CSList<ISurveyPanel> surveyPanelsList = surveyPanelFactory.findByPanelID(panelID);
            if (surveyPanelsList.size() > 0) {
                for (ISurveyPanel surveyPanel : surveyPanelsList) {
                    CSList<ISurvey> surveysList = factory.findBySurveyID(surveyPanel.getSurveyID());
                    if(BuildConfig.DEBUG)
                        System.out.println("No Surveys " + surveysList.size());

                    if (surveysList.size() > 0) {
                        ISurvey survey = surveysList.get(0);
                        OPGSurvey opgSurvey = new OPGSurvey();
                        opgSurvey = RetriveOPGObjects.convertSurvey2OPGSurvey(survey);
                        opgSurveyList.add(opgSurvey);
                    }

                }
            }
        }
        Collections.reverse(opgSurveyList);
        return opgSurveyList;
    }

    /**
     * Retrive all the geofencing surveys from database based on the panelid
     * @param panelID
     * @return
     * @throws Exception
     */
    public synchronized static List<OPGSurvey> getGeofencingSurveys(long panelID) throws Exception {
        List<OPGSurvey> opgSurveyList = new ArrayList<>();
        SurveyFactory factory = new SurveyFactory();
        if (factory.findAllObjects().size() > 0) {
            SurveyPanelFactory surveyPanelFactory = new SurveyPanelFactory();
            CSList<ISurveyPanel> surveyPanelsList = surveyPanelFactory.findByPanelID(panelID);
            if (surveyPanelsList.size() > 0) {
                for (ISurveyPanel surveyPanel : surveyPanelsList) {
                    CSList<ISurvey> surveysList = factory.findBySurveyID(surveyPanel.getSurveyID());
                    if(BuildConfig.DEBUG)
                        System.out.println("No Surveys " + surveysList.size());

                    if (surveysList.size() > 0 ) {
                        ISurvey survey = surveysList.get(0);
                        if(survey.getIsGeofencing()){
                            OPGSurvey opgSurvey = new OPGSurvey();
                            opgSurvey = RetriveOPGObjects.convertSurvey2OPGSurvey(survey);
                            opgSurveyList.add(opgSurvey);
                        }
                    }

                }
            }
        }
        return opgSurveyList;
    }


    /**
     * To fetch a panel based on the panelID
     * @param panelId
     * @return
     * @throws Exception
     */
    public synchronized static OPGPanel getPanel(long panelId) throws Exception{
        OPGPanel opgPanel = new OPGPanel();
        PanelFactory factory = new PanelFactory();
        CSList<IPanel> models = factory.findByPanelID(panelId);
        for (IPanel panel:models){
            opgPanel.setName(panel.getName());
            opgPanel.setThemeTemplateID(panel.getThemeTemplateID());
            opgPanel.setThemeTemplateIDSpecified(panel.getThemeTemplateIDSpecified());
            opgPanel.setPanelID(panel.getPanelID());
            opgPanel.setLastUpdatedDate(panel.getLastUpdatedDate());
            opgPanel.setCreatedDate(panel.getCreatedDate());

            opgPanel.setLogoIDSpecified(panel.getIsDeleted());
            opgPanel.setLogoUrl(panel.getSearchTag());
            opgPanel.setLogoID(panel.getRemark() != null ? Long.parseLong(panel.getRemark()) : 0 ) ;

            opgPanel.setMediaIDSpecified(panel.getPanelType() == 1 ? true : false);
            opgPanel.setMediaUrl(panel.getDescription());
            opgPanel.setMediaID(panel.getUserID());

        }
        return opgPanel;
    }

    /**
     * Retrive the panels from the database.
     * @return
     * @throws Exception
     */
    public  synchronized static List<OPGPanel> getPanels() throws Exception{
        List<OPGPanel> opgPanelsList = new ArrayList<>();
        PanelFactory factory = new PanelFactory();
        CSList<IPanel> models = factory.findAllObjects();
        if (models.size() > 0) {
            for (IPanel panel : models) {
                OPGPanel opgPanel = new OPGPanel();
                opgPanel.setName(panel.getName());
                opgPanel.setThemeTemplateID(panel.getThemeTemplateID());
                opgPanel.setThemeTemplateIDSpecified(panel.getThemeTemplateIDSpecified());
                opgPanel.setPanelID(panel.getPanelID());
                opgPanel.setLastUpdatedDate(panel.getLastUpdatedDate());
                opgPanel.setCreatedDate(panel.getCreatedDate());
                opgPanelsList.add(opgPanel);

                opgPanel.setLogoIDSpecified(panel.getIsDeleted());
                opgPanel.setLogoUrl(panel.getSearchTag());
                opgPanel.setLogoID(panel.getRemark() != null ? Long.parseLong(panel.getRemark()) : 0 ) ;

                opgPanel.setMediaIDSpecified(panel.getPanelType() == 1 ? true : false);
                opgPanel.setMediaUrl(panel.getDescription());
                opgPanel.setMediaID(panel.getUserID());
            }
        }
        return opgPanelsList;
    }


    /**
     * Retrive the themes from the database
     * @return
     * @throws Exception
     */
    public synchronized static List<OPGTheme> getThemes() throws  Exception{
        // Themes
        ThemeFactory factory = new ThemeFactory();
        CSList<ITheme> models = factory.findAllObjects();
        List<OPGTheme> opgThemeList = new ArrayList<>();
        if (models.size() > 0) {
            for (ITheme theme : models) {
                OPGTheme opgTheme = new OPGTheme();
                opgTheme.setName(theme.getName());
                opgTheme.setThemeID(theme.getThemeID());
                opgTheme.setValue(theme.getValue());
                opgTheme.setThemeElementTypeID(theme.getThemeElementTypeID());
                opgTheme.setThemeTemplateID(theme.getThemeTemplateID());
                opgTheme.setDeleted(theme.getIsDeleted());
                opgTheme.setLastUpdatedDate(theme.getLastUpdatedDate());
                opgTheme.setCreatedDate(theme.getCreatedDate());
                opgThemeList.add(opgTheme);
            }
        }
        return opgThemeList;
    }

    /**
     * Retrive the themes based on the themeTemplateID from the database
     * @param themeTemplateID
     * @return
     * @throws Exception
     */
    public synchronized static List<OPGTheme> getThemes(long themeTemplateID) throws Exception{
        ThemeFactory factory = new ThemeFactory();
        CSList<ITheme> models = factory.findByThemeTemplateID(themeTemplateID);
        List<OPGTheme> opgThemeList = new ArrayList<>();
        if (models.size() > 0) {
            for (ITheme theme : models) {
                OPGTheme opgTheme = new OPGTheme();
                opgTheme.setName(theme.getName());
                opgTheme.setThemeID(theme.getThemeID());
                opgTheme.setValue(theme.getValue());
                opgTheme.setThemeElementTypeID(theme.getThemeElementTypeID());
                opgTheme.setThemeTemplateID(theme.getThemeTemplateID());
                opgTheme.setDeleted(theme.getIsDeleted());
                opgTheme.setLastUpdatedDate(theme.getLastUpdatedDate());
                opgTheme.setCreatedDate(theme.getCreatedDate());
                opgThemeList.add(opgTheme);
            }
        }
        return opgThemeList;
    }

    /**
     * Retrive the countries from the database
     * @return
     * @throws Exception
     */
    public synchronized static List<OPGCountry> getCountries() throws Exception{
        CountryFactory factory = new CountryFactory();
        CSList<ICountry> countries = factory.findAllObjects();
        List<OPGCountry> opgCountries = new ArrayList<>();
        if(countries.size() > 0){
            for (ICountry country : countries){
                OPGCountry opgCountry = new OPGCountry();
                opgCountry.setCountryID(country.getCountryID());
                opgCountry.setCountryCode(country.getCountryCode());
                opgCountry.setCountryName(country.getName());
                opgCountry.setStd(country.getStd());
                opgCountry.setGmt(country.getGmt());
                opgCountry.setCreditRate(country.getCreditRate());
                opgCountry.setDeleted(country.getIsDeleted());
                opgCountries.add(opgCountry);
            }
        }
        return opgCountries;
    }


    public synchronized static List<AppNotification> getNotificationList() throws Exception
    {
        AppNotificationFactory factory = new AppNotificationFactory();
        CSList<IAppNotification> appNotifications = factory.findAllObjects();
        List<AppNotification> notifications = new ArrayList<>();
        if(appNotifications.size() > 0)
        {
            for (IAppNotification appNotification : appNotifications)
            {
                notifications.add((AppNotification)appNotification);
            }
        }
        Collections.reverse(notifications);
        return notifications;
    }

    /**
     * Retrive the survey based on the surveyID
     */
    public synchronized static OPGSurvey getSurvey(long surveyID) throws Exception{
        OPGSurvey opgSurvey = new OPGSurvey();

        SurveyFactory factory = new SurveyFactory();
        if (factory.findAllObjects().size() > 0) {
            CSList<ISurvey> surveysList = factory.findBySurveyID(surveyID);
            for (ISurvey survey : surveysList) {
                opgSurvey = RetriveOPGObjects.convertSurvey2OPGSurvey(survey);
            }
        }
        return opgSurvey;
    }


    public synchronized static OPGSurvey convertSurvey2OPGSurvey(ISurvey survey) throws Exception{
        OPGSurvey opgSurvey = new OPGSurvey();
        opgSurvey.setGeofencing(survey.getIsGeofencing());
        opgSurvey.setName(survey.getName());
        opgSurvey.setDescription(survey.getDescription());
        opgSurvey.setScriptID(survey.getScriptID());
        if(survey.getIsOffline())
        {
            opgSurvey.setStatus(survey.getStatus() == null ? Util.DOWNLOAD_STATUS_KEY : survey.getStatus());
        }
        else
        {
            opgSurvey.setStatus(survey.getStatus() == null ? Util.NEW_STATUS_KEY : survey.getStatus());
        }

        opgSurvey.setSurveyReference(survey.getsurveyReference());//we are using search tags as survey reference
        opgSurvey.setSurveyID(survey.getSurveyID());
        opgSurvey.setCreatedDate(survey.getCreatedDate());
        opgSurvey.setLastUpdatedDate(survey.getLastUpdatedDate());
        opgSurvey.setOffline(survey.getIsOffline());
        opgSurvey.setEstimatedTime(survey.getEstimatedTime());
        opgSurvey.setDeadLine(survey.getDeadLine());
        opgSurvey.setErrorMessage("");
        return opgSurvey;
    }

    public synchronized static OPGGeofenceSurvey convertGeofenceSurvey2OPGGeofenceSurvey(IGeofenceSurvey iGeofenceSurvey) throws Exception
    {
        OPGGeofenceSurvey opgGeofenceSurvey = new OPGGeofenceSurvey();
        opgGeofenceSurvey.setSurveyName(iGeofenceSurvey.getSurveyName().replace("'","''"));
        opgGeofenceSurvey.setSurveyID(iGeofenceSurvey.getSurveyID());
        opgGeofenceSurvey.setSurveyReference(iGeofenceSurvey.getSurveyReference().replace("'","''"));
        opgGeofenceSurvey.setAddress(iGeofenceSurvey.getAddress().replace("'","''"));
        opgGeofenceSurvey.setAddressID(iGeofenceSurvey.getAddressID());
        opgGeofenceSurvey.setLatitude(iGeofenceSurvey.getLatitude());
        opgGeofenceSurvey.setLongitude(iGeofenceSurvey.getLongitude());
        opgGeofenceSurvey.setGeocode(iGeofenceSurvey.getGeoCode());
        opgGeofenceSurvey.setCreatedDate(iGeofenceSurvey.getCreatedDate());
        opgGeofenceSurvey.setLastUpdatedDate(iGeofenceSurvey.getLastUpdatedDate());
        opgGeofenceSurvey.setRange(iGeofenceSurvey.getRange());
        opgGeofenceSurvey.setDistance(iGeofenceSurvey.getDistance());
        opgGeofenceSurvey.setEntered(iGeofenceSurvey.getIsEntered());
        opgGeofenceSurvey.setExit(iGeofenceSurvey.getIsExit());
        opgGeofenceSurvey.setEnter(iGeofenceSurvey.getIsEnter());
        opgGeofenceSurvey.setTimeInterval(iGeofenceSurvey.getGeofenceTimeInterval());
        return opgGeofenceSurvey ;
    }
    /**
     *
     * @return
     * @throws Exception
     */
    public synchronized static List<OPGGeofenceSurvey> getOPGGeofenceSurveys() throws Exception{
        ArrayList<OPGGeofenceSurvey> opgGeofenceSurveyList = new ArrayList<>();
        GeofenceSurveyFactory factory = new GeofenceSurveyFactory();
        if (factory.findAllObjects().size() > 0) {
            CSList<IGeofenceSurvey> geofenceSurveyList = factory.findAllObjects();
            if(BuildConfig.DEBUG)
                System.out.println("No Surveys " + geofenceSurveyList.size());

            if (geofenceSurveyList.size() > 0) {
                for (IGeofenceSurvey survey : geofenceSurveyList) {
                    OPGGeofenceSurvey opgGeofenceSurvey = new OPGGeofenceSurvey();
                    opgGeofenceSurvey.setSurveyName(survey.getSurveyName().replace("'","''"));
                    opgGeofenceSurvey.setSurveyID(survey.getSurveyID());
                    opgGeofenceSurvey.setSurveyReference(survey.getSurveyReference().replace("'","''"));
                    opgGeofenceSurvey.setAddress(survey.getAddress().replace("'","''"));
                    opgGeofenceSurvey.setAddressID(survey.getAddressID());
                    opgGeofenceSurvey.setLatitude(survey.getLatitude());
                    opgGeofenceSurvey.setLongitude(survey.getLongitude());
                    opgGeofenceSurvey.setGeocode(survey.getGeoCode());
                    opgGeofenceSurvey.setCreatedDate(survey.getCreatedDate());
                    opgGeofenceSurvey.setLastUpdatedDate(survey.getLastUpdatedDate());
                    opgGeofenceSurvey.setRange(survey.getRange());
                    opgGeofenceSurvey.setDistance(survey.getDistance());
                    opgGeofenceSurvey.setEntered(survey.getIsEntered());
                    opgGeofenceSurvey.setExit(survey.getIsExit());
                    opgGeofenceSurvey.setEnter(survey.getIsEnter());
                    opgGeofenceSurvey.setTimeInterval(survey.getGeofenceTimeInterval());
                    opgGeofenceSurveyList.add(opgGeofenceSurvey);
                }
            }
        }
        return opgGeofenceSurveyList;
    }

    public synchronized static OPGGeofenceSurvey getOPGGeofenceSurveyBySurveyIDAddressID(long surveyID, long addressID)
    {
        OPGGeofenceSurvey opgGeofenceSurvey =  null;
        try {
            GeofenceSurveyFactory factory = new GeofenceSurveyFactory();
            CSList<IGeofenceSurvey> list = factory.findByAddressIDSurveyID((int) addressID, surveyID);
            if (list.size() > 0) {
                GeofenceSurvey survey = (GeofenceSurvey) list.get(0);
                opgGeofenceSurvey = convertGeofenceSurvey2OPGGeofenceSurvey(survey);
            }
        }catch (Exception ex)
        {
        }
        return opgGeofenceSurvey;
    }
    /*public static long getPanellistID() {
    long panellistID = 0;
    try
    {

        PanellistProfileFactory panellistProfileFactory = new PanellistProfileFactory();
        panellistID  = getPanellistProfile().getPanellistID();
    }
    catch (Exception ex)
    {
        Log.i("",ex.getLocalizedMessage());
    }
    return panellistID;
  }*/
}
