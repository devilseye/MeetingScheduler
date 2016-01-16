package devilseye.android.meetingscheduler.client;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import devilseye.android.meetingscheduler.Participant;

public class PinDialog extends DialogPreference {
    private EditText login;
    private EditText password;
    private int selectedValue;

    String mValue;

    public PinDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.dialog_text);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setTitle(R.string.login_title);
        builder.setMessage(R.string.enter_login_message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
                selectedValue = which;
                mValue= login.getText().toString();
                preferences.edit().putString(Participant.PASSWORD, password.getText().toString()).apply();
                preferences.edit().putString(Participant.LOGIN, login.getText().toString()).apply();
                preferences.edit().commit();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedValue=which;
                getDialog().dismiss();
            }
        });
    }

    @Override
    public void onBindDialogView(@NonNull View view){
        login = (EditText)view.findViewById(R.id.userText);
        password = (EditText) view.findViewById(R.id.passwordText);
        super.onBindDialogView(view);

    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if(selectedValue==DialogInterface.BUTTON_POSITIVE)
        {
            String user = login.getText().toString();
            if(!user.equals("")) {
                setSummary(user);
                persistString(mValue);
            }
        }
    }
    protected void onSaveUsername(String user) {
        persistString(user != null ? user : "");
    }
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mValue = getPersistedString(getContext().getString(R.string.not_logged_in));
        }
        else {
            mValue = (String) defaultValue;
            persistString(mValue);
        }
    }



}
