package com.example.massengerapp.customadapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.massengerapp.R;
import com.example.massengerapp.ui.RequestsPersonActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Recycle_Request_Person_Adapter extends RecyclerView.Adapter<Recycle_Request_Person_Adapter.PersonRequestViewHolder> {
    ArrayList<Person> list_person = new ArrayList<>();
    Context context;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    public Recycle_Request_Person_Adapter(ArrayList<Person> list_person, Context context) {
        this.list_person = list_person;
        this.context = context;

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
    }

    @Override
    public int getItemCount() {
        return list_person.size();
    }

    @NonNull
    @Override
    public PersonRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_item_request, parent, false);
        return new PersonRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonRequestViewHolder holder, int position) {
        Person person = list_person.get(position);
        try {
            Glide.with(context).load(person.getImg_profile()).placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person).into(holder.img_profile_friend);
        } catch (Exception e) {
            holder.img_profile_friend.setImageResource(R.drawable.ic_person);
        }

        holder.txt_name_friend.setText(person.getName());

        holder.img_btn_accept_rc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //from this place , will add friend to people
                db.collection("users").
                        document(user.getUid()).
                        collection("friends")
                        .document(person.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (!documentSnapshot.exists()) {
                                    db.collection("users").
                                            document(user.getUid()).
                                            collection("friends")
                                            .document(person.getId()).set(person).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(context, "Requests accept ", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(context, "Requests accept failed ", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                            });
                                } else {
                                    Toast.makeText(context, "Requests already accepted ", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                db.collection("users").
                        document(person.getId()).
                        collection("friends")
                        .document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (!documentSnapshot.exists()) {
                                    db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                Person user = task.getResult().toObject(Person.class);
                                                db.collection("users").
                                                        document(person.getId()).
                                                        collection("friends")
                                                        .document(user.getId()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(context, "Requests accept ", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(context, "Requests accept failed ", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }

                                                        });
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(context, "Requests already accepted ", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                db.collection("users").
                        document(user.getUid()).
                        collection("Requests")
                        .document(person.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    db.collection("users").
                                            document(user.getUid()).
                                            collection("Requests")
                                            .document(person.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(context, "Requests delete1 ", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(context, "Requests delete failed ", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            }
                        });

                db.collection("users").
                        document(person.getId()).
                        collection("Requests")
                        .document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    db.collection("users").
                                            document(person.getId()).
                                            collection("Requests")
                                            .document(user.getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(context, "Requests delete2 ", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(context, "Requests delete failed ", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            }
                        });

                //to untill all above this code ir running
                (new Handler()).postDelayed(this::refreshview, 1000);

            }

            private void refreshview() {
                if (context.getClass().equals(RequestsPersonActivity.class)) {
                    Toast.makeText(context, "context", Toast.LENGTH_SHORT).show();

                    ((RequestsPersonActivity) context).setData();
                }
            }

        });
    }

    public class PersonRequestViewHolder extends RecyclerView.ViewHolder {

        CircleImageView img_profile_friend;
        TextView txt_name_friend;
        ImageButton img_btn_accept_rc;

        public PersonRequestViewHolder(@NonNull View itemView) {
            super(itemView);

            img_profile_friend = itemView.findViewById(R.id.img_profile_request_friend);
            txt_name_friend = itemView.findViewById(R.id.txt_name_request_friend);
            img_btn_accept_rc = itemView.findViewById(R.id.img_btn_accept_rc);

        }
    }
}
