package com.example.sarah.whosthere;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.content.ContentResolver;
import android.provider.Settings;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by JustineSim on 11/20/17.
 */

/*  Added these permissions to Manifest

<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />*/


public class LocationActivity extends Activity implements LocationListener {

    private static final long FIVE_MINS = 5 * 60 * 1000;

    private static String TAG = "Whos-There";

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private LoginActivity.UserLoginTask mAuthTask = null;
    private FirebaseAuth mAuthority = null;
    private DatabaseReference mUserFriendsDatabase = null;

    private Location mLastLocationReading;

    // default minimum time between new readings
    private long mMinTime = 5000;

    // default minimum distance between old and new readings.
    private float mMinDistance = 1000.0f;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_home_page); not sure if this is right*/

        /*Create LocationManager instance*/
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        //create map here



    }

    public final static  int MY_PERMISSIONS_LOCATION= 4;

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                "android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(getApplicationContext(),
                "android.permission.ACCESS_COARSE_LOCATION") != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(LocationActivity.this,
                    new String[]{"android.permission.ACCESS_FINE_LOCATION",
                            "android.permission.ACCESS_COARSE_LOCATION"},
                    MY_PERMISSIONS_LOCATION);
        }else {
            getLocationUpdates();
        }

        mAuthority = FirebaseAuth.getInstance();
        mUserFriendsDatabase = FirebaseDatabase.getInstance().getReference("Facebook Friends");

        mUserFriendsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                    /*TODO - get facebook email/username of requestor*/
                    // -> change this to facebook profile email
                    //String email = mAuthority.getCurrentUser().getEmail();

                     /*TODO - store data structure pulled from firebase as variable*/
                    // some sort of data structure, list, HashMap<email string, location string>
                    HashMap<String, String> friendlist =
                            (HashMap<String, String>) userSnapshot.child("friendsList").getValue();

                     /*TODO - traverse through list data structure
                            1-  function calculate distance
                            2- display distance in miles on friendlist
                            3- notify if within 5 miles
                            4- update marker in map
                      */


                    Iterator friendListIterator = friendlist.keySet().iterator();
                    while(friendListIterator.hasNext()) {
                        String key=(String)friendListIterator.next();
                        String value=(String)friendlist.get(key);

                        //calculate function

                        // display distance in miles on friendlist - update friendlist

                        // notify if within 5 miles

                        // update marker in map
                    }



                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getLocationUpdates()
    {
        try {

            //  Check GPS_PROVIDER for an existing location reading.
            // Only keep this last reading if it is fresh - less than 5 minutes old.
            Location lastKnownLocation =
                    mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(lastKnownLocation != null && ageInMilliseconds(lastKnownLocation) < FIVE_MINS) {
                mLastLocationReading = lastKnownLocation;
            }

            // register to receive location updates from GPS_PROVIDER
            mLocationManager.requestLocationUpdates
                    (LocationManager.GPS_PROVIDER, mMinTime, mMinDistance, this);

        } catch (SecurityException e) {
            Log.d(TAG,e.getLocalizedMessage());
        }
    }

    private long ageInMilliseconds(Location location) {
        return System.currentTimeMillis() - location.getTime();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_LOCATION:
                int g = 0;
                Log.d(TAG, "Perm?: " + permissions.length + " -? " + grantResults.length);
                for (String perm : permissions) {
                    Log.d(TAG, "Perm: " + perm + " --> " + grantResults[g++]);
                }
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocationUpdates();
                } else {
                    Log.i(TAG, "Permission was not granted to access location");
                    finish();
                }
        }
    }

    @Override
    public void onLocationChanged(Location currentLocation) {

        //  Handle location updates
        // Cases to consider
        // 1) If there is no last location, keep the current location.
        // 2) If the current location is older than the last location, ignore
        // the current location
        // 3) If the current location is newer than the last locations, keep the
        // current location.

        if(mLastLocationReading == null || ageInMilliseconds(currentLocation)
                < ageInMilliseconds(mLastLocationReading)) {
            mLastLocationReading = currentLocation;
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
