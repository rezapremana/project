package com.example.gedenata.passwordless;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;

import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import static java.util.Arrays.asList;

public class AccountActivity extends AppCompatActivity {

    TextView id;
    TextView info;
    TextView infoLabel;
    TextView location;
    TextView locationLabel;
    ImageView profilePic;

    ProfileTracker profileTracker;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FontHelper.setCustomTypeface(findViewById(R.id.view_root));

        id = (TextView) findViewById(R.id.id);
        info = (TextView) findViewById(R.id.info);
        infoLabel = (TextView) findViewById(R.id.info_label);
        location = (TextView) findViewById(R.id.location);
        locationLabel = (TextView) findViewById(R.id.location_label);
        profilePic = (ImageView) findViewById(R.id.profile_image);

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile != null) {
                    displayProfileInfo(currentProfile);
                }
            }
        };

        if (AccessToken.getCurrentAccessToken() != null) {
            Profile currentProfile = Profile.getCurrentProfile();
            if (currentProfile != null) {
                displayProfileInfo(currentProfile);
            } else {
                Profile.fetchProfileForCurrentAccessToken();
            }
        } else {
            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(final Account account) {
                    String accountKitId = account.getId();
                    id.setText(accountKitId);

                    PhoneNumber phoneNumber = account.getPhoneNumber();
                    if (account.getPhoneNumber() != null) {
                        String formattedPhoneNumber = formatPhoneNumber(phoneNumber.toString());
                        info.setText(formattedPhoneNumber);
                        infoLabel.setText(R.string.email_label);
                    }
                }

                @Override
                public void onError(final AccountKitError error) {
                    String toastMessage = error.getErrorType().getMessage();
                    Toast.makeText(AccountActivity.this, toastMessage, Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        profileTracker.stopTracking();
    }

    public void onLogout(View view) {
        AccountKit.logOut();
        LoginManager.getInstance().logOut();

        setResult(RESULT_OK);
        finish();
    }

    private void displayProfileInfo(Profile profile) {
        callbackManager = CallbackManager.Factory.create();
        Set permission = AccessToken.getCurrentAccessToken().getPermissions();
        if (permission.contains("user_location")) {
            fetchLocation();
        } else {
            LoginManager loginManager = LoginManager.getInstance();
            loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    fetchLocation();
                }

                @Override
                public void onCancel() {
                    String permissionMsg = getResources().getString(R.string.location_permission_message);
                    Toast.makeText(AccountActivity.this, permissionMsg, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(FacebookException error) {

                }
            });
            loginManager.logInWithReadPermissions(this, Arrays.asList("user_location"));
        }

        String profileId = profile.getId();
        id.setText(profileId);

        String name = profile.getName();
        info.setText(name);
        infoLabel.setText(R.string.name_label);

        Uri profilePicUri = profile.getProfilePictureUri(100, 100);
        displayProfilePic(profilePicUri);
    }

    private String formatPhoneNumber(String phoneNumber) {
        try {
            PhoneNumberUtil pnu = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber pn = pnu.parse(phoneNumber, Locale.getDefault().getCountry());
            phoneNumber = pnu.format(pn, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return phoneNumber;
    }

    private void displayProfilePic(Uri uri) {
        Transformation transformation = new RoundedTransformationBuilder()
                .cornerRadiusDp(30)
                .oval(false)
                .build();
        Picasso.with(AccountActivity.this)
                .load(uri)
                .placeholder(R.drawable.icon_profile_empty)
                .transform(transformation)
                .into(profilePic);
    }

    private void fetchLocation() {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "location");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        if (response.getError() != null) {
                            Toast.makeText(AccountActivity.this, response.getError().getErrorMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        JSONObject jsonResponse = response.getJSONObject();
                        try {
                            JSONObject locationObj = jsonResponse.getJSONObject("location");
                            String locationStr = locationObj.getString("name");
                            location.setText(locationStr);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
