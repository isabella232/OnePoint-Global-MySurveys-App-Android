package com.opg.my.surveys.lite.common.db;

import com.opg.cs2jnet.system.collections.lcc.CSList;
import com.opg.prom.model.AppNotification;
import com.opg.prom.model.AppNotificationFactory;
import com.opg.prom.model.GeofenceSurvey;
import com.opg.prom.model.GeofenceSurveyFactory;
import com.opg.prom.model.IAppNotification;
import com.opg.prom.model.IGeofenceSurvey;
import com.opg.prom.model.ISurvey;
import com.opg.prom.model.Survey;
import com.opg.prom.model.SurveyFactory;

/**
 * Created by Padmavathi on 17/11/2016.
 */

public class UpdateOPGObjects {

    public static boolean updateSurveyStatus(long surveyID,String status) throws Exception {
        SurveyFactory factory = new SurveyFactory();
        CSList<ISurvey> list = factory.findBySurveyID(surveyID);
        if (list.size() > 0) {
            Survey survey = (Survey) list.get(0);
            survey.setStatus(status);
            return factory.save(survey);
        } else{
            return false;
        }
    }

    public static boolean updateAppNotificationStatus(long appNotificationID,boolean isRead) throws Exception {
        AppNotificationFactory factory = new AppNotificationFactory();
        CSList<IAppNotification> list = factory.findByAppNotificationID(appNotificationID);
        if (list.size() > 0) {
            AppNotification appNotification = (AppNotification) list.get(0);
            appNotification.setIsRead(isRead);
            return factory.save(appNotification);
        } else{
            return false;
        }
    }

    public static boolean updateOPGGeofenceSurveyStatus(long addressID,long surveyID,boolean status) throws Exception {
        GeofenceSurveyFactory factory = new GeofenceSurveyFactory();
        CSList<IGeofenceSurvey> list = factory.findByAddressIDSurveyID((int)addressID,surveyID);
        if (list.size() > 0) {
            GeofenceSurvey survey = (GeofenceSurvey) list.get(0);
            survey.setIsEntered(status);
            return factory.save(survey);
        } else{
            return false;
        }
    }

    public static void clearOPGGeofencesDB() throws Exception{
        GeofenceSurveyFactory factory = new GeofenceSurveyFactory();
        CSList<IGeofenceSurvey> list = factory.findAllObjects();
        for (IGeofenceSurvey geofenceSurvey:list){
            factory.deleteByPk(geofenceSurvey.getGeofenceID());
        }

    }

    public static boolean deleteNotification(long notificationID) throws Exception{
        AppNotificationFactory notificationFactory = new AppNotificationFactory();
        return notificationFactory.deleteByPk(notificationID);
    }

}
