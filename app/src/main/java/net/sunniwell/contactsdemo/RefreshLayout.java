package net.sunniwell.contactsdemo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by admin on 17/11/4.
 */

public class RefreshLayout extends LinearLayout {
    private static final String TAG = "jpd-RefreshLayout";
    private Context mContext;
    private View mHeader;
    private RelativeLayout mLeftLayout;
    private LinearLayout mRightLayout;
    private ImageView mArrow;
    private ProgressBar mProgressBar;
    private TextView mStatusText;
    private TextView mUpdateText;
    
    
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

    private void initViews() {
        Log.d(TAG, "initViews: ");
        mHeader = LayoutInflater.from(mContext).inflate(R.layout.refresh_layout_item, null);
        mLeftLayout = (RelativeLayout)mHeader.findViewById(R.id.refresh_left_layout);
        mRightLayout = (LinearLayout)mHeader.findViewById(R.id.refresh_right_layout);
        mArrow = (ImageView)mHeader.findViewById(R.id.refresh_arrow);
        mProgressBar = (ProgressBar)mHeader.findViewById(R.id.refresh_progress);
        mStatusText = (TextView)mHeader.findViewById(R.id.refresh_status_text);
        mUpdateText = (TextView)mHeader.findViewById(R.id.refresh_update_text);
        setOrientation(VERTICAL);
        addView(mHeader);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.d(TAG, "onLayout: changed:" + changed);
        if (changed) {
            MarginLayoutParams params = (MarginLayoutParams)getLayoutParams();
            params.topMargin = 0;
        }
    }
}