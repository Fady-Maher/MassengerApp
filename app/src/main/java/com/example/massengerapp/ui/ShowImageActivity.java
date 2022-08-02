package com.example.massengerapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.massengerapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowImageActivity extends AppCompatActivity {
    FloatingActionButton fbtn_save_image;
    ImageView img_show_save;
    CircleImageView profile_image_save, img_back_save_photo;
    byte[] byteArray;
    Bitmap bitmap = null, bitmap_save = null;
    private static final String PHOTOS_FOLDER = "Photos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        setID();
        if (getIntent().getByteArrayExtra("image_profile") != null
                && getIntent().getByteArrayExtra("image_show") != null) {
            byteArray = getIntent().getByteArrayExtra("image_profile");
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            profile_image_save.setImageBitmap(bitmap);

            byteArray = getIntent().getByteArrayExtra("image_show");
            bitmap_save = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            img_show_save.setImageBitmap(bitmap_save);
        } else {
            Toast.makeText(getApplicationContext(), "Failed when recieve a photo!!", Toast.LENGTH_SHORT).show();
        }


        fbtn_save_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bitmap_save != null) {
                    savePhoto(bitmap_save);
                } else {
                    Toast.makeText(getApplicationContext(), "Failed when recieve a photo!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        img_back_save_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    private void setID() {
        fbtn_save_image = findViewById(R.id.fbtn_save_image);
        img_show_save = findViewById(R.id.img_show_save);
        img_back_save_photo = findViewById(R.id.img_back_save_photo);
        profile_image_save = findViewById(R.id.profile_image_save);
    }

    private void savePhoto(Bitmap bitmap) {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(getFilename());
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), "Error when save photo!!", Toast.LENGTH_SHORT).show();
            Log.e("error : ", e.getMessage());
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        try {
            outStream.flush();
            Toast.makeText(getApplicationContext(), "Successful save photo!!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error when save photo!!", Toast.LENGTH_SHORT).show();
            Log.e("error : ", e.getMessage());
        }
        try {
            Toast.makeText(getApplicationContext(), "Successful save photo!!", Toast.LENGTH_SHORT).show();
            outStream.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error when save photo!!", Toast.LENGTH_SHORT).show();
            Log.e("error : ", e.getMessage());
        }
    }

    public String getFilename() {

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
                , PHOTOS_FOLDER);
        Log.e("file : ", file.getAbsolutePath());
        if (!file.exists()) {
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
    }
}