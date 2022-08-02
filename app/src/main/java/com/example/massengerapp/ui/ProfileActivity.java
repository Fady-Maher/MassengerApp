package com.example.massengerapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.massengerapp.R;
import com.example.massengerapp.connectionnewtwork.NetworkChangeListener;
import com.example.massengerapp.customadapter.Person;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    CircleImageView image_profile_activity2;
    TextView txt_name_activity_value, txt_email_activity_value, txt_name_profile_activity, txt_id_activity_value;
    ImageView image_profile_activity, image_camera_activity, img_back_profile;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private Bitmap bitmap;
    int REQUEST_CODE_OPEN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setID();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference().child("images/profiles/" + user.getUid());

        db.collection("users").whereEqualTo("id", user.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Person person = document.toObject(Person.class);
                                try {
                                    Glide.with(getApplicationContext()).load(person.getImg_profile()).placeholder(R.drawable.ic_person).
                                            error(R.drawable.ic_person).into(image_profile_activity);
                                } catch (Exception e) {
                                    image_profile_activity.setImageResource(R.drawable.ic_person);
                                }
                                try {
                                    Glide.with(getApplicationContext()).load(person.getImg_profile()).placeholder(R.drawable.ic_person).
                                            error(R.drawable.ic_person).into(image_profile_activity2);
                                } catch (Exception e) {
                                    image_profile_activity2.setImageResource(R.drawable.ic_person);
                                }

                                txt_name_activity_value.setText(person.getName());
                                txt_email_activity_value.setText(person.getEmail());
                                txt_name_profile_activity.setText(person.getName());
                                txt_id_activity_value.setText(user.getUid());
                            }
                        }
                    }
                });

        img_back_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        image_camera_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                String[] mimetypes = {"image/*", "video/*"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                startActivityForResult(intent, REQUEST_CODE_OPEN);
            }
        });

    }

    private void setID() {
        img_back_profile = findViewById(R.id.img_back_profile);
        txt_name_activity_value = findViewById(R.id.txt_name_activity_value);
        txt_email_activity_value = findViewById(R.id.txt_email_activity_value);
        txt_name_profile_activity = findViewById(R.id.txt_name_profile_activity);
        txt_id_activity_value = findViewById(R.id.txt_id_activity_value);
        image_profile_activity = findViewById(R.id.image_profile_activity);
        image_profile_activity2 = findViewById(R.id.image_profile_activity2);
        image_camera_activity = findViewById(R.id.image_camera_activity);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN && resultCode == Activity.RESULT_OK && data != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getBaseContext().getContentResolver(), data.getData());
                image_profile_activity.setImageBitmap(bitmap);
                image_profile_activity2.setImageBitmap(bitmap);
                uploadImage(bitmap);
                Toast.makeText(getBaseContext(), "Successful", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getBaseContext(), "Canceled", Toast.LENGTH_SHORT).show();
        }
    }


    private void uploadImage(Bitmap bitmap) {
        ByteArrayOutputStream output_image = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, output_image);
        byte[] data_image = output_image.toByteArray();

        storageRef.putBytes(data_image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    //     progressBar_signup.setVisibility(View.GONE);
                    getDownloadUrl();
                    Toast.makeText(getApplicationContext(), "Successful Upload ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "UnSuccessful Upload ", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                //      progressBar_signup.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getDownloadUrl() {
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.e("onSuccess : ", uri.toString());
                Toast.makeText(getBaseContext(), " Successful during get url of image ", Toast.LENGTH_SHORT).show();

                addInformation(uri.toString());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(), " unSuccessful during get url of image ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addInformation(String uri) {
        Map<String, Object> map = new HashMap<>();
        map.put("img_profile", uri);
        db.collection("users").document(user.getUid()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Successful update image ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Unsuccessful update image ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(networkChangeListener);
    }
}