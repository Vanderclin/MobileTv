package com.mobiletv.app.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.mobiletv.app.R;
import com.mobiletv.app.player.AdvancedPlayer;
import com.mobiletv.app.pojo.AccountData;
import com.mobiletv.app.pojo.Series;
import com.mobiletv.app.widget.ImageRounded;

public class FragmentB extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mData;
    private RecyclerView mRecyclerView;
    private int cardWidth;
    private AccountData mAccountData;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_b, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle(getString(R.string.series));
        initializeFindView();
        initializeFirebase();
    }

    private void initializeFindView() {
        mRecyclerView = requireActivity().findViewById(R.id.recycler_b);
        GridLayoutManager gridManager = new GridLayoutManager(requireActivity(), 3);
        gridManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(gridManager);
        calculateCardWidth();
    }

    private void calculateCardWidth() {
        WindowManager windowManager = (WindowManager) requireActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        int horizontalSpacing = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        int gridPadding = getResources().getDimensionPixelSize(R.dimen.grid_padding);
        cardWidth = (screenWidth - (2 * gridPadding) - (2 * horizontalSpacing)) / 3;
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mData = FirebaseDatabase.getInstance().getReference();
        if (mUser != null) {
            mData.child("users").child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mAccountData = snapshot.getValue(AccountData.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            initializeSetup(mAuth, mUser);
        }
    }

    private void initializeSetup(FirebaseAuth mAuth, FirebaseUser mUser) {
        Query FirebaseQuery = mData.child("series").orderByChild("title");
        FirebaseRecyclerOptions<Series> recyclerOptions = new FirebaseRecyclerOptions.Builder<Series>().setQuery(FirebaseQuery, Series.class).build();
        FirebaseRecyclerAdapter<Series, ViewSeries> recyclerAdapter = new FirebaseRecyclerAdapter<Series, ViewSeries>(recyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ViewSeries mHolder, @SuppressLint("RecyclerView") int p, final Series series) {
                String cover = series.getCover();
                String description = series.getDescription();
                String key = getRef(p).getKey();
                String title = series.getTitle();

                Glide.with(requireActivity()).load(cover).placeholder(R.drawable.ic_launcher_background).into(mHolder.cardMovie);
                ViewGroup.LayoutParams layoutParams = mHolder.cardMovie.getLayoutParams();
                layoutParams.width = cardWidth;
                mHolder.cardMovie.setLayoutParams(layoutParams);

                mHolder.cardMovie.setOnClickListener(v -> {
                    openDialogWatch(cover, description, key, title);
                });
            }

            @NonNull
            @Override
            public ViewSeries onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_series, viewGroup, false);
                return new ViewSeries(view);
            }
        };
        mRecyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();
    }

    public static class ViewSeries extends RecyclerView.ViewHolder {
        public AppCompatImageView cardMovie;

        public ViewSeries(@NonNull View rootView) {
            super(rootView);
            cardMovie = rootView.findViewById(R.id.item_series);
        }
    }

    @SuppressLint("InflateParams")
    private void openDialogWatch(String cover, String description, String key, String title) {
        MaterialAlertDialogBuilder mBuilder = new MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialDialog);
        final View rootView = requireActivity().getLayoutInflater().inflate(R.layout.dialog_watch, null);
        final AlertDialog mDialog = mBuilder.create();

        mDialog.setView(rootView);
        ImageRounded dialogImage = rootView.findViewById(R.id.dialog_watch_image);
        AppCompatTextView dialogTitle = rootView.findViewById(R.id.dialog_watch_title);
        AppCompatTextView dialogDescription = rootView.findViewById(R.id.dialog_watch_description);
        MaterialButton dialogDismiss = rootView.findViewById(R.id.dialog_watch_dismiss);
        MaterialButton dialogAction = rootView.findViewById(R.id.dialog_watch_action);

        dialogTitle.setSelected(true);

        if (cover != null && description != null && title != null) {
            Glide.with(requireActivity()).load(cover).placeholder(R.drawable.icon_placeholder_cards).into(dialogImage);
            dialogTitle.setText(title);
            dialogDescription.setText(description);
        }
        dialogDismiss.setOnClickListener(v -> mDialog.dismiss());
        dialogAction.setOnClickListener(v -> {
            if (mAccountData != null && mUser != null) {
                String uid = mUser.getUid();
                if (mAccountData.getPoints() > 0) {
                    mData.child("users").child(uid).child("points").setValue(ServerValue.increment(-10));
                    mData.child("series").child(key).child("views").setValue(ServerValue.increment(1));
                    startActivity(new Intent(requireActivity(), AdvancedPlayer.class).putExtra("key", key));
                    mDialog.dismiss();
                } else {
                    Toast.makeText(requireActivity(), getString(R.string.insufficient_points), Toast.LENGTH_SHORT).show();
                    mDialog.dismiss();
                }
            }
        });
        mDialog.setCancelable(false);
        mDialog.show();
    }

}
