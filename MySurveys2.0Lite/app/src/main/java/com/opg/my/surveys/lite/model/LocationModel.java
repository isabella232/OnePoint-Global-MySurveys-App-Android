package com.opg.my.surveys.lite.model;

import android.location.Location;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Dinesh-opg on 12/26/2017.
 */

public class LocationModel implements Parcelable {

    float accuracy;
    double altitude;
    float bearing;
    String provider;
    double latitude;
    double longitude;
    float speed;
    long time;
    long elapsedRealTime;

    public LocationModel() {
    }

    public void saveLocation(Location location) throws Exception{
        //LocationModel locationModel = new LocationModel();
        this.setAccuracy(location.getAccuracy());
        this.setAltitude(location.getAltitude());
        this.setBearing(location.getBearing());
        this.setProvider(location.getProvider());
        this.setLatitude(location.getLatitude());
        this.setLongitude(location.getLongitude());
        this.setSpeed(location.getSpeed());
        this.setTime(location.getTime());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            this.setElapsedRealTime(location.getElapsedRealtimeNanos());
        }
       // return locationModel;
    }

    public Location retriveLocation() throws Exception{
        Location location = new Location(this.getProvider());
        location.setAccuracy(this.getAccuracy());
        location.setAltitude(this.getAltitude());
        location.setBearing(this.getBearing());
        location.setLatitude(this.getLatitude());
        location.setLongitude(this.getLongitude());
        location.setSpeed(this.getSpeed());
        location.setTime(this.getTime());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            location.setElapsedRealtimeNanos(this.getElapsedRealTime());
        }
        return location;
    }

    protected LocationModel(Parcel in) {
        accuracy = in.readFloat();
        altitude = in.readDouble();
        bearing = in.readFloat();
        provider = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        speed = in.readFloat();
        time = in.readLong();
        elapsedRealTime = in.readLong();
    }

    public static final Creator<LocationModel> CREATOR = new Creator<LocationModel>() {
        @Override
        public LocationModel createFromParcel(Parcel in) {
            return new LocationModel(in);
        }

        @Override
        public LocationModel[] newArray(int size) {
            return new LocationModel[size];
        }
    };

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getElapsedRealTime() {
        return elapsedRealTime;
    }

    public void setElapsedRealTime(long elapsedRealTime) {
        this.elapsedRealTime = elapsedRealTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(accuracy);
        parcel.writeDouble(altitude);
        parcel.writeFloat(bearing);
        parcel.writeString(provider);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeFloat(speed);
        parcel.writeLong(time);
        parcel.writeLong(elapsedRealTime);
    }


}
