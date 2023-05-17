package com.mobiletv.app.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobiletv.app.R;
import com.mobiletv.app.fragment.FragmentA;
import com.mobiletv.app.fragment.FragmentB;
import com.mobiletv.app.pojo.User;
import com.mobiletv.app.widget.MaterialEditText;
import com.mobiletv.update.UpdateChecker;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mData;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private AppCompatImageView NavHeaderSignOut;
    private AppCompatTextView NavHeaderName, NavHeaderEmail;
    private Fragment mFragment;
    private int mFragmentSelected = -1;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeNavigation();
        initializeFirebase();
        initializeViews();
        checkConnection();
    }

    private void initializeNavigation() {
        Toolbar mToolbar = findViewById(R.id.act_main_toolbar);
        mDrawerLayout = findViewById(R.id.act_main_drawer);
        mNavigationView = findViewById(R.id.act_main_navigation);
        setSupportActionBar(mToolbar);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open_drawer, R.string.close_drawer);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        View navigationViewHeader = mNavigationView.getHeaderView(0);
        NavHeaderName = navigationViewHeader.findViewById(R.id.nav_header_name);
        NavHeaderEmail = navigationViewHeader.findViewById(R.id.nav_header_email);
        NavHeaderSignOut = navigationViewHeader.findViewById(R.id.nav_header_sign_out);
        mNavigationView.setNavigationItemSelectedListener(this);
        setFragmentScreen(R.id.navigation_a);
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mData = FirebaseDatabase.getInstance().getReference();
        String uid = mAuth.getUid();
        if (mAuth.getCurrentUser() != null) {
            mData.child("users").child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mUser = snapshot.getValue(User.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finish();
        }
    }

    private void initializeViews() {
        if (mAuth.getCurrentUser() != null) {
            String username = mAuth.getCurrentUser().getDisplayName();
            String email = mAuth.getCurrentUser().getEmail();
            if (TextUtils.isEmpty(username)) {
                openDialogUpdate();
            }
            NavHeaderName.setText(username);
            NavHeaderEmail.setText(email);
            NavHeaderSignOut.setOnClickListener(view -> {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                openDialogSignOut();
            });
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void setFragmentScreen(int itemId) {
        if (mFragmentSelected == itemId) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        switch (itemId) {
            case R.id.navigation_a:
                mFragment = new FragmentA();
                break;
            case R.id.navigation_b:
                mFragment = new FragmentB();
                break;
            case R.id.navigation_c:
                if (mUser.isAdmin()) {
                    startActivity(new Intent(MainActivity.this, FormActivity.class));
                } else {
                    Toast.makeText(this, getString(R.string.admin_permissions_required), Toast.LENGTH_SHORT).show();
                    mNavigationView.setCheckedItem(mFragmentSelected); // Reverte a seleção do item de menu
                }
                break;
        }
        if (mFragment != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.act_main_frame, mFragment);
            fragmentTransaction.commit();
        }
        mFragmentSelected = itemId;
        mNavigationView.setCheckedItem(itemId);
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        setFragmentScreen(item.getItemId());
        return true;
    }

    private void openDialogSignOut() {
        MaterialAlertDialogBuilder mBuilder = new MaterialAlertDialogBuilder(this, R.style.MaterialDialog);
        mBuilder.setTitle(getString(R.string.logoff));
        mBuilder.setMessage(getString(R.string.do_you_really_want_to_end_your_session));
        mBuilder.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
            mAuth.signOut();
            dialogInterface.dismiss();
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finish();
        });
        mBuilder.setNegativeButton(getString(R.string.no), (dialogInterface, i) -> dialogInterface.dismiss());
        mBuilder.setCancelable(false);
        mBuilder.show();
    }

    private void openDialogUpdate() {
        MaterialAlertDialogBuilder mBuilder = new MaterialAlertDialogBuilder(this, R.style.MaterialDialog);
        final View rootView = getLayoutInflater().inflate(R.layout.dialog_update, null);
        final AlertDialog mDialog = mBuilder.create();
        mDialog.setView(rootView);
        MaterialEditText actionUsername = rootView.findViewById(R.id.dialog_action_username);
        MaterialButton actionDismiss = rootView.findViewById(R.id.dialog_action_dismiss);
        MaterialButton actionUpdate = rootView.findViewById(R.id.dialog_action_update);
        actionDismiss.setOnClickListener(v -> mDialog.dismiss());
        actionUpdate.setOnClickListener(v -> {
            String username = actionUsername.getText().trim();
            if (!TextUtils.isEmpty(username)) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                    user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();
                            mData.child("users").child(uid).child("username").setValue(username).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    mDialog.dismiss();
                                }
                            });
                        }
                    });
                }
            }
        });
        mDialog.setCancelable(false);
        mDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void checkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            UpdateChecker.checkForDialog(MainActivity.this);
            UpdateChecker.checkForNotification(MainActivity.this);
        }
    }
}