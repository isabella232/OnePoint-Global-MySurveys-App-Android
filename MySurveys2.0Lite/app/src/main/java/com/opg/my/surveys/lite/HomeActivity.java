package com.opg.my.surveys.lite;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.iid.FirebaseInstanceId;
import com.opg.dbhelper.DBHelperApplication;
import com.opg.dbhelper.OPGDBHelper;
import com.opg.logging.LogManager;
import com.opg.my.surveys.lite.Fragments.ProfileFragment;
import com.opg.my.surveys.lite.Fragments.RootFragment;
import com.opg.my.surveys.lite.Fragments.SettingsRootFragment;
import com.opg.my.surveys.lite.Fragments.SurveysFragment;
import com.opg.my.surveys.lite.common.LoginType;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.RetriveOPGObjects;
import com.opg.my.surveys.lite.common.db.SaveOPGObjects;
import com.opg.sdk.OPGSDK;
import com.opg.sdk.exceptions.OPGException;
import com.opg.sdk.geofence.OPGGeofenceTriggerEvents;
import com.opg.sdk.models.OPGDownloadMedia;
import com.opg.sdk.models.OPGGeofenceStatus;
import com.opg.sdk.models.OPGGeofenceSurvey;
import com.opg.sdk.models.OPGPanellistProfile;
import com.opg.sdk.models.OPGSurvey;
import com.opg.sdk.models.OPGUpdatePanellistProfile;

import java.io.File;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.opg.my.surveys.lite.common.Util.REQUEST_CODE_LOCATION;
import static com.opg.my.surveys.lite.common.Util.REQUEST_CODE_LOCATION_SETTINGS;
import static com.opg.my.surveys.lite.common.Util.STORAGE_PERMISSION_REQUEST_CODE;
import static com.opg.my.surveys.lite.common.Util.REQUEST_CODE_PROFILE;
import static com.opg.my.surveys.lite.common.Util.TAG;
import static com.opg.my.surveys.lite.common.Util.mediaDownloadingList;

public class HomeActivity extends AppCompatActivity implements OPGGeofenceTriggerEvents, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private UploadProfilePicListener uploadProfilePicListener;
    private OfflineScriptDownloadListener offlineRefreshListener;
    private GoogleApiClient mGoogleApiClient;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    //private ViewPager mViewPager;
    // private CoordinatorLayout mainLayout;
    // Keys
    private TabLayout tabLayout;
    private  Toolbar toolbar;
    private Context mContext;
    private Menu mMenu;
    private MenuItem refreshMenuItem;
    private Snackbar snackbar;
    private Animation rotation = null;
    private OPGSDK opgsdk;
    public AsyncTask<String,Void,String> updProfileImage;
    private TextView notify_tv,tv_header_logo;
    private CountDownTimer countDownTimer;
    private ImageView ivHeaderLogo;



    private SurveysFragment surveysFragment;
    private RootFragment rootFragment;
    private SettingsRootFragment settingsRootFragment;
    private ProfileFragment profileFragment;


    private int[] tab_icons = {R.drawable.icon_survey,R.drawable.icon_notification,R.drawable.icon_settings,R.drawable.icon_profile};


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Util.BROADCAST_ACTION_SAVE_DATA))
            {

                if (MySurveysPreference.isDownloaded(mContext))
                {
                    ThemeManager.getThemeManagerInstance().init(mContext);
                    updateTheme();
                    fetchGeofences();
                }
                else
                {
                    String message = "Failed to store the data ";
                    if (intent.hasExtra("message"))
                    {
                        String msg = intent.getStringExtra("message");
                        if(msg.equals(Util.SESSION_TIME_OUT_ERROR))
                        {
                            Util.launchLoginActivity(HomeActivity.this);
                        }
                        else
                        {
                            message = message + "\n" + msg + "\n" + "refresh or try login again";
                        }
                        showSnackBar(message,Snackbar.LENGTH_LONG);
                    }
                    else
                    {

                        if(!Util.isOnline(mContext))
                        {
                            showSnackBar(getString(R.string.no_network_msg),Toast.LENGTH_LONG);
                        }
                        else
                        {
                            showSnackBar("Some error happen,refresh or try login again",Toast.LENGTH_LONG);
                        }
                    }
                }
                refreshAnimationStop();
            }
            else if (intent.getAction().equalsIgnoreCase(Util.BROADCAST_ACTION_GEOFENCE_STOP)
                    || intent.getAction().equalsIgnoreCase(Util.BROADCAST_ACTION_GEOFENCE_START)) {
                try {
                    if (intent.getAction().equalsIgnoreCase(Util.BROADCAST_ACTION_GEOFENCE_START)) {
                        fetchGeofences();
                    } else if (intent.getAction().equalsIgnoreCase(Util.BROADCAST_ACTION_GEOFENCE_STOP)) {
                        if(Util.isServiceRunning(mContext,LocationService.class))
                        { stopService(new Intent(mContext,LocationService.class)); }
                        opgsdk.stopGeofencingMonitor(mContext, mGoogleApiClient, HomeActivity.this);
                        MySurveysPreference.setLastLocationKnown(mContext,"");
                    }
                } catch (OPGException e) {
                    MySurveysPreference.setIsGeofencingEnabled(mContext,false);
                    e.printStackTrace();
                }
            }
            else if(intent.getAction().equals(Util.ACTION_UPLOAD_RESULT))
            {
                String msg = intent.getStringExtra("message");
                showSnackBar(msg,Snackbar.LENGTH_INDEFINITE);
            }
            else if(intent.getAction().equals(Util.BROADCAST_ACTION_UPLOADED_ALL))
            {
                if(intent.hasExtra("message")){
                    refreshAnimationStop();
                    Util.showMessageDialog(mContext,intent.getStringExtra("message"),"");
                }else{
                    refreshData();
                }
            }
            else if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION) && Util.isOnline(context))
            {
                if(!MySurveysPreference.isDownloaded(context))
                {
                    onRefresh();
                }
            }
            else if(intent.getAction().equals(Util.ACTION_SESSION_EXPIRED))
            {
                Util.launchLoginActivity((Activity) mContext);
            }
        }
    };

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mContext = this;
        if(!Util.isTablet(mContext)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        opgsdk = new OPGSDK();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        notify_tv  = (TextView)findViewById(R.id.notify_msg_tv);
        ivHeaderLogo = (ImageView)findViewById(R.id.iv_header_logo);
        tv_header_logo = (TextView)findViewById(R.id.tv_header_logo);
        rotation   = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.clockwise_refresh);
        rotation.setRepeatCount(Animation.INFINITE);
        Util.setTypeface(this,tv_header_logo,"font/roboto_bold.ttf");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //To hide the default title from ToolBar
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);


        tabLayout = (TabLayout) findViewById(R.id.tabs);
        setTabs();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setCurrentTabFragment(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if(tab.getPosition()==2 || tab.getPosition()==1 ){
                    Util.sendBroadcastMessage(true,mContext,"",Util.BROADCAST_ACTION_REFRESH_FRAGMENT);
                }
            }
        });
        buildGoogleApiClient();
        updateTheme();
    }

    /**
     * Fetch the data from api and store in db
     */
    private void performAPIOperations() {
        if (MySurveysPreference.isDBCreated(mContext) && !MySurveysPreference.isDownloaded(mContext))
        {
            if (Util.isOnline(mContext)) {
                refreshAnimationStart();
                startService(new Intent(mContext, FetchDataService.class));
                deleteAppMediaFolders();
                Util.sendBroadcastMessage(true, mContext, "", Util.BROADCAST_ACTION_REFRESH);
            } else {
                showSnackBar(getString(R.string.no_network_msg), Snackbar.LENGTH_LONG);
            }
        }else {
            Util.sendBroadcastMessage(true, mContext, "", Util.BROADCAST_ACTION_REFRESH);
        }

    }

    private void setTabs() {
        if(surveysFragment == null){
            surveysFragment = new SurveysFragment();
        }
        if(rootFragment == null){
            rootFragment    = new RootFragment();
        }
        if(settingsRootFragment == null){
            settingsRootFragment = new SettingsRootFragment();
        }
        if(profileFragment == null){
            profileFragment = new ProfileFragment();
        }
        tabLayout.addTab(tabLayout.newTab().setText("Surveys").setCustomView(getTabView(0,mContext)),true);
        tabLayout.addTab(tabLayout.newTab().setText("Notifications").setCustomView(getTabView(1,mContext)));
        tabLayout.addTab(tabLayout.newTab().setText("Settings").setCustomView(getTabView(2,mContext)));
        tabLayout.addTab(tabLayout.newTab().setText("Profile").setCustomView(getTabView(3,mContext)));
        replaceFragment(surveysFragment);
    }

    public View getTabView(int position,Context mContext) {
        // Given you have a custom layout in `res/layout/custom_tab.xml` with a TextView and ImageView
        View v = LayoutInflater.from(mContext).inflate(R.layout.custom_tab, null);
        ImageView img = (ImageView) v.findViewById(R.id.tab_icon);
        img.setImageResource(tab_icons[position]);
        return v;
    }

    private void setCurrentTabFragment(int tabPosition)
    {
        switch (tabPosition)
        {
            case 0 :
                replaceFragment(surveysFragment);
                break;
            case 1 :
                replaceFragment(rootFragment);
                break;
            case 2 :
                replaceFragment(settingsRootFragment);
                break;
            case 3 :
                replaceFragment(profileFragment);
                break;
        }
    }
    public void replaceFragment(Fragment fragment) {
        try{
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.frame_container, fragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.addToBackStack(null);
            ft.commit();
        }catch(Exception e){
            if(BuildConfig.DEBUG)
                Log.e(mContext.getPackageName(),"Error in replacing the fragments");
        }
    }

    /**
     * This methods helps to setup the database
     */
    private void setDatabase() {
        DBHelperApplication.setAppContext(getApplicationContext());
        OPGDBHelper.mContext = HomeActivity.this;
        if (!MySurveysPreference.isDBCreated(mContext)) {
            OPGDBHelper.setDatabaseName(Util.db_name);
            if(BuildConfig.DEBUG)
                LogManager.getLogger(getClass()).error(Util.db_name + " SETUP STARTED!");
            OPGDBHelper.getInstance().getWritableDatabase();
            if(BuildConfig.DEBUG)
                LogManager.getLogger(getClass()).error(Util.db_name + " SETUP ENDED!");
            MySurveysPreference.setIsDBCreated(mContext, true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        mMenu = menu;
        refreshMenuItem = menu.findItem(R.id.action_refresh);
        View view = refreshMenuItem.getActionView();
        if (view != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMenu.performIdentifierAction(refreshMenuItem.getItemId(), 0);
                }
            });
        }

        if(Util.isServiceRunning(mContext,FetchDataService.class)){
            refreshAnimationStart();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            if (Util.isOnline(mContext)) {
                onRefresh();
            } else {
                if(!Util.isOnline(mContext)) {
                    showSnackBar(getString(R.string.no_network_msg), Snackbar.LENGTH_LONG);
                }
            }

        }
        return true;
    }

    public void showSnackBar(String text,int duration )
    {
        if(notify_tv != null){
            notify_tv.setText(text);
            if(countDownTimer!=null){
                countDownTimer.cancel();
            }
            notify_tv.setVisibility(View.VISIBLE);
            if(duration != Snackbar.LENGTH_INDEFINITE){
                countDownTimer = new CountDownTimer(4000, 1000) {

                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                        dismissSnackBar();
                    }

                }.start();
            }
        }

    }

    public void dismissSnackBar() {
        if(notify_tv!=null)
            notify_tv.setVisibility(View.GONE);
    }

    /**
     * Called when we are refreshing the db
     */
    private void onRefresh() {
        MySurveysPreference.clearEnabledSurveys(this);
        if (!Util.isServiceRunning(mContext , FetchDataService.class)) {
            refreshAnimationStart();
                refreshData();
        }
    }

    private void refreshData() {
        try
        {
            MySurveysPreference.clearScriptData(mContext);//Clearing local script shared preference
            Util.clearDB("AppNotification");//clearing the db
            MySurveys.clearHashMaps();//clearing all the download hashmaps
            MySurveysPreference.setIsDownloaded(mContext, false);//making the download flag as default (false)
            startService(new Intent(mContext, FetchDataService.class));//calling the background service to download the data
            if(Util.isServiceRunning(mContext,LocationService.class)) {
                stopService(new Intent(mContext,LocationService.class));
                MySurveysPreference.setLastLocationKnown(mContext,"");
            }
            deleteAppMediaFolders();
            Util.sendBroadcastMessage(true, mContext, "", Util.BROADCAST_ACTION_REFRESH);//broadcast to refresh the views
        }catch (Exception ex)
        {
            if(BuildConfig.DEBUG) {
                Log.i(TAG, ex.getMessage());
            }
        }
    }

    private void deleteAppMediaFolders() {
        try {
            File mySurveysMediaPath = new File(Environment.getExternalStorageDirectory() + "/MySurveys/Theme/");
            boolean delMySurveysMedPth = deleteDirectory(mContext, mySurveysMediaPath);
            //FilePath related to the profile files in the mysurveys app
            File mySurveysProfileImagePath = new File(Environment.getExternalStorageDirectory() + "/MySurveys/Profile/");
            boolean delMProfileMedPth = deleteDirectory(mContext, mySurveysProfileImagePath);
        }catch (Exception e){
            if(BuildConfig.DEBUG){
                Log.d(TAG,"Delete Exception"+e.toString());
            }
        }
    }

    /**
     * This is method is used to implement start the animation for the refresh button present in the actionbar
     */
    public void refreshAnimationStart() {
        if (refreshMenuItem != null)
            refreshMenuItem.getActionView().startAnimation(rotation);
    }

    /**
     * Checks whether the device is having google play services or not.
     * @return
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(mContext);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                // googleApiAvailability.getErrorDialog(HomeActivity.this, status, 2404).show();
                googleApiAvailability.getErrorDialog(HomeActivity.this, status, 2404, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        HomeActivity.this.finish();
                    }
                });
            }
            return false;
        }
        return true;
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * This method is used to stop the animation for the refresh button present in the actionbar.
     */
    private void refreshAnimationStop() {
        if (!Util.isServiceRunning(mContext , FetchDataService.class) && refreshMenuItem != null) {
            refreshMenuItem = mMenu.findItem(R.id.action_refresh);
            refreshMenuItem.getActionView().clearAnimation();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        boolean locationPerGranted = false;
        boolean isLocationDialogDisplayed = false;
        for (int i = 0, len = permissions.length; i < len; i++)
        {
            final String permission = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_DENIED)
            {
                // user rejected the permission
                boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,permission );
                if (! showRationale)
                {
                    if(requestCode ==STORAGE_PERMISSION_REQUEST_CODE )
                    {
                        Util.showPermissionDialog(this,getString(R.string.storage_permission));
                    }
                    else if(requestCode == REQUEST_CODE_PROFILE)
                    {
                        switch (permission)
                        {
                            case android.Manifest.permission.WRITE_EXTERNAL_STORAGE : Util.showPermissionDialog(this,getString(R.string.gallery_permission));
                                break;
                            case Manifest.permission.CAMERA :  Util.showPermissionDialog(this,getString(R.string.camera_permission));
                                break;
                        }
                    }
                    else if(requestCode == REQUEST_CODE_LOCATION && !isLocationDialogDisplayed)
                    {
                        isLocationDialogDisplayed = true;
                        Util.showPermissionDialog(this,getString(R.string.location_permission));
                    }
                    else
                    {
                        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                    }
                }
                else
                {
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
            }
            else
            {
                switch (requestCode)
                {
                    case REQUEST_CODE_LOCATION_SETTINGS :
                        if(!locationPerGranted && Util.locationServicesEnabled(mContext)) {
                            MySurveysPreference.setIsGeofencingEnabled(mContext, !MySurveysPreference.getIsGeofencingEnabled(mContext));
                            try {
                                if (MySurveysPreference.getIsGeofencingEnabled(mContext)) {
                                    fetchGeofences();
                                } else if (!MySurveysPreference.getIsGeofencingEnabled(mContext)) {
                                    opgsdk.stopGeofencingMonitor(mContext, mGoogleApiClient, HomeActivity.this);
                                }
                            } catch (OPGException e) {
                                e.printStackTrace();
                                MySurveysPreference.setIsGeofencingEnabled(mContext,false);
                            }
                            locationPerGranted = true;
                        }
                        break;

                }
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Launching the notificationFragment for status bar
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(Util.POSITION))
        {
            int position = getIntent().getExtras().getInt(Util.POSITION);
            setCurrentTabFragment(position);
            tabLayout.getTabAt(position).select();
            getIntent().removeExtra(Util.POSITION);
        }
        IntentFilter iff = new IntentFilter();
        iff.addAction(Util.BROADCAST_ACTION_SAVE_DATA);
        iff.addAction(Util.BROADCAST_ACTION_GEOFENCE_START);
        iff.addAction(Util.BROADCAST_ACTION_GEOFENCE_STOP);
        iff.addAction(Util.BROADCAST_ACTION_GEOFENCES_UPDATED);
        iff.addAction(Util.BROADCAST_ACTION_UPLOADED_ALL);
        iff.addAction(Util.ACTION_UPLOAD_RESULT);
        iff.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        iff.addAction(Util.ACTION_SESSION_EXPIRED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, iff);
        performAPIOperations();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        if (Util.isServiceRunning(mContext, FetchDataService.class)) {
            stopService(new Intent(mContext, FetchDataService.class));
        }
        MySurveys.clearHashMaps();
        uploadProfilePicListener = null;
        offlineRefreshListener = null;
        mSectionsPagerAdapter = null;
        // mViewPager = null;
        tabLayout = null;
        mContext = null;
        mMenu = null;
        refreshMenuItem = null;
        snackbar = null;
        rotation = null;
        opgsdk  = null;
        updProfileImage  = null;
        notify_tv = null;
        countDownTimer = null;
        surveysFragment = null;
        rootFragment = null;
        settingsRootFragment = null;
        profileFragment = null;
        super.onDestroy();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
    @Override
    public void onBackPressed() {
        finish();
    }

    //Related to the sdk geofence start/stop  status
    @Override
    public void onResult(OPGGeofenceStatus opgGeofenceStatus) {
        MySurveysPreference.setIsGeofencingEnabled(mContext, opgGeofenceStatus.isMonitoring());
        if(!opgGeofenceStatus.isSuccess())
            Toast.makeText(mContext, opgGeofenceStatus.getMessage(), Toast.LENGTH_LONG).show();
    }


    //Triggered when the device enters any particular geofence areas
    @Override
    public void didEnterSurveyRegion(Location location, List<OPGGeofenceSurvey> list) {
        if(BuildConfig.DEBUG)
            System.out.println("Location:"+location.getLatitude()+"\n List Size:"+list.size());
        try {
            //MySurveys.updateOPGGeofenceSurveys(list,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Triggered when the device exit any particular geofence areas
    @Override
    public void didExitSurveyRegion(Location location, List<OPGGeofenceSurvey> list) {
        if(BuildConfig.DEBUG)
            System.out.println("Location:"+location.getLatitude()+"\n List Size:"+list.size());
        try {
           // MySurveys.updateOPGGeofenceSurveys(list,false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            return;
        }
        fetchGeofences();
    }

    private void fetchGeofences() {
        if(MySurveysPreference.getIsGeofencingEnabled(mContext)){
            if(Util.locationServicesEnabled(mContext)){
                try {
                    if(!Util.isServiceRunning(mContext,LocationService.class))
                    { startService(new Intent(mContext,LocationService.class)); }
                    else {
                        if(BuildConfig.DEBUG){
                            Log.i(TAG,"LocationService already running");
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                Util.showLocationServicesError(mContext);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class UpdateProfilePic extends AsyncTask<String,Void,String>{
        OPGPanellistProfile panellistProfile;
        Exception exception;
        String picturePath;

        public UpdateProfilePic(String profilePicturePath) {
            picturePath = profilePicturePath;
            try {
                this.panellistProfile = RetriveOPGObjects.getPanellistProfile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(uploadProfilePicListener != null){
                uploadProfilePicListener.onStartUpload();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String mediaID = null;
            try
            {
                mediaID = Util.getOPGSDKInstance().uploadMediaFile(picturePath, mContext);
                panellistProfile = RetriveOPGObjects.getPanellistProfile();
                panellistProfile.setMediaID(mediaID);
                if(mediaID != null){
                    OPGUpdatePanellistProfile opgUpdatePanellistProfile = Util.getOPGSDKInstance().updatePanellistProfile(mContext,panellistProfile);
                    if(opgUpdatePanellistProfile.isSuccess()){
                        SaveOPGObjects.storePanellistProfile(panellistProfile);
                    }
                    else if(opgUpdatePanellistProfile.getStatusMessage().contains(Util.SESSION_TIME_OUT_ERROR))
                    {
                        throw new OPGException(Util.SESSION_TIME_OUT_ERROR);
                    }
                }
            }
            catch (Exception ex)
            {
                exception = ex;
                if(BuildConfig.DEBUG) {
                    Log.i(Util.TAG, ex.getMessage());
                }
            }
            return mediaID;
        }

        @Override
        protected void onPostExecute(String mediaID)
        {
            super.onPostExecute(mediaID);
            if(uploadProfilePicListener!=null){
                if(exception != null)
                    uploadProfilePicListener.onUploadCompleted(mediaID,picturePath,exception.getMessage());
                else
                    uploadProfilePicListener.onUploadCompleted(mediaID,picturePath,"");
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    public interface UploadProfilePicListener{
        public void onStartUpload();
        public void onUploadCompleted(String mediaID, String picturePath,String errorMessage);
    }

    public void logout(GoogleApiClient mGoogleApiClient,LogoutListener logoutListener)
    {
        LogoutAsyncTask asyncTask = new LogoutAsyncTask(logoutListener);
        asyncTask.execute(mGoogleApiClient);

    }
    public interface LogoutListener
    {
        void onStart();
        void onCompleted();
    }
    public class LogoutAsyncTask extends AsyncTask<GoogleApiClient,Void,Void>
    {
        OPGSDK opgsdk ;
        LogoutListener logoutListener;
        public LogoutAsyncTask( LogoutListener logoutListener)
        {
            this.opgsdk = new OPGSDK();
            this.logoutListener = logoutListener;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            logoutListener.onStart();
        }

        @Override
        protected Void doInBackground(GoogleApiClient... params) {
            if(params.length>0)
            {
                GoogleApiClient mGoogleApiClient = params[0];

                String actionBtnColor  = MySurveysPreference.getThemeActionBtnColor(mContext);
                String loginBgMediaId     = MySurveysPreference.getLoginBgMediaId(mContext);
                String headerMediaId = MySurveysPreference.getHeaderMediaId(mContext);
                long panelId = MySurveysPreference.getCurrentPanelID(mContext);
                String currentPanelName = MySurveysPreference.getCurrentPanelName(mContext);
                String logoText = MySurveysPreference.getLogoText(mContext);
                int loginType  = MySurveysPreference.getLoginType(mContext);
                this.opgsdk.logout(mContext); //SDK Logout

                stopFetchDataService();
                stopLocationServiceIfRunning();
                stopProfileImageIfUploading();
                stopGeofencing();
                try {
                    signOutFromGoogle(loginType);
                    signOutFromFacebook(loginType);
                    unregisterFromFCMNotifications();
                } catch (Exception ex) {
                    Log.i(TAG,ex.getMessage());
                }finally {
                    clearPreferences(actionBtnColor,loginBgMediaId,headerMediaId,panelId,currentPanelName,logoText);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            logoutListener.onCompleted();
        }


        private void unregisterFromFCMNotifications() {
            String response = this.opgsdk.unRegisterNotifications(mContext, FirebaseInstanceId.getInstance().getToken());
            if(BuildConfig.DEBUG) {
                Log.d("Server Response:", response);
            }
        }

        /**
         * Logout from facebook
         * @param loginType
         */
        private void signOutFromFacebook(int loginType) {
            if(loginType == LoginType.FACEBOOK.ordinal())
            {
                LoginManager.getInstance().logOut();//Facebook Logout
            }
        }

        /**
         * Logout from google+
         * @param loginType
         */
        private void signOutFromGoogle(int loginType) {
            if(loginType == LoginType.GOOGLE.ordinal()&& mGoogleApiClient != null && mGoogleApiClient.isConnected())
            {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<com.google.android.gms.common.api.Status>() {
                            @Override
                            public void onResult(com.google.android.gms.common.api.Status status) {
                                if(BuildConfig.DEBUG && status!=null){
                                    Log.i(TAG,status.getStatusMessage() !=null ? status.getStatusMessage() : "");
                                    if(status.isSuccess()) //  Use isSuccess() to determine whether the call was successful
                                    {
                                        Log.i(TAG,String.valueOf(status.getStatus().getStatusCode()));
                                    }
                                }
                            }
                        });
            }
        }

        /**
         * Stops the background service which fetch the data from the server.
         */
        private void stopFetchDataService() {
            if(Util.isServiceRunning(mContext,FetchDataService.class)){
                mContext.stopService(new Intent(mContext,FetchDataService.class));
            }
        }

        /**
         * Stops the location service if it is running in background
         */
        private void stopLocationServiceIfRunning() {
            if(Util.isServiceRunning(mContext,LocationService.class)){
                mContext.stopService(new Intent(mContext,LocationService.class));
            }
        }

        /**
         *  Stops the already running updProfileImageAsyncTask
         */
        private void stopProfileImageIfUploading() {
            try {
                if (((HomeActivity) mContext).updProfileImage != null && ((HomeActivity) mContext).updProfileImage.getStatus() != AsyncTask.Status.FINISHED) {
                    ((HomeActivity) mContext).updProfileImage.cancel(true);
                }
            }catch (Exception ex){
                Log.i(TAG,ex.getMessage());
            }
        }

        /**
         * Stop the  geofencing in the sdk if it is already started.
         */
        private void stopGeofencing() {
            try {
                this.opgsdk.stopGeofencingMonitor(mContext.getApplicationContext(),((HomeActivity)mContext).getmGoogleApiClient(), (HomeActivity)mContext);
            } catch (OPGException e) {
                Log.i(TAG,e.getMessage());
            }
        }


        private void clearPreferences(String actionBtnColor,String loginBgMediaId,String headerMediaId,long panelId,String currentPanelName,String logoText) {
            MySurveysPreference.clearPreference(mContext); //Clearing SharedPreference
            MySurveysPreference.setThemeActionBtnColor(mContext,actionBtnColor);
            MySurveysPreference.setLoginBgMediaId(mContext,loginBgMediaId);
            MySurveysPreference.setHeaderMediaId(mContext,headerMediaId);
            MySurveysPreference.setCurrentPanelID(mContext,panelId);
            MySurveysPreference.setCurrentPanelName(mContext,currentPanelName);
            MySurveysPreference.setLogoText(mContext,logoText);
            Util.clearDB("");  //Clearing DB
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll(); // clearing the pending notification
        }
    }
    public void downloadMedia(DownloadMediaListener downloadMediaListener,long mediaID)
    {
        DownloadMediaTask downloadMediaTask = new DownloadMediaTask(downloadMediaListener);
        downloadMediaTask.execute(mediaID+"");
    }
    public interface DownloadMediaListener {
        public void onStartDownload();
        public void onDownloadCompleted(String filePath);
        public void onDownloadFailed(String errorMsg);
    }
    public class DownloadMediaTask extends AsyncTask<String, Void, OPGDownloadMedia>
    {
        DownloadMediaListener downloadMediaListener;
        String mediaID = "";
        public DownloadMediaTask(DownloadMediaListener downloadMediaListener)
        {
            this.downloadMediaListener = downloadMediaListener;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            downloadMediaListener.onStartDownload();
        }

        @Override
        protected OPGDownloadMedia doInBackground(String... strings) {
            mediaID = strings[0];
            return Util.getOPGSDKInstance().downloadMediaFile(HomeActivity.this,mediaID, "PNG");
        }

        @Override
        protected void onPostExecute(OPGDownloadMedia opgDownloadMedia) {
            super.onPostExecute(opgDownloadMedia);
            if(opgDownloadMedia != null && opgDownloadMedia.getMediaPath() != null)
            {
                mediaDownloadingList.remove(mediaID);
                downloadMediaListener.onDownloadCompleted(opgDownloadMedia.getMediaPath());
            }
            else
            {
                mediaDownloadingList.remove(mediaID);
                downloadMediaListener.onDownloadFailed(getString(R.string.err_failed_to_load_profile_pic));
            }
        }
    }
    public interface OfflineScriptDownloadListener{
        public void refreshView(OPGSurvey opgSurvey);
    }

    public void setUploadProfilePicListener(UploadProfilePicListener profilePicListener){
        uploadProfilePicListener = profilePicListener;
    }

    public void setOfflineRefreshListener(OfflineScriptDownloadListener refreshListener){
        offlineRefreshListener = refreshListener;
    }

    public void startUploadPic(String filePath){
        updProfileImage = new UpdateProfilePic(filePath).execute();
    }

    public  AsyncTask<String,Void,String> getUpdProfileImage(){
        return  updProfileImage;
    }


    public void updateTheme()
    {
        toolbar.setBackgroundColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(this)));
        tabLayout.setBackgroundColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(this)));
        Util.setStatusBarColor(this);

        //Setting the header image according to applied theme
        tv_header_logo.setVisibility(View.GONE);
        ivHeaderLogo.setVisibility(View.VISIBLE);
        final String mediaID = MySurveysPreference.getHeaderMediaId(this);
        if(mediaID != null )
        {
            String filePath = Util.searchFile(mContext,mediaID, Util.THEME_PICS);
            if (filePath != null)
            {
                Bitmap imageBitmap = BitmapFactory.decodeFile(new File(filePath).getAbsolutePath());
                if (imageBitmap != null && ivHeaderLogo != null) {
                    ivHeaderLogo.setImageBitmap(imageBitmap);
                }
            } else {
                new AsyncTask<Void, Void, OPGDownloadMedia>() {
                    @Override
                    protected OPGDownloadMedia doInBackground(Void... voids) {
                        return Util.getOPGSDKInstance().downloadMediaFile(mContext, mediaID, "PNG");
                    }

                    @Override
                    protected void onPostExecute(OPGDownloadMedia opgDownloadMedia) {
                        super.onPostExecute(opgDownloadMedia);
                        if (opgDownloadMedia != null && opgDownloadMedia.isSuccess()) {
                            String filePath = opgDownloadMedia.getMediaPath();
                            filePath = Util.moveFile(mContext, filePath, mediaID, Util.THEME_PICS);
                            if (filePath != null) {
                                Bitmap imageBitmap = BitmapFactory.decodeFile(new File(filePath).getAbsolutePath());
                                if (imageBitmap != null && ivHeaderLogo != null) {
                                    ivHeaderLogo.setImageBitmap(imageBitmap);
                                }
                            }
                        }
                    }
                }.execute();
            }
        }
        else if(MySurveysPreference.getLogoText(mContext) != null)
        {
            tv_header_logo.setVisibility(View.VISIBLE);
            ivHeaderLogo.setVisibility(View.GONE);
            tv_header_logo.setText(MySurveysPreference.getLogoText(mContext));
        }
        else
        {
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.logo);
            //putting default profile image
            if(bitmap != null) {
                ivHeaderLogo.setImageBitmap(bitmap);
            }
        }
        //Downloading the login background image a
        final String mediaId = MySurveysPreference.getLoginBgMediaId(this);
        if(mediaId != null)
        {
            String filePath = Util.searchFile(mContext,mediaId,Util.THEME_PICS);
            if(filePath == null)
            {
                AsyncTask<Void,Void,OPGDownloadMedia>  asyncTask = new AsyncTask<Void,Void,OPGDownloadMedia>()
                {
                    @Override
                    protected OPGDownloadMedia doInBackground(Void... voids) {
                        return Util.getOPGSDKInstance().downloadMediaFile(mContext,mediaId,"PNG");
                    }
                    @Override
                    protected void onPostExecute(OPGDownloadMedia opgDownloadMedia) {
                        super.onPostExecute(opgDownloadMedia);
                        if(opgDownloadMedia != null && opgDownloadMedia.isSuccess())
                        {
                            Util.moveFile(mContext, opgDownloadMedia.getMediaPath(), mediaId, Util.THEME_PICS);
                        }
                    }
                };
                asyncTask.execute();
            }
        }
    }


    static public boolean deleteDirectory(Context mContext,File path) {
        if(Util.checkPermission(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) && Util.checkPermission(mContext, android.Manifest.permission.READ_EXTERNAL_STORAGE) )
        {
            if( path.exists() ) {
                File[] files = path.listFiles();
                if (files == null) {
                    return true;
                }
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(mContext,files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
            return( path.delete() );
        }
        return false;

    }

}
