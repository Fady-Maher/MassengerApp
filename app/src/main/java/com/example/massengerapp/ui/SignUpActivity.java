package com.example.massengerapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.massengerapp.R;
import com.example.massengerapp.customadapter.Person;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {

    Boolean flag_name = false, flag_email_or_phone = false, flag_pass = false, flag_conpass = false;
    EditText name, email_or_phone, pass, confirm_pass;
    ProgressBar progressBar_signup;
    TextView txt_sign_in;
    Button btn_sign_up;
    CircleImageView img_profile;
    int REQUEST_CODE_OPEN = 1;
    Bitmap bitmap;
    boolean flag = false;
    private Person person;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;


    private FirebaseStorage storage;
    private StorageReference storageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();

        setID();

        //to check to all edittext is not empty
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    flag_name = true;
                } else {
                    flag_name = false;
                }
                if (checktext()) {
                    btn_sign_up.setEnabled(true);
                } else {
                    btn_sign_up.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        email_or_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    flag_email_or_phone = true;
                } else {
                    flag_email_or_phone = false;
                }
                if (checktext()) {
                    btn_sign_up.setEnabled(true);
                } else {
                    btn_sign_up.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    flag_pass = true;
                } else {
                    flag_pass = false;
                }
                if (checktext()) {
                    btn_sign_up.setEnabled(true);
                } else {
                    btn_sign_up.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        confirm_pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    flag_conpass = true;
                } else {
                    flag_conpass = false;
                }
                if (checktext()) {
                    btn_sign_up.setEnabled(true);
                } else {
                    btn_sign_up.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        btn_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("info", name.getText().toString() + email_or_phone.getText().toString() + pass.getText().toString() + confirm_pass.getText().toString());
                if (checkOfInformation()) {
                    createAccountEmailAndPass(email_or_phone.getText().toString(), pass.getText().toString());
                }
            }
        });

        txt_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        img_profile.setOnClickListener(new View.OnClickListener() {
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

    @Override
    public void onBackPressed() {
        signIn();
    }

    private void signIn() {
        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN && resultCode == Activity.RESULT_OK && data != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getBaseContext().getContentResolver(), data.getData());
                img_profile.setImageBitmap(bitmap);
                Toast.makeText(getBaseContext(), "Successful", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getBaseContext(), "Canceled", Toast.LENGTH_SHORT).show();
        }
    }


    private void setID() {
        img_profile = findViewById(R.id.img_profile);
        name = findViewById(R.id.edit_name_up);
        email_or_phone = findViewById(R.id.edit_email_up);
        pass = findViewById(R.id.edit_pass_up);
        confirm_pass = findViewById(R.id.edit_conpass_up);
        txt_sign_in = findViewById(R.id.txt_signin);
        btn_sign_up = findViewById(R.id.btn_sign_up);
        progressBar_signup = findViewById(R.id.progressBar_signup);
    }

    boolean checktext() {
        if (flag_name && flag_email_or_phone && flag_pass && flag_conpass) {
            return true;
        }
        return false;
    }

    private void createAccountEmailAndPass(String email, String pass) {

        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    user = mAuth.getCurrentUser();
                    storageRef = storage.getReference().child("images/profiles/" + user.getUid());
                    //Add information of user to firebasefirestore
                    uploadImage(bitmap);
                } else {
                    Log.e("except : ", task.getException().toString());
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addInformation(Person person) {
        db.collection("users").document(user.getUid()).set(person).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Successful add information ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Unsuccessful add information ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadImage(Bitmap bitmap) {
        ByteArrayOutputStream output_image = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, output_image);
        byte[] data_image = output_image.toByteArray();

        storageRef.putBytes(data_image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    progressBar_signup.setVisibility(View.GONE);
                    uploadinformation(name.getText().toString(), email_or_phone.getText().toString(),
                            pass.getText().toString(), "2/2/2020", bitmap);
                    Toast.makeText(getApplicationContext(), "Successful Upload ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "UnSuccessful Upload ", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                progressBar_signup.setVisibility(View.VISIBLE);
                btn_sign_up.setEnabled(false);
                txt_sign_in.setEnabled(false);
                name.setEnabled(false);
                email_or_phone.setEnabled(false);
                pass.setEnabled(false);
                confirm_pass.setEnabled(false);
            }
        });
    }

    private void uploadinformation(String name, String email, String pass, String date, Bitmap bitmap) {
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.e("onSuccess : ", uri.toString());
                Toast.makeText(getBaseContext(), " Successful during get url of image ", Toast.LENGTH_SHORT).show();

                person = new Person(uri.toString(), name, email, pass, "date", mAuth.getUid());
                addInformation(person);
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "Successful Sign Up ", Toast.LENGTH_SHORT).show();
                flag = false;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(), " unSuccessful during get url of image ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkOfInformation() {
        if (bitmap != null) {
            if (pass.getText().toString().trim().equals(confirm_pass.getText().toString().trim())) {
                return true;
            } else {
                Toast.makeText(getApplicationContext(), "Please,confirm password not same Password or vice versa!! ", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getBaseContext(), "failed,please check of image profile", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}