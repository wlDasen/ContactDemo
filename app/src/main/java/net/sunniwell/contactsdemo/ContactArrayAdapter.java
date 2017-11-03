package net.sunniwell.contactsdemo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.sunniwell.contactsdemo.db.Contact;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by admin on 2017/11/2.
 */

public class ContactArrayAdapter extends ArrayAdapter<Contact> {
    private static final String TAG = "jpd-Adapter";
    private Context mContext;
    private List<Contact> mList;
    private int mResource;
    private AlphabetIndexer mIndexer;

    public ContactArrayAdapter(Context context, int resource, List<Contact> objects) {
        super(context, resource, objects);
        Log.d(TAG, "ContactArrayAdapter: resource:" + resource);
        mResource = resource;
        mContext = context;
        mList = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        Log.d(TAG, "getView: position:" + position);
        LinearLayout layout;
        if (convertView == null) {
            layout = new LinearLayout(mContext);
            layout = (LinearLayout)LayoutInflater.from(mContext).inflate(mResource, null);
        } else {
            layout = (LinearLayout)convertView;
        }
        ImageView img = (ImageView)layout.findViewById(R.id.item_image);
        TextView text = (TextView)layout.findViewById(R.id.item_text);
        img.setBackgroundResource(R.drawable.icon);
        text.setText(mList.get(position).getDispalyName());
        LinearLayout sortLayout = (LinearLayout)layout.findViewById(R.id.sort_layout);
        TextView sortText = (TextView)sortLayout.findViewById(R.id.sort_text);
        int section = mIndexer.getSectionForPosition(position);
        int firstPosition = mIndexer.getPositionForSection(section);
        if (firstPosition == position) {
//            Log.d(TAG, "getView: text:" + mList.get(position).getSortKey());
            sortLayout.setVisibility(View.VISIBLE);
            sortText.setText(mList.get(position).getSortKey());
        } else {
//            Log.d(TAG, "getView: ");
            sortLayout.setVisibility(View.GONE);
        }

        return layout;
    }

    public void setAlphabetIndexer(AlphabetIndexer indexer) {
        mIndexer = indexer;
    }
}