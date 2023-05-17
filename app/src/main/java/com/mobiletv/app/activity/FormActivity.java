package com.mobiletv.app.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobiletv.app.R;
import com.mobiletv.app.pojo.Carousel;
import com.mobiletv.app.pojo.EpisodeDetails;
import com.mobiletv.app.pojo.SeriesDetails;
import com.mobiletv.app.widget.MaterialBox;
import com.mobiletv.app.widget.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class FormActivity extends AppCompatActivity {

    private LinearLayoutCompat containerEpisode;
    private MaterialButton actionEpisodeRem;
    private MaterialButton actionEpisodeAdd;
    private MaterialBox actionCarousel;
    private AppCompatImageView actionImage;

    private MaterialEditText actionTitle, actionDescription;
    private FloatingActionButton actionButtonSave;

    private final ArrayList<Integer> IDT = new ArrayList<>();
    private final ArrayList<Integer> IDA = new ArrayList<>();

    private DatabaseReference mData;
    private StorageReference mStorage;
    private static final int REQUEST_CODE_GALLERY_PERMISSION = 1;
    private Uri imageUriCover;
    private int episodeCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        initializeFindViews();
        mData = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
    }

    private void initializeFindViews() {
        actionImage = findViewById(R.id.action_image);
        actionTitle = findViewById(R.id.action_title);
        actionCarousel = findViewById(R.id.action_carousel);
        actionDescription = findViewById(R.id.action_description);

        containerEpisode = findViewById(R.id.container_episodes);
        actionEpisodeRem = findViewById(R.id.action_episode_rem);
        actionEpisodeAdd = findViewById(R.id.action_episode_add);
        actionButtonSave = findViewById(R.id.action_save);
        initialization();
    }

    private void initialization() {
        actionImage.setOnClickListener(v -> checkGalleryPermission());
        actionEpisodeAdd.setOnClickListener(v -> addEpisode());
        actionEpisodeRem.setOnClickListener(v -> remEpisode());
        actionButtonSave.setOnClickListener(v -> actionSaveDatabase());
    }

    private void addEpisode() {
        LinearLayoutCompat verticalLayout = new LinearLayoutCompat(this);
        verticalLayout.setOrientation(LinearLayoutCompat.VERTICAL);

        MaterialEditText address = new MaterialEditText(this);
        MaterialEditText title = new MaterialEditText(this);

        title.setId(View.generateViewId());
        address.setId(View.generateViewId());

        int episodeNumber = episodeCounter + 1;

        int newEditTextIdAddress = address.getId();
        int newEditTextIdTitle = title.getId();

        IDA.add(newEditTextIdAddress);
        IDT.add(newEditTextIdTitle);

        title.setHint(getString(R.string.hint_title, episodeNumber));
        title.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        address.setHint(getString(R.string.hint_address, episodeNumber));
        address.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        verticalLayout.addView(title);
        verticalLayout.addView(address);

        containerEpisode.addView(verticalLayout);
        actionEpisodeRem.setEnabled(true);
        episodeCounter++;
    }

    private void remEpisode() {
        int numEpisode = containerEpisode.getChildCount();
        if (numEpisode > 0) {
            containerEpisode.removeViewAt(numEpisode - 1);
            episodeCounter--;
            IDA.remove(IDA.size() - 1);
            IDT.remove(IDT.size() - 1);
            if (episodeCounter == 0) {
                actionEpisodeRem.setEnabled(false);
            }
        }
    }


    private void checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_GALLERY_PERMISSION);
        } else {
            openGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_GALLERY_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(FormActivity.this, getString(R.string.gallery_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openGallery() {
        openLauncher.launch("image/*");
    }


    ActivityResultLauncher<String> openLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null) {
            imageUriCover = result;
            actionImage.setImageURI(imageUriCover);
        }
    });

    @SuppressLint("DefaultLocale")
    private void actionSaveDatabase() {
        String title = actionTitle.getText();
        String description = actionDescription.getText();
        if (imageUriCover != null && !TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) && episodeCounter > 0) {
            SeriesDetails seriesDetails = new SeriesDetails();
            uploadImage(seriesDetails, title, description);
        } else {
            Toast.makeText(FormActivity.this, getString(R.string.fill_required_fields), Toast.LENGTH_SHORT).show();
        }
    }


    @SuppressLint("DefaultLocale")
    private void uploadImage(SeriesDetails seriesDetails, String title, String description) {
        StorageReference coverImageRef = mStorage.child("covers/" + UUID.randomUUID().toString());
        UploadTask coverUploadTask = coverImageRef.putFile(imageUriCover);
        coverUploadTask.addOnSuccessListener(taskSnapshot -> coverImageRef.getDownloadUrl().addOnSuccessListener(cover -> saveSeriesDetails(seriesDetails, cover, title, description))).addOnFailureListener(e -> Toast.makeText(FormActivity.this, getString(R.string.error_upload_cover_image), Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("DefaultLocale")
    private void saveSeriesDetails(SeriesDetails seriesDetails, Uri cover, String title, String description) {
        String position = mData.push().getKey();
        seriesDetails.setCover(cover.toString());
        seriesDetails.setTitle(title);
        seriesDetails.setDescription(description);
        if (actionCarousel.isChecked()) {
            Carousel carousel = new Carousel();
            carousel.setAddress(position);
            carousel.setDescription(description);
            carousel.setTitle(title);
            carousel.setImage(cover.toString());
            mData.child("carousel").push().setValue(carousel);
        }
        HashMap<String, EpisodeDetails> episodes = new HashMap<>();
        for (int i = 0; i < IDA.size(); i++) {
            EpisodeDetails episodeDetails = new EpisodeDetails();
            int episodeNumber = i + 1;
            String episodeKey = "ep" + String.format("%03d", episodeNumber);
            MaterialEditText eTitle = findViewById(IDT.get(i));
            MaterialEditText eAddress = findViewById(IDA.get(i));
            String episodeTitle = eTitle.getText();
            String episodeAddress = eAddress.getText();
            String episodeCover = cover.toString();
            episodeDetails.setAddress(episodeAddress);
            episodeDetails.setCover(episodeCover);
            episodeDetails.setTitle(episodeNumber + " - " + episodeTitle);
            episodes.put(episodeKey, episodeDetails);
        }
        seriesDetails.setEpisodes(episodes);
        if (position != null) {
            mData.child("series").child(position).setValue(seriesDetails).addOnSuccessListener(aVoid -> {
                startActivity(new Intent(FormActivity.this, MainActivity.class));
                Toast.makeText(FormActivity.this, getString(R.string.added_successfully), Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                // Ocorreu um erro ao salvar os dados no Firebase Realtime Database
            });
        }
    }

}