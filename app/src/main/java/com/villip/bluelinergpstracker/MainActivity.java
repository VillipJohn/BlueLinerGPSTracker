package com.villip.bluelinergpstracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import static com.villip.bluelinergpstracker.R.id.intervalRadioGroup;
import static com.villip.bluelinergpstracker.R.id.trackingButton;


public class MainActivity extends AppCompatActivity {
    private EditText mUploadWebsiteEditText;
    private TextView mIntervalTextView;
    private RadioGroup mIntervalRadioGroup;
    private Button mTrackingButton;

    private boolean currentlyTracking;

    //private double x,y;

    private static final String TAG = "myLOG";

    String strSpeed, strTime, imei;

    private SharedPreferences mSharedPreferences;
    private static final String APP_PREFERENCES = "mySettings";
    private static final String APP_PREFERENCES_IMEI = "imei";
    private static final String APP_PREFERENCES_CURRENTLY_TRACKING = "currentlyTracking";
    private static final String APP_PREFERENCES_UPLOAD_WEBSITE = "uploadWebsite";
    private static final String APP_PREFERENCES_INTERVAL_UPDATE_WEBSITE = "intervalUpdateWebsite";

    private String uploadWebsiteString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Request for permissions when the application is first launched
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE}, 1);


        mUploadWebsiteEditText = (EditText) findViewById(R.id.uploadWebsiteEditText);


        mIntervalTextView = (TextView) findViewById(R.id.intervalTextView);
        mIntervalRadioGroup = (RadioGroup) findViewById(intervalRadioGroup);

        mTrackingButton = (Button) findViewById(trackingButton);
        mTrackingButton.setTextColor(Color.WHITE);

        mIntervalRadioGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        saveInterval();
                    }
                });

        mSharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        getDeviceImei();

        mTrackingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                trackLocation();
            }
        });

        //startService(new Intent(MainActivity.this, LocationService.class));


    }


    @Override
    protected void onResume() {
        super.onResume();

        displayUserSettings();
        setTrackingButtonState();
    }

    private void displayUserSettings() {
        uploadWebsiteString = mSharedPreferences.getString(APP_PREFERENCES_UPLOAD_WEBSITE, "");

        if(uploadWebsiteString.equals("")){
            uploadWebsiteString = getResources().getString(R.string.default_upload_website);
        }

        mUploadWebsiteEditText.setText(uploadWebsiteString);


        int intervalInMinutes = mSharedPreferences.getInt(APP_PREFERENCES_INTERVAL_UPDATE_WEBSITE, 5);

        switch (intervalInMinutes) {
            case 5:
                mIntervalRadioGroup.check(R.id.interval5);
                break;
            case 10:
                mIntervalRadioGroup.check(R.id.interval10);
                break;
            case 15:
                mIntervalRadioGroup.check(R.id.interval15);
                break;
            case 20:
                mIntervalRadioGroup.check(R.id.interval20);
                break;
            case 25:
                mIntervalRadioGroup.check(R.id.interval25);
                break;
            case 30:
                mIntervalRadioGroup.check(R.id.interval30);
                break;
            case 60:
                mIntervalRadioGroup.check(R.id.interval60);
                break;
        }
    }

    private void saveInterval() {
        if (currentlyTracking) {
            Toast.makeText(getApplicationContext(), R.string.user_needs_to_restart_tracking, Toast.LENGTH_LONG).show();
        }

        SharedPreferences.Editor editor = mSharedPreferences.edit();

        switch (mIntervalRadioGroup.getCheckedRadioButtonId()) {
            case R.id.interval5:
                editor.putInt(APP_PREFERENCES_INTERVAL_UPDATE_WEBSITE, 5);
                break;
            case R.id.interval10:
                editor.putInt(APP_PREFERENCES_INTERVAL_UPDATE_WEBSITE, 10);
                break;
            case R.id.interval15:
                editor.putInt(APP_PREFERENCES_INTERVAL_UPDATE_WEBSITE, 15);
                break;
            case R.id.interval20:
                editor.putInt(APP_PREFERENCES_INTERVAL_UPDATE_WEBSITE, 20);
                break;
            case R.id.interval25:
                editor.putInt(APP_PREFERENCES_INTERVAL_UPDATE_WEBSITE, 25);
                break;
            case R.id.interval30:
                editor.putInt(APP_PREFERENCES_INTERVAL_UPDATE_WEBSITE, 30);
                break;
            case R.id.interval60:
                editor.putInt(APP_PREFERENCES_INTERVAL_UPDATE_WEBSITE, 60);
                break;
        }

        editor.apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /*switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }*/
    }

    private void getDeviceImei() {

        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String deviceID = mTelephonyManager.getDeviceId();

        // получить доступ к объекту Editor, чтобы изменить общие настройки.
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(APP_PREFERENCES_IMEI, deviceID);
        editor.apply();
    }

    // called when trackingButton is tapped
    protected void trackLocation() {
        //SharedPreferences sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        currentlyTracking = mSharedPreferences.getBoolean(APP_PREFERENCES_CURRENTLY_TRACKING, false);

        if (textWebsiteFieldAreEmptyOrHasSpaces()) {
            return;
        }

        if (currentlyTracking) {
            stopService(new Intent(MainActivity.this, LocationService.class));

            currentlyTracking = false;
            editor.putBoolean(APP_PREFERENCES_CURRENTLY_TRACKING, false);
            //editor.putString("sessionID", "");
        } else {
            //startAlarmManager();
            startService(new Intent(MainActivity.this, LocationService.class));

            currentlyTracking = true;
            editor.putBoolean(APP_PREFERENCES_CURRENTLY_TRACKING, true);

            uploadWebsiteString = mUploadWebsiteEditText.getText().toString();
            editor.putString(APP_PREFERENCES_UPLOAD_WEBSITE, uploadWebsiteString);
            //editor.putFloat("totalDistanceInMeters", 0f);
            //editor.putBoolean("firstTimeGettingPosition", true);
            //editor.putString("sessionID",  UUID.randomUUID().toString());
        }

        editor.apply();
        setTrackingButtonState();
    }

    private boolean textWebsiteFieldAreEmptyOrHasSpaces() {
        String websiteString = mUploadWebsiteEditText.getText().toString().trim();

        if (websiteString.length() == 0 || (websiteString.split(" ").length > 1)) {
            Toast.makeText(this, R.string.textfield_empty_or_spaces, Toast.LENGTH_LONG).show();
            return true;
        }

        return false;
    }


    private void setTrackingButtonState() {
        currentlyTracking = mSharedPreferences.getBoolean(APP_PREFERENCES_CURRENTLY_TRACKING, false);

        if (currentlyTracking) {
            mTrackingButton.setBackgroundResource(R.drawable.green_tracking_button);
            mTrackingButton.setTextColor(Color.BLACK);
            mTrackingButton.setText(R.string.tracking_is_on);
        } else {
            mTrackingButton.setBackgroundResource(R.drawable.red_tracking_button);
            mTrackingButton.setTextColor(Color.WHITE);
            mTrackingButton.setText(R.string.tracking_is_off);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        finish();
    }

    /*public void testPostObject() {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://45.55.132.244/blue_lainer/bl_api_v1/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Link mLink = retrofit.create(Link.class); //Создаём объект через который будем выполнять запрос
        GPSPost gpsPost = new GPSPost(imei, x, y, strTime, strSpeed);

        Call<ResponseBody> call = mLink.addPost(gpsPost);

        //String call = call.toString();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Get result Repo from response.body()
                Log.d(TAG, "onResponse");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure");
            }
        });

    }*/
}
