package me.danielml.finalschoolapp.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.managers.FirebaseManager;

public class ProfilePictureActivity extends AppCompatActivity {

    private Button takeCameraPhotoBtn, galleryPhotoBtn;
    private ImageView pfpView;
    private ProgressBar uploadBar;
    private TextView usernameView;

    private FirebaseManager firebaseManager;

    private final int CAMERA_INTENT = 120;
    private final int GALLERY_INTENT = 121;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_picture_view);

        firebaseManager = new FirebaseManager();

        usernameView = findViewById(R.id.usernameView);
        usernameView.setText(firebaseManager.getCurrentUser().getDisplayName());

        pfpView = findViewById(R.id.profilePictureView);

        takeCameraPhotoBtn = findViewById(R.id.takeCameraPictureBtn);
        galleryPhotoBtn = findViewById(R.id.galleryUploadBtn);
        uploadBar = findViewById(R.id.uploadProgressBar);

        firebaseManager.getProfilePictureForCurrentUser((imageURL) -> {
            if(imageURL != null)
                Picasso.get().load(imageURL).noFade().into(pfpView);
        });

        takeCameraPhotoBtn.setOnClickListener((v) -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_INTENT);
        });

        galleryPhotoBtn.setOnClickListener((v) -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, GALLERY_INTENT);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null) {
            galleryPhotoBtn.setEnabled(false);
            takeCameraPhotoBtn.setEnabled(false);
            uploadBar.setVisibility(View.VISIBLE);
            Bitmap photo = null;
            switch(requestCode) {
                case CAMERA_INTENT:
                    photo = (Bitmap) data.getExtras().get("data");
                    break;
                case GALLERY_INTENT:
                    Uri imageURI = data.getData();
                    try {
                        ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), imageURI);
                        photo = ImageDecoder.decodeBitmap(source);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            firebaseManager.uploadImageForCurrentUser(photo, (imageURL) -> {
                Toast.makeText(this, "Uploaded successfully!", Toast.LENGTH_SHORT).show();
                pfpView.setImageURI(imageURL);
                Picasso.get().load(imageURL).noFade().into(pfpView);
                unlockScreen();
            }, (failedReason) -> {
                Toast.makeText(this, failedReason, Toast.LENGTH_LONG).show();
                unlockScreen();
            });
        }
    }
    
    public void unlockScreen() {
        galleryPhotoBtn.setEnabled(true);
        takeCameraPhotoBtn.setEnabled(true);
        uploadBar.setVisibility(View.INVISIBLE);
    }
}