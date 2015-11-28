package com.ruidacosta.GPSTracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by bubum on 31/10/2015.
 */
public class GPSCollectorService extends Service implements LocationListener {
    private LocationManager locationManager;
    private DatabaseConnector db;
    private long current_route;
    private int count=1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        this.db = new DatabaseConnector(this);
        this.db.open();
        Toast.makeText(this,"Service created",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
        Toast.makeText(this,"Service destroy",Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        if (intent == null) {
            Bundle extras = intent.getExtras();
            this.current_route = extras.getLong(DatabaseConnector.COLUMN_ROUTES_NAME);
            String profile = extras.getString("PROFILE");

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            int prefProfile = 10;
            if (profile.equals(R.string.car_motorbike)) {
                prefProfile = sharedPreferences.getInt("pref_car_moto_profile", 10);
            } else if (profile.equals(R.string.bike)) {
                prefProfile = sharedPreferences.getInt("", 10);
            } else if (profile.equals(R.string.walk)) {
                prefProfile = sharedPreferences.getInt("", 10);
            } else if (profile.equals(R.string.run)) {
                prefProfile = sharedPreferences.getInt("", 10);
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, prefProfile * 1000, 10, this);
        }
        Toast.makeText(this,"Service started.",Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags,startid);
    }

    @Override
    public void onLocationChanged(Location location) {
        db.insertPoint(this.current_route,location.getLatitude(),location.getLongitude(),location.getAltitude(),location.getAccuracy(),location.getBearing(),location.getSpeed());
        Toast.makeText(this, "Count." + count + "lat." + location.getLatitude() + "\nlong." + location.getLongitude() + "\nalt." + location.getAltitude(), Toast.LENGTH_LONG).show();
        count++;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(getBaseContext(),"GPS turned OFF ", Toast.LENGTH_LONG).show();
        stopSelf();
    }
}
