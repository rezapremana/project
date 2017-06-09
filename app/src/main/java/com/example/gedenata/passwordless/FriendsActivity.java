package com.example.gedenata.passwordless;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class FriendsActivity extends AppCompatActivity {

    TextView emptyText;
    RecyclerView recyclerView;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        FontHelper.setCustomTypeface(findViewById(R.id.view_root));

        if (AccessToken.getCurrentAccessToken() == null) {
            finish();
            return;
        }

        emptyText = (TextView) findViewById(R.id.empty_text);
        recyclerView = (RecyclerView) findViewById(R.id.friends_list);
        callbackManager = CallbackManager.Factory.create();

        Set permissions = AccessToken.getCurrentAccessToken().getPermissions();
        if (permissions.contains("user_friends")) {
            fetchFriends();
        } else {
            LoginManager loginManager = LoginManager.getInstance();
            loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    fetchFriends();
                }

                @Override
                public void onCancel() {
                    String permissionMsg = getResources().getString(R.string.permission_message);
                    Toast.makeText(FriendsActivity.this, permissionMsg, Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onError(FacebookException error) {

                }
            });
            loginManager.logInWithReadPermissions(this, Arrays.asList("user_friends"));
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void fetchFriends() {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, name, picture");
        parameters.putInt("limit", 100);
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        if (response.getError() != null) {
                            Toast.makeText(FriendsActivity.this, response.getError().getErrorMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        List<FriendsAdapter.FriendItem> friendList = new ArrayList<>();

                        JSONObject jsonResponse = response.getJSONObject();
                        try {
                            JSONArray jsonData = jsonResponse.getJSONArray("data");
                            for (int i = 0; i < jsonData.length(); i++) {
                                JSONObject jsonUser = jsonData.getJSONObject(i);
                                String id = jsonUser.getString("id");
                                String name = jsonUser.getString("name");
                                String image = jsonUser.getJSONObject("picture").getJSONObject("data").getString("url");

                                FriendsAdapter.FriendItem friend = new FriendsAdapter.FriendItem(id, name, image);
                                friendList.add(friend);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        FriendsAdapter friendsAdapter = new FriendsAdapter(friendList);
                        recyclerView.setAdapter(friendsAdapter);

                        if (friendList.size() == 0) {
                            emptyText.setVisibility(View.VISIBLE);
                        }
                    }
                }
        ).executeAsync();
    }
}
