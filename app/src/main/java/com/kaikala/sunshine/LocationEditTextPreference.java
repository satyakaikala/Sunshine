package com.kaikala.sunshine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by skai0001 on 2/14/17.
 */

public class LocationEditTextPreference extends EditTextPreference {
    static final private int DEFAULT_MINIMUM_LOCATION_LENGTH = 2;
    private int minLength;

    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LocationEditTextPreference, 0, 0);
        try {
            minLength = typedArray.getInteger(R.styleable.LocationEditTextPreference_minLength, DEFAULT_MINIMUM_LOCATION_LENGTH);
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        EditText editText = getEditText();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Dialog dialog = getDialog();
                if (dialog instanceof AlertDialog) {
                    AlertDialog alertDialog = (AlertDialog)dialog;
                    Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                    if (s.length() < minLength) {
                        positiveButton.setEnabled(false);
                    } else {
                        positiveButton.setEnabled(true);
                    }
                }
            }
        });
    }
}
