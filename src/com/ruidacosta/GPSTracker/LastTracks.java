package com.ruidacosta.GPSTracker;

import android.app.Activity;
import android.app.IntentService;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

/**
 * Created by ruidacosta on 14/10/2015.
 */
public class LastTracks extends ListActivity implements AdapterView.OnItemClickListener {

    private DatabaseConnector db;
    private ListView list;
    private long[] ids;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.last_tracks);

        db = new DatabaseConnector(this);
        db.open();

        fillData();

        list = (ListView)findViewById(android.R.id.list);
        list.setOnItemClickListener(this);
    }

    protected void fillData() {
        Cursor cursor = db.getRoutes();
        startManagingCursor(cursor);

        String[] from = new String[]{DatabaseConnector.COLUMN_ROUTES_NAME};
        int[] to = new int[]{R.id.route_label};

        CustomCursorAdapter items = new CustomCursorAdapter(this,R.layout.list_route_item,cursor,from,to);
        setListAdapter(items);
    }

    public class CustomCursorAdapter extends SimpleCursorAdapter {
        private int layout;
        Context context;

        public CustomCursorAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
            super(context,layout,cursor,from,to);
            this.layout = layout;
            this.context = context;
        }

        @Override
        public void bindView(View view,Context context,Cursor cursor) {
            ViewHolder holder;

            if (view != null) {
                holder = new ViewHolder();
                holder.viewName = (TextView) view.findViewById(R.id.route_label);

                view.setTag(holder);
            } else {
                holder = (ViewHolder)view.getTag();
            }

            String name = cursor.getString(cursor.getColumnIndex(DatabaseConnector.COLUMN_ROUTES_NAME));
            long id = cursor.getLong(cursor.getColumnIndex("_id"));

            if (holder.viewName != null) {
                holder.viewName.setText(name);
                holder.id = id;
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            LayoutInflater inflater = LayoutInflater.from(context);
            final View view = inflater.inflate(layout, parent, false);

            return view;
        }

        @Override
        public long getItemId(int id){
            return id;
        }

        @Override
        public Object getItem(int position){
            return position;
        }
    }

    static class ViewHolder {
        long id;
        TextView viewName;
    }

    public void onItemClick(AdapterView parent, View view, int position, long rowId) {

        ViewHolder holder = (ViewHolder) view.getTag();

        Log.e("ViewID", Long.toString(holder.id));
        Intent intent = new Intent(this,TrackDetail.class);
        intent.putExtra("id", holder.id);
        startActivity(intent);
    }


}