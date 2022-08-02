package com.example.massengerapp.customadapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.massengerapp.R;
import com.example.massengerapp.fragments.PeopleFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Recycle_Friends_Adapter extends RecyclerView.Adapter<Recycle_Friends_Adapter.FriendsViewHolder> {
    ArrayList<Person> list_person = new ArrayList<>();
    Context context;
    OnClickListenerItem onClickListenerItem;
    PeopleFragment peopleFragment;
    String text = "";
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    public Recycle_Friends_Adapter(ArrayList<Person> list_person, Context context, OnClickListenerItem onClickListenerItem, PeopleFragment peopleFragment) {
        this.list_person = list_person;
        this.context = context;
        this.onClickListenerItem = onClickListenerItem;
        this.peopleFragment = peopleFragment;

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
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_item_friends, parent, false);
        return new FriendsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
        Person person = list_person.get(position);
        try {
            Glide.with(context).load(person.getImg_profile()).placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person).into(holder.img_profile_friend);
        } catch (Exception e) {
            holder.img_profile_friend.setImageResource(R.drawable.ic_person);
        }

        holder.txt_name_friend.setText(person.getName());

        holder.btn_delete_rc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);

                View viewDailog = LayoutInflater.from(context).inflate(R.layout.alert_dialog_delete, null);

                Button btn_delete = viewDailog.findViewById(R.id.btn_delete_person);
                Button btn_cancel = viewDailog.findViewById(R.id.btn_cancel);
                RadioGroup radioGroup = viewDailog.findViewById(R.id.radio_group_delete);

                alertBuilder.setView(viewDailog);
                AlertDialog alertDialog = alertBuilder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        // find which radio button is selected
                        if (checkedId == R.id.radioButton_with_message) {
                            text = "with_message";
                        } else if (checkedId == R.id.radioButton_without_message) {
                            text = "without_message";
                        }
                    }
                });


                btn_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (text.equals("with_message")) {
                            //          Toast.makeText(context,"with_message",Toast.LENGTH_SHORT).show();
                            deletePerson(person);
                            deleteChannel(person);
                            (new Handler()).postDelayed(this::refreshview, 1500);
                            alertDialog.dismiss();
                        } else if (text.equals("without_message")) {
                            //         Toast.makeText(context,"without_message",Toast.LENGTH_SHORT).show();
                            deletePerson(person);
                            (new Handler()).postDelayed(this::refreshview, 1500);
                            alertDialog.dismiss();
                        } else {
                            Toast.makeText(context, "Please, check one of two options!!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    private void refreshview() {
                        //        Toast.makeText(context,"context",Toast.LENGTH_SHORT).show();

                        peopleFragment.setData();
                    }

                });

                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //            Toast.makeText(context,"Cancel process of delete friend ",Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                });


                alertDialog.show();
                ///////ONCLICK
            }
        });
    }

    private void deletePerson(Person person) {
        db.collection("users").
                document(user.getUid()).
                collection("friends")
                .document(person.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            db.collection("users").
                                    document(user.getUid()).
                                    collection("friends")
                                    .document(person.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //         Toast.makeText(context,"delete1 friend succ ",Toast.LENGTH_SHORT).show();
                                            } else {
                                                //        Toast.makeText(context,"delete1 friend succ failed ",Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                    });
                        } else {
                            //     Toast.makeText(context,"delete1 friend already ",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        db.collection("users").
                document(person.getId()).
                collection("friends")
                .document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            db.collection("users").
                                    document(person.getId()).
                                    collection("friends")
                                    .document(user.getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //              Toast.makeText(context,"delete2 friend succ",Toast.LENGTH_SHORT).show();
                                            } else {
                                                //             Toast.makeText(context,"delete2 friend succ failed",Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                    });
                        } else {
                            //      Toast.makeText(context,"delete2 friend already ",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deleteChannel(Person person) {

        db.collection("users").
                document(user.getUid()).
                collection("channels")
                .document(person.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            deleteChats(documentSnapshot.getId());
                            db.collection("users").
                                    document(user.getUid()).
                                    collection("channels")
                                    .document(person.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //                   Toast.makeText(context,"delete1 deleteChannel succ ",Toast.LENGTH_SHORT).show();
                                            } else {
                                                //                    Toast.makeText(context,"delete1 deleteChannel succ failed ",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            //        Toast.makeText(context,"delete1 deleteChannel already ",Toast.LENGTH_SHORT).show();
                        }
                    }
                });


        db.collection("users").
                document(person.getId()).
                collection("channels")
                .document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            db.collection("users").
                                    document(person.getId()).
                                    collection("channels")
                                    .document(user.getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //              Toast.makeText(context,"delete2 deleteChannel succ ",Toast.LENGTH_SHORT).show();
                                            } else {
                                                //                Toast.makeText(context,"delete2 deleteChannel succ failed ",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            //     Toast.makeText(context,"delete2 deleteChannel already ",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // find error in firebase
    private void deleteChats(String iDChannel) {

        db.collection("chatsChannel").document(iDChannel).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.e("delete succ", "delete2 chatsChannel succ");
                    //      Toast.makeText(context,"delete2 chatsChannel succ ",Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("delete failed", "delete2 chatsChannel failed");
                    //       Toast.makeText(context,"delete2 chatsChannel succ failed ",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("e", e.toString());

            }
        });

    }


    public class FriendsViewHolder extends RecyclerView.ViewHolder {

        CircleImageView img_profile_friend;
        TextView txt_name_friend;
        ImageButton btn_delete_rc;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            img_profile_friend = itemView.findViewById(R.id.image_profile_friend);
            txt_name_friend = itemView.findViewById(R.id.text_name_friend);
            btn_delete_rc = itemView.findViewById(R.id.img_btn_delete_rc);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickListenerItem.onClick(list_person.get(getAdapterPosition()));
                }
            });
        }
    }
}