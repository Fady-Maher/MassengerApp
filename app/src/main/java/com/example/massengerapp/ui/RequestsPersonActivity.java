package com.example.massengerapp.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import com.example.massengerapp.R;
import com.example.massengerapp.connectionnewtwork.NetworkChangeListener;
import com.example.massengerapp.customadapter.Person;
import com.example.massengerapp.customadapter.Recycle_Request_Person_Adapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsPersonActivity extends AppCompatActivity {

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private SearchView searchView;
    private CircleImageView img_back;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<Person> personArrayList = null;
    private Recycle_Request_Person_Adapter recycle_request_person_adapter = null;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests_person);

        setID();


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        personArrayList = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);


        setData();
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                personArrayList.clear();
                db.collection("users").
                        document(user.getUid()).
                        collection("Requests").orderBy("name").startAt(s.toLowerCase())
                        .endAt(s.toLowerCase() + "\uf8ff").addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                if (error != null) {
                                    Log.e("Error:", error.getMessage());
                                } else if (value.getDocumentChanges().size() == 0) {
                                    recycle_request_person_adapter.notifyDataSetChanged();
                                } else {
                                    for (DocumentChange document : value.getDocumentChanges()) {
                                        if (document.getType() == DocumentChange.Type.ADDED) {
                                            Person person = document.getDocument().toObject(Person.class);
                                            personArrayList.add(person);
                                            recycle_request_person_adapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            }
                        });
                return true;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                personArrayList.clear();
                recycle_request_person_adapter.notifyDataSetChanged();
                setData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public void setData() {
        personArrayList.clear();
        db.collection("users").
                document(user.getUid()).
                collection("Requests").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    Person person = document.toObject(Person.class);
                                    personArrayList.add(person);
                                }
                            }
                            recycle_request_person_adapter = new Recycle_Request_Person_Adapter(personArrayList, RequestsPersonActivity.this);
                            recyclerView.setAdapter(recycle_request_person_adapter);
                            recycle_request_person_adapter.notifyDataSetChanged();
                        }
                    }
                });


    }

    void setID() {
        recyclerView = findViewById(R.id.rv_request_activity);
        swipeRefreshLayout = findViewById(R.id.refresh_request_activity);
        searchView = findViewById(R.id.search_request_friend);
        img_back = findViewById(R.id.img_back_chat_request_activity);
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





