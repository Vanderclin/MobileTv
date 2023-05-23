package com.mobiletv.app.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.mobiletv.app.R;
import com.mobiletv.app.fragment.FragmentA;
import com.mobiletv.app.fragment.FragmentB;
import com.mobiletv.app.fragment.FragmentC;
import com.mobiletv.app.pojo.Account;
import com.mobiletv.app.widget.Badge;
import com.mobiletv.app.widget.MaterialEditText;
import com.mobiletv.app.update.UpdateChecker;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Badge NavHeaderPoints;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private AppCompatTextView NavHeaderName;
    private Fragment mFragment;
    private int mFragmentSelected = -1;
    private DatabaseReference mData;
    private MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeConnection();
        initializeFindViews();
        initializeUnity();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeFirebase();
        if (UnityAds.isInitialized()) {
            initializeInterstitial();
        }
    }

    private void initializeFindViews() {
        Toolbar mToolbar = findViewById(R.id.act_main_toolbar);
        mDrawerLayout = findViewById(R.id.act_main_drawer);
        mNavigationView = findViewById(R.id.act_main_navigation);
        setSupportActionBar(mToolbar);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open_drawer, R.string.close_drawer);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        View navigationViewHeader = mNavigationView.getHeaderView(0);
        NavHeaderName = navigationViewHeader.findViewById(R.id.nav_header_name);
        NavHeaderName.setSelected(true);
        NavHeaderPoints = navigationViewHeader.findViewById(R.id.nav_header_points);
        mNavigationView.setNavigationItemSelectedListener(this);
        setFragmentScreen(R.id.navigation_a);
        menuItem = mNavigationView.getMenu().findItem(R.id.navigation_d);
    }

    private void initializeFirebase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        mData = FirebaseDatabase.getInstance().getReference();
        if (mUser != null) {
            mData.child("users").child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Account mAccount = snapshot.getValue(Account.class);
                    if (mAccount != null) {
                        initializeViews(mAuth, mAccount, mUser);
                    }
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

    private void initializeViews(FirebaseAuth mAuth, Account mAccount, FirebaseUser mUser) {
        if (mAuth != null && mAccount != null && mUser != null) {
            String username = mUser.getDisplayName();
            String points = String.valueOf(mAccount.getPoints());
            NavHeaderName.setText(username);
            NavHeaderPoints.setText(points);
            if (TextUtils.isEmpty(username)) {
                openDialogUpdate();
            }
            menuItem.setVisible(mAccount.isAdmin());
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        setFragmentScreen(item.getItemId());
        return true;
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
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                    user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            mData.child("users").child(uid).child("username").setValue(username).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    mData.child("users").child(uid).child("points").setValue(ServerValue.increment(1500));
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

    private void initializeConnection() {
        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            UpdateChecker.checkForDialog(MainActivity.this);
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
                mFragment = new FragmentC();
                break;
            case R.id.navigation_d:
                startActivity(new Intent(MainActivity.this, FormActivity.class));
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

    // ADS UNITY
    private void initializeUnity() {
        UnityAds.initialize(MainActivity.this, "5283279", false, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                // initializeUnityBanner();
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {

            }
        });
    }

    public void initializeUnityInterstitial() {
        UnityAds.load("Interstitial_Android", new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                UnityAds.show(MainActivity.this, placementId, new IUnityAdsShowListener() {
                    @Override
                    public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {

                    }

                    @Override
                    public void onUnityAdsShowStart(String placementId) {

                    }

                    @Override
                    public void onUnityAdsShowClick(String placementId) {

                    }

                    @Override
                    public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {

                    }
                });
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {

            }
        });
    }

    private void initializeInterstitial() {
        SharedPreferences sharedPreferences = getSharedPreferences("ads", Context.MODE_PRIVATE);
        long lastAdTime = sharedPreferences.getLong("last_ad_time", 0);
        long currentTime = System.currentTimeMillis();
        long thirtyMinutesInMillis = 30 * 60 * 1000;
        if (currentTime - lastAdTime >= thirtyMinutesInMillis) {
            initializeUnityInterstitial();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("last_ad_time", currentTime);
            editor.apply();
        }
    }

}
