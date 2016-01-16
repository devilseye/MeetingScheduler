package devilseye.android.meetingscheduler.client.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;
import devilseye.android.meetingscheduler.Participant;
import devilseye.android.meetingscheduler.client.R;

public class BackgroundReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MeetingRestClientService.class);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            if (preferences.contains(Participant.LOGIN) && preferences.contains(Participant.PASSWORD)) {
                String login = preferences.getString(Participant.LOGIN, "");
                String password = preferences.getString(Participant.PASSWORD, "");
                i.putExtra(MeetingRestClientService.TASK_CODE, MeetingRestClientService.TASK_BACKGROUND_RECEIVE);
                i.putExtra(Participant.LOGIN, login);
                i.putExtra(Participant.PASSWORD, password);
                context.startService(i);
            }
        }
    }
}
