package com.example.wind_speed;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class SignUp extends AppCompatActivity {

    TextView textBox;
    TextView login;
    EditText name;
    EditText lastName;
    EditText email;
    EditText pass;
    EditText verifyPass;
    Button signUp;
    private static final String TAG = "SignUp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

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
                } else {
                    Log.i(TAG, "information is correctly filled");
                    //TODO
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
    }

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

}
