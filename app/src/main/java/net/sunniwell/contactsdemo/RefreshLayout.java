package net.sunniwell.contactsdemo;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.speech.tts.Voice;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by admin on 17/11/4.
 */

public class RefreshLayout extends LinearLayout implements View.OnTouchListener {
    private static final String TAG = "jpd-RefreshLayout";
    private static int STATUS_PULL_REFRESH = 1; // 下拉状态
    private static int STATUS_RELEASE_PULL_REFRESH = 2; // 释放刷新状态
    private static int STATUS_PULL_REFRESHING = 3;
    private static int STATUS_PULL_REFRESH_FINISH = 4;
    private static int SCROLL_SPEED = -20;
    private int currentStatus = STATUS_PULL_REFRESH_FINISH;
    private int lastStatus;
    private Context mContext;
    private View mHeader;
    private RelativeLayout mLeftLayout;
    private LinearLayout mRightLayout;
    private ImageView mArrow;
    private ProgressBar mProgressBar;
    private TextView mStatusText;
    private TextView mUpdateText;
    private MarginLayoutParams mParams;
    private int mRefreshLayoutHeight;
    private boolean ableToPull;
    private ListView mListView;
    private float yDown;
    private float yMove;
    private int touchSlot;
    private boolean firstLoad = true;
    private Handler handler;
    private Runnable task;

    public RefreshLayout(Context context) {
        this(context, null);
        Log.d(TAG, "RefreshLayout: ");
    }
    
    public RefreshLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        Log.d(TAG, "RefreshLayout: ");
    }

    public RefreshLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d(TAG, "RefreshLayout: ");

        mContext = context;
        initViews();

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d(TAG, "onTouch: ");
        setAbleToPullStatus(motionEvent);
        if (ableToPull) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "onTouch: DOWN.....");
                    yDown = motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG, "MOVE: MOVE.......");
                    yMove = motionEvent.getRawY();
                    int yDistance = (int)(yMove - yDown);
                    Log.d(TAG, "MOVE: yDistance:" + yDistance);
                    if (yDistance < 0 && mParams.topMargin <= mRefreshLayoutHeight) {
                        return false;
                    }
                    if (yDistance < touchSlot) {
                        return false;
                    }
                    mParams.topMargin = mRefreshLayoutHeight + yDistance / 2;
                    Log.d(TAG, "onTouch: topMargin:" + mParams.topMargin);
                    setLayoutParams(mParams);

                    if (mParams.topMargin > 0) {
                        currentStatus = STATUS_RELEASE_PULL_REFRESH;
                    } else {
                        currentStatus = STATUS_PULL_REFRESH;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (currentStatus == STATUS_RELEASE_PULL_REFRESH) {
                        new ReleaseTask().execute();
                    } else if (currentStatus == STATUS_PULL_REFRESH) {
                        new PullTask().execute();
                    }
                    Log.e(TAG, "onTouch: topMargin:" + mParams.topMargin );
                    break;
                }
            }
            if (currentStatus == STATUS_RELEASE_PULL_REFRESH || currentStatus == STATUS_PULL_REFRESH) {
               mListView.setPressed(false);
                mListView.setFocusable(false);
                mListView.setFocusableInTouchMode(false);
                updateHeader();
                lastStatus = currentStatus;
                return true;
             }
        return false;
    }
    private void updateHeader() {
        if (lastStatus != currentStatus) {
            if (currentStatus == STATUS_PULL_REFRESHING) {
                Log.d(TAG, "updateHeader: thread:" + Thread.currentThread().getId());
                mStatusText.setText(R.string.refreshi_status_refreshing);
                mArrow.clearAnimation();
                mArrow.setVisibility(GONE);
                mProgressBar.setVisibility(VISIBLE);
                handler.postDelayed(task, 3000);
            }
            if (currentStatus == STATUS_RELEASE_PULL_REFRESH) {
                mStatusText.setText(R.string.refresh_status_refresh);
                rotateArrow();
            }
            if (currentStatus == STATUS_PULL_REFRESH) {
                mProgressBar.setVisibility(GONE);
                mArrow.setVisibility(VISIBLE);
            }
        }
    }

    private void rotateArrow() {
        float pivotX = mArrow.getWidth() / 2;
        float pivotY = mArrow.getHeight() / 2;
        RotateAnimation animation = new RotateAnimation(0f, 180f, pivotX, pivotY);
        animation.setDuration(200);
        animation.setFillAfter(true);
        mArrow.startAnimation(animation);
    }

    private void setAbleToPullStatus(MotionEvent event) {
        View firstChild = mListView.getChildAt(0);
        if (firstChild != null) {
            int firstPos = mListView.getFirstVisiblePosition();
            Log.d(TAG, "setAbleToPullStatus: top:" + firstChild.getTop() + ",pos:" + firstPos);
            if (firstPos == 0 && firstChild.getTop() == 0) {
                ableToPull = true;
            } else {
                ableToPull = false;
            }
        } else { // listView没有元素也要允许下拉
            ableToPull = true;
        }
    }

    private void initViews() {
        Log.d(TAG, "initViews: ");
        handler = new Handler();
        task = new Runnable() {
            @Override
            public void run() {
                finishRefresh();
            }
        };
        touchSlot = ViewConfiguration.get(mContext).getScaledTouchSlop();
        Log.d(TAG, "initViews: touchSlot:" + touchSlot);
        mHeader = LayoutInflater.from(mContext).inflate(R.layout.refresh_layout_item, null);
        mLeftLayout = (RelativeLayout)mHeader.findViewById(R.id.refresh_left_layout);
        mRightLayout = (LinearLayout)mHeader.findViewById(R.id.refresh_right_layout);
        mArrow = (ImageView)mHeader.findViewById(R.id.refresh_arrow);
        mProgressBar = (ProgressBar)mHeader.findViewById(R.id.refresh_progress);
        mStatusText = (TextView)mHeader.findViewById(R.id.refresh_status_text);
        mUpdateText = (TextView)mHeader.findViewById(R.id.refresh_update_text);
        setOrientation(VERTICAL);
        addView(mHeader, 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && firstLoad) {
            View view = getChildAt(1);
            mListView = (ListView) view.findViewById(R.id.list_view);
            mListView.setOnTouchListener(this);
            Log.d(TAG, "onLayout: changed:" + changed);
            mRefreshLayoutHeight = -mHeader.getHeight();
            Log.d(TAG, "onLayout: height:" + mRefreshLayoutHeight);
            mParams = (MarginLayoutParams) getLayoutParams();
            mParams.topMargin = mRefreshLayoutHeight;
            firstLoad = false;
        }
    }

    public class ReleaseTask extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {
            while (true) {
                mParams.topMargin = mParams.topMargin + SCROLL_SPEED;
                if (mParams.topMargin < 0) {
                    mParams.topMargin = 0;
                    break;
                }
                publishProgress(mParams.topMargin);
            }
            currentStatus = STATUS_PULL_REFRESHING;
            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mParams.topMargin = values[0];
            setLayoutParams(mParams);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            updateHeader();
            mParams.topMargin = integer;
            setLayoutParams(mParams);
        }
    }

    private void finishRefresh() {
        currentStatus = STATUS_PULL_REFRESH_FINISH;
        new PullTask().execute();
    }

    public class PullTask extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {
            while (true) {
                mParams.topMargin = mParams.topMargin + SCROLL_SPEED;
                if (mParams.topMargin < mRefreshLayoutHeight) {
                    mParams.topMargin = mRefreshLayoutHeight;
                    break;
                }
                publishProgress(mParams.topMargin);
            }
            currentStatus = STATUS_PULL_REFRESH_FINISH;
            return mRefreshLayoutHeight;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mParams.topMargin = values[0];
            setLayoutParams(mParams);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            mParams.topMargin = integer;
            setLayoutParams(mParams);
        }
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}