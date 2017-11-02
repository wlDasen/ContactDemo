package net.sunniwell.contactsdemo;

import android.Manifest;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.sunniwell.contactsdemo.db.Contact;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "jpd-MActivity";
    private static final int REQUEST_READ_CONTACTS = 1;
    private List<Contact> mContactList;
    private static String alphabet = "#ABCEDFGHIGKLMNOPQRSTUVWXYZ";
    private AlphabetIndexer mIndexer;
    private ListView mListView;
    private ContactArrayAdapter mAdapter;
    private LinearLayout headerLayout;
    private TextView headerText;
    private int lastItem = -1;
    private LinearLayout.MarginLayoutParams mParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();
        initViews();
    }

    private void initViews() {
        headerLayout = (LinearLayout)findViewById(R.id.header_layout);
        headerText = (TextView)findViewById(R.id.header_text);
        mListView = (ListView)findViewById(R.id.list_view);
        mAdapter = new ContactArrayAdapter(this, R.layout.list_item, mContactList);
        mListView.setAdapter(mAdapter);
        mAdapter.setAlphabetIndexer(mIndexer);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemClick: i:" + i);
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firtItem, int i1, int i2) {
//                Log.d(TAG, "onScroll: " + absListView);
                int firstSec = mIndexer.getSectionForPosition(firtItem);
                int nextItem = mIndexer.getPositionForSection(firstSec + 1);
                mParam = (LinearLayout.MarginLayoutParams)headerLayout.getLayoutParams();
//                Log.d(TAG, "onScroll: firstItem:" + firtItem + ",nextItem:" + nextItem);
                if (firtItem != lastItem) {
                    Log.d(TAG, "onScroll: marginT:" + mParam.topMargin);
                    mParam.topMargin = 0;
                    headerText.setText(mContactList.get(firtItem).getSortKey());
                    headerLayout.setLayoutParams(mParam);
                }
                if (nextItem == firtItem + 1) {
                    View view = absListView.getChildAt(0);
                    if (view != null) {
                        int bottom = view.getBottom();
                        int headerHeight = headerLayout.getHeight();
                        Log.d(TAG, "onScroll: height:" + headerHeight + ",bottom:" + bottom + ",topMargin:" + mParam.topMargin);
                        if (bottom == headerHeight) {
                            mParam.topMargin = bottom - headerHeight;
                            headerLayout.setLayoutParams(mParam);
                        } else {
                            mParam.topMargin = 0;
                            headerLayout.setLayoutParams(mParam);
                        }
                    }
                }
                lastItem = firtItem;
            }
        });
    }

    private void getContactData() {
        mContactList = new ArrayList<>();
        Log.d(TAG, "getContactData: ");
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = getContentResolver().query(uri, new String[]{"display_name", "phonebook_label"}, null, null, "phonebook_label");
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("display_name"));
            String sortkey = cursor.getString(cursor.getColumnIndex("phonebook_label"));
            Log.d(TAG, "getContactData: name:" + name + ",sortkey:" + sortkey);
            Contact contact = new Contact();
            contact.setDispalyName(name);
            contact.setSortKey(getSortkey(sortkey));
            mContactList.add(contact);
        }
        mIndexer = new AlphabetIndexer(cursor, 1, alphabet);
    }

    private String getSortkey(String key) {
        if (key.matches("[A-Z]")) {
            return key;
        } else {
            return "#";
        }
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        } else {
            getContactData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getContactData();
            } else {
                Toast.makeText(this, "用户拒绝权限", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
