package devilseye.android.meetingscheduler.client;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
    final static private String login = "login";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_headers);
        PinDialog dialog = (PinDialog) findPreference(login);
        String defaultUser = dialog.getSharedPreferences().getString(dialog.getKey(), getString(R.string.not_logged_in));
        dialog.setSummary(defaultUser);
    }
}
