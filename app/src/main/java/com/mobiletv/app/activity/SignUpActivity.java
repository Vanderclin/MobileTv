package com.mobiletv.app.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mobiletv.app.R;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mData;
    private DeviceUtils deviceUtils;
    private TextInputEditText inputEmail, inputPassword;
    private MaterialButton buttonSignUp, buttonSignIn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        mAuth = FirebaseAuth.getInstance();
        mData = FirebaseDatabase.getInstance().getReference();
        deviceUtils = new DeviceUtils();

        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        buttonSignUp = findViewById(R.id.button_sign_up);
        buttonSignIn = findViewById(R.id.button_sign_in);
        buttonSignUp.setText(R.string.sign_up);
        buttonSignIn.setVisibility(View.GONE);
        initializationSign();
    }

    private void initializationSign() {
        buttonSignUp.setOnClickListener(view -> {
            String email = String.valueOf(inputEmail.getText());
            String password = String.valueOf(inputPassword.getText());
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, getString(R.string.enter_your_email), Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, getString(R.string.enter_your_password), Toast.LENGTH_SHORT).show();
            } else {
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(taskOne -> {
                    if (taskOne.isSuccessful()) {
                        if (mAuth.getCurrentUser() != null) {
                            String uid = mAuth.getCurrentUser().getUid();
                            HashMap<String, Object> account = new HashMap<>();
                            account.put("access", getTimestamp());
                            account.put("admin", false);
                            account.put("device", getDevice());
                            account.put("email", email);
                            account.put("name", "");
                            account.put("points", 1500);
                            account.put("uid", uid);
                            mData.child("users").child(uid).setValue(account).addOnCompleteListener(taskTwo -> {
                                if (taskTwo.isSuccessful()) {
                                    initializationFirebase();
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private void initializationFirebase() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            finish();
        }
    }

    private long getTimestamp() {
        return System.currentTimeMillis();
    }

    private String getDevice() {
        return deviceUtils.getDeviceId(this);
    }

    public static class DeviceUtils {
        @SuppressLint("HardwareIds")
        public String getDeviceId(Context context) {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
    }

}
