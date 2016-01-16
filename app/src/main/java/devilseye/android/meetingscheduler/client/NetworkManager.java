package devilseye.android.meetingscheduler.client;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetworkManager {
    public static Context mContext;
    public static boolean internetConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
