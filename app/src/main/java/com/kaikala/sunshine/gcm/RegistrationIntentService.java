package com.kaikala.sunshine.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.kaikala.sunshine.MainActivity;
import com.kaikala.sunshine.R;

/**
 * Created by skai0001 on 2/17/17.
 */

public class RegistrationIntentService extends IntentService{

    private static final String TAG = "RegistrationIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {

            synchronized (TAG) {
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                sendRegistrationToServer(token);

                preferences.edit().putBoolean(MainActivity.TOKEN_TO_SERVER_SENT, true).apply();
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to compete token refresh", e);
            preferences.edit().putBoolean(MainActivity.TOKEN_TO_SERVER_SENT, false).apply();

        }
    }

    private void sendRegistrationToServer(String token) {
        Log.i(TAG, "GCM Registration token: " + token);
    }

}
