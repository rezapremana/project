package com.example.gedenata.passwordless;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static int ACCOUNT_ACTIVITY_REQUEST_CODE = 1;
    ProfileTracker profileTracker;
    ImageView friendsButton;
    ImageView accountButton;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        friendsButton = (ImageView) findViewById(R.id.friends_button);
        accountButton = (ImageView) findViewById(R.id.account_button);
        recyclerView = (RecyclerView) findViewById(R.id.destination_list);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null) {
            friendsButton.setVisibility(View.GONE);
        } else {
            friendsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, FriendsActivity.class);
                    startActivity(intent);
                }
            });
        }

        accountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                startActivityForResult(intent, ACCOUNT_ACTIVITY_REQUEST_CODE);

            }
        });

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile != null) {
                    displayProfilePic(currentProfile);
                }
            }
        };

        Profile currentProfile = Profile.getCurrentProfile();
        if (currentProfile != null) {
            displayProfilePic(currentProfile);
        } else {
            Profile.fetchProfileForCurrentAccessToken();
        }

        if (accessToken != null && accessToken.getPermissions().contains("user_friends")) {

            Bundle parameters = new Bundle();
            parameters.putString("fields", "picture");
            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/me/friends",
                    parameters,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {
                            ArrayList<String> friendsPics = new ArrayList<>();

                            if (response.getError() == null) {
                                JSONObject jsonResponse = response.getJSONObject();
                                try {
                                    JSONArray jsonData = jsonResponse.getJSONArray("data");
                                    for (int i = 0; i < jsonData.length(); i++) {
                                        JSONObject jsonUser = jsonData.getJSONObject(i);
                                        String image = jsonUser.getJSONObject("picture").getJSONObject("data").getString("url");
                                        friendsPics.add(image);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            recyclerView.setAdapter(new DestinationsAdapter(friendsPics));
                        }
                    }
            ).executeAsync();
        } else {
            recyclerView.setAdapter(new DestinationsAdapter());
        }

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACCOUNT_ACTIVITY_REQUEST_CODE && requestCode == RESULT_OK) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        profileTracker.stopTracking();
    }

    private void displayProfilePic(Profile profile) {
        Uri uri = profile.getProfilePictureUri(28, 28);
        Transformation transformation = new RoundedTransformationBuilder()
                .cornerRadiusDp(30)
                .oval(false)
                .build();
        Picasso.with(this)
                .load(uri)
                .placeholder(R.drawable.icon_profile_empty)
                .transform(transformation)
                .into(accountButton);
    }
}