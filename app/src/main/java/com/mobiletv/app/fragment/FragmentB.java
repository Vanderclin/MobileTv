package com.mobiletv.app.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.mobiletv.app.R;
import com.mobiletv.app.activity.AlternativePlayer;
import com.mobiletv.app.pojo.Series;

public class FragmentB extends Fragment {

    private DatabaseReference mData;
    private RecyclerView mRecyclerView;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_b, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle(getString(R.string.action));
        initializeFindView();
        initializeFirebase();
    }

    private void initializeFindView() {
        mRecyclerView = requireActivity().findViewById(R.id.recycler_b);
        GridLayoutManager gridManager = new GridLayoutManager(requireActivity(), 3);
        gridManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(gridManager);
    }

    private void initializeFirebase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
        if (mUser != null) {
            initializeSetup(mAuth, mData, mUser);
        }
    }


    private void initializeSetup(FirebaseAuth mAuth, DatabaseReference mData, FirebaseUser mUser) {
        Query FirebaseQuery = mData.child("series").orderByChild("title");
        FirebaseRecyclerOptions<Series> recyclerOptions = new FirebaseRecyclerOptions.Builder<Series>().setQuery(FirebaseQuery, Series.class).build();
        FirebaseRecyclerAdapter<Series, FragmentB.ViewSeries> recyclerAdapter = new FirebaseRecyclerAdapter<Series, FragmentB.ViewSeries>(recyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FragmentB.ViewSeries mHolder, @SuppressLint("RecyclerView") int p, final Series series) {
                String position = getRef(p).getKey();
                String cover = series.getCover();
                Glide.with(requireActivity()).load(cover).placeholder(R.drawable.ic_launcher_background).into(mHolder.cardMovie);
                mHolder.cardMovie.setOnClickListener(v -> {
                    startActivity(new Intent(requireActivity(), AlternativePlayer.class).putExtra("position", position));
                });
            }

            @NonNull
            @Override
            public FragmentB.ViewSeries onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_series, viewGroup, false);
                return new FragmentB.ViewSeries(view);
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

}
