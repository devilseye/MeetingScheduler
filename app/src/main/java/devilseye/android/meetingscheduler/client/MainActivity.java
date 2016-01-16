package devilseye.android.meetingscheduler.client;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import devilseye.android.meetingscheduler.client.receiver.BackgroundReceiver;
import devilseye.android.meetingscheduler.client.receiver.DownloadReceiver;
import devilseye.android.meetingscheduler.client.receiver.MeetingRestClientService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.support.v4.widget.SwipeRefreshLayout;
import devilseye.android.meetingscheduler.Participant;


public class MainActivity extends ActionBarActivity implements DownloadReceiver.Receiver, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "MainActivity";
    private String login;
    private String password;
    String mFIO;
    String mDescription;
    String mPost;
    String mMeetingName;
    String mStartDate;
    String mEndDate;
    String mPriority;
    ListView mListView;
    DownloadReceiver mReceiver;
    JSONArray array = null;
    ArrayList<Meeting> mAcceptedMeetingsList;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    int mYear;
    int mMonth;
    int mDay;
    int mHour;
    int mMinute;
    TextView mTVStartDate;
    TextView mTVEndDate;
    TextView mTVStartTime;
    TextView mTVEndTime;

    Intent i;
    private PendingIntent mPendingIntent;
    private BroadcastReceiver mBackgroundReceiver;

    private static final int ALARM_INTERVAL = 6000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.meetingList);
        registerForContextMenu(mListView);
        Intent receiverIntent = new Intent(this, BackgroundReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(this, 0, receiverIntent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), ALARM_INTERVAL, mPendingIntent);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        getOverflowMenu();

        NetworkManager.mContext = this;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(getString(R.string.intent_name));
        mBackgroundReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra(getString(R.string.intent_msg_tag));
                if (msg.equals(getString(R.string.intent_received_msg))) {
                    array = JsonProcessor.readJsonObject();
                    fillListView();
                } else {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        };

        this.registerReceiver(mBackgroundReceiver, intentFilter);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences != null) {
            if (preferences.contains(Participant.LOGIN) && preferences.contains(Participant.PASSWORD)) {
                login = preferences.getString(Participant.LOGIN, "");
                password = preferences.getString(Participant.PASSWORD, "");
                if (getIntent().getBooleanExtra(getString(R.string.started_from_notif), false)) {
                    array = JsonProcessor.readJsonObject();
                    fillListView();
                } else {
                    if (!NetworkManager.internetConnected())
                        Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    else {
                        startSendService(MeetingRestClientService.TASK_REQUEST_MEETINGS);
                    }
                }
            } else {
                Toast.makeText(this, R.string.auth_request, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.unregisterReceiver(this.mBackgroundReceiver);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        int taskCode = resultData.getInt(MeetingRestClientService.TASK_CODE);
        switch (taskCode) {
            case MeetingRestClientService.TASK_REQUEST_MEETINGS: {
                switch (resultCode) {
                    case MeetingRestClientService.RESULT_OK:
                        try {
                            setSupportProgressBarIndeterminateVisibility(false);
                            String result = resultData.getString(getString(R.string.result));
                            array = new JSONArray(result);
                            fillListView();
                        } catch (JSONException e) {
                            Log.e(TAG, "onReceiveResult " + e.getMessage());
                        }
                        break;
                    case MeetingRestClientService.RESULT_ERROR:
                        String errorMessage = resultData.getString(Intent.EXTRA_TEXT);
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        break;
                }
            }
            break;
            case MeetingRestClientService.TASK_DELETE_MEETING: {
                switch (resultCode) {
                    case MeetingRestClientService.RESULT_OK: {
                        Toast.makeText(this, R.string.delete_message, Toast.LENGTH_SHORT).show();
                        startSendService(MeetingRestClientService.TASK_REQUEST_MEETINGS);
                    }
                    break;
                    case MeetingRestClientService.RESULT_ERROR: {
                        String errorMessage = resultData.getString(Intent.EXTRA_TEXT);
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                    break;
                }
            }
            break;
            case MeetingRestClientService.TASK_GET_DETAIS: {
                switch (resultCode) {
                    case MeetingRestClientService.RESULT_OK: {
                        String result = resultData.getString(getString(R.string.result));
                        result = JsonProcessor.parseDescription(result);
                        showTextDialog(true, result);
                    }
                    break;
                    case MeetingRestClientService.RESULT_ERROR: {
                        String errorMessage = resultData.getString(Intent.EXTRA_TEXT);
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        break;
                    }
                }
            }
            break;
            case MeetingRestClientService.TASK_ADD_PARTICIPANT: {
                switch (resultCode) {
                    case MeetingRestClientService.RESULT_OK: {
                        Toast.makeText(this, R.string.participant_add_message, Toast.LENGTH_SHORT).show();
                        startSendService(MeetingRestClientService.TASK_REQUEST_MEETINGS);
                    }
                    break;
                    case MeetingRestClientService.RESULT_ERROR: {
                        String errorMessage = resultData.getString(Intent.EXTRA_TEXT);
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                    break;
                }
            }
            break;
            case MeetingRestClientService.TASK_FIND_MEETING_BY_DESCRIPTION: {
                mSwipeRefreshLayout.setRefreshing(false);
                setSupportProgressBarIndeterminateVisibility(false);
                switch (resultCode) {
                    case MeetingRestClientService.RESULT_OK:
                        try {
                            String result = resultData.getString(getString(R.string.result));
                            array = new JSONArray(result);
                            fillListView();
                        } catch (JSONException e) {
                            Log.e(TAG, "onReceiveResult " + e.getMessage());
                        }
                        break;
                    case MeetingRestClientService.RESULT_ERROR: {
                        String errorMessage = resultData.getString(Intent.EXTRA_TEXT);
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                    break;
                }
            }
            break;
            case MeetingRestClientService.TASK_ADD_MEETING: {
                switch (resultCode) {
                    case MeetingRestClientService.RESULT_OK:
                        Toast.makeText(this, R.string.add_message, Toast.LENGTH_SHORT).show();
                        startSendService(MeetingRestClientService.TASK_REQUEST_MEETINGS);
                        break;
                    case MeetingRestClientService.RESULT_ERROR:
                        String errorMessage = resultData.getString(Intent.EXTRA_TEXT);
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        break;
                }
            }
            break;
            case MeetingRestClientService.TASK_BACKGROUND_RECEIVE: {
                switch (resultCode) {
                    case MeetingRestClientService.RESULT_OK:
                        try {
                            setSupportProgressBarIndeterminateVisibility(false);
                            String result = resultData.getString(getString(R.string.result));
                            array = new JSONArray(result);
                            fillListView();
                        } catch (JSONException e) {
                            Log.e(TAG, "onReceiveResult " + e.getMessage());
                        }
                        break;
                    case MeetingRestClientService.RESULT_ERROR:
                        String errorMessage = resultData.getString(Intent.EXTRA_TEXT);
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        break;
                }
            }
            break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings: {
                Intent intent;
                Activity currentActivity = this;
                intent = new Intent(currentActivity, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                currentActivity.startActivity(intent);
            }
            break;
            case R.id.action_search: {
                enterDescription();
            }
            break;
            case R.id.action_add: {
                showMeetingDialog();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillListView() {

        if (array != null) {
            try {
                mListView.setAdapter(null);
                mListView = (ListView) findViewById(R.id.meetingList);
                mListView.setAdapter(null);
                mAcceptedMeetingsList = new ArrayList<Meeting>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    mAcceptedMeetingsList.add(new Meeting(
                            item.getInt(devilseye.android.meetingscheduler.Meeting.ID),
                            item.getString(devilseye.android.meetingscheduler.Meeting.NAME),
                            item.getString(devilseye.android.meetingscheduler.Meeting.STARTDATE),
                            item.getString(devilseye.android.meetingscheduler.Meeting.ENDDATE),
                            item.getString(devilseye.android.meetingscheduler.Meeting.PRIORITY)));
                }
                mListView.setAdapter(new MeetingAdapter(this, R.layout.list_item, mAcceptedMeetingsList));

            } catch (JSONException e) {
                Log.e(TAG, "fillListView " + e.getMessage());
            }
        }
    }

    @Override
    public void onRefresh() {
        fetchMeetings();
    }

    private void fetchMeetings() {
        // showing refresh animation before making http call
        mSwipeRefreshLayout.setRefreshing(true);
        if (!NetworkManager.internetConnected())
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        else {
            startSendService(MeetingRestClientService.TASK_REQUEST_MEETINGS);
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void enterDescription() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.desc_title)
                .setMessage(R.string.input_descrip);
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        dialog.setView(input);
        dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDescription = input.getText().toString().trim();
                if (!mDescription.equals("")) {
                    if (!NetworkManager.internetConnected())
                        Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    else {
                        startSendService(MeetingRestClientService.TASK_FIND_MEETING_BY_DESCRIPTION);
                    }

                }
            }

        })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void showTextDialog(boolean isDecription, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }

                });
        if (isDecription) {
            dialog.setTitle(R.string.description)
                    .setMessage(message);
        }
        dialog.show();
    }

    private void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startSendService(int code) {
        mReceiver = new DownloadReceiver(new Handler());
        mReceiver.setReceiver(this);
        i = new Intent(this, MeetingRestClientService.class);
        switch (code) {
            case MeetingRestClientService.TASK_DELETE_MEETING:
            case MeetingRestClientService.TASK_GET_DETAIS: {
                i.putExtra(devilseye.android.meetingscheduler.Meeting.ID, SwipeDetector.swipeID);
            }
            break;
            case MeetingRestClientService.TASK_ADD_PARTICIPANT: {
                i.putExtra(devilseye.android.meetingscheduler.Meeting.ID, SwipeDetector.swipeID);
                i.putExtra(Participant.FIO, mFIO);
                i.putExtra(Participant.POSITION, mPost);
            }
            break;
            case MeetingRestClientService.TASK_FIND_MEETING_BY_DESCRIPTION: {
                setSupportProgressBarIndeterminateVisibility(true);
                i.putExtra(devilseye.android.meetingscheduler.Meeting.DESCRIPTION, mDescription);
            }
            break;
            case MeetingRestClientService.TASK_ADD_MEETING: {
                i.putExtra(devilseye.android.meetingscheduler.Meeting.DESCRIPTION, mDescription);
                i.putExtra(devilseye.android.meetingscheduler.Meeting.NAME, mMeetingName);
                i.putExtra(devilseye.android.meetingscheduler.Meeting.STARTDATE, mStartDate);
                i.putExtra(devilseye.android.meetingscheduler.Meeting.ENDDATE, mEndDate);
                i.putExtra(devilseye.android.meetingscheduler.Meeting.PRIORITY, mPriority);
            }
            break;
        }
        i.putExtra(Participant.LOGIN, login);
        i.putExtra(Participant.PASSWORD, password);
        i.putExtra(MeetingRestClientService.TASK_CODE, code);
        i.putExtra(MeetingRestClientService.RECEIVER, mReceiver);
        this.startService(i);
    }

    public void showParticipantDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View participantDialogView = factory.inflate(
                R.layout.dialog_participant, null);
        final AlertDialog.Builder partDialog = new AlertDialog.Builder(this);
        partDialog.setView(participantDialogView)
                .setTitle(R.string.participantTitle)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText fn = (EditText) participantDialogView.findViewById(R.id.nameText);
                        EditText pos = (EditText) participantDialogView.findViewById(R.id.postText);
                        mFIO = fn.getText().toString().trim();
                        mPost = pos.getText().toString().trim();
                        if (NetworkManager.internetConnected())
                            startSendService(MeetingRestClientService.TASK_ADD_PARTICIPANT);
                        else
                            Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();

                    }
                });

        partDialog.show();
    }

    private void showMeetingDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View meetingDialogView = factory.inflate(
                R.layout.new_meeting_dialog, null);
        final AlertDialog.Builder newMeetingDialog = new AlertDialog.Builder(this);
        mTVStartDate = (TextView) meetingDialogView.findViewById(R.id.startDatePickerText);
        mTVEndDate = (TextView) meetingDialogView.findViewById(R.id.endDatePickerText);
        mTVStartTime = (TextView) meetingDialogView.findViewById(R.id.beginTimeText);
        mTVEndTime = (TextView) meetingDialogView.findViewById(R.id.endTimeText);
        mTVStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDataPicker(myCallBack);
            }
        });
        mTVEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDataPicker(myCallBackEnd);
            }
        });
        mTVStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(myTimeBeginCall);
            }
        });
        mTVEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(myTimeEndCall);
            }
        });
        newMeetingDialog.setView(meetingDialogView)
                .setTitle(R.string.new_meeting)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText mn = (EditText) meetingDialogView.findViewById(R.id.etMeetingName);
                        EditText dn = (EditText) meetingDialogView.findViewById(R.id.etDescription);
                        RadioGroup rad = (RadioGroup) meetingDialogView.findViewById(R.id.radioPriority);
                        RadioButton check = (RadioButton) meetingDialogView.findViewById(rad.getCheckedRadioButtonId());
                        TextView etStartDate = (TextView) meetingDialogView.findViewById(R.id.startDatePickerText);
                        TextView etStartTime = (TextView) meetingDialogView.findViewById(R.id.beginTimeText);
                        TextView etEndDate = (TextView) meetingDialogView.findViewById(R.id.endDatePickerText);
                        TextView etEndTime = (TextView) meetingDialogView.findViewById(R.id.endTimeText);

                        mMeetingName = mn.getText().toString().trim();
                        mDescription = dn.getText().toString().trim();
                        mStartDate = etStartDate.getText().toString().trim() + " " + etStartTime.getText().toString().trim();
                        mEndDate = etEndDate.getText().toString().trim() + " " + etEndTime.getText().toString().trim();
                        mPriority = check.getText().toString().trim();
                        if (NetworkManager.internetConnected())
                            startSendService(MeetingRestClientService.TASK_ADD_MEETING);
                        else
                            Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    }
                });

        newMeetingDialog.show();
    }

    private void showDataPicker(DatePickerDialog.OnDateSetListener myCallBack) {
        Calendar calendar=Calendar.getInstance();
        mYear=calendar.get(Calendar.YEAR);
        mMonth=calendar.get(Calendar.MONTH);
        mDay=calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog tpd = new DatePickerDialog(this, myCallBack, mYear, mMonth, mDay);
        tpd.show();
    }

    DatePickerDialog.OnDateSetListener myCallBack = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            String monthStr;
            String dayStr;
            if ((mMonth+1)<10){
                monthStr="0"+(mMonth+1);
            } else {
                monthStr=(mMonth+1)+"";
            }
            if (mDay<10){
                dayStr="0"+mDay;
            } else {
                dayStr=mDay+"";
            }
            mTVStartDate.setText(mYear + "-" + monthStr + "-" + dayStr);
        }
    };

    DatePickerDialog.OnDateSetListener myCallBackEnd = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            String monthStr;
            String dayStr;
            if ((mMonth+1)<10){
                monthStr="0"+(mMonth+1);
            } else {
                monthStr=(mMonth+1)+"";
            }
            if (mDay<10){
                dayStr="0"+mDay;
            } else {
                dayStr=mDay+"";
            }
            mTVEndDate.setText(mYear + "-" + monthStr + "-" + dayStr);
        }
    };

    private void showTimePicker(TimePickerDialog.OnTimeSetListener myCallBack) {
        Calendar calendar=Calendar.getInstance();
        mHour=calendar.get(Calendar.HOUR_OF_DAY);
        mMinute=calendar.get(Calendar.MINUTE);
        TimePickerDialog tpd = new TimePickerDialog(this, myCallBack, mHour, mMinute, true);
        tpd.show();
    }

    TimePickerDialog.OnTimeSetListener myTimeBeginCall = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour = hourOfDay;
            mMinute = minute;
            String hourStr;
            String minStr;
            if (mHour<10){
                hourStr="0"+mHour;
            } else {
                hourStr=mHour+"";
            }
            if (mMinute<10){
                minStr="0"+mMinute;
            } else {
                minStr=mMinute+"";
            }
            mTVStartTime.setText(hourStr + ":" + minStr);
        }
    };

    TimePickerDialog.OnTimeSetListener myTimeEndCall = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour = hourOfDay;
            mMinute = minute;
            String hourStr;
            String minStr;
            if (mHour<10){
                hourStr="0"+mHour;
            } else {
                hourStr=mHour+"";
            }
            if (mMinute<10){
                minStr="0"+mMinute;
            } else {
                minStr=mMinute+"";
            }
            mTVEndTime.setText(hourStr + ":" + minStr);
        }
    };
}
