package com.subrasystems.record.utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.subrasystems.record.services.CallAccessibilityService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Base64;

public class Utility {

    private String TAG = this.getClass().getSimpleName();
    private static Utility mUtility;

    public static Utility getInstance() {
        if (mUtility == null) {
            mUtility = new Utility();
        }
        return mUtility;
    }

    //====================================================| DatePicker
    public void getDatePickerDialog(Context context, final EditText birth) {
        DatePicker datePicker = new DatePicker(context);
        int day = datePicker.getDayOfMonth();
        int mon = datePicker.getMonth();
        int year = datePicker.getYear();
        new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                birth.setText(dayOfMonth +"/"+ (month+1) +"/"+ year);
            }
        }, year, mon, day).show();
    }

    //====================================================| Checkbox
    public String getCheckboxValue(LinearLayout checkboxLayout) {
        StringBuilder value = new StringBuilder();
        for(int i=0; i<checkboxLayout.getChildCount(); i++) {
            CheckBox cb = (CheckBox) checkboxLayout.getChildAt(i);
            if (cb.isClickable()) {
                //last element do not append comma
                if (i == checkboxLayout.getChildCount()-1) {
                    value.append(cb.getText().toString());
                } else {
                    value.append(cb.getText().toString()).append(",");
                }
            }
        }
        return value.toString();
    }

    //====================================================| For Image
    public String bitmapToBase64(ImageView imageView) {
        String encode = null;
        if (imageView.getVisibility()== View.VISIBLE) {
            Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            //String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,"Title",null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                encode = Base64.getEncoder().encodeToString(stream.toByteArray());
            } else {
                encode = android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.DEFAULT);
            }
        }
        return encode;
    }

    //https://github.com/elye/demo_android_base64_image/tree/master/app/src/main/java/com/elyeproj/base64imageload
    public Bitmap base64ToBitmap(String encode) {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            byte[] decodedBytes = Base64.getDecoder().decode(encode);
            bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } else {
            byte[] decodedString = android.util.Base64.decode(encode, android.util.Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }
        return bitmap;
    }

    public String saveToInternalStorage(Context context, Bitmap bitmapImage, String imageName){
        File directory = new File(context.getFilesDir() + "/UsersPhoto/");
        directory.mkdir(); //Create imageDir
        File file = new File(directory, imageName);
        try {
            OutputStream output = new FileOutputStream(file);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, output); // Compress into png format image from 0% - 100%
            output.flush();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }

    public Bitmap loadImage(String imagePath, String imageName){
        Bitmap bitmap = null;
        try {
            File file = new File(imagePath, imageName);
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    // To check if service is enabled
    public boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + CallAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }
}
