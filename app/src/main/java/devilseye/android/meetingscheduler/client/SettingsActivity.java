package devilseye.android.meetingscheduler.client;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment()).commit();
        }
    }
}
