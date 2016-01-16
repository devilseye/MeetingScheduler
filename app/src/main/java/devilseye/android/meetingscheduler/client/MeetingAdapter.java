package devilseye.android.meetingscheduler.client;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import devilseye.android.meetingscheduler.client.receiver.MeetingRestClientService;

import java.util.ArrayList;

import static devilseye.android.meetingscheduler.Meeting.*;

public class MeetingAdapter extends ArrayAdapter<Meeting> {
    private static final String TAG = "MeetingListAdapter";
    public static MainActivity mContext;
    private ArrayList<Meeting> meetings;
    private MeetingViewHolder meetingHolder;
    SwipeDetector swipeDetector;

    public class MeetingViewHolder {
        TextView meetingName;
        TextView startDate;
        TextView endDate;
        //swipe delete layouts
        RelativeLayout listItem;
        LinearLayout mainView;
    }

    public MeetingAdapter(Context context, int layoutResource, ArrayList<Meeting> items) {
        super(context,layoutResource,items);
        this.meetings = items;
        mContext = (MainActivity)context;
        SwipeDetector.mContext = mContext;
        SwipeDetector.meetingItemsAdapter=this;
    }

    @Override
    public View getView(final int pos, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item, null);
            meetingHolder = new MeetingViewHolder();
            //main layouts
            meetingHolder.mainView = (LinearLayout) view.findViewById(R.id.mainview);
            meetingHolder.listItem = (RelativeLayout) view.findViewById(R.id.listitem);

            //meeting values
            meetingHolder.meetingName = (TextView) view.findViewById(R.id.meetingName);
            meetingHolder.startDate = (TextView) view.findViewById(R.id.startDate);
            meetingHolder.endDate = (TextView) view.findViewById(R.id.endDate);
            view.setTag(meetingHolder);
        } else meetingHolder = (MeetingViewHolder) view.getTag();

        Meeting meeting = meetings.get(pos);

        if (meeting != null) {
            meetingHolder.meetingName.setText(meeting.getMeetingName());
            meetingHolder.startDate.setText(meeting.getStartDate());
            meetingHolder.endDate.setText(meeting.getEndDate());

            String priority = meeting.getPriority();
            if (priority.equals(Priority.URGENT.toString())) {
                meetingHolder.meetingName.append(": " + priority);
            } else if (priority.equals(Priority.PLANNED.toString())) {
                meetingHolder.meetingName.append(": " + priority);
            } else if (priority.equals(Priority.OPTIONAL.toString())) {
                meetingHolder.meetingName.append(": " + priority);
            }
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) meetingHolder.mainView.getLayoutParams();
        params.rightMargin = 0;
        params.leftMargin = 0;
        meetingHolder.mainView.setLayoutParams(params);

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!swipeDetector.isNone()) {
                    Log.d(TAG, "onLongClick swipe ? isNone=" + swipeDetector.isNone());
                    return true;
                } else {
                    Log.d(TAG, "onLongClick notSwipe?  isNone=" + swipeDetector.isNone());
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                    popupMenu.inflate(R.menu.menu_context);
                    popupMenu
                            .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.add: {
                                            Meeting ti = getItem(pos);
                                            SwipeDetector.swipeID = ti.getId();
                                            mContext.showParticipantDialog();
                                            return true;
                                        }
                                        case R.id.info: {
                                            Meeting ti = getItem(pos);
                                            SwipeDetector.swipeID = ti.getId();
                                            if (NetworkManager.internetConnected())
                                                mContext.startSendService(MeetingRestClientService.TASK_GET_DETAIS);
                                            else
                                                Toast.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                                            return true;
                                        }
                                    }
                                    return true;
                                }
                            });

                    popupMenu.show();
                    return true;
                }
            }
        });
        swipeDetector = new SwipeDetector(meetingHolder, pos);
        view.setOnTouchListener(swipeDetector);
        return view;
    }
}