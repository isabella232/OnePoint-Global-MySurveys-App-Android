package com.opg.my.surveys.lite;

import android.content.Context;

import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.db.RetriveOPGObjects;
import com.opg.sdk.models.OPGPanel;
import com.opg.sdk.models.OPGTheme;

import java.util.HashMap;
import java.util.List;

/**
 * Created by kiran on 21-02-2017.
 */

public class ThemeManager
{
  private  String headerLogoMediaID;
  private  String logoText;
  private  String loginBackgroundMediaID;
  private  String linksColor;
  private  String actionBtn = "#F79137" ;
  private static ThemeManager themeManager;

public static ThemeManager getThemeManagerInstance()
{
    if(themeManager == null)
    {
     themeManager   = new ThemeManager();
    }
    return themeManager;
}
public  void init(Context context)
{
    try
    {
        List<OPGTheme> opgThemes =  RetriveOPGObjects.getThemes();
        HashMap<String,String> hashMap = new HashMap<String, String>(5);
        for (OPGTheme opgTheme : opgThemes)
        {
            OPGPanel opgPanel = RetriveOPGObjects.getPanel(MySurveysPreference.getCurrentPanelID(context));
            if( opgPanel.getThemeTemplateID() == opgTheme.getThemeTemplateID())
            {
                hashMap.put(opgTheme.getName(),opgTheme.getValue());
            }
        }
        actionBtn = hashMap.get("Actionbtn");
        headerLogoMediaID = hashMap.get("Headerlogo");
        logoText = hashMap.get("Logotext");
        loginBackgroundMediaID = hashMap.get("Loginbackground");
        linksColor = hashMap.get("Linkscolor");
        MySurveysPreference.setHeaderMediaId(context,headerLogoMediaID == null || Integer.parseInt(headerLogoMediaID) == 0 ? null : headerLogoMediaID);
        MySurveysPreference.setLoginBgMediaId(context,loginBackgroundMediaID == null ? null : loginBackgroundMediaID);
        MySurveysPreference.setThemeActionBtnColor(context,actionBtn == null ?  "#F79137" : actionBtn);
        MySurveysPreference.setLogoText(context,logoText == null ? null : logoText);
    }
    catch (Exception ex)
    {

    }
}


}
