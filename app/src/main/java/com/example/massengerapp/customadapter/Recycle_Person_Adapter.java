package com.example.massengerapp.customadapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.massengerapp.classes.LastMessage;
import com.example.massengerapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Recycle_Person_Adapter extends RecyclerView.Adapter<Recycle_Person_Adapter.PersonViewHolder> {
    ArrayList<LastMessage> list_message = null;
    ArrayList<Person> list_person = new ArrayList<>();
    Context context;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    OnClickListenerItem onClickListenerItem;
    OnLongClickListenerItem onLongClickListenerItem;


    public Recycle_Person_Adapter(ArrayList<LastMessage> list_message, Context context, OnClickListenerItem onClickListenerItem,
                                  OnLongClickListenerItem onLongClickListenerItem) {
        this.list_message = list_message;
        this.context = context;
        this.onClickListenerItem = onClickListenerItem;
        this.onLongClickListenerItem = onLongClickListenerItem;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
    }

    @Override
    public int getItemCount() {
        return list_message.size();
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_item, parent, false);
        return new PersonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, int position) {
        LastMessage message = list_message.get(position);
        if (message.getMessage().equals("")) {
            holder.txt_massage.setText("You received a media");
        } else {
            holder.txt_massage.setText(message.getMessage());
        }

        holder.txt_time.setText(message.getTime());
        db.collection("users").whereEqualTo("id", message.getId())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Person person = document.toObject(Person.class);
                                try {
                                    Glide.with(context).load(person.getImg_profile()).placeholder(R.drawable.ic_person).
                                            error(R.drawable.ic_person).into(holder.img_profile_friend);
                                } catch (Exception e) {
                                    holder.img_profile_friend.setImageResource(R.drawable.ic_person);
                                }

                                holder.txt_name_friend.setText(person.getName());
                                list_person.add(person);
                            }
                            //      Toast.makeText(getActivity(),"Successful",Toast.LENGTH_SHORT).show();
                        } else {
                            //     Toast.makeText(getActivity(),"un Successful",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public class PersonViewHolder extends RecyclerView.ViewHolder {

        CircleImageView img_profile_friend;
        TextView txt_name_friend, txt_time, txt_massage;

        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);

            img_profile_friend = itemView.findViewById(R.id.img_profile_friend);
            txt_name_friend = itemView.findViewById(R.id.txt_name_friend);
            txt_time = itemView.findViewById(R.id.txt_time);
            txt_massage = itemView.findViewById(R.id.txt_massage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("pos :  ", getAdapterPosition() + "");
                    onClickListenerItem.onClick(list_person.get(getAdapterPosition()));
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    onLongClickListenerItem.onLongClick();
                    return false;
                }
            });

        }
    }

    public ArrayList<Person> getDate() {
        return list_person;
    }

}
