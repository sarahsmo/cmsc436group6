package com.example.sarah.whosthere;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.login.LoginManager;
import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class HomePage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final long FIVE_MINS = 5 * 60 * 1000;

    private static String TAG = "Whos-There";

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private LoginActivity.UserLoginTask mAuthTask = null;
    private FirebaseAuth mAuthority = null;
    private DatabaseReference mUserToPassDatabase = null;

    private Location mLastLocationReading;

    // default minimum time between new readings
    private long mMinTime = 5000;

    // default minimum distance between old and new readings.
    private float mMinDistance = 1000.0f;

    private String userFacebookID;

    private ArrayList<String> friendsList;
    private String fireBaseLocation;

    private Location mInitialLocation;
    private Boolean mFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "On Create Called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getLocationUpdates();

        if(AccessToken.getCurrentAccessToken()!=null) {
            userFacebookID = AccessToken.getCurrentAccessToken().getUserId();

            mAuthority = FirebaseAuth.getInstance();
            mUserToPassDatabase = FirebaseDatabase.getInstance().getReference("FacebookFriends");
            mUserToPassDatabase.push();

            /*String mLastLocationReadingString = Double.toString(mLastLocationReading.getLatitude()) + "," +
                    Double.toString(mLastLocationReading.getLongitude());*/
            Log.i(TAG, "Set Location");
            mUserToPassDatabase.child(userFacebookID).child("Location").setValue(mLastLocationReading);

        }
        else {
            userFacebookID = null;
        }


        friendsList = new ArrayList<String>();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_page, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent i = new Intent();
        if (id == R.id.nav_home) {
            // Handle the camera action
            i = new Intent(this, HomePage.class);
        } else if (id == R.id.nav_friends) {
            i = new Intent(this, FriendsPageActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        } else if (id == R.id.nav_settings) {
            i = new Intent(this, SettingsActivity.class);
        } else if (id == R.id.nav_logout) {
            i = new Intent(this, LoginActivity.class);
            LoginManager.getInstance().logOut();
            FirebaseAuth.getInstance().signOut();
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(i);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public final static  int MY_PERMISSIONS_LOCATION= 4;

    @Override
    protected void onResume() {
        Log.i(TAG, "On Resume Called");
        super.onResume();
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                "android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(getApplicationContext(),
                "android.permission.ACCESS_COARSE_LOCATION") != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(HomePage.this,
                    new String[]{"android.permission.ACCESS_FINE_LOCATION",
                            "android.permission.ACCESS_COARSE_LOCATION"},
                    MY_PERMISSIONS_LOCATION);
        }else {
            Log.i(TAG, "Get Location Updates");
            getLocationUpdates();

            //updates firebase when you go back into the app
            mAuthority = FirebaseAuth.getInstance();
            mUserToPassDatabase = FirebaseDatabase.getInstance().getReference("FacebookFriends");

            if(AccessToken.getCurrentAccessToken()!= null) {
                userFacebookID = AccessToken.getCurrentAccessToken().getUserId();
            }

            if(userFacebookID != null) {
                Log.i(TAG, "User Facebook Not Null");
                mUserToPassDatabase.push();
                /*String mLastLocationReadingString = Double.toString(mLastLocationReading.getLatitude()) + "," +
                        Double.toString(mLastLocationReading.getLongitude());*/
                Log.i(TAG, "Set Location");
                if(mLastLocationReading == null){
                    Log.i(TAG, "location is NULL");

                    mInitialLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(mInitialLocation == null) {
                        Log.i(TAG, " initial location is NULL");
                    } else {
                        Log.i(TAG, "initial location is: " + mInitialLocation.toString());
                        mUserToPassDatabase.child(userFacebookID).child("Location").setValue(mInitialLocation);
                    }


                }else{
                    Log.i(TAG, "location is: " + mLastLocationReading.toString());
                }
                //mUserToPassDatabase.child(userFacebookID).child("Location").setValue(mLastLocationReading);
            }



            mUserToPassDatabase.addValueEventListener(new ValueEventListener() {

                TreeMap<Float, LinkedList<String>> distances = new TreeMap<Float, LinkedList<String>>();

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                            if(userSnapshot.getKey().equals(userFacebookID)) {

                                friendsList = (ArrayList<String>)userSnapshot.child("FriendsList").getValue();
                                if(friendsList != null) {
                                    for(DataSnapshot friendSnapshot : dataSnapshot.getChildren()){
                                        if(friendsList.contains(friendSnapshot.getKey())){
                                            float distance = 0;
                                            Double latitude = (Double) friendSnapshot.child("Location").child("latitude").getValue();
                                            Double longitude = (Double) friendSnapshot.child("Location").child("longitude").getValue();

                                            Location friendLocation = null;

                                            if(latitude!=null && longitude!=null) {
                                                friendLocation = new Location("");
                                                friendLocation.setLatitude(latitude);
                                                friendLocation.setLongitude(longitude);
                                            }

                                            if(friendLocation != null) {
                                                distance = mLastLocationReading.distanceTo(friendLocation);
                                                if(!distances.containsKey(distance)){
                                                    distances.put(distance, new LinkedList<String>());
                                                }

                                                distances.get(distance).add((String) friendSnapshot.child("Name").getValue());
                                            }
                                        }
                                    }
                                }
                            }
                            updateView();
                            break;
                    }
                }


                private void updateView(){

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, new LinkedList<String>());

                    for(Map.Entry<Float, LinkedList<String>> entry : distances.entrySet()){
                        double miles = entry.getKey() * 0.000621371;
                        ListView view;

                        if(miles > 20){
                            continue;
                        }
                        else if(miles > 10){
                            //They're within 20 miles
                            view = (ListView) findViewById(R.id.within20miles);
                        }
                        else if(miles > 5){
                            //They're within 10 miles
                            view = (ListView) findViewById(R.id.within10miles);
                        }
                        else if(miles > 1){
                            //They're within 5 miles
                            view = (ListView) findViewById(R.id.within5miles);
                        }else if(miles > 0.095){
                            //They're within a mile
                            view = (ListView) findViewById(R.id.within1mile);
                        }else{
                            //They're within 500 feet
                            view = (ListView) findViewById(R.id.within500feet);
                        }

                        for(String name : entry.getValue()){
                            view.
                        }
                    }
                }



                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
    }

    private void getLocationUpdates()
    {
        try {
            Log.i(TAG, "Get Location Updates Method Enter");

            //  Check GPS_PROVIDER for an existing location reading.
            // Only keep this last reading if it is fresh - less than 5 minutes old.
            Location lastKnownLocation =
                    mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(lastKnownLocation == null){
                Log.i(TAG, "Last Known Location Is Null");
            }
            //Log.i(TAG, "Last Known Location: " + lastKnownLocation.toString());
            if(lastKnownLocation != null && ageInMilliseconds(lastKnownLocation) < FIVE_MINS) {
                Log.i(TAG, "Get Location Updates If Statement Good");
                mLastLocationReading = lastKnownLocation;
            }

            // register to receive location updates from GPS_PROVIDER
            mLocationManager.requestLocationUpdates
                    (LocationManager.GPS_PROVIDER, mMinTime, mMinDistance, locationListener);

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

    LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location currentLocation) {

            Log.i(TAG, "ON LOCATION CHANGED");

            if(mLastLocationReading == null || ageInMilliseconds(currentLocation)
                    < ageInMilliseconds(mLastLocationReading)) {
                mLastLocationReading = currentLocation;
            }

            if(AccessToken.getCurrentAccessToken()!=null) {
                userFacebookID = AccessToken.getCurrentAccessToken().getUserId();
            //updates firebase when your location changes
                mAuthority = FirebaseAuth.getInstance();
                mUserToPassDatabase = FirebaseDatabase.getInstance().getReference("FacebookFriends");

                //String userFacebookID = AccessToken.getCurrentAccessToken().getUserId();
                /*String mLastLocationReadingString = Double.toString(mLastLocationReading.getLatitude()) + "," +
                        Double.toString(mLastLocationReading.getLongitude());*/
                mUserToPassDatabase.push();
                mUserToPassDatabase.child(userFacebookID).child("Location").setValue(mLastLocationReading);
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
    };


}
