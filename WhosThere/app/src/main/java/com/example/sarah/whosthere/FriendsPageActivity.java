package com.example.sarah.whosthere;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FriendsPageActivity extends HomePage {

    //Button to connect to Facebook
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private LinearLayout friendsListLayout;

    private DatabaseReference mUserToPassDatabase = null;

    private List<String> friendsList;
    private List<String> addedFriends;

    String user_id;
    String token;

    String friend_id;
    private List<String> friend_friendsList;

    Boolean newUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        friendsListLayout = (LinearLayout)findViewById(R.id.friendsListLayout);

        callbackManager = CallbackManager.Factory.create();

        //Enable permissions to view public profile, friends list, and location
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends", "user_location"));

        newUser = true;
        friendsList = new ArrayList<>();
        friend_friendsList = new ArrayList<>();
        addedFriends = new ArrayList<>();

        //If already logged into facebook account populate the friends list
        if(Profile.getCurrentProfile() != null) {
            populateFriendsList();
        }

        //Log into facebook
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Log.d("FACEBOK LOGIN", "SUCCESS");
                user_id = loginResult.getAccessToken().getUserId();
                token = loginResult.getAccessToken().getToken();

                mUserToPassDatabase = FirebaseDatabase.getInstance().getReference("FacebookFriends");

                mUserToPassDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Log.d("SNAPSHOT", snapshot.toString());
                        for(DataSnapshot user : snapshot.getChildren()) {
                            Log.d("USER", user.getKey().toString());
                            if(user.getKey().equals(user_id)) {
                                Log.i("USER ID MATCH", user_id);
                                newUser = false;
                                friendsList = (ArrayList<String>)user.child("FriendsList").getValue();
                                if(friendsList == null) {
                                    friendsList = new ArrayList<String>();
                                }
                                addedFriends = (ArrayList<String>)user.child("AddedFriends").getValue();
                                if(addedFriends == null) {
                                    addedFriends = new ArrayList<String>();
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                //if facebook id does not exist yet in the database
                if(newUser) {
                    Log.d("NEW USER", "DOESN'T EXIST IN DATABASE");
                    mUserToPassDatabase.push();

                    mUserToPassDatabase.child(user_id).child("Location").setValue(null);
                    mUserToPassDatabase.child(user_id).child("FriendsList").setValue(friendsList);
                    mUserToPassDatabase.child(user_id).child("AddedFriends").setValue(addedFriends);
                } else {
                    Log.d("RETURNING USER", "ALREADY EXISTS IN DATABASE");

                }

                Log.i("FRIENDS LIST ", friendsList.toString());

                Log.i("user_id", user_id);
                Log.i("token", token);

               populateFriendsList();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }

        });

    }


    //Populate page with list of facebook friends who also have the app
    private void populateFriendsList() {
        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                AccessToken.getCurrentAccessToken().getUserId()+"/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        Log.d("RECEIVED FRIEND LIST", response.toString());
                        try {

                            JSONArray friends = response.getJSONObject().getJSONArray("data");


                            for(int i=0; i< friends.length(); i++) {

                                final JSONObject friend = friends.getJSONObject(i);

                                friend_id = null;

                                try {
                                    friend_id = friend.getString("id");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    continue;
                                }

                                Log.i("FRIEND", friend_id);
                                final TextView friendView = new TextView(FriendsPageActivity.this);
                                //ImageView profilePic = new ImageView(FriendsPageActivity.this);
                                //Bitmap profilePicture = getFacebookProfilePicture(String.valueOf(friend.getId()));
                                //profilePic.setImageBitmap(profilePicture);
                                final Button addFriend = new Button(getApplicationContext());

                                addFriend.setTag(friend_id);

                                if(addedFriends!= null && addedFriends.contains(friend_id)) {
                                    addFriend.setText("Friends");

                                } else {
                                    addFriend.setText("Add Friend");
                                }

                                addFriend.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Log.i("ON CLICK", friend.toString());

                                        friend_id = (String) addFriend.getTag();

                                        mUserToPassDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot snapshot) {
                                                friend_friendsList = (ArrayList<String>) snapshot.child(friend_id).child("FriendsList").getValue();
//                                                Log.d("SNAPSHOT", snapshot.toString());
//                                                boolean found = false;
//                                                for(DataSnapshot user : snapshot.getChildren()) {
//                                                    Log.d("USER", user.getKey().toString());
//
//                                                    if(user.getKey().equals(friend_id)) {
//                                                        found = true;
//                                                        Log.i("USER ID MATCH", friend_id);
//
//                                                        //add current user to friendslist of friend
//                                                        friend_friendsList = (ArrayList<String>)user.child("FriendsList").getValue();
//
//                                                        if(friend_friendsList!=null) {
//                                                            Log.i("RECEIVED FRIENDS LIST", friend_friendsList.toString());
//                                                        }
//
//                                                    }
//                                                }

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }

                                        });

                                        //add friend to list of added friends
                                        if(addedFriends == null) {
                                            addedFriends = new ArrayList<>();
                                        }
                                        addedFriends.add(friend_id);
                                        //add current user to friend's list of friend
                                        if(friend_friendsList == null) {
                                            friend_friendsList = new ArrayList<>();
                                        }
                                        friend_friendsList.add(user_id);

                                        mUserToPassDatabase.push();
                                        mUserToPassDatabase.child(friend_id).child("FriendsList").setValue(friend_friendsList);
                                        mUserToPassDatabase.child(user_id).child("AddedFriends").setValue(addedFriends);
                                        addFriend.setText("Friends");

                                        Log.i("Current User", user_id);
                                        Log.i("My added friends", addedFriends.toString());
                                        Log.i("Friend", friend_id);
                                        Log.i("Their friends list", friend_friendsList.toString());

                                    }
                                });
                                friendView.setText(friend.getString("name"));
                                friendsListLayout.addView(friendView);
                                friendsListLayout.addView(addFriend);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });


        request.executeAsync();

    }


//    public static Bitmap getFacebookProfilePicture(String userID){
//        URL imageURL = null;
//        try {
//            imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?type=large");
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        Bitmap bitmap = null;
//        try {
//            bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return bitmap;
//    }

    //Forward activity result to callback manager
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


}
