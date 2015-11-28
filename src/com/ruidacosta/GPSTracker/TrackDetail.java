package com.ruidacosta.GPSTracker;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ruidacosta on 15/10/2015.
 */
public class TrackDetail extends Activity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_detail);
        Bundle extras = getIntent().getExtras();
        DatabaseConnector db = new DatabaseConnector(this);
        db.open();
        try {
            long route_id = extras.getLong("id");
            Log.e("rout_id", Long.toString(route_id));

            TextView title = (TextView) findViewById(R.id.detail_route_name);
            title.setText(db.getRouteById(route_id));

            TextView detail_points = (TextView) findViewById(R.id.detail_route_points);
            detail_points.setText(db.getPointsByRouteId(route_id));
            db.close();
        } catch (Exception e) {
            String tmp = "";
            Cursor cursor = db.getRoutes();
            while (cursor.moveToNext()) {
                tmp += cursor.getString(cursor.getColumnIndex("_id"));
                tmp += ", ";
                tmp += cursor.getString(cursor.getColumnIndex(DatabaseConnector.COLUMN_ROUTES_NAME));
                tmp += "\n";
            }
            TextView det = (TextView)findViewById(R.id.detail_route_points);
            det.setText(tmp);
            Toast.makeText(this,"Some error happen :-(",Toast.LENGTH_LONG).show();
            Log.e("Error",e.getMessage());
        }
    }
}
