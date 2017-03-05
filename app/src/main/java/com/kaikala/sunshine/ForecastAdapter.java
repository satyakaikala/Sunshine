package com.kaikala.sunshine;

/**
 * Created by skai0001 on 1/3/17.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    private boolean useTodayLayout = true;

    private Cursor cursor;
    private Context context;
    public ForecastAdapter(Context context) {
        this.context = context;
    }

    public static class ForecastAdapterViewHolder extends RecyclerView.ViewHolder{
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ForecastAdapterViewHolder (View v) {
            super(v);
            iconView = (ImageView) v.findViewById(R.id.list_item_icon);
            dateView = (TextView) v.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) v.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) v.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) v.findViewById(R.id.list_item_low_textview);

        }
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(context);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor

        String highAndLow = formatHighLows(
                cursor.getDouble(ForeCastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForeCastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForeCastFragment.COL_WEATHER_MAX_TEMP)) +
                " - " + cursor.getString(ForeCastFragment.COL_WEATHER_MIN_TEMP) +
                " - " + highAndLow;
    }


    public void setUseTodayLayout(boolean useTodayLayout) {
        this.useTodayLayout = useTodayLayout;
    }

    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if ( viewGroup instanceof RecyclerView ) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_TODAY: {
                    layoutId = R.layout.list_item_forecast_today;
                    break;
                }
                case VIEW_TYPE_FUTURE_DAY: {
                    layoutId = R.layout.list_item_forecast;
                    break;
                }
            }
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            return new ForecastAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder viewHolder, int position) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        cursor.moveToPosition(position);
        int weatherId = cursor.getInt(ForeCastFragment.COL_WEATHER_CONDITION_ID);
        int defaultImage;

        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                defaultImage = Utility.getArtResourceForWeatherCondition(cursor.getInt(ForeCastFragment.COL_WEATHER_CONDITION_ID));
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                defaultImage = Utility.getIconResourceForWeatherCondition(cursor.getInt(ForeCastFragment.COL_WEATHER_CONDITION_ID));
                break;
            }
        }

        // Read date from cursor
        long dateInMillis = cursor.getLong(ForeCastFragment.COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

        // Read weather forecast from cursor
        String description = cursor.getString(ForeCastFragment.COL_WEATHER_DESC);
        // Find TextView and set weather forecast on it
        viewHolder.descriptionView.setText(description);

        viewHolder.iconView.setContentDescription(description);

        // Read high temperature from cursor
        double high = cursor.getDouble(ForeCastFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.highTempView.setText(Utility.formatTemperature(context, high));

        // Read low temperature from cursor
        double low = cursor.getDouble(ForeCastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempView.setText(Utility.formatTemperature(context, low));

    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && useTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        if (null == cursor) return 0;
        return cursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        cursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return cursor;
    }

}
