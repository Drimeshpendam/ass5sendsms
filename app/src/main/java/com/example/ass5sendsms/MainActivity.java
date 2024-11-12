package com.example.ass5sendsms;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 101;

    private EditText phoneNumberEditText, messageEditText;
    private TextView smsStatusTextView, incomingSmsTextView;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        messageEditText = findViewById(R.id.messageEditText);
        smsStatusTextView = findViewById(R.id.smsStatusTextView);
        incomingSmsTextView = findViewById(R.id.incomingSmsTextView);
        sendButton = findViewById(R.id.sendButton);

        // Check and request SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
        }

        // Send SMS button listener
        sendButton.setOnClickListener(v -> sendSms());
    }

    // Method to send SMS using SmsManager
    private void sendSms() {
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String message = messageEditText.getText().toString().trim();

        if (phoneNumber.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please provide phone number and message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send SMS using SMSManager
        SmsManager smsManager = SmsManager.getDefault();
        try {
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            smsStatusTextView.setText("SMS sent to: " + phoneNumber);
        } catch (Exception e) {
            smsStatusTextView.setText("SMS failed to send: " + e.getMessage());
        }
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // BroadcastReceiver to listen for incoming SMS messages
    public static class SMSReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Retrieve SMS data from the intent
            if (intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Object[] pdus = (Object[]) intent.getExtras().get("pdus");
                if (pdus != null) {
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }

                    StringBuilder messageBody = new StringBuilder();
                    for (SmsMessage message : messages) {
                        messageBody.append(message.getMessageBody());
                    }

                    String sender = messages[0].getOriginatingAddress();
                    String message = messageBody.toString();

                    // Display the incoming message
                    TextView incomingSmsTextView = ((MainActivity) context).findViewById(R.id.incomingSmsTextView);
                    incomingSmsTextView.setText("Received from: " + sender + "\n" + message);
                }
            }
        }
    }
}
