package com.kaikala.sunshine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.kaikala.sunshine.data.WeatherContract;
import com.kaikala.sunshine.services.SunshineService;
import com.kaikala.sunshine.sync.SunshineSyncAdapter;

import java.util.ArrayList;

/**
 * Created by kaIkala on 8/5/2016.
 */
public class ForeCastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private ForecastAdapter mForecastAdapter;
    private static final String LOG_TAG = ForeCastFragment.class.getSimpleName();
    private static final int FORECAST_LOADER = 0;
    private ListView listView;

    private int selectedPosition = ListView.INVALID_POSITION;

    public boolean useTodayLayout;

    private static final String POSITION_KEY = "selected_position";

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;


    public interface CallBack {

        public void onItemSelected(Uri dateUri);
    }

    public ForeCastFragment() {
    }

    private void updateWeather() {
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fore_cast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_layout, container, false);

        listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSettings = Utility.getPreferredLocation(getActivity());

                    ((CallBack) getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSettings, cursor.getLong(COL_WEATHER_DATE)));
                }
                selectedPosition = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY)) {
            selectedPosition = savedInstanceState.getInt(POSITION_KEY);
        }

        mForecastAdapter.setUseTodayLayout(useTodayLayout);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (selectedPosition != ListView.INVALID_POSITION) {
            outState.putInt(POSITION_KEY, selectedPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE+ " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS,
                null,
                null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);

        if (selectedPosition != ListView.INVALID_POSITION) {
            listView.smoothScrollToPosition(selectedPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    public void setUseTodayLayout (boolean useTodayLayout) {
        this.useTodayLayout = useTodayLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(useTodayLayout);
        }
    }

    private void openPreferredLocationInMap(){

        if (null != mForecastAdapter) {
            Cursor c = mForecastAdapter.getCursor();
            if (null != c) {
                c.moveToPosition(0);
                String posLat = c.getString(COL_COORD_LAT);
                String posLong = c.getString(COL_COORD_LONG);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null){
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "couldn't call" + geoLocation.toString() + ", no receiving apps installed !");
                }
            }
        }
    }

}
