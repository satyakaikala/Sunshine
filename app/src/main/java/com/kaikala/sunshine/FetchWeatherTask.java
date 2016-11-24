package com.kaikala.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * Created by kaIkala on 9/23/2016.
 */

public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    
    private final Context mcontext;

    public FetchWeatherTask(Context context) {
        this.mcontext = context;
    }

    private String getReadableDateString(long time){
        SimpleDateFormat dateFormate = new SimpleDateFormat("EEE MMM dd");
        return dateFormate.format(time);
    }

    private String formateHighLows(double high, double low, String unitType){

        if(unitType.equals(getString(R.string.pref_units_imperial))){
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        } else if (!unitType.equals(getString(R.string.pref_units_metric))){
            Log.d(LOG_TAG, "unit type not found:" + unitType);
        }
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowString = roundedHigh + "/" + roundedLow;
        return  highLowString;
    }

    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) throws JSONException {

        // these are the names of the JSON object that need to be extracted.

        final String OWM_LIST = "list";
        final  String OWM_WEATHER = "weather";
        final  String OWM_TEMPERATURE = "temp";
        final  String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final  String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        android.text.format.Time dayTime = new android.text.format.Time();
        dayTime.setToNow();

        int julianStartDay = android.text.format.Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        dayTime = new android.text.format.Time();

        String[] resultStrs = new String[numDays];

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String unitType = sharedPreferences.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_metric));
        for (int i = 0; i < weatherArray.length(); i++){
            String day;
            String description;
            String highAndLow;

            JSONObject dayForecast = weatherArray.getJSONObject(i);

            long dateTime;

            dateTime = dayTime.setJulianDay(julianStartDay + i);
            day = getReadableDateString(dateTime);

            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);


            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formateHighLows(high, low, unitType);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }
        for (String s : resultStrs){
            Log.v(LOG_TAG, "Forecast entry :" + s);
        }
        return  resultStrs;
    }

    @Override
    protected String[] doInBackground(String... params) {


        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String forecastJson = null;

        String format = "json";
        String units = "metric";
        int numDays = 7;
        try {
            //construct the url for the openweathermap query

            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORAMT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APPID_PARAM = "APPID";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, params[0])
                    .appendQueryParameter(FORAMT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                    .build();
            URL url = new URL(builtUri.toString());
            Log.v(LOG_TAG, "BUild uri : " + builtUri.toString());

            //create the request to openweathermap and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //read the input stream into string
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                //noting to do
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return  null;
            }
            forecastJson = buffer.toString();

            Log.v(LOG_TAG, "Forecast data :" + forecastJson);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
            return  null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return getWeatherDataFromJson(forecastJson, numDays);
        } catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] strings) {
        if (strings != null){
            mForecastAdapter.clear();
            for (String dayForecastStr : strings){
                mForecastAdapter.add(dayForecastStr);
            }
        }
    }
}
