package com.example.wind_speed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
    private RequestQueue m_queue;
    private String token = "";
    private static final String EMAIL = "email";
    private static final String LOCATION = "user_location";
    private static final String TAG = "MainActivity";
    private static final String FBTAG = "facebook main";
    private static final String m_REQUEST_URL = "http://10.0.2.2:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_queue = Volley.newRequestQueue(this);

        signUp = (Button) findViewById(R.id.signUpButton);
        fbLogin = (LoginButton) findViewById(R.id.fbLoginButton);
        loginButton = (Button) findViewById(R.id.loginButton);
        userName = (EditText) findViewById(R.id.userName);
        pass = (EditText) findViewById(R.id.userPassword);

        // when sign up is clicked, goes to Sign up page
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
                    Log.i(TAG, "[INPUTS] inputs are correct form");
                    LogUserIn(userName.getText().toString(), pass.getText().toString(), false);
                }

                Log.i(TAG, "[INPUTS] inputs are not in correct form");
                //resetInputs();
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
            }

            @Override
            public void onCancel() {
                // App code
                Log.i(FBTAG, "login was canceled");
                setResult(RESULT_CANCELED);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.e(FBTAG, "login encountered error");
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
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
                        token = task.getResult();
                        Log.d(TAG, "user token - " + token);
                    }
                });

    }

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
                            // get user info from facebook
                            String fbEmail = object.getString("email");
                            Log.d(FBTAG, "[FACEBOOK OC] the user email is " + fbEmail);
                            String id = object.getString("id");
                            Log.d(FBTAG, "[FACEBOOK OC] the user is is " + id);
                            String firstName = object.getString("first_name");
                            Log.d(FBTAG, "[FACEBOOK OC] the user firstName is " + firstName);
                            String lastName = object.getString("last_name");
                            Log. d(FBTAG, "[FACEBOOK OC] the user lastName is " + lastName);

                            // send message tos erver to see if he is logged in
                            Log.d(FBTAG, "[FACEBOOK DB] checking if user is int DB");
                            facebookLogin(fbEmail, id, firstName, lastName, token);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(FBTAG, "[ERROR] with fabook on complete");
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

    /**
     * This method checks that the input is in proper form
     * @return true if the input is correct, false otherwise
     */
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


    /**
     * This method resets the input to their original hints
     */
    private void resetInputs(){
        Log.i(TAG, "reseting inputs");
        userName.setText(null);
        userName.setHint("Username");
        pass.setText(null);
        pass.setHint("Password");
    }

    /**
     * This method checks the users login credentials and logs him in if he is in the system
     * @param email - the users email/ username
     * @param password - the users password
     */
    private void LogUserIn(String email, String password, boolean isFaceBook){
        JSONObject requestObject = new JSONObject();

        try {
            requestObject.put("email", email);
            requestObject.put("password", password);
        }
        catch (JSONException e) {
            resetInputs();
            Log.e(TAG, "[ERROR] in verifying user");
            Toast.makeText(MainActivity.this, "Please re-enter user and pass", Toast.LENGTH_SHORT).show();
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,  m_REQUEST_URL + email + "/check",
                requestObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "[RESPONSE] Post went through");
                Log.d(TAG, "[RESPONSE] " + response.toString());

                try {
                    String respMessage = response.getString("msg");
                    boolean isMatch = response.getBoolean("match");
                    Log.i(TAG, "[RESPONSE MESG] " +  respMessage);

                    // if the user has been found in the db then login, else show error
                    if (isMatch){
                        Toast.makeText(MainActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), HomePage.class);
                        intent.putExtra("username",userName.getText().toString());
                        Log.i(TAG,"CHECK "+userName.getText().toString());
                        resetInputs();
                        startActivity(intent);
                    }
                    else{
                        userName.setError("Username and password do not match");
                        Toast.makeText(MainActivity.this, "Wrong username/password", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    resetInputs();
                    Log.e(TAG,"[ERROR] got a json exception");
                    Toast.makeText(MainActivity.this, "Error occured Please re-enter user and pass", Toast.LENGTH_SHORT).show();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "[RESPONSE ERROR] Failed to Log in - " + error);
                    }
                });
        m_queue.add(req);
    }


    /**
     * This method sends the user data to the server to sign them up with their fb credentials
     * @param email - the users email/ username
     * @param password - the users password
     * @param firstName - the users first name
     * @param lastName - the users last name
     * @param token - the users unique firebase token
     */
    public void signFaceBookUserUp(String email, String password, String firstName, String lastName, String token){
        JSONObject requestObject = new JSONObject();

        try {
            requestObject.put("email", email);
            requestObject.put("password", password);
            requestObject.put("firstname", firstName);
            requestObject.put("lastname", lastName);
            requestObject.put("token", token);
        }
        catch (JSONException e) {
            Log.e(TAG, "[ERROR] in verifying user");
            Toast.makeText(MainActivity.this, "Error please sign up normally", Toast.LENGTH_SHORT).show();
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,  m_REQUEST_URL + email + "/newuser",
                requestObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "[RESPONSE] Post went through");
                Log.d(TAG, "[RESPONSE] " + response.toString());

                try {
                    String respMessage = response.getString("msg");
                    boolean hasBeenAdded = response.getBoolean("added");
                    Log.d(TAG, "[RESPONSE MESG] " +  respMessage);

                    //if the user has been successfully added to the db then login automatically,
                    if (hasBeenAdded){
                        Log.d(FBTAG, "[FACEBOOK SIGN UP] user found added to db");
                        Intent intent = new Intent(getApplicationContext(), HomePage.class);
                        intent.putExtra("username",email);
                        resetInputs();
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "An Error occurred, sign up normally", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG,"[ERROR] got a json exception");
                    Toast.makeText(MainActivity.this, "Error occured, sign up normally", Toast.LENGTH_SHORT).show();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "[RESPONSE ERROR] Failed to sign up - " + error);
                    }
                });

        m_queue.add(req);
    }

    /**
     * This method is activivated when the user is logged in.
     * it checks if the user is in the db, and if so logs in,
     * else, it adds user to the db
     * @param email - user email
     * @param password - user password
     * @param firstName - user first name
     * @param lastName - user last name
     * @param token - tooken
     */
    private void facebookLogin(String email, String password, String firstName, String lastName, String token){
        JSONObject requestObject = new JSONObject();

        try {
            requestObject.put("email", email);
            requestObject.put("password", password);
        }
        catch (JSONException e) {
            resetInputs();
            Log.e(TAG, "[ERROR] in verifying user");
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,  m_REQUEST_URL + email + "/check",
                requestObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "[RESPONSE] Post went through");
                Log.d(TAG, "[RESPONSE] " + response.toString());

                try {
                    String respMessage = response.getString("msg");
                    boolean isMatch = response.getBoolean("match");
                    Log.i(TAG, "[RESPONSE MESG] " +  respMessage);

                    // if the user is in the db already login, else sign up
                    if (isMatch){
                        Log.d(FBTAG, "[FACEBOOK LOGIN] user found in db");
                        Intent intent = new Intent(getApplicationContext(), HomePage.class);
                        intent.putExtra("username",email);
                        resetInputs();
                        startActivity(intent);
                    }
                    else{
                        Log.d(FBTAG, "[FACEBOOK LOGIN] no user found, attempting signup");
                        signFaceBookUserUp(email, password, firstName, lastName, token);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG,"[ERROR] got a json exception");
                    Toast.makeText(MainActivity.this, "Error occured Please re sign in", Toast.LENGTH_SHORT).show();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "[RESPONSE ERROR] Failed to Log in - " + error);
                    }
                });
        m_queue.add(req);
    }
}
