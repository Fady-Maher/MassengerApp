package com.example.massengerapp.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.widget.ProgressBar;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.massengerapp.ui.ChatActivity;
import com.example.massengerapp.classes.LastMessage;
import com.example.massengerapp.R;
import com.example.massengerapp.customadapter.OnClickListenerItem;
import com.example.massengerapp.customadapter.OnLongClickListenerItem;
import com.example.massengerapp.customadapter.Person;
import com.example.massengerapp.customadapter.Recycle_Person_Adapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ChatsFragment extends Fragment implements OnClickListenerItem, OnLongClickListenerItem {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private SearchView searchView;
    private View view;
    private Recycle_Person_Adapter recycle_person_adapter = null;
    private ArrayList<LastMessage> chatArrayList;
    private ArrayList<LastMessage> lastMessages = new ArrayList();
    private RecyclerView recyclerView = null;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager manager;
    private Map<String, String> map = new HashMap<>();
    private ArrayList<Person> old_list_person = null;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private LastMessage lastMessage;
    private boolean flag = false;
    private ProgressBar progress;


    public ChatsFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        chatArrayList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chats, container, false);

        setID();
        recyclerView.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        setData();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(getActivity(), "swipeRefreshLayout", Toast.LENGTH_SHORT).show();
                chatArrayList.clear();
                recycle_person_adapter.notifyDataSetChanged();
                setData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        Toast.makeText(getActivity(), "oncreateview", Toast.LENGTH_SHORT).show();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (flag) {
            recyclerView.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            chatArrayList.clear();
            recycle_person_adapter.notifyDataSetChanged();
            setData();
        }

    }

    void setID() {
        recyclerView = view.findViewById(R.id.rv_chats);
        swipeRefreshLayout = view.findViewById(R.id.refresh_chats);
        progress = view.findViewById(R.id.progress_fragment_chat);
    }


    void setData() {
        chatArrayList.clear();
        db.collection("users").document(user.getUid()).collection("channels").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        lastMessage = Objects.requireNonNull(document.toObject(LastMessage.class));
                        if (lastMessage.getMessage() != null) {
                            lastMessage.setId(document.getId());
                            chatArrayList.add(lastMessage);
                        }
                    }
                    sortMessageByDate();
                    Log.e("chatArrayList", chatArrayList.size() + "");
                    recycle_person_adapter = new Recycle_Person_Adapter(chatArrayList, getActivity(), ChatsFragment.this, ChatsFragment.this);
                    recyclerView.setAdapter(recycle_person_adapter);
                    progress.setVisibility(View.GONE);
                    recyclerView.setEnabled(true);
                    //      Toast.makeText(getActivity(),"Successful",Toast.LENGTH_SHORT).show();
                } else {
                    //     Toast.makeText(getActivity(),"un Successful",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sortMessageByDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss:SS a", Locale.US);
        Collections.sort(chatArrayList, new Comparator<LastMessage>() {
            @Override
            public int compare(LastMessage lastMessage, LastMessage t1) {
                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = dateFormat.parse(lastMessage.getDate());
                    date2 = dateFormat.parse(t1.getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                assert date2 != null;
                return date2.compareTo(date1);
            }
        });
    }


    @Override
    public void onClick(Person person) {

        flag = true;
        /*
        db.collection("users").
                document(user.getUid()).
                collection("channels")
                .document(person.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    dateOfPersonFriend(person);
                }else{
                    Toast.makeText(getActivity(),"Friend is delete before",Toast.LENGTH_SHORT).show();
                    setData();
                }
            }
        });
        */

        Intent intent = new Intent(getActivity(), ChatActivity.class);
        map.put("name", person.getName());
        map.put("img_url", person.getImg_profile());
        map.put("id_friend", person.getId());
        map.put("name_sender", "account_user.getName()");
        map.put("tokenID", person.getTokenID());
        intent.putExtra("mapPerson", (Serializable) map);

        if (getActivity().getIntent().getStringExtra("message") != null) {
            intent.putExtra("message", getActivity().getIntent().getStringExtra("message"));
            getActivity().getIntent().removeExtra("message");
            Log.e("sssssss: ", "true fragment");
        }
        startActivity(intent);
    }

    private void dateOfPersonFriend(Person person) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        map.put("name", person.getName());
        map.put("img_url", person.getImg_profile());
        map.put("id_friend", person.getId());
        map.put("name_sender", "account_user.getName()");
        map.put("tokenID", person.getTokenID());
        intent.putExtra("mapPerson", (Serializable) map);

        if (getActivity().getIntent().getStringExtra("message") != null) {
            intent.putExtra("message", getActivity().getIntent().getStringExtra("message"));
            getActivity().getIntent().removeExtra("message");
            Log.e("sssssss: ", "true fragment");
        }
        startActivity(intent);
    }

    @Override
    public void onLongClick() {
        Toast.makeText(getActivity(), "onLongClick", Toast.LENGTH_SHORT).show();
    }
}




