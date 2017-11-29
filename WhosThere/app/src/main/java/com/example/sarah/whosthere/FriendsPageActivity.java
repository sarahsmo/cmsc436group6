package com.example.sarah.whosthere;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;

public class FriendsPageActivity extends HomePage {

    //Button to connect to Facebook
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private LinearLayout friendsListLayout;

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

        //Enable permissions to view public profile and friends list
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_friends"));

        //If already logged into facebook account populate the friends list
        if(Profile.getCurrentProfile() != null) {
            populateFriendsList();
        }

        //Log into facebook
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
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
                                TextView friend = new TextView(FriendsPageActivity.this);
                                friend.setText(friends.get(i).toString());
                                friendsListLayout.addView(friend);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });


        request.executeAsync();

    }


    //Forward activity result to callback manager
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }



}
