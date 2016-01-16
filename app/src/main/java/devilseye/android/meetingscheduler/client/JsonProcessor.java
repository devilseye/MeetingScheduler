package devilseye.android.meetingscheduler.client;

import android.content.Context;
import android.util.Log;

import devilseye.android.meetingscheduler.Meeting;
import devilseye.android.meetingscheduler.Participant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class JsonProcessor {
    //for Debug
    private static final String TAG = "JsonWorkHelper";
    public static Context mContext;
    public static final String MEETINGS_FILE_NAME = "meetings.json";

    public static JSONArray readJsonObject() {
        JSONArray array = null;
        String path = mContext.getFilesDir().getAbsolutePath() + "/" + MEETINGS_FILE_NAME;
        File file = new File(path);
        FileInputStream stream = null;
        if (file.exists()) {
            try {
                stream = new FileInputStream(file);
                String jsonStr = null;

                FileChannel channel = stream.getChannel();
                MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                jsonStr = Charset.defaultCharset().decode(buffer).toString();
                Log.d(TAG, "readJsonObject " + jsonStr);
                if (jsonStr != null) {
                    array = new JSONArray(jsonStr);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "readJsonObject " + e.getMessage());
            } finally {
                try {
                    if (stream != null)
                        stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "readJsonObject " + e.getMessage());
                }
                return array;
            }

        } else {
            File jFile = new File(mContext.getFilesDir(), MEETINGS_FILE_NAME);
            System.out.println("in readJsonObject ------");
            return new JSONArray();
        }
    }

    public static void writeJsonObject(JSONArray array) {
        FileOutputStream outputStream = null;
        if (array != null) {
            try {
                outputStream = mContext.openFileOutput(MEETINGS_FILE_NAME, Context.MODE_PRIVATE);
                outputStream.write(array.toString().getBytes());
            } catch (IOException e) {
                Log.e(TAG, "writeJsonObject " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "writeJsonObject " + e.getMessage());
                        System.out.println("writeJsonObject ERROR");
                    }
                }
            }
        }
    }

    public static String parseDescription(String result) {
        String detailedInformation = "";
        try {
            System.out.println("In parseDescription");
            System.out.println("In  result"+  result);
            JSONArray array = new JSONArray(result);
            System.out.println("In  array"+  array);
            JSONObject item = array.getJSONObject(0);
            String description = item.getString(Meeting.DESCRIPTION);
            if (description != null) {
                detailedInformation += description;
            }
            if (item.has(Meeting.PARTICIPANTS)) {
                System.out.println("In participans");
                JSONArray participants = item.getJSONArray(Meeting.PARTICIPANTS);
                System.out.println("JSONArray participants " + participants.toString());
                if (participants != null) {
                    String NEW_LINE = "\r\n";
                    detailedInformation += NEW_LINE +"["+ mContext.getString(R.string.meeting_participants)+"]" + NEW_LINE;
                    for (int i = 0; i < participants.length(); i++) {
                        item = participants.getJSONObject(i);
                        detailedInformation += item.getString(Participant.FIO);
                        detailedInformation += ": " + item.getString(Participant.POSITION) + "" + NEW_LINE;
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "onReceiveResult " + e.getMessage());
            System.out.println("parseDescription ERROR");
        }
        return detailedInformation;
    }
}
