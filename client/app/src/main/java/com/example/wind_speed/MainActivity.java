package com.example.wind_speed;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    EditText userName;
    EditText pass;
    Button signUp;
    private LoginButton fbLogin;
    private CallbackManager callbackManager;
    private static final String EMAIL = "email";
    private static final String LOCATION = "user_location";
    private static final String TAG = "MainActivity";
    private static final String FBTAG = "facebook main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signUp = (Button) findViewById(R.id.signUpButton);
        fbLogin = (LoginButton) findViewById(R.id.fbLoginButton);

        // when sign up is clicked, go to Sign up page
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SignUp.class);
                startActivity(intent);
            }
        });

        callbackManager = CallbackManager.Factory.create();

        fbLogin.setPermissions(Arrays.asList(EMAIL, LOCATION));

        // Callback registration
        fbLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(FBTAG, "login was success");
                String userId = loginResult.getAccessToken().getUserId();
                setResult(RESULT_OK);
                Log.i(FBTAG, userId);
                // TODO MOVE TO NEXT PAGE
            }

            @Override
            public void onCancel() {
                // App code
                Log.i(FBTAG, "login was canceled");
                setResult(RESULT_CANCELED);
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.e(FBTAG, "login encountered error");
                // TODO
            }
        });

    }

    //called whn an activity I launch exists.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean checkInput() {
        boolean isValid = true;

        if (userName.getText().toString().isEmpty()) {
            isValid = false;
            userName.setError("Must not be empty");
        } else {
            String emailText = userName.getText().toString();
            isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches();
            if (!isValid) {
                userName.setError("Not a valid username");
            }
        }
        if (pass.getText().toString().isEmpty()) {
            isValid = false;
            pass.setError("Must not be empty");
        }

        return isValid;
    }

}