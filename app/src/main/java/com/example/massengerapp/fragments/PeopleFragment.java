package com.example.massengerapp.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.massengerapp.ui.AddPersonActivity;
import com.example.massengerapp.ui.ChatActivity;
import com.example.massengerapp.R;
import com.example.massengerapp.ui.RequestsPersonActivity;
import com.example.massengerapp.customadapter.OnClickListenerItem;
import com.example.massengerapp.customadapter.Recycle_Friends_Adapter;
import com.example.massengerapp.customadapter.Person;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PeopleFragment extends Fragment implements OnClickListenerItem {

    private View view;
    private FloatingActionButton fbtn_add_friend, fbtn_search_friend;

    private Recycle_Friends_Adapter recycle_friends_adapter = null;
    private ArrayList<Person> personArrayList;
    private RecyclerView recyclerView = null;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private SearchView searchView;
    private Map<String, String> map = new HashMap<>();
    private boolean flag = false;

    public PeopleFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        personArrayList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_people, container, false);

        setID();


        Toast.makeText(getActivity(), "oncreate", Toast.LENGTH_SHORT).show();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        setData();

        fbtn_add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = true;
                Intent intent = new Intent(getActivity(), AddPersonActivity.class);
                startActivity(intent);
            }
        });

        fbtn_search_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = true;
                Intent intent = new Intent(getActivity(), RequestsPersonActivity.class);
                startActivity(intent);
            }
        });
        searchView.setIconifiedByDefault(false);
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
                db.collection("users")
                        .document(user.getUid()).
                        collection("friends").orderBy("name").startAt(s.toLowerCase())
                        .endAt(s.toLowerCase() + "\uf8ff").addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                if (error != null) {
                                    Log.e("Error:", error.getMessage());
                                } else if (value.getDocumentChanges().size() == 0) {
                                    recycle_friends_adapter.notifyDataSetChanged();
                                } else {
                                    for (DocumentChange document : value.getDocumentChanges()) {
                                        if (document.getType() == DocumentChange.Type.ADDED) {
                                            Person person = document.getDocument().toObject(Person.class);
                                            if (!person.getId().equals(user.getUid())) {
                                                personArrayList.add(person);
                                                recycle_friends_adapter.notifyDataSetChanged();
                                            }
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
                recycle_friends_adapter.notifyDataSetChanged();
                setData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        Toast.makeText(getActivity(), "oncreate", Toast.LENGTH_SHORT).show();

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(getActivity(), "onResume", Toast.LENGTH_SHORT).show();
        if (flag) {
            personArrayList.clear();
            recycle_friends_adapter.notifyDataSetChanged();
            setData();
        }
    }

    public void setData() {
        personArrayList.clear();
        db.collection("users").
                document(user.getUid()).
                collection("friends").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    Person person = document.toObject(Person.class);
                                    personArrayList.add(person);
                                }
                            }
                            recycle_friends_adapter = new Recycle_Friends_Adapter(personArrayList, getActivity(), PeopleFragment.this, PeopleFragment.this);
                            recyclerView.setAdapter(recycle_friends_adapter);
                            //           Toast.makeText(getBaseContext(),"Successful",Toast.LENGTH_SHORT).show();
                        } else {
                            //          Toast.makeText(getBaseContext(),"un Successful",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    void setID() {
        recyclerView = view.findViewById(R.id.rv_people);
        swipeRefreshLayout = view.findViewById(R.id.refresh_people);
        fbtn_add_friend = view.findViewById(R.id.fbtn_add_friend);
        fbtn_search_friend = view.findViewById(R.id.fbtn_search_friend);
        searchView = view.findViewById(R.id.search_name_fragment);
    }

    @Override
    public void onClick(Person person) {
        db.collection("users").
                document(user.getUid()).
                collection("friends").document(person.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            dateOfPersonFriend(person);
                        } else {
                            Toast.makeText(getActivity(), "Friend is delete before", Toast.LENGTH_SHORT).show();

                            setData();
                        }
                    }
                });
    }

    private void dateOfPersonFriend(Person person) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        map.put("name", person.getName());
        map.put("img_url", person.getImg_profile());
        map.put("id_friend", person.getId());
        map.put("name_sender", "account_user.getName()");
        map.put("tokenID", person.getTokenID());
        intent.putExtra("mapPerson", (Serializable) map);
        startActivity(intent);
    }

}