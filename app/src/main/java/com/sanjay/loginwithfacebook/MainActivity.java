package com.sanjay.loginwithfacebook;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private Button btn_sign_in, btn_logout;
    private ImageView img_profile;
    private TextView tv_info;
    private LoginButton loginButton;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        callbackManager = CallbackManager.Factory.create();

        initUI();
    }

    private void initUI() {
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(getResources().getString(R.string.app_name));
        btn_sign_in = findViewById(R.id.btn_sign_in);
        btn_logout = findViewById(R.id.btn_logout);
        img_profile = findViewById(R.id.img_profile);
        tv_info = findViewById(R.id.tv_info);
        loginButton = findViewById(R.id.login_button);

        //Default Login Button
       /* loginButton.setReadPermissions("public_profile", "email");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Utils.showToast(MainActivity.this, "Login Successfully.");
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                getUserData(object);
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, email, gender, birthday, location");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Utils.showToast(MainActivity.this, "Login Cancel.");
                updateUI(false);
            }

            @Override
            public void onError(FacebookException exception) {
                Utils.showToast(MainActivity.this, "Login Error.");
                updateUI(false);
            }
        });*/
    }

    //Custom Login Button
    public void onClickSignIn(View view) {
        if (Utils.isNetworkAvailable(this)) {
            LoginManager.getInstance().logInWithReadPermissions(
                    this, Arrays.asList("public_profile", "email"));

            LoginManager.getInstance().registerCallback(
                    callbackManager, new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            Utils.showToast(MainActivity.this, "Login Successfully.");
                            GraphRequest request = GraphRequest.newMeRequest(
                                    loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(JSONObject object, GraphResponse response) {
                                            getUserData(object);
                                        }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "id, first_name, last_name, email, gender, birthday, location");
                            request.setParameters(parameters);
                            request.executeAsync();
                        }

                        @Override
                        public void onCancel() {
                            Utils.showToast(MainActivity.this, "Login Cancel.");
                            updateUI(false);
                        }

                        @Override
                        public void onError(FacebookException e) {
                            Utils.showToast(MainActivity.this, "Login Error.");
                            updateUI(false);
                        }
                    });
        } else {
            Utils.showAlertDialog(this, getResources().getString(R.string.alert)
                    , getResources().getString(R.string.internet_error));
        }
    }

    //Get User Profile Information
    private void getUserData(JSONObject object) {
        try {
            updateUI(true);
            StringBuilder stringBuilder = new StringBuilder();
            if (object.has("first_name")) {
                stringBuilder.append("First Name: " + object.getString("first_name"));
            }
            if (object.has("last_name")) {
                stringBuilder.append("\nLast Name: " + object.getString("last_name"));
            }
            if (object.has("email")) {
                stringBuilder.append("\nEmail: " + object.getString("email"));
            }
            if (object.has("gender")) {
                stringBuilder.append("\nGender: " + object.getString("gender"));
            }
            if (object.has("birthday")) {
                stringBuilder.append("\nBirthday: " + object.getString("birthday"));
            }
            if (object.has("location")) {
                stringBuilder.append("\nLocation: " + object.getJSONObject("location").getString("name"));
            }
            tv_info.setText(stringBuilder.toString());

            //Profile Picture
            String id = object.getString("id");
            try {
                URL profile_pic = new URL("https://graph.facebook.com/"
                        + id + "/picture?");
                Glide.with(getApplicationContext()).load(profile_pic.toString())
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(img_profile);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    //UpDate UI
    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            btn_sign_in.setVisibility(View.GONE);
            btn_logout.setVisibility(View.VISIBLE);
            img_profile.setVisibility(View.VISIBLE);
            tv_info.setVisibility(View.VISIBLE);
        } else {
            btn_sign_in.setVisibility(View.VISIBLE);
            btn_logout.setVisibility(View.GONE);
            img_profile.setVisibility(View.GONE);
            tv_info.setVisibility(View.GONE);
        }
    }

    //Facebook Logout
    public void onClickLogout(View view) {
        if (AccessToken.getCurrentAccessToken() == null) {
            updateUI(false);
            return;
        }
        new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me/permissions/", null,
                HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                LoginManager.getInstance().logOut();
                Utils.showToast(MainActivity.this, "Logout Successfully.");
                updateUI(false);
            }
        }).executeAsync();
    }
}


