package com.ruidacosta.GPSTracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static java.text.DateFormat.getTimeInstance;

/**
 * Created by ruidacosta on 14/10/2015.
 */
public class Main extends Activity {

    private boolean running;
    private LocationManager locationManager;
    private MyLocationListener myLocationListener;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.running = false;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationManager.addGpsStatusListener(new GpsStatus.Listener(){

            @Override
            public void onGpsStatusChanged(int event) {
                TextView tmp = (TextView) findViewById(R.id.gps_status_text);
                switch (event) {
                    case GpsStatus.GPS_EVENT_STARTED:
                        tmp.setText("GPS Started");
                        break;
                    case GpsStatus.GPS_EVENT_STOPPED:
                        tmp.setText("GPS Stoped");
                        break;
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        tmp.setText("GPS First Fix");
                        break;
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        tmp.setText("GPS Satellite status");
                        break;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.gps_conf_item:
                intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                return true;
            case R.id.car_motorbike_item:
                changeCarMotoProfile();
                return true;
            case R.id.bike_item:
                changeBikeProfile();
                return true;
            case R.id.run_item:
                changeRunProfile();
                return true;
            case R.id.walk_item:
                changeWalkProfile();
                return true;
            case R.id.last_track_item:
                intent = new Intent(this,LastTracks.class);
                startActivity(intent);
                return true;
            case R.id.settings_item:
                intent = new Intent(this, AppPreferences.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onStartStopClick(View view) {
        ImageView image = (ImageView) findViewById(R.id.button_start_stop);
        TextView tmp = (TextView)findViewById(R.id.start_at_text);

        if (running) { // stop
            image.setImageResource(R.drawable.ic_rec_circle_outline_red);
            this.running = false;
            //stopService(new Intent(this,GPSCollectorService.class));
            tmp.setText(R.string.start_at_not_started);
            locationManager.removeUpdates(this.myLocationListener);
        }
        else { //start
            image.setImageResource(R.drawable.ic_pause_circle_outline_red);
            this.running = true;
            tmp.setText(new SimpleDateFormat("HH:mm:ss",Locale.getDefault()).format(new Date()));
            initiateRoute();
        }
    }

    private void changeCarMotoProfile() {
        ImageView image = (ImageView) findViewById(R.id.profile_image);
        image.setImageResource(R.drawable.ic_directions_car_white_48dp);

        TextView text = (TextView) findViewById(R.id.profile_text);
        text.setText(R.string.car_motorbike);
    }

    private void changeBikeProfile() {
        ImageView image = (ImageView) findViewById(R.id.profile_image);
        image.setImageResource(R.drawable.ic_directions_bike_white_48dp);

        TextView text = (TextView) findViewById(R.id.profile_text);
        text.setText(R.string.bike);
    }

    private void changeRunProfile() {
        ImageView image = (ImageView) findViewById(R.id.profile_image);
        image.setImageResource(R.drawable.ic_directions_run_white_48dp);

        TextView text = (TextView) findViewById(R.id.profile_text);
        text.setText(R.string.run);
    }

    private void changeWalkProfile() {
        ImageView image = (ImageView) findViewById(R.id.profile_image);
        image.setImageResource(R.drawable.ic_directions_walk_white_48dp);

        TextView text = (TextView) findViewById(R.id.profile_text);
        text.setText(R.string.walk);
    }

    private void initiateRoute() {
        // insert new route
        final DatabaseConnector db = new DatabaseConnector(this);
        db.open();
        final long route_id = db.insertRoute("Route_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        db.close();

        // start service
        //Intent newIntent = new Intent(this,GPSCollectorService.class);
        //newIntent.putExtra(DatabaseConnector.COLUMN_ROUTES_NAME, route_id);
        TextView text = (TextView) findViewById(R.id.profile_text);
        //newIntent.putExtra("PROFILE", text.getText());
        //startService(new Intent(this,GPSCollectorService.class));

        String tmp = (String) text.getText();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int prefProfile = 10;
        if (tmp.equals(R.string.car_motorbike)) {
            prefProfile = sharedPreferences.getInt("pref_car_moto_profile", 10);
        } else if (tmp.equals(R.string.bike)) {
            prefProfile = sharedPreferences.getInt("", 10);
        } else if (tmp.equals(R.string.walk)) {
            prefProfile = sharedPreferences.getInt("", 10);
        } else if (tmp.equals(R.string.run)) {
            prefProfile = sharedPreferences.getInt("", 10);
        }

        myLocationListener = new MyLocationListener(db,route_id);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, prefProfile * 1000, 100, this.myLocationListener);
    }

    class MyLocationListener implements LocationListener {
        private int count = 0;
        private long current_route_id;
        private DatabaseConnector db;

        public MyLocationListener(DatabaseConnector database, long route_id) {
            this.current_route_id = route_id;
            this.db = database;
        }

        @Override
        public void onLocationChanged(Location location) {
            db.insertPoint(current_route_id,location.getLatitude(),location.getLongitude(),location.getAltitude(),location.getAccuracy(),location.getBearing(),location.getSpeed());
            Toast.makeText(getApplicationContext(), "Count." + count + "lat." + location.getLatitude() + "\nlong." + location.getLongitude() + "\nalt." + location.getAltitude(), Toast.LENGTH_LONG).show();
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
            Toast.makeText(getApplicationContext(),"GPS disaled.",Toast.LENGTH_LONG).show();
        }
    }
}
