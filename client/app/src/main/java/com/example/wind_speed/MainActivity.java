package com.example.wind_speed;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class MainActivity extends AppCompatActivity {

    EditText userName;
    EditText pass;
    Button signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //app activations enables almost all other functionality and should be the first thing you add to your app.
        // The SDK provides a helper method to log app activation. By logging an activation event, you can observe how frequently users activate your app,
        // how much time they spend using it,
        // and view other demographic information through Facebook Analytics for Apps.
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        signUp = (Button) findViewById(R.id.signUpButton);

        // when sign up is clicked, go to Sign up page
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SignUp.class);
                startActivity(intent);
            }
        });



    }

    private boolean checkInput(){
        boolean isValid = true;

        if (userName.getText().toString().isEmpty()){
            isValid = false;
            userName.setError("Must not be empty");
        }
        else{
            String emailText = userName.getText().toString();
            isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches();
            if(!isValid) {
                userName.setError("Not a valid username");
            }
        }
        if(pass.getText().toString().isEmpty()){
            isValid = false;
            pass.setError("Must not be empty");
        }

        return isValid;
    }

}