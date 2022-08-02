package com.example.massengerapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.massengerapp.R;
import com.example.massengerapp.connectionnewtwork.NetworkChangeListener;
import com.example.massengerapp.customadapter.Person;
import com.example.massengerapp.fragments.ChatsFragment;
import com.example.massengerapp.fragments.MoreFragment;
import com.example.massengerapp.fragments.PeopleFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    public static final String PUSH_LOCATION_WORK_TAG = "michael";
    public static BottomNavigationView bottomNavigationView;
    private CircleImageView profile_image;
    private GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount acct = null;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private Person person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.constraint_fragment, new ChatsFragment()).commit();

        setID();

        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        initialGoogleAccount();

        uploadTokenID();
        retriveInfoEmailAndPassword();

/*
        Log.e("error main:","");
        if(acct != null){
            retriveInfoGoogleAccount();
        }else if(user.getUid() != null){
            retriveInfoEmailAndPassword();
        }

*/
    }

    private void uploadTokenID() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        String token = Objects.requireNonNull(task.getResult());
                        updateInformation(token);
                    }
                });
    }


    private void updateInformation(String token) {
        db.collection("users").document(user.getUid()).update("tokenID", token).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Successful add token ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Unsuccessful add token ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void setID() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        profile_image = findViewById(R.id.profile_image);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.nav_bottom_chat:
                    getSupportFragmentManager().beginTransaction().replace(R.id.constraint_fragment, new ChatsFragment()).commit();
                    break;
                case R.id.nav_bottom_people:
                    getSupportFragmentManager().beginTransaction().replace(R.id.constraint_fragment, new PeopleFragment()).commit();
                    break;
                case R.id.nav_bottom_more:
                    getSupportFragmentManager().beginTransaction().replace(R.id.constraint_fragment, new MoreFragment()).commit();
                    break;
            }
            return true;
        }
    };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    private void retriveInfoEmailAndPassword() {
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    person = task.getResult().toObject(Person.class);
                    Toast.makeText(getApplicationContext(), "Successful retrive ", Toast.LENGTH_SHORT).show();
                    try {
                        Glide.with(getBaseContext()).load(person.getImg_profile()).placeholder(R.drawable.ic_chat)
                                .error(R.drawable.ic_chat).into(profile_image);
                    } catch (Exception e) {
                        profile_image.setImageResource(R.drawable.ic_person);
                    }


                    //      Log.e("information", userAccount.getImg_profile());
                } else {
                    Toast.makeText(getApplicationContext(), "usSuccessful retrive ", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void initialGoogleAccount() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(getBaseContext(), gso);
        acct = GoogleSignIn.getLastSignedInAccount(getBaseContext());
    }

    private void retriveInfoGoogleAccount() {
        Uri uri = user.getPhotoUrl();
        if (uri == null) {
            profile_image.setImageResource(R.drawable.ic_person);
        } else {
            Glide.with(getBaseContext()).load(uri).placeholder(R.drawable.ic_chat)
                    .error(R.drawable.ic_chat).into(profile_image);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        retriveInfoEmailAndPassword();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(networkChangeListener);
    }
}
