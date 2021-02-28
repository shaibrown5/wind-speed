package com.example.wind_speed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;



import java.util.Arrays;

public class FacebookLoginActivity extends Activity {

    LoginButton fbLogin;
    private CallbackManager callbackManager;
    private static final String FBTAG = "facebook login activity";
    private static final String EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //handle login responses
        super.onCreate(savedInstanceState);

        callbackManager = CallbackManager.Factory.create();

        fbLogin.setPermissions(Arrays.asList(EMAIL));

        // Callback registration
        fbLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(FBTAG, "login was success");
                String userId = loginResult.getAccessToken().getUserId();
                setResult(RESULT_OK);
                Log.i(FBTAG, userId);
                finish();
            }

            @Override
            public void onCancel() {
                // App code
                Log.i(FBTAG, "login was canceled");
                setResult(RESULT_CANCELED);
                finish();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.e(FBTAG, "login encountered error");
                // TODO
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }


}
