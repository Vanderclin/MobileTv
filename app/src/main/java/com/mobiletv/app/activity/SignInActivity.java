package com.mobiletv.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mobiletv.app.R;
import com.mobiletv.app.update.UpdateChecker;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mData;

    private TextInputEditText inputEmail, inputPassword;
    private MaterialButton buttonSignIn, buttonSignUp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        mAuth = FirebaseAuth.getInstance();
        mData = FirebaseDatabase.getInstance().getReference();
        initializationFirebase();

        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        buttonSignUp = findViewById(R.id.button_sign_up);
        buttonSignIn = findViewById(R.id.button_sign_in);
        buttonSignUp.setText(R.string.sign_up);
        buttonSignIn.setText(R.string.sign_in);

        initializationSign();
    }

    private void initializationSign() {
        buttonSignUp.setOnClickListener(v -> startActivity(new Intent(SignInActivity.this, SignUpActivity.class)));
        buttonSignIn.setOnClickListener(view -> {
            String email = String.valueOf(inputEmail.getText());
            String password = String.valueOf(inputPassword.getText());
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, getString(R.string.enter_your_email), Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, getString(R.string.enter_your_password), Toast.LENGTH_SHORT).show();
            } else {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        initializationFirebase();
                    }
                });
            }
        });
    }

    private void initializationFirebase() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            mData.child("users").child(uid).child("timestamp").setValue(getTimestamp());
            finish();
        }
    }

    private long getTimestamp() {
        return System.currentTimeMillis();
    }
}
