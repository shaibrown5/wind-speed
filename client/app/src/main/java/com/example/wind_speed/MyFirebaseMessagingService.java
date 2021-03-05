package com.example.wind_speed;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    private static final String SERVER_ADDRESS = "http://10.0.2.2:8080/";
    private static  String USERNAME;
    private RequestQueue _queue;
    @Override
    public void onCreate() {
        super.onCreate();
        _queue = Volley.newRequestQueue(this);
        Log.d(TAG, "test");

    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            doNotification(MyFirebaseMessagingService.this,remoteMessage.getNotification().getBody());
        }
    }

    /**
     * gets new token and send to registration
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("TAG",token);
        sendRegistrationToServer(token);
    }

    /**
     * This method send the token to registration
     * @param token - the token to send
     */
    private void sendRegistrationToServer(String token) {
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("token", token);
        }
        catch (JSONException e) {}
        USERNAME = MainActivity.userName.getText().toString();
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, SERVER_ADDRESS + USERNAME + "/token", requestObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Token saved successfully");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Failed to save token - " + error);
                    }
                });

        _queue.add(req);
    }

    /**
     * This method build a notification out of the received  message
     * @param text - the text of the  received  message
     * @param context - context of the app
     */
    private void doNotification(Context context, String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                NotificationChannel channel = new NotificationChannel("C1", "C1", NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "C1")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentTitle("Surfing time!")
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text)
                        .setBigContentTitle("your favorite surfing spot have good conditions, check it out "));

        if (notificationManager != null) {
            notificationManager.notify(0, builder.build());
        }
    }
}
