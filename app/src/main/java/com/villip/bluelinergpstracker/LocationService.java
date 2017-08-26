package com.villip.bluelinergpstracker;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationService extends Service {
    private static final String TAG = "myLOGLocationService";

    private FusedLocationProviderClient mFusedLocationClient;

    private double longitude, latitude;

    private String imei, strTime, strSpeed;

    private SharedPreferences mSharedPreferences;
    private static final String APP_PREFERENCES = "mySettings";
    private static final String APP_PREFERENCES_IMEI = "imei";
    private static final String APP_PREFERENCES_INTERVAL_UPDATE_WEBSITE = "intervalUpdateWebsite";
    private static final String APP_PREFERENCES_UPLOAD_WEBSITE = "uploadWebsite";

    private LocationRequest mLocationRequest;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    /**
     * The current location.
     */
    private Location mLocation;

    //The identifier for the notification displayed for the foreground service.
    private static final int NOTIFICATION_ID = 12345678;

    private Handler mServiceHandler;

    private NotificationManager mNotificationManager;

    private Link mLink;

    //private Handler handler;

    //private int intervalUpdateWebsite;

    //LocationManager mLocationManager;
    //LocationListener mLocationListener;

    //private Runnable mRunnable;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        mSharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        String uploadWebsite = mSharedPreferences.getString(APP_PREFERENCES_UPLOAD_WEBSITE, "");

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(uploadWebsite)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mLink = retrofit.create(Link.class);

   /*     // Acquire a reference to the system Location Manager
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                mLocation = location;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        intervalUpdateWebsite = mSharedPreferences.getInt(APP_PREFERENCES_INTERVAL_UPDATE_WEBSITE, 5)*1000;

        requestRepeatingLocationUpdates();
*/


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        createLocationRequest();
        getLastLocation();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                sendLocationDataToWebsite(locationResult.getLastLocation());
            }
        };

        requestLocationUpdates();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        startForeground(NOTIFICATION_ID, getNotification());

        return START_STICKY;
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {
        Log.i(TAG, "getNotification");

            //These three lines makes Notification to open activity after clicking on it
            Intent notificationIntent = new Intent(this, StartPasswordActivity.class);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            notificationIntent.setAction(Intent.ACTION_VIEW);
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentIntent(contentIntent)
                    .setOngoing(true)   //Can't be swiped out
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    //.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.large))   // большая картинка
                    //.setTicker(Ticker)
                    .setContentTitle(getResources().getString(R.string.gps_tracker_is_working)) //Заголовок
                    .setContentText("text")//.setContentText(getResources().getString(R.string.click_to_go_to_the_application)) // Текст уведомления
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(getLocationText()))
                    .setWhen(System.currentTimeMillis());

            //Notification notification = new Notification.BigTextStyle().bigText(getLocationText());
            Notification notification;
            if (android.os.Build.VERSION.SDK_INT<=15) {
                notification = builder.getNotification(); // API-15 and lower
            }else{
                notification = builder.build();
            }

            return notification;

            //startForeground(DEFAULT_NOTIFICATION_ID, notification);
        }

    private String getLocationText() {
        return mLocation == null ? "Unknown location" :
                "long: " + longitude
                + "\n" + "lat: " + latitude
                + "\n" + "time: " + strTime
                + "\n" + "speed: " + strSpeed ;
    }

        /*private void requestRepeatingLocationUpdates() {
            Log.i(TAG, "requestRepeatingLocation");

            // Create the Handler object (on the main thread by default)
            handler = new Handler();
           // Define the code block to be executed
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "run");

                    // Do something here on the main thread


                    try {
                        //Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        sendLocationDataToWebsite();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }

                    // Repeat this the same runnable code block again another 2 seconds
                    // 'this' is referencing the Runnable object
                    handler.postDelayed(this, intervalUpdateWebsite);
                }
            };
            // Start the initial runnable task by posting through the handler
            handler.post(mRunnable);
        }*/

        private void getLastLocation() {
            Log.i(TAG, "getLastLocation");
            try {
                mFusedLocationClient.getLastLocation()
                        .addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    mLocation = task.getResult();
                                } else {
                                    Log.w(TAG, "Failed to get location.");
                                }
                            }
                        });

            } catch (SecurityException unlikely) {
                Log.e(TAG, "Lost location permission." + unlikely);
            }
        }



    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        Log.i(TAG, "createLocationRequest");

        int intervalUpdateWebsite = mSharedPreferences.getInt(APP_PREFERENCES_INTERVAL_UPDATE_WEBSITE, 5)*1000;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(intervalUpdateWebsite);
        mLocationRequest.setFastestInterval(intervalUpdateWebsite/2);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void requestLocationUpdates() {
        Log.i(TAG, "requestLocationUpdates");
        //Utils.setRequestingLocationUpdates(this, true);
        //startService(new Intent(getApplicationContext(), LocationService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            //Utils.setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    /*private void onNewLocation(Location location) {
        Log.i(TAG, "onNewLocation");

        mLocation = location;

        if (location != null) {
            String imei = mSharedPreferences.getString(APP_PREFERENCES_IMEI, "");


            longitude = mLocation.getLongitude();
            latitude = mLocation.getLatitude();

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            strTime = dateFormat.format(new Date(location.getTime()));

            strSpeed = String.valueOf(mLocation.getSpeed()) + " km/h";

            Log.d(TAG, "imei: " + imei
                    + "\n" + "longitude: " + longitude
                    + "\n" + "latitude: " + latitude
                    + "\n" + "time: " + strTime
                    + "\n" + "speed: " + strSpeed
            );

            Toast.makeText(getApplicationContext(), "imei: " + imei
                    + "\n" + "longitude: " + longitude
                    + "\n" + "latitude: " + latitude
                    + "\n" + "time: " + strTime
                    + "\n" + "speed: " + strSpeed, Toast.LENGTH_LONG).show();
        }
    }
*/
    private void sendLocationDataToWebsite(Location location){
        imei = mSharedPreferences.getString(APP_PREFERENCES_IMEI, "");

        mLocation = location;

        longitude = mLocation.getLongitude();
        latitude = mLocation.getLatitude();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        strTime = dateFormat.format(new Date(mLocation.getTime()));

        strSpeed = String.valueOf(mLocation.getSpeed()) + " km/h";


        GPSPost gpsPost = new GPSPost(imei, longitude, latitude, strTime, strSpeed);

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



        Log.d(TAG, "imei: " + imei
                + "\n" + "longitude: " + longitude
                + "\n" + "latitude: " + latitude
                + "\n" + "time: " + strTime
                + "\n" + "speed: " + strSpeed
        );

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }

       /* Toast.makeText(getApplicationContext(), "imei: " + imei
                + "\n" + "longitude: " + longitude
                + "\n" + "latitude: " + latitude
                + "\n" + "time: " + strTime
                + "\n" + "speed: " + strSpeed, Toast.LENGTH_LONG).show();*/
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");

        // Removes pending code execution
        //handler.removeCallbacks(mRunnable);
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        mServiceHandler.removeCallbacksAndMessages(null);
    }
}
