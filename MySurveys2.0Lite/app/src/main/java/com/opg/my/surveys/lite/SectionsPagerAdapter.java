package com.opg.my.surveys.lite;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.opg.my.surveys.lite.Fragments.ChangePasswordFragment;
import com.opg.my.surveys.lite.Fragments.NotificationsFragment;
import com.opg.my.surveys.lite.Fragments.ProfileFragment;
import com.opg.my.surveys.lite.Fragments.RootFragment;
import com.opg.my.surveys.lite.Fragments.SettingsFragment;
import com.opg.my.surveys.lite.Fragments.SettingsRootFragment;
import com.opg.my.surveys.lite.Fragments.SurveysFragment;
import com.opg.my.surveys.lite.R;

/**
 * Created by kiran on 02-11-2016.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter
{
    private int[] tab_icons = {R.drawable.icon_survey,R.drawable.icon_notification,R.drawable.icon_settings,R.drawable.icon_profile};
    private FragmentManager fm;
    private Fragment mFragmentAtPos0;
    private SurveysFragment surveysFragment;
    private RootFragment  rootFragment;
    private SettingsRootFragment settingsRootFragment;
    private ProfileFragment profileFragment;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fm = fm;
    }


    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return SurveysFragment.newInstance();
            case 1:
                //Reason to add RootFragment http://www.pineappslab.com/post/fragments-viewpager/
                return RootFragment.newInstance();
            case 2:
                //Reason to add RootFragment http://www.pineappslab.com/post/fragments-viewpager/
                return SettingsRootFragment.newInstance();
            case 3:
                return ProfileFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Surveys";
            case 1:
                return "Notifications";
            case 2:
                return "Settings";
            case 3:
                return "Profile";
        }
        return null;
    }

    @Override
    public int getItemPosition(Object object)
    {
        return POSITION_UNCHANGED;
    }

    public View getTabView(int position,Context mContext) {
        // Given you have a custom layout in `res/layout/custom_tab.xml` with a TextView and ImageView
        View v = LayoutInflater.from(mContext).inflate(R.layout.custom_tab, null);
        ImageView img = (ImageView) v.findViewById(R.id.tab_icon);
        img.setImageResource(tab_icons[position]);
        return v;
    }

}
