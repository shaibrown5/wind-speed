package com.example.wind_speed;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.namespace.QName;


public class SignUp extends AppCompatActivity {

    TextView textBox;
    TextView login;
    EditText name;
    EditText lastName;
    EditText email;
    EditText pass;
    EditText verifyPass;
    Button signUp;
    private static String token = "";
    private RequestQueue m_queue;
    private static final String TAG = "SignUp";
    private static final String m_REQUEST_URL = "http://10.0.2.2:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        m_queue = Volley.newRequestQueue(this);

        textBox = (TextView) findViewById(R.id.verifyText);
        login = (TextView) findViewById(R.id.login);
        name = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        email = (EditText) findViewById(R.id.email);
        pass = (EditText) findViewById(R.id.newPassword);
        verifyPass = (EditText) findViewById(R.id.verifyPassword);
        signUp = (Button) findViewById(R.id.newSignUp);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "button was pressed");
                boolean isFilled = checkFilled();

                if (!isFilled) {
                    textBox.setText("All Fields Must be Filled and The passwords must match");
                }
                else {
                    Log.i(TAG, "information is correctly filled");
                    signUserUp(email.getText().toString(), pass.getText().toString(), name.getText().toString(), lastName.getText().toString(), token);
                }
            }
        });


        //goes back to the login page
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
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


    /**
     * This method sends the user data to the server to sign them up
     * @param email - the users email/ username
     * @param password - the users password
     * @param firstName - the users first name
     * @param lastName - the users last name
     * @param token - the users unique firebase token
     */
    protected void signUserUp(String email, String password, String firstName, String lastName, String token){
        JSONObject requestObject = new JSONObject();

        try {
            requestObject.put("email", email);
            requestObject.put("password", password);
            requestObject.put("firstname", firstName);
            requestObject.put("lastname", lastName);
            requestObject.put("token", token);
        }
        catch (JSONException e) {
            resetInputs();
            Log.e(TAG, "[ERROR] in verifying user");
            Toast.makeText(SignUp.this, "Please re-enter credentials", Toast.LENGTH_SHORT).show();
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

                    if (hasBeenAdded){
                        Toast.makeText(SignUp.this, "Welcome, Please Log In!", Toast.LENGTH_SHORT).show();
                        resetInputs();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                    else{
                        resetInputs();
                        textBox.setText("Please refill the form");
                        Toast.makeText(SignUp.this, "An Error occurred", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG,"[ERROR] got a json exception");
                    textBox.setText("Please refill the form");
                    Toast.makeText(SignUp.this, "Error occured", Toast.LENGTH_SHORT).show();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "[RESPONsE ERROR] Failed to sign up - " + error);
                    }
                });

        m_queue.add(req);
    }

    /**
     * This method checks the validity of the inputs
     * @return true if all the inputs are valid, false otherwise
     */
    private boolean checkFilled(){
        boolean isFilled = true;

        if (name.getText().toString().isEmpty()){
            isFilled = false;
            name.setError("Must not be empty");
        }
        if (lastName.getText().toString().isEmpty()){
            isFilled = false;
            lastName.setError("Must not be empty");
        }
        if (!pass.getText().toString().equals(verifyPass.getText().toString())){
            isFilled = false;
            verifyPass.setError("Passwords do not match");
            Log.e(TAG,"Passwords do now match");
        }
        if (pass.getText().toString().isEmpty()){
            isFilled = false;
            pass.setError("Must not be empty");
        }
        if (verifyPass.getText().toString().isEmpty()){
            isFilled = false;
            verifyPass.setError("Must not be empty");
        }
        if (email.getText().toString().isEmpty()){
            isFilled = false;
            email.setError("Must not be empty");
        }
        else{
            String emailText = email.getText().toString();
            isFilled = android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches();
            if(!isFilled){
                email.setError("Not a valid email");
            }
        }

        return isFilled;
    }

    /**
     * This method re sets the inputs to their original form
     */
    private void resetInputs(){
        Log.i(TAG, "reseting inputs");

        name.setText(null);
        name.setHint("First name");
        lastName.setText(null);
        lastName.setHint("Last name");
        email.setText(null);
        email.setHint("Email");
        pass.setText(null);
        pass.setHint("Enter password");
        verifyPass.setText(null);
        verifyPass.setHint("Re-enter password");
    }

}
