package devilseye.android.meetingscheduler.client;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import devilseye.android.meetingscheduler.client.receiver.MeetingRestClientService;

public class SwipeDetector implements View.OnTouchListener {
    private static final String TAG = "SwipeDetector";
    public static MainActivity mContext;
    public static MeetingAdapter meetingItemsAdapter;
    private static final int MIN_DISTANCE = 130;
    private static final int MIN_LOCK_DISTANCE = 30;
    private float downX, upX;
    private MeetingAdapter.MeetingViewHolder holder;
    private int position;
    private boolean isNone = true;
    public static int swipeID;
    

    public SwipeDetector(MeetingAdapter.MeetingViewHolder holder, int position) {
        this.holder = holder;
        this.position = position;
    }

    public boolean isNone() {
        return isNone;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                Log.d(TAG, "onTouch ACTION_DOWN");
                downX = event.getX();
                isNone = true;
                Log.d(TAG, "onTouch ACTION_DOWN position " + position);
                return false;
            }
            case MotionEvent.ACTION_MOVE: {
                Log.d(TAG, "onTouch ACTION_MOVE");
                upX = event.getX();
                float deltaX = downX - upX;
                if (Math.abs(deltaX) > MIN_LOCK_DISTANCE && mContext.mListView != null) {
                    if (deltaX > 0) {
                        holder.listItem.setVisibility(View.GONE);
                        isNone = false;
                    } else {
                        holder.listItem.setVisibility(View.VISIBLE);
                        isNone = false;
                    }
                    swipe(-(int) deltaX);
                    return true;
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                Log.d(TAG, "onTouch ACTION_UP");
                upX = event.getX();
                float deltaX = upX - downX;
                if (deltaX > MIN_DISTANCE) {
                    swipeRemove();
                } else {
                    swipe(0);
                }
                holder.listItem.setVisibility(View.VISIBLE);
                return true;
            }

            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "onTouch ACTION_CANCEL");
                swipe(0);
                holder.listItem.setVisibility(View.VISIBLE);
                return false;
        }
        return true;
    }

    private void swipe(int distance) {
        View animationView = holder.mainView;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) animationView.getLayoutParams();
        params.rightMargin = -distance;
        params.leftMargin = distance;
        animationView.setLayoutParams(params);
    }

    private void swipeRemove() {
        if (NetworkManager.internetConnected()) {
            Meeting meeting = meetingItemsAdapter.getItem(position);
            swipeID = meeting.getId();
            meetingItemsAdapter.remove(meeting);
            mContext.startSendService(MeetingRestClientService.TASK_DELETE_MEETING);
            meetingItemsAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(mContext, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();

        }
    }
}
