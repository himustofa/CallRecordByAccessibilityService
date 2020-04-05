package com.subrasystems.record.activities;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.subrasystems.record.R;
import com.subrasystems.record.adapters.CallRecordAdapter;
import com.subrasystems.record.models.Record;
import com.subrasystems.record.utils.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements CallRecordAdapter.CallBackListener, EasyPermissions.PermissionCallbacks {

    //Link: https://developer.android.com/guide/topics/media/mediarecorder?hl=en
    private static final String TAG = "MainActivity";

    private ArrayList<Record> mArrayList;
    private RecyclerView mRecyclerView;
    private CallRecordAdapter mAdapter;

    private String[] PERMISSIONS = {
            //android.Manifest.permission.SYSTEM_ALERT_WINDOW,
            //android.Manifest.permission.BIND_ACCESSIBILITY_SERVICE,
            //android.Manifest.permission.MANAGE_OWN_CALLS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            //android.Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.CAPTURE_AUDIO_OUTPUT,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private static final int RC_APP_PERM = 124;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {}
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_item);

        //-----------------------------------------------| List of Permissions
        requestPermissions();
    }

    @AfterPermissionGranted(RC_APP_PERM)
    private void requestPermissions() {
        if (EasyPermissions.hasPermissions(this, PERMISSIONS)) {
            // Already have permission, do the thing
            onCheckPermissionOverOtherApp();
            getInternalStorageFiles();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_APP_PERM, PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        // Some permissions have been granted
        onCheckPermissionOverOtherApp();
        getInternalStorageFiles();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // Some permissions have been denied
    }

    //===============================================| On over other app | open the settings screen
    private void onCheckPermissionOverOtherApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {
            if (!Utility.getInstance().isAccessibilitySettingsOn(getApplicationContext())) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            if (!Utility.getInstance().isAccessibilitySettingsOn(getApplicationContext())) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        } else {
            onCheckPermissionOverOtherApp();
            Toast.makeText(this,"Draw over other app permission not available. Closing the application", Toast.LENGTH_SHORT).show();
        }
    }

    //===============================================| Delete record file from storage
    @Override
    public void onDelete(int position, Record model) {
        File file = new File(model.getFilePath()+"/"+model.getFileName());
        if (file.exists()) {
            if (file.delete()) {
                Toast.makeText(this, "Deleted the file successfully", Toast.LENGTH_SHORT).show();
                if (mArrayList != null && mArrayList.size() > 0) {
                    mArrayList.remove(position);
                    mRecyclerView.removeViewAt(position);
                    mAdapter.notifyItemRemoved(position);
                    mAdapter.notifyItemRangeChanged(position, mArrayList.size());
                }
            } else {
                Toast.makeText(this, "Did not delete this file " + model.getFileName(), Toast.LENGTH_SHORT).show();
            };
        }
    }

    //===============================================| Get all records file from Storage
    private void getInternalStorageFiles() {
        mArrayList = new ArrayList<>();
        //String path = Environment.getExternalStorageDirectory().toString() + "/Testing"; //getExternalFilesDir(), getExternalCacheDir(), or getExternalMediaDir()
        //String path = this.getApplicationContext().getFilesDir() + "/system_sound"; //file.getAbsolutePath()
        //String[] listOfFiles = Environment.getExternalStoragePublicDirectory (Environment.DIRECTORY_DOWNLOADS).list();

        String path = getApplicationContext().getFilesDir().getPath();
        String[] listOfFiles = getApplicationContext().getFilesDir().list();
        Log.d(TAG, "Files: " + new Gson().toJson(listOfFiles));
        if (listOfFiles != null) {
            for (String fileName : listOfFiles) {
                mArrayList.add(new Record(fileName, path));
            }
        }
        if (mArrayList != null && mArrayList.size() > 0) {
            initRecyclerView();
        }
    }

    private void initRecyclerView() {
        mAdapter = new CallRecordAdapter(this, mArrayList, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter.notifyDataSetChanged();
    }
}
