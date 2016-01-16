package devilseye.android.meetingscheduler.client.receiver;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import devilseye.android.meetingscheduler.Meeting;
import devilseye.android.meetingscheduler.Participant;
import devilseye.android.meetingscheduler.client.JsonProcessor;
import devilseye.android.meetingscheduler.client.MainActivity;

import devilseye.android.meetingscheduler.client.R;
import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeetingRestClientService extends IntentService {
    //for Debug
    private static final String TAG = "MeetingClientService";

    public static final int TASK_REQUEST_MEETINGS = 1;
    public static final int TASK_ADD_PARTICIPANT = 2;
    public static final int TASK_GET_DETAIS = 3;
    public static final int TASK_DELETE_MEETING = 4;
    public static final int TASK_ADD_MEETING = 5;
    public static final int TASK_FIND_MEETING_BY_DESCRIPTION = 6;
    public static final int TASK_BACKGROUND_RECEIVE = 7;

    public static final int RESULT_OK = 1;
    public static final int RESULT_ERROR = 0;

    public static final String TASK_CODE = "codeTask";
    public static final String RECEIVER = "receiver";
    public static final int RETRY_TIMEOUT = 3000;
    public static int mTaskCode = -1;

    public MeetingRestClientService() {
        super("MeetingRestClientService");
    }

    protected void onHandleIntent(Intent i) {
        JsonProcessor.mContext = getApplicationContext();
        String login = i.getStringExtra(Participant.LOGIN);
        String password = i.getStringExtra(Participant.PASSWORD);
        mTaskCode = i.getIntExtra(TASK_CODE, -1);
        final ResultReceiver receiver = i.getParcelableExtra(RECEIVER);
        switch (mTaskCode) {
            case TASK_REQUEST_MEETINGS: {
                String url = getString(R.string.url)+getString(R.string.url_request_meeting);
                url += "?" + Participant.LOGIN + "=" + login + "&" + Participant.PASSWORD + "=" + password;
                requestMeetings(receiver,url, mTaskCode);
            }
            break;
            case TASK_ADD_PARTICIPANT: {
                addParticipant(receiver,i, login, password);
            }
            break;
            case TASK_GET_DETAIS: {
                final int id = i.getIntExtra(Meeting.ID, -1);
                String url = getString(R.string.url)+getString(R.string.url_get_details);
                url += "?" + Participant.LOGIN + "=" + login + "&" + Participant.PASSWORD + "=" + password +
                        "&" + Meeting.ID + "=" + id;
                requestMeetings(receiver,url, mTaskCode);
            }
            break;
            case TASK_DELETE_MEETING: {
                cancelMeetingRequest(receiver,i, login, password);
            }
            break;
            case TASK_ADD_MEETING: {
                addMeeting(receiver,i, login, password);
            }
            break;
            case TASK_FIND_MEETING_BY_DESCRIPTION: {
                try {
                    String description = i.getStringExtra(Meeting.DESCRIPTION);
                    description = URLEncoder.encode(description, "UTF-8");
                    String url = getString(R.string.url)+getString(R.string.url_get_meeting_by_description);
                    url += "?" + Participant.LOGIN + "=" + login + "&" + Participant.PASSWORD + "=" + password
                            + "&" + Meeting.DESCRIPTION + "=" + description;
                    requestMeetings(receiver,url, mTaskCode);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onHandleIntent " + e.getMessage());
                }
            }
            break;

            case TASK_BACKGROUND_RECEIVE: {
                String url = getString(R.string.url)+getString(R.string.url_request_meeting);
                url += "?" + Participant.LOGIN + "=" + login + "&" + Participant.PASSWORD + "=" + password;
                getMeetingsBackground(receiver,url);
            }
            break;
        }
    }


    public void addParticipant(final ResultReceiver receiver, Intent i, String username, String password) {
        RequestQueue queue = Volley.newRequestQueue(this);
        int id = i.getIntExtra(Meeting.ID, -1);
        String fio = i.getStringExtra(Participant.FIO);
        String position = i.getStringExtra(Participant.POSITION);
        String url = getString(R.string.url)+getString(R.string.url_add_participant);
        try {
            fio = URLEncoder.encode(fio, "UTF-8");
            position = URLEncoder.encode(position, "UTF-8");
            url += "/" + username + "/" + password + "/" + id + "/" + fio + "/" + position;
            /**
             * Listeners
             */
            mTaskCode = MeetingRestClientService.TASK_ADD_PARTICIPANT;
            ResponseListenerForTasks<JSONArray> responseListener = new ResponseListenerForTasks<JSONArray>(receiver);
            ErrorListenerForTasks errorListener = new ErrorListenerForTasks(receiver);
            //
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.PUT, url, null, responseListener, errorListener)
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    return params;
                }
            };
            request.setRetryPolicy(
                    new DefaultRetryPolicy(RETRY_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestMeetings(final ResultReceiver receiver, String url, final int code) {
        RequestQueue queue = Volley.newRequestQueue(this);
        /**
         * Listeners
         */
        mTaskCode = code;
        RequestMeetingsResponseListener<JSONArray> responseListener = new RequestMeetingsResponseListener<JSONArray>(receiver);
        RequestMeetingsErrorListener errorListener = new RequestMeetingsErrorListener(receiver);
        //
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, responseListener, errorListener);
        request.setRetryPolicy(new DefaultRetryPolicy(RETRY_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    public void cancelMeetingRequest(final ResultReceiver receiver, Intent i, final String login, final String password) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getString(R.string.url)+getString(R.string.url_cancel_meeting);
        final int id = i.getIntExtra(Meeting.ID, -1);

        try {
            /**
             * Listeners
             */
            mTaskCode = MeetingRestClientService.TASK_DELETE_MEETING;
            ResponseListenerForTasks<JSONArray> responseListener = new ResponseListenerForTasks<JSONArray>(receiver);
            ErrorListenerForTasks errorListener = new ErrorListenerForTasks(receiver);
            //
            JsonArrayRequest request =
                    new JsonArrayRequest(Request.Method.DELETE, url, null, responseListener, errorListener) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Content-Type", "application/json");
                            headers.put("Accept-Charset", "UTF-8");
                            headers.put(Meeting.ID, String.valueOf(id));
                            headers.put(Participant.LOGIN, login);
                            headers.put(Participant.PASSWORD, password);
                            return headers;
                        }
                    };
            request.setRetryPolicy(
                    new DefaultRetryPolicy(RETRY_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addMeeting(final ResultReceiver receiver, Intent i, final String login, final String password) {
        RequestQueue queue = Volley.newRequestQueue(this);

        String meetingName = i.getStringExtra(Meeting.NAME);
        String description = i.getStringExtra(Meeting.DESCRIPTION);
        String startDate = i.getStringExtra(Meeting.STARTDATE);
        String endDate = i.getStringExtra(Meeting.ENDDATE);
        String priority = i.getStringExtra(Meeting.PRIORITY);
        String url = getString(R.string.url)+getString(R.string.url_add_meeting);

        try {
            meetingName = URLEncoder.encode(meetingName, "UTF-8");
            description = URLEncoder.encode(description, "UTF-8");
            startDate = URLEncoder.encode(startDate, "UTF-8");
            endDate = URLEncoder.encode(endDate, "UTF-8");
            priority = URLEncoder.encode(priority, "UTF-8");
            url += "/" + login + "/" + password + "/" + meetingName
                    + "/" + description + "/" + startDate + "/" + endDate + "/" + priority;

            /**
             * Listeners
             */
            mTaskCode = MeetingRestClientService.TASK_ADD_MEETING;
            ResponseListenerForTasks<JSONArray> responseListener = new ResponseListenerForTasks<JSONArray>(receiver);
            ErrorListenerForTasks errorListener = new ErrorListenerForTasks(receiver);
            //
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, url, null, responseListener, errorListener) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    return params;
                }
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    return params;
                }
            };
            request.setRetryPolicy(
                    new DefaultRetryPolicy(RETRY_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getMeetingsBackground(final ResultReceiver receiver, String url) {
        RequestQueue queue = Volley.newRequestQueue(this);
        /**
         * Listeners
         */
        mTaskCode = MeetingRestClientService.TASK_ADD_MEETING;
        BackgroundResponseListener<JSONArray> responseListener = new BackgroundResponseListener<JSONArray>(receiver);
        BackgroundErrorListener errorListener = new BackgroundErrorListener(receiver);
        //
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, responseListener,errorListener);
        request.setRetryPolicy(new DefaultRetryPolicy(RETRY_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    private class BackgroundErrorListener extends ListenerForService implements Response.ErrorListener {
        private final ResultReceiver receiver;
        public BackgroundErrorListener (ResultReceiver receiver){
            this.receiver = receiver;
        }
        @Override
        public void onErrorResponse(VolleyError error) {
            if (checkApp()) {
                Intent i = new Intent(getString(R.string.intent_name)).putExtra(getString(R.string.intent_msg_tag), getString(R.string.rest_server_not_available));
                sendBroadcast(i);
            }
        }
    }

    private class BackgroundResponseListener <T> extends ListenerForService implements Response.Listener<T>  {
        private final ResultReceiver receiver;
        public BackgroundResponseListener (ResultReceiver receiver){
            this.receiver = receiver;
        }
        @Override
        public void onResponse(T response) {
            Bundle bundle = new Bundle();
            T array = response;
            if (!response.toString().equals(RESULT_ERROR)) {
                if(!response.toString().equals(EMPTY_LIST)){
                    JSONArray read = JsonProcessor.readJsonObject();
                    if(read!=null){
                        if(!array.toString().equals(read.toString())){
                            JsonProcessor.writeJsonObject((JSONArray) array);
                            if(!checkApp()) {
                                showNotification();
                            } else {
                                Intent i = new Intent(getString(R.string.intent_name)).putExtra(getString(R.string.intent_msg_tag), getString(R.string.intent_received_msg));
                                sendBroadcast(i);
                                Log.d(TAG, "onResponse " + response.toString());
                            }
                        }
                    }
                }
            } else {
                if(checkApp()) {
                    Intent i = new Intent(getString(R.string.intent_name))
                            .putExtra(getString(R.string.intent_msg_tag), getString(R.string.rest_server_not_available));
                    sendBroadcast(i);
                    Log.d(TAG, "onResponse " + response.toString());
                }
            }
        }


    }


    private class ErrorListenerForTasks extends ListenerForService implements Response.ErrorListener{
        private final ResultReceiver receiver;
        public ErrorListenerForTasks (ResultReceiver receiver){
            this.receiver = receiver;
        }
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "onErrorResponse Request failed: " + error.toString());

            Bundle bundle = new Bundle();
            String message = getString(R.string.rest_server_not_available);
            prepareResponse(bundle, message, mTaskCode,
                    MeetingRestClientService.RESULT_ERROR, receiver);
        }
    }

    private class ListenerForService {
        protected static final String TAG = "MeetingClientService";
        protected static final String RESULT_OK = "[{\"result\":\"OK\"}]";
        protected static final String RESULT_ERROR = "[{\"result\":\"Error\"}]";
        protected static final String EMPTY_LIST = "[]";


        public void prepareResponse(Bundle bundle, String extraText, int TASK_TYPE, int RESULT_CODE, ResultReceiver resultReceiver) {
            bundle.putString(Intent.EXTRA_TEXT, extraText);
            bundle.putInt(MeetingRestClientService.TASK_CODE, TASK_TYPE);
            resultReceiver.send(RESULT_CODE, bundle);
        }

        protected boolean checkApp() {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            // get the info from the currently running task
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equalsIgnoreCase(getString(R.string.app_full_name))) {
                return true;
            } else {
                return false;
            }
        }

        public void showNotification(){
            Context context = getApplicationContext();
            NotificationManager manager = (NotificationManager)getSystemService(context.NOTIFICATION_SERVICE);
            Intent intent1 = new Intent(context, MainActivity.class);
            Notification notification = new Notification(R.drawable.ic_launcher, context.getString(R.string.notif_new_meetings_received), System.currentTimeMillis());
            intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent1.putExtra(context.getString(R.string.started_from_notif), true);
            PendingIntent pendingNotificationIntent = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.setLatestEventInfo(context, context.getString(R.string.app_name), context.getString(R.string.notif_new_meetings_received), pendingNotificationIntent);
            manager.notify(0, notification);
        }

    }


    private class RequestMeetingsErrorListener extends ListenerForService implements Response.ErrorListener {
        private final ResultReceiver receiver;
        public RequestMeetingsErrorListener(ResultReceiver receiver){
            this.receiver = receiver;
        }
        @Override
        public void onErrorResponse(VolleyError error) {
            Bundle bundle = new Bundle();
            String message = getString(R.string.rest_server_not_available);
            prepareResponse(bundle, message, mTaskCode,
                    MeetingRestClientService.RESULT_ERROR, receiver);
        }
    }

    private class RequestMeetingsResponseListener<JSONArray> extends ListenerForService implements Response.Listener <JSONArray> {
        private final ResultReceiver receiver;
        public RequestMeetingsResponseListener(ResultReceiver receiver){
            this.receiver = receiver;
        }
        @Override
        public void onResponse( JSONArray response) {
            Bundle bundle = new Bundle();
            org.json.JSONArray array = (org.json.JSONArray)response;
            if (response.toString().equals(ListenerForService.RESULT_OK)) {
                String message = getString(R.string.rest_login_or_password_error);
                prepareResponse(bundle, message, mTaskCode, MeetingRestClientService.RESULT_ERROR, receiver);
            } else if (response.toString().equals(EMPTY_LIST)) {
                String message = getString(R.string.data_not_found);
                prepareResponse(bundle, message, mTaskCode, MeetingRestClientService.RESULT_ERROR, receiver);
            } else {
                if (mTaskCode == MeetingRestClientService.TASK_REQUEST_MEETINGS) {
                    JsonProcessor.writeJsonObject(array);
                }
                bundle.putString(getString(R.string.result), response.toString());
                bundle.putInt(MeetingRestClientService.TASK_CODE, mTaskCode);
                receiver.send(MeetingRestClientService.RESULT_OK, bundle);
            }
        }
    }
    private class ResponseListenerForTasks  <JSONArray> extends ListenerForService implements Response.Listener<JSONArray>  {
        private final ResultReceiver receiver;
        public ResponseListenerForTasks(ResultReceiver receiver){
            this.receiver = receiver;
        }
        @Override
        public void onResponse(JSONArray response) {
            Bundle bundle = new Bundle();
            if (response.toString().equals(RESULT_ERROR)) {
                String message = getString(R.string.rest_login_or_password_error);
                prepareResponse(bundle, message, mTaskCode,
                        MeetingRestClientService.RESULT_ERROR, receiver);
            } else {
                bundle.putInt(MeetingRestClientService.TASK_CODE, mTaskCode);
                receiver.send(MeetingRestClientService.RESULT_OK, bundle);
            }
            Log.d(TAG, "onResponse " + response.toString());
        }
    }
}
