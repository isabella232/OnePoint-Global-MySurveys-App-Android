package com.opg.my.surveys.lite.Fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;

import com.google.android.gms.location.LocationListener;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.opg.my.surveys.lite.DividerItemDecoration;
import com.opg.my.surveys.lite.HomeActivity;
import com.opg.my.surveys.lite.MySurveys;
import com.opg.my.surveys.lite.R;
import com.opg.my.surveys.lite.SurveyDetailActivity;
import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;
import com.opg.my.surveys.lite.common.db.RetriveOPGObjects;
import com.opg.sdk.models.OPGGeofenceSurvey;
import com.opg.sdk.models.OPGSurvey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.opg.my.surveys.lite.common.Util.DOWNLOAD_STATUS_KEY;
import static com.opg.my.surveys.lite.common.Util.NEW_STATUS_KEY;
import static com.opg.my.surveys.lite.common.Util.REQUEST_CODE_LOCATION;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class SurveyByLocationFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, LocationListener, GoogleMap.OnInfoWindowClickListener {

    private static final int PERMISSION_LOCATION_REQUEST_CODE = 120;

    private static SurveyByLocationFragment fragment;
    private static View view;


    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private HashMap<String, OPGGeofenceSurvey> markerTags;
    private RelativeLayout mainLayout;
    private RecyclerView offlineRecyclerView;

    private OfflineSurveyAdapter surveysAdapter;
    private Dialog geofenceAlert;
    private List<OPGGeofenceSurvey> notificationSurveys;
    private LinearLayout mapFrame;
    private List<OPGSurvey> panelGeofenceSurveys;
    private boolean isMapSet = false;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (MySurveysPreference.getIsGeofencingEnabled(getActivity())) {
                try {
                    if (intent.getAction().equalsIgnoreCase(Util.BROADCAST_ACTION_GEOFENCES_UPDATED)) {
                        plotSurveys();
                        updateMainViews();
                    } else if (intent.getAction().equalsIgnoreCase(Util.BROADCAST_GEOFENCE_TRANSITION_ENTER) ||
                            intent.getAction().equalsIgnoreCase(Util.BROADCAST_GEOFENCE_TRANSITION_EXIT))
                    {
                        List<OPGGeofenceSurvey> newSurveys = Util.convertStringToOPGGeofenceList(intent.getStringExtra("triggeredGeofences"));

                        for(OPGGeofenceSurvey survey : newSurveys)
                        {
                            if(intent.getAction().equalsIgnoreCase(Util.BROADCAST_GEOFENCE_TRANSITION_ENTER) && survey.isEnter())
                            {
                                notificationSurveys.add(survey);
                            }else if(intent.getAction().equalsIgnoreCase(Util.BROADCAST_GEOFENCE_TRANSITION_EXIT) && survey.isExit())
                            {
                                notificationSurveys.add(survey);
                            }
                        }
                        showSurveyMessageDialog();
                        plotSurveys();
                        updateMainViews();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };


    public SurveyByLocationFragment() {
        notificationSurveys = new ArrayList<>();
    }

    public static SurveyByLocationFragment getInstance() {
        if (fragment == null) {
            fragment = new SurveyByLocationFragment();
        }
        return fragment;
    }

    public static boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = ((HomeActivity) getActivity()).getmGoogleApiClient();
        createLocationRequest();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            isMapSet = false;
            view = inflater.inflate(R.layout.fragment_survey_by_location, container, false);
        } catch (InflateException e) {
            Log.i(Util.TAG, e.getMessage());
        } catch (Exception ex) {
            Log.i(Util.TAG, ex.getMessage());
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            mapFrame = (LinearLayout) view.findViewById(R.id.mapFrame);
            ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment)).getMapAsync(this);
            mainLayout = (RelativeLayout) view.findViewById(R.id.main_layout);
            offlineRecyclerView = (RecyclerView) view.findViewById(R.id.offlineRecyclerView);
            markerTags = new HashMap<>();
            panelGeofenceSurveys = new ArrayList<>();
            panelGeofenceSurveys = RetriveOPGObjects.getGeofencingSurveys(MySurveysPreference.getCurrentPanelID(getActivity()));
            setRecyclerViewWithAdapter();
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
            }
            try {
                surveysAdapter.swap(RetriveOPGObjects.getOPGGeofenceSurveys());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (InflateException e) {
            Log.i(Util.TAG, e.getMessage());
        } catch (Exception ex) {
            Log.i(Util.TAG, ex.getMessage());
        }
    }

    private void setRecyclerViewWithAdapter() {
        surveysAdapter = new OfflineSurveyAdapter(panelGeofenceSurveys);
        offlineRecyclerView.setAdapter(surveysAdapter);
        offlineRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        offlineRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), R.drawable.divider));
    }

    private OPGSurvey getSurveyByID(long surveyID) {
        OPGSurvey opgSurvey = null;
        for (OPGSurvey survey : panelGeofenceSurveys) {
            if (survey.getSurveyID() == surveyID) {
                opgSurvey = survey;
            }
        }
        return opgSurvey;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            updateMainViews();
            checkLocationServices(true);
            animateCamera();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        //stop location updates
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMap = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter iff = new IntentFilter();
        iff.addAction(Util.BROADCAST_ACTION_GEOFENCES_UPDATED);
        iff.addAction(Util.BROADCAST_GEOFENCE_TRANSITION_DWELL);
        iff.addAction(Util.BROADCAST_GEOFENCE_TRANSITION_ENTER);
        iff.addAction(Util.BROADCAST_GEOFENCE_TRANSITION_EXIT);
        getActivity().registerReceiver(mReceiver, iff);
        if( mMap!=null && !isMapSet){
            setupMap();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        checkLocationServices(false);
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMap();
    }

    private void setupMap() {
        if (Build.VERSION.SDK_INT < 23 || (Build.VERSION.SDK_INT >= 23 && checkPermission(getContext()))) {
            isMapSet = true;
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            try {
                plotSurveys();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION_REQUEST_CODE);
        }
    }

    private void createLocationRequest() {
        if (mGoogleApiClient != null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(Util.INTERVAL);
            mLocationRequest.setFastestInterval(Util.FAST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        //move map camera
        animateCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_LOCATION_REQUEST_CODE) {
            setupMap();
        }
    }

    public void plotSurveys() throws Exception {
        if (mMap != null) {
            mMap.clear();
            markerTags.clear();
            if (MySurveysPreference.getIsGeofencingEnabled(getActivity())) {
                if (RetriveOPGObjects.getOPGGeofenceSurveys() != null && RetriveOPGObjects.getOPGGeofenceSurveys().size() > 0
                        && surveysAdapter != null) {
                    surveysAdapter.swap(RetriveOPGObjects.getOPGGeofenceSurveys());
                }
                mMap.setOnInfoWindowClickListener(this);
                StringBuilder stringBuilder = new StringBuilder();
                List<OPGGeofenceSurvey> opgGeofenceSurveys = RetriveOPGObjects.getOPGGeofenceSurveys();
                for (OPGGeofenceSurvey entry : opgGeofenceSurveys) {
                    stringBuilder.setLength(0);
                    try {
                        stringBuilder.append(RetriveOPGObjects.getSurvey(entry.getSurveyID()).getName());
                        stringBuilder.append("\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    stringBuilder.append("Geocode: ").append(entry.getGeocode());
                    LatLng surveyLatLng = new LatLng(entry.getLatitude(), entry.getLongitude());

                    CircleOptions circleOptions = new CircleOptions()
                            .center(surveyLatLng)
                            .radius(entry.getRange())
                            .fillColor(Color.alpha(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity()))))
                            .strokeColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity())))
                            .strokeWidth(2);
                    mMap.addCircle(circleOptions);
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(surveyLatLng)
                            .title(entry.getAddress()).snippet(getString(R.string.welcome_message_survey)));
                    markerTags.put(marker.getId(), entry);
                }
                animateCamera();
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        checkLocationServices(isVisibleToUser);
    }

    private void checkLocationServices(boolean startLocationUpdates) {
        if (notificationSurveys != null) {
            notificationSurveys.clear();
        } else {
            notificationSurveys = new ArrayList<>();
        }
        if (startLocationUpdates) {
            startLocationUpdates();
        } else {
            stopLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (MySurveysPreference.getIsGeofencingEnabled(getActivity())) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                try {
                    plotSurveys();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast toast = Toast.makeText(getActivity(), getString(R.string.enable_geo_location), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }

    public void animateCamera() {
        if (mMap != null && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))      // Sets the center of the map to location user
                        .zoom(Util.CAMERA_ZOOM_VALUE)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(0)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }

    private void updateMainViews() throws Exception {
        if (MySurveysPreference.getIsGeofencingEnabled(getActivity()) && panelGeofenceSurveys.size() > 0) {
            float inPixels= getActivity().getResources().getDimension(R.dimen.survey_item_height);
            offlineRecyclerView.setVisibility(View.VISIBLE);
            if (panelGeofenceSurveys.size() >= 2) {
                if((panelGeofenceSurveys.size() > 2 && !getActivity().getResources().getBoolean(R.bool.portrait_only))){
                    inPixels = 3 * inPixels;
                }else{
                    inPixels = 2 * inPixels;
                }
            }
            ViewGroup.LayoutParams params = offlineRecyclerView.getLayoutParams();
            params.height = (int)inPixels;
            offlineRecyclerView.requestLayout();
        } else {
            offlineRecyclerView.setVisibility(View.GONE);
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
    }

    public boolean checkSurveyIsPanelSurvey(OPGGeofenceSurvey survey) {
        for (OPGSurvey opgSurvey : panelGeofenceSurveys) {
            if (opgSurvey.getSurveyID() == survey.getSurveyID()) {
                return true;
            }
        }
        return false;
    }

    public void showSurveyMessageDialog() {
        if (geofenceAlert != null && geofenceAlert.isShowing()) {
            return;
        }
        if (notificationSurveys.size() > 0 && getUserVisibleHint())
        {
            final OPGGeofenceSurvey opgGeofenceSurvey = notificationSurveys.get(0);
            if (checkSurveyIsPanelSurvey(opgGeofenceSurvey))
            {
                try {
                    //OPGSurvey survey = RetriveOPGObjects.getSurvey(opgGeofenceSurvey.getSurveyID());
                    notificationSurveys.remove(opgGeofenceSurvey);
                    geofenceAlert = new Dialog(getActivity());
                    geofenceAlert.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    geofenceAlert.setContentView(R.layout.dialog_logout);

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(opgGeofenceSurvey.getSurveyName()).append("\n\n");
                    if(opgGeofenceSurvey.isEnter())
                    {

                        stringBuilder.append(getString(R.string.enter_survey_location))./*append(survey.getName()).*/append("\n\n").append(opgGeofenceSurvey.getAddress());
                        stringBuilder.append(getString(R.string.geofence_noti_msg));
                    }
                    else if(opgGeofenceSurvey.isExit())
                    {
                        stringBuilder.append(getString(R.string.thank_you_for_visiting)).append(" ").append(opgGeofenceSurvey.getAddress()).append("\n");
                        stringBuilder.append(getString(R.string.survey_available));
                    }


                    ((TextView) geofenceAlert.findViewById(R.id.tv_title_logout_dialog)).setText(stringBuilder.toString());
                    Button btncancel = (Button) geofenceAlert.findViewById(R.id.btn_cancel_logout);
                    Button btnTakeSurvey = (Button) geofenceAlert.findViewById(R.id.btn_confirm_logout);
                    int colorCode = Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity()));
                    btncancel.setTextColor(colorCode);
                    btnTakeSurvey.setTextColor(colorCode);
                    btnTakeSurvey.setText(getString(R.string.take_survey));
                    btncancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            geofenceAlert.dismiss();
                            showSurveyMessageDialog();
                        }
                    });
                    btnTakeSurvey.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            geofenceAlert.dismiss();
                            OPGSurvey opgSurvey = getSurveyByID(opgGeofenceSurvey.getSurveyID());
                            if (opgSurvey != null) {
                                startSurveyDetailActivity(opgSurvey);
                            } else {
                                Toast.makeText(getActivity(), getString(R.string.geofence_survey_panel_error), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    geofenceAlert.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void startSurveyDetailActivity(OPGSurvey opgGeofenceSurvey) {
        try {
            Intent intent = new Intent(getActivity(), SurveyDetailActivity.class);
            intent.putExtra(Util.OPGSURVEY_KEY, opgGeofenceSurvey);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (markerTags.size() > 0 && markerTags.containsKey(marker.getId())) {
            OPGGeofenceSurvey opgGeofenceSurvey = markerTags.get(marker.getId());
            if (checkSurveyIsPanelSurvey(opgGeofenceSurvey) && opgGeofenceSurvey.isDeleted()) {
                OPGSurvey survey = getSurveyByID(opgGeofenceSurvey.getSurveyID());
                if (survey != null) {
                    startSurveyDetailActivity(survey);
                }
            } else {
                if (!checkSurveyIsPanelSurvey(opgGeofenceSurvey))
                    Toast.makeText(getActivity(), getString(R.string.geofence_survey_panel_error), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), getString(R.string.survey_range_error), Toast.LENGTH_SHORT).show();

            }
        }
    }


    private class OfflineSurveyAdapter extends RecyclerView.Adapter<OfflineSurveyAdapter.SurveyViewHolder> {

        List<OPGSurvey> opgSurveys;
        List<Long> enabledSurveys;

        OfflineSurveyAdapter(List<OPGSurvey> opgSurveys) {
            this.opgSurveys = opgSurveys;
            enabledSurveys = new ArrayList<>();
        }

        @Override
        public SurveyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_survey, parent,
                    false);
            return new OfflineSurveyAdapter.SurveyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(SurveyViewHolder holder, int position) {
            final OPGSurvey opgSurvey = opgSurveys.get(position);
            try {
                setViewFont(holder);
                if(opgSurvey.getStatus().equalsIgnoreCase(DOWNLOAD_STATUS_KEY)
                        && (MySurveysPreference.isScriptDataPresent(getActivity(),String.valueOf(opgSurvey.getSurveyID())))) {
                    opgSurvey.setStatus(NEW_STATUS_KEY);
                }

                holder.surveyTitle.setText(opgSurvey.getName());
                setSurveyStatus(holder,opgSurvey);

                int surveyDetailDrawable = enabledSurveys.contains(opgSurvey.getSurveyID()) ? R.drawable.roundedbtn_active:R.drawable.roundedbtn_inactive;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    holder.surveyDetail.setBackground(Util.getDrawable(getActivity(),surveyDetailDrawable));
                }else{
                    holder.surveyDetail.setBackgroundDrawable(Util.getDrawable(getActivity(),surveyDetailDrawable));
                }

                int surveyDetailColor = enabledSurveys.contains(opgSurvey.getSurveyID()) ? Color.parseColor(MySurveysPreference.getThemeActionBtnColor(getActivity()))
                        : ContextCompat.getColor(getActivity(), android.R.color.darker_gray);

                Drawable background = holder.surveyDetail.getBackground();
                {
                    // cast to 'GradientDrawable'
                    GradientDrawable gradientDrawable = (GradientDrawable) background;
                    gradientDrawable.setColor(surveyDetailColor);
                }
                holder.surveyDetail.setEnabled(enabledSurveys.contains(opgSurvey.getSurveyID()));
                holder.itemView.setEnabled(enabledSurveys.contains(opgSurvey.getSurveyID()));
                holder.surveyDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), SurveyDetailActivity.class);
                        intent.putExtra(Util.OPGSURVEY_KEY, opgSurvey);
                        startActivity(intent);
                    }
                });
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), SurveyDetailActivity.class);
                        intent.putExtra(Util.OPGSURVEY_KEY, opgSurvey);
                        startActivity(intent);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setViewFont(SurveyViewHolder holder) {
            Util.setTypeface(getActivity(), holder.surveyTitle, "font/roboto_regular.ttf");
            Util.setTypeface(getActivity(), holder.surveyStatus, "font/roboto_regular.ttf");

        }

        @Override
        public int getItemCount() {
            return opgSurveys.size();
        }

        public void swap(List<OPGGeofenceSurvey> opgSurveyList) {
            List<Long> geofenceEnabledSurveys = new ArrayList<>();
            for (OPGGeofenceSurvey opgGeofenceSurvey : opgSurveyList) {
                if (!geofenceEnabledSurveys.contains(opgGeofenceSurvey.getSurveyID())) {
                    if (opgGeofenceSurvey.isDeleted()) {
                        geofenceEnabledSurveys.add(opgGeofenceSurvey.getSurveyID());
                    }
                }
            }
            enabledSurveys = geofenceEnabledSurveys;
            notifyDataSetChanged();
        }

        private void setSurveyStatus(SurveyByLocationFragment.OfflineSurveyAdapter.SurveyViewHolder holder, OPGSurvey opgSurvey) {
            try {
                if (opgSurvey.getStatus() != null) {
                    if (opgSurvey.getStatus().equals(Util.COMPLETED_STATUS_KEY)) {
                        holder.surveyStatus.setText(getString(R.string.survey_status_completed));
                        holder.surveyStatus.setTextColor(getResources().getColor(R.color.sub_title_text_color));
                    } else if (opgSurvey.getStatus().equals(Util.PENDING_STATUS_KEY)) {
                        holder.surveyStatus.setText(getString(R.string.survey_status_pending));
                        holder.surveyStatus.setTextColor(getResources().getColor(R.color.sub_title_text_color));
                    } else if (opgSurvey.getStatus().equals(Util.NEW_STATUS_KEY)) {
                        holder.surveyStatus.setText(getString(R.string.survey_status_new));
                        holder.surveyStatus.setTextColor(getResources().getColor(R.color.sub_title_text_color));
                    }
                }
            } catch (Exception ex) {
                Log.i(Util.TAG, ex.getMessage());
            }

        }

        class SurveyViewHolder extends RecyclerView.ViewHolder {

            TextView surveyTitle;
            ImageView surveyDetail;
            TextView surveyStatus;
            View itemView;
            ProgressBar progressBar;

            SurveyViewHolder(View itemView) {
                super(itemView);
                surveyTitle = (TextView) itemView.findViewById(R.id.survey_title_tv);
                surveyDetail = (ImageView) itemView.findViewById(R.id.iv_sur_det);
                surveyStatus = (TextView) itemView.findViewById(R.id.survey_status_tv);
                this.itemView = itemView;
                progressBar = (ProgressBar) itemView.findViewById(R.id.survey_progress_bar);
            }
        }
    }
}
