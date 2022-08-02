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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.massengerapp.classes.CountryCodes;
import com.example.massengerapp.R;
import com.example.massengerapp.customadapter.Person;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpWithPhoneActivity extends AppCompatActivity {

    CircleImageView img_profile;
    EditText edit_name, edit_phone_up, edit_verify_up;
    Button btn_send_up, btn_phone_signup;
    Spinner spinner_country_code;
    ProgressBar progress_phone;
    TextView txt_sign_in;
    String phone, mVerificationId;
    boolean flag_name = false, flag_phone = false;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    int REQUEST_CODE_OPEN = 1;
    Bitmap bitmap;

    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_with_phone);

        setID();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        ArrayAdapter<String> countryName = new ArrayAdapter<String>(getBaseContext(),
                android.R.layout.simple_spinner_dropdown_item, CountryCodes.countryNames);
        spinner_country_code.setAdapter(countryName);

        btn_phone_signup.setEnabled(false);
        edit_name.addTextChangedListener(new TextWatcher() {
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
                    btn_send_up.setEnabled(true);
                } else {
                    btn_send_up.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        edit_phone_up.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 5) {
                    flag_phone = true;
                } else {
                    flag_phone = false;
                }
                if (checktext()) {
                    btn_send_up.setEnabled(true);
                } else {
                    btn_send_up.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        btn_send_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  btn_send_up.setEnabled(false);
                String code = CountryCodes.countryAreaCodes[spinner_country_code.getSelectedItemPosition()];
                //     code ="1";
                phone = "+" + code + edit_phone_up.getText().toString();
                if (checkOfInformation()) {
                    sendVerificationCode(phone);
                }
            }
        });

        btn_phone_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code_verify = edit_verify_up.getText().toString();
                if (!code_verify.equals("")) {
                    verifyVerificationCode(code_verify);
                }
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

        txt_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
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


    private void sendVerificationCode(String phone) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    boolean checktext() {
        if (flag_name && flag_phone) {
            return true;
        }
        return false;
    }


    private void setID() {
        img_profile = findViewById(R.id.img_profile_phone);
        edit_name = findViewById(R.id.edit_name_phone);
        edit_phone_up = findViewById(R.id.edit_phone_up);
        btn_send_up = findViewById(R.id.btn_send_up);
        spinner_country_code = findViewById(R.id.spinner_country_code);
        btn_phone_signup = findViewById(R.id.btn_phone_signup);
        progress_phone = findViewById(R.id.progress_signup_phone);
        txt_sign_in = findViewById(R.id.txt_signin_phone);
        edit_verify_up = findViewById(R.id.edit_verify_up);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            Log.e("VerCompleted:", phoneAuthCredential + "");
            Toast.makeText(SignUpWithPhoneActivity.this, "VerCompleted", Toast.LENGTH_SHORT).show();

            if (code != null) {
                edit_verify_up.setText(code);
                //verifying the code
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.e("onVerifFailed:", "onVerifFailed" + e.getMessage());
            Toast.makeText(SignUpWithPhoneActivity.this, "onVerifFailed" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(verificationId, forceResendingToken);
            Log.e("onCodeSent:", verificationId + "");
            Toast.makeText(SignUpWithPhoneActivity.this, "onCodeSent" + verificationId, Toast.LENGTH_SHORT).show();
            mVerificationId = verificationId;
            btn_phone_signup.setEnabled(true);

        }
    };


    private void verifyVerificationCode(String code) {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        //signing the user
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e("signInWithCredential:", "success");
                            Toast.makeText(SignUpWithPhoneActivity.this, "signInWithCredential success", Toast.LENGTH_SHORT).show();

                            // FirebaseUser user = task.getResult().getUser();
                            if (bitmap != null) {
                                user = mAuth.getCurrentUser();
                                storageRef = storage.getReference().child("images/profiles/" + user.getUid());

                                uploadImage(bitmap);

                            } else {
                                Log.e("error:", "failure" + task.getException());
                            }

                        } else {
                            // Sign in failed, display a message and update the UI
                            Toast.makeText(SignUpWithPhoneActivity.this, "signInWithCredential failure", Toast.LENGTH_SHORT).show();

                            Log.e("signInWithCredential:", "failure" + task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
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
                    progress_phone.setVisibility(View.GONE);

                    uploadinformation(edit_name.getText().toString(), edit_phone_up.getText().toString(),
                            "phone", "2/2/2020", bitmap);
                    Toast.makeText(getApplicationContext(), "Successful Upload ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "UnSuccessful Upload ", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                progress_phone.setVisibility(View.VISIBLE);
                btn_phone_signup.setEnabled(false);
                btn_send_up.setEnabled(false);
                edit_name.setEnabled(false);
                edit_phone_up.setEnabled(false);
                txt_sign_in.setEnabled(false);
                edit_verify_up.setEnabled(false);
            }
        });
    }

    private void uploadinformation(String name, String phone, String pass, String date, Bitmap bitmap) {
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.e("onSuccess : ", uri.toString());
                Toast.makeText(getBaseContext(), " Successful during get url of image ", Toast.LENGTH_SHORT).show();
                Person person = new Person(uri.toString(), name, phone, pass, "date", user.getUid());
                addInformation(person);
                signIn();
                Toast.makeText(getApplicationContext(), "Successful Sign Up ", Toast.LENGTH_SHORT).show();
                //  flag = false;
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
            return true;
        } else {
            Toast.makeText(getBaseContext(), "failed,please check of image profile", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


}