package com.kaikala.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.kaikala.sunshine.sync.SunshineSyncAdapter;

public class MainActivity extends AppCompatActivity implements ForeCastFragment.CallBack{

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private final String DETAIL_FRAGMENT_TAG = "detail_fragment_tag";
    private String location;
    private boolean isTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        location = Utility.getPreferredLocation(this);
        if (findViewById(R.id.weather_detail_container) != null) {
            // if detail container view will be present only in large-screen layouts i.e res/layout-sw600dp for tablets
            isTablet = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container, new DetailFragment(), DETAIL_FRAGMENT_TAG).commit();
            }
        } else {
            isTablet = false;
            getSupportActionBar().setElevation(0f);
        }

        ForeCastFragment foreCastFragment = ((ForeCastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast));
        foreCastFragment.setUseTodayLayout(!isTablet);

        SunshineSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings){
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String newlocation = Utility.getPreferredLocation(this);

        if (newlocation != null && !newlocation.equals(location)) {
            ForeCastFragment foreCastFragment = (ForeCastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if (null != foreCastFragment) {
                foreCastFragment.onLocationChanged();
            }

            DetailFragment detailFragment = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if (detailFragment != null) {
                detailFragment.onLocationChanged(location);
            }
            newlocation = location;
        }
    }

    @Override
    public void onItemSelected(Uri dateUri) {

        if (isTablet) {
            Bundle args = new Bundle();

            args.putParcelable(DetailFragment.DETAIL_URI, dateUri);

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container, detailFragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class).setData(dateUri);
            startActivity(intent);
        }
    }
}
