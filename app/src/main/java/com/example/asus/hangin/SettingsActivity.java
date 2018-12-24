package com.example.asus.hangin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    private StorageReference mImageStorageRef;

    //layout
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;

    private Button mStatusBtn;
    private Button mImageBtn;
    private ImageButton mNameBtn;

    private ProgressDialog mProgressDialog;

    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar) findViewById(R.id.settings_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Your Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDisplayImage = (CircleImageView) findViewById(R.id.settings_image);
        mName = (TextView) findViewById(R.id.settings_display_name);
        mStatus = (TextView) findViewById(R.id.settings_status);

        mStatusBtn = (Button) findViewById(R.id.settings_status_btn);
        mImageBtn = (Button) findViewById(R.id.settings_image_btn);
        mNameBtn = (ImageButton) findViewById(R.id.settings_name_btn);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(current_uid);
        mUserDatabase.keepSynced(true);                             //offline capa

        mImageStorageRef = FirebaseStorage.getInstance().getReference();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                String tempStr = "default";
                if(!image.equals(tempStr)){

                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).
                            into(mDisplayImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {

                                    Picasso.get().load(image).into(mDisplayImage);

                                }
                            });

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent nameIntent = new Intent(SettingsActivity.this, NameActivity.class);

                String nameValue = mName.getText().toString();
                nameIntent.putExtra("name_value", nameValue);

                startActivity(nameIntent);

            }
        });

        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);

                String statusValue = mStatus.getText().toString();
                statusIntent.putExtra("status_value", statusValue);

                startActivity(statusIntent);

            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

                /*
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);
                */

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri ImageUri = data.getData();

            CropImage.activity(ImageUri)
                    .setAspectRatio(1, 1)
                    .start(this);

            //Toast.makeText(SettingsActivity.this, ImageUri, Toast.LENGTH_LONG).show();

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please wait while we upload the image...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());

                final String current_uid = mCurrentUser.getUid();

                Bitmap thumb_bitmap = new Compressor(this)
                        .setMaxHeight(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_filePath);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();


                StorageReference filepath = mImageStorageRef.child("profile_images").child(current_uid + ".jpg");
                final StorageReference thumb_filepath= mImageStorageRef.child("profile_images").child("thumbs").child(current_uid + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>(){
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){

                            mImageStorageRef.child("profile_images").child(current_uid + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                   final String downloadUrl = uri.toString();

                                    UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);

                                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                            if(thumb_task.isSuccessful()){

                                                mImageStorageRef.child("profile_images").child("thumbs").child(current_uid + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {

                                                        String thumb_downloadUrl = uri.toString();

                                                        Map update_hashMap = new HashMap<>();
                                                        update_hashMap.put("image",downloadUrl);
                                                        update_hashMap.put("thumb_image", thumb_downloadUrl);

                                                        mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task){

                                                                if (task.isSuccessful()) {

                                                                    mProgressDialog.dismiss();
                                                                    Toast.makeText(SettingsActivity.this, "Profile picture updated", Toast.LENGTH_LONG).show();

                                                                }
                                                                else{

                                                                    mProgressDialog.dismiss();
                                                                    Toast.makeText(SettingsActivity.this, "Couldn't upload image.", Toast.LENGTH_LONG).show();

                                                                }

                                                            }
                                                        });

                                                    }
                                                });
                                            }
                                            else{

                                                mProgressDialog.dismiss();
                                                Toast.makeText(SettingsActivity.this, "Error in uploading thumbnail", Toast.LENGTH_LONG).show();

                                            }

                                        }
                                    });
                                }
                            });


                        }
                        else{

                            mProgressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Error in uploading image", Toast.LENGTH_LONG).show();

                        }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }


}
