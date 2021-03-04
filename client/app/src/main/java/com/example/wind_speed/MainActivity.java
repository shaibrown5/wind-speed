package com.example.wind_speed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public static EditText userName;
    EditText pass;
    Button signUp;
    Button loginButton;
    private LoginButton fbLogin;
    private CallbackManager callbackManager;
    protected Bundle facebookInfoBundle;
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
        loginButton = (Button) findViewById(R.id.loginButton);
        userName = (EditText) findViewById(R.id.userName);
        pass = (EditText) findViewById(R.id.userPassword);

        // when sign up is clicked, go to Sign up page
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SignUp.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInput()){
                    //TODO LOGIN....
                    Log.i(TAG, "inputs are correct form");
                    Intent intent = new Intent(getApplicationContext(), HomePage.class);
                    Log.i(TAG, "username is :" + userName.getText().toString());
                    intent.putExtra("username",userName.getText().toString());
                    resetInputs();

                    startActivity(intent);
                }

                resetInputs();
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
                Intent intent = new Intent(getApplicationContext(), HomePage.class);
                startActivity(intent);
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

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        Log.d(TAG, "user token - " + token);
                    }
                });

    }

    //called when an activity I launch exists.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // retrieve users own profile
        // response in the form of json we can get the data from
        GraphRequest graphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        // this is how the data is shown:
                        // {"name":"Shai Brown","email":"email@gmail.com","location":{"id":"long_id","name":"city_name, country"},"first_name":"Shai","last_name":"Brown","id":"long_id"}
                        try {
                            Log.d(FBTAG, object.toString());
                            String name = object.getString("name");
                            String id = object.getString("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        // retreive info from graph api
        facebookInfoBundle = new Bundle();
        facebookInfoBundle.putString("fields", "id, name, email, location, first_name, last_name");

        graphRequest.setParameters(facebookInfoBundle);
        graphRequest.executeAsync();
    }

    /**
     * Used to track the access token.
     * Whenever the access token changes, the method onCurrentAccessTokenChanged is automatically called.
     */
    AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if (currentAccessToken == null){
                LoginManager.getInstance().logOut();
                Log.i(FBTAG, " user is logged out of facebook");
            }
            else{
                Intent intent = new Intent(getApplicationContext(), HomePage.class);
                startActivity(intent);
            }
        }
    };

    /**
     * THis will be called before the activity is destroyed
     * Recommended in the FB api to stop tracking the access token
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    private boolean checkInput() {
        boolean isValid = true;

        if (userName.getText().toString().isEmpty()) {
            isValid = false;
            userName.setError("Must not be empty");
        }
        else{
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

    private void resetInputs(){
        Log.i(TAG, "reseting inputs");
        userName.setText(null);
        userName.setHint("Username");
        pass.setText(null);
        pass.setHint("Password");
    }

}
