package com.example.massengerapp.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.massengerapp.classes.Chats;
import com.example.massengerapp.FCMSenderNotifiaction.FcmNotificationsSender;
import com.example.massengerapp.additionalclasses.GPSTracker;
import com.example.massengerapp.R;
import com.example.massengerapp.classes.RecordAudio;
import com.example.massengerapp.connectionnewtwork.NetworkChangeListener;
import com.example.massengerapp.customadapter.Person;
import com.example.massengerapp.customadapter.Recycle_Message_Adapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    CircleImageView profile_image_chat, img_back_chat;
    ImageView img_attach_file, image_record, img_camera;
    TextView txt_name_chat, txt_timer_record, txt_cancel_record;
    EditText edit_message;
    LinearLayout lin_activity_chat1, lin_activity_chat2;
    String iDChannel = "", senderId, geoUri = "", currentdate = "", nextdate = "";
    int Gallary_REQUEST_CODE = 1, SONGS_REQUEST_CODE = 2;
    private Map<String, String> map = new HashMap<>();
    private final Map<String, String> mapChannel = new HashMap<>();
    private Chats chat;
    private Chats messages;
    private RecyclerView recyclerView = null;
    private Recycle_Message_Adapter recycle_message_adapter = null;
    private ArrayList<Chats> chatsArrayList;
    private Bitmap bitmap;
    private boolean flag_check_message = false, flagFirstTime = true;

    private FirebaseUser user;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    ProgressBar progress;


    private static final int Location_REQUEST_CODE = 2000;

    private static final int CAMERA_REQUEST_CODE = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    final int MY_Record_PERMISSION_CODE = 0;
    final Handler handler = new Handler();
    RecordAudio recordAudio;
    boolean flag_running = true;

    private Handler customHandler = new Handler();
    private long startHTime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        chatsArrayList = new ArrayList<>();
        recordAudio = new RecordAudio();
        setID();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext());
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);


        if (getIntent().getSerializableExtra("mapPerson") != null) {
            map = (Map<String, String>) getIntent().getSerializableExtra("mapPerson");

            retriveDate();

        } else if (getIntent().getStringExtra("senderid") != null) {
            senderId = getIntent().getStringExtra("senderid");
            Log.e("senderid", getIntent().getStringExtra("senderid"));

            getSpecifyPersonInfo(senderId);

            (new Handler()).postDelayed(this::retriveDate, 2000);
        }

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getIntent().getStringExtra("message") != null) {
                    Gson gson = new Gson();
                    Chats chat = gson.fromJson(getIntent().getStringExtra("message"), Chats.class);
                    getIntent().removeExtra("message");
                    chat.setDate(getDateTime());
                    storeMessageOnFirebase(chat);
                    Log.e("message : ", "true");
                }
            }
        }, 1000);


        //////////////////to get location

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    Location_REQUEST_CODE);
        } else {

            GPSTracker currentLocation = new GPSTracker(getApplicationContext());
            geoUri = "http://www.google.com/maps/place/" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "";
        }

        img_camera.setClickable(true);
        img_attach_file.setClickable(true);
        //to check to all edittext is not empty
        edit_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    flag_check_message = true;
                    image_record.setImageResource(R.drawable.ic_send);
                } else {
                    flag_check_message = false;
                    image_record.setImageResource(R.drawable.ic_mic);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        img_back_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recycle_message_adapter != null) {
                    recycle_message_adapter.stoppingPlayer();
                }
                finish();
            }
        });

        img_attach_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog();
            }
        });

        img_camera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CAMERA) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.CAMERA},
                            MY_CAMERA_PERMISSION_CODE);
                } else {

                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                }
            }
        });

        image_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag_check_message) {
                    chat = new Chats(edit_message.getText().toString(), getDateTime(), user.getUid(), map.get("id_friend"), "", map.get("name_sender"), "");
                    edit_message.setText("");
                    storeMessageOnFirebase(chat);

                    FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(map.get("tokenID"),
                            chat.getNameSender(), chat.getMessage(), user.getUid(), getApplicationContext(), ChatActivity.this);
                    fcmNotificationsSender.SendNotifications();
                } else {

                    if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{
                                        Manifest.permission.RECORD_AUDIO}
                                , MY_Record_PERMISSION_CODE);
                    } else {
                        if (flag_running) { // running         flagrunning == true --> start
                            actionStartRecord();
                            Log.i("AudioRecorder", "Start Recording");
                            recordAudio.startRecording();
                            startHTime = SystemClock.uptimeMillis();
                            customHandler.postDelayed(updateTimerThread, 0);
                            flag_running = false;
                        } else {
                            if (!flag_running) {
                                progress.setVisibility(View.VISIBLE);
                                actionStopRecord();
                                Log.i("AudioRecorder", "stop Recording");
                                uploadRecord(Uri.fromFile(new File(recordAudio.getFilename())));
                            }
                        }
                    }
                }
            }
        });

        txt_cancel_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!flag_running) {
                    File fdelete = new File(recordAudio.getFilename());
                    if (fdelete.exists()) {
                        if (fdelete.delete()) {
                            Toast.makeText(getApplicationContext(), "Cancel Record ", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed Cancel Record ", Toast.LENGTH_SHORT).show();
                        }
                    }
                    actionStopRecord();
                }

            }
        });


    }

    private void actionStopRecord() {
        txt_timer_record.setText("00:00");
        lin_activity_chat1.setVisibility(View.VISIBLE);
        lin_activity_chat2.setVisibility(View.GONE);
        image_record.setImageResource(R.drawable.ic_mic);
        recordAudio.stopRecording();
        flag_running = true;
        edit_message.setEnabled(true);
        edit_message.setInputType(InputType.TYPE_CLASS_TEXT);
        img_camera.setEnabled(true);
        img_attach_file.setEnabled(true);
        timeSwapBuff = 0L;
        customHandler.removeCallbacks(updateTimerThread);
    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startHTime;

            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            if (txt_timer_record != null)
                txt_timer_record.setText("" + String.format("%02d", mins) + ":"
                        + String.format("%02d", secs));
            customHandler.postDelayed(this, 0);
        }

    };

    private void actionStartRecord() {
        lin_activity_chat1.setVisibility(View.GONE);
        lin_activity_chat2.setVisibility(View.VISIBLE);
        edit_message.setEnabled(false);
        edit_message.setInputType(InputType.TYPE_NULL);
        img_camera.setEnabled(false);
        img_attach_file.setEnabled(false);
        image_record.setImageResource(R.drawable.ic_send);
    }


    private void alertDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ChatActivity.this);
        View viewDailog = LayoutInflater.from(ChatActivity.this).inflate(R.layout.alert_dialog_chat_activity, null);
        alertBuilder.setView(viewDailog);

        ImageView image_audio_file = viewDailog.findViewById(R.id.image_audio_file);
        ImageView image_gallary_file = viewDailog.findViewById(R.id.image_gallary_file);
        ImageView image_location = viewDailog.findViewById(R.id.image_location);

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.y = 150;
        window.setAttributes(wlp);

        // alertDialog.setCancelable(false);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        image_audio_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent song_intent = new Intent();
                song_intent.setAction(android.content.Intent.ACTION_PICK);
                song_intent.setData(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(song_intent, SONGS_REQUEST_CODE);
                alertDialog.dismiss();
            }
        });

        image_gallary_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                String[] mimetypes = {"image/*", "video/*"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                startActivityForResult(intent, Gallary_REQUEST_CODE);
                alertDialog.dismiss();

            }
        });

        image_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!geoUri.equals("")) {
                    edit_message.setText(geoUri);
                } else {
                    Toast.makeText(getApplicationContext(), "please , Check premmision of location !!", Toast.LENGTH_SHORT).show();
                }
                alertDialog.dismiss();
            }
        });

    }

    private void uploadRecord(Uri path) {
        Log.e("uploadRecord : ", path + "");

        storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference storageReference = storageRef.child("records/recordsChat/" + System.currentTimeMillis());
        storageReference.putFile(path).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.e("uri.toString() : ", uri.toString());

                        chat = new Chats("", getDateTime(), user.getUid(), map.get("id_friend"), "", map.get("name_sender"), uri.toString());
                        storeMessageOnFirebase(chat);
                        progress.setVisibility(View.GONE);
                        Toast.makeText(getBaseContext(), "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(getApplicationContext(), "Successful Upload ", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("onFailure : ", e.toString());
                //  Toast.makeText(getApplicationContext(),"UnSuccessful Upload ",Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void retriveDate() {
        try {
            Glide.with(getBaseContext()).asBitmap().load(map.get("img_url")).placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person).into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            profile_image_chat.setImageBitmap(resource);
                            //     Log.e(" bitmap.toString : ",  resource+"");
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        } catch (Exception e) {
            profile_image_chat.setImageResource(R.drawable.ic_person);
        }

        txt_name_chat.setText(map.get("name"));

        createChannelOnFirebase(map.get("id_friend"));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            try {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                uploadImage(bitmap);
            } catch (Exception e) {
                Log.e("camera exception: ", e.getMessage());
            }
        } else if (requestCode == Gallary_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getBaseContext().getContentResolver(), data.getData());
                uploadImage(bitmap);
                //     Toast.makeText(getBaseContext(), "Successful", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e("gallary exception: ", e.getMessage());
            }
        } else if (requestCode == SONGS_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            progress.setVisibility(View.VISIBLE);
            Uri uri = data.getData();
            Log.e("uri : ", uri + "");
            uploadRecord(Uri.fromFile(new File(getRealPathFromURI(uri))));
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // Toast.makeText(getBaseContext(), "Canceled", Toast.LENGTH_SHORT).show();
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void uploadImage(Bitmap bitmap) {
        ByteArrayOutputStream output_image = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, output_image);
        byte[] data_image = output_image.toByteArray();
        StorageReference storageReference = storageRef.child("images/imagesChat/" + user.getUid());

        storageReference.putBytes(data_image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.e("uri.toString() : ", uri.toString());

                        chat = new Chats("", getDateTime(), user.getUid(), map.get("id_friend"), uri.toString(), map.get("name_sender"), "");
                        storeMessageOnFirebase(chat);

                        //           Toast.makeText(getBaseContext(), "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    }
                });

                //      Toast.makeText(getApplicationContext(),"Successful Upload ",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //  Toast.makeText(getApplicationContext(),"UnSuccessful Upload ",Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getlast_MessagesFriend();
                handler.postDelayed(this, 1000); //repeat handler
            }
        };
        handler.post(runnable);


    }

    private void setID() {


        profile_image_chat = findViewById(R.id.profile_image_chat);
        txt_name_chat = findViewById(R.id.txt_name_chat);

        edit_message = findViewById(R.id.edit_message);
        img_back_chat = findViewById(R.id.img_back_chat);
        recyclerView = findViewById(R.id.rv_chat_friend);
        img_camera = findViewById(R.id.img_camera);
        img_attach_file = findViewById(R.id.img_attach_file);
        image_record = findViewById(R.id.image_record);
        progress = findViewById(R.id.progress_activity_chat);
        lin_activity_chat1 = findViewById(R.id.lin_activity_chat1);
        lin_activity_chat2 = findViewById(R.id.lin_activity_chat2);
        txt_timer_record = findViewById(R.id.txt_timer_record);
        txt_cancel_record = findViewById(R.id.txt_cancel_record);
    }


    private void storeMessageOnFirebase(Chats chats) {

        db.collection("chatsChannel").
                document(iDChannel).
                collection("messages")
                .add(chats);

        nextdate = chats.getDate();
        addlastMessagetochatsChannel(chats, chats.getDate());
    }

    private String[] splitDateTime(String dateFormat) {

        return dateFormat.split(" ");
    }

    private void addlastMessagetochatsChannel(Chats chats, String date) {
        Map<String, Object> maplastMessage = new HashMap<>();
        String time = splitDateTime(date)[1].substring(0, splitDateTime(date)[1].length() - 6) + " " + splitDateTime(date)[2];
        maplastMessage.put("time", time);
        maplastMessage.put("message", chats.getMessage());
        maplastMessage.put("date", chats.getDate());
        maplastMessage.put("senderid", chats.getSenderid());
        maplastMessage.put("recieveid", chats.getRecieveid());
        maplastMessage.put("image", chats.getImage());
        maplastMessage.put("nameSender", chats.getNameSender());
        maplastMessage.put("record", chats.getRecord());

        db.collection("users").document(user.getUid()).collection("channels").document(Objects.requireNonNull(map.get("id_friend")))
                .update(maplastMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Successful add message ", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Unsuccessful add message ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        db.collection("users").document(Objects.requireNonNull(map.get("id_friend"))).collection("channels").document(user.getUid())
                .update(maplastMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Successful add message ", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Unsuccessful add message ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void createChannelOnFirebase(String id_friend) {

        db.collection("users").
                document(id_friend).
                collection("channels")
                .document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {

                            String id_channel = Objects.requireNonNull(documentSnapshot.get("id")).toString();
                            Log.e("id", id_channel);

                            iDChannel = id_channel;
                        } else {

                            DocumentReference idChannel = FirebaseFirestore.getInstance().collection("users").document();
                            mapChannel.put("id", idChannel.getId());

                            db.collection("users").
                                    document(user.getUid()).
                                    collection("channels")
                                    .document(id_friend).set(mapChannel);

                            db.collection("users").
                                    document(id_friend).
                                    collection("channels")
                                    .document(user.getUid()).set(mapChannel);

                            iDChannel = idChannel.getId();
                        }
                        getMessages(iDChannel);
                    }
                });
    }

    private void getMessages(String iDChannel) {
        getBitmapFromImage();

        chatsArrayList.clear();

        db.collection("chatsChannel").document(iDChannel).collection("messages")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    messages = document.toObject(Chats.class);
                                    //    Log.e("a  all : ", messages.getDate());
                                    chatsArrayList.add(messages);
                                }
                            }
                            sortArrayMessages(chatsArrayList);
                            //Toast.makeText(getBaseContext(),"Successful",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getBitmapFromImage() {
        try {
            bitmap = ((BitmapDrawable) profile_image_chat.getDrawable()).getBitmap();
        } catch (Exception E) {
            bitmap = Bitmap.createBitmap(profile_image_chat.getDrawable().getIntrinsicWidth(), profile_image_chat.getDrawable().getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            profile_image_chat.getDrawable().setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            profile_image_chat.getDrawable().draw(canvas);
        }
    }

    private void getlast_MessagesFriend() {
        db.collection("users").
                document(Objects.requireNonNull(map.get("id_friend"))).collection("channels").document(user.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        messages = documentSnapshot.toObject(Chats.class);
                        if (messages.getDate() != null) {
                            if (flagFirstTime) {
                                currentdate = messages.getDate();
                                nextdate = messages.getDate();
                                flagFirstTime = false;
                            } else {
                                //     Log.e("curr : ",currentdate);

                                //      Log.e("messages.getDate() : ",messages.getDate());
                                if (!currentdate.equals(messages.getDate())) {
                                    currentdate = messages.getDate();
                                    addItemToRecycleView(messages);
                                }
                            }
                        }
                    }
                });
    }


    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss:SS a", Locale.US);
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void addItemToRecycleView(Chats chats) {
        Log.e("last message :", chats.getMessage());
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss:SS a", Locale.US);
        Log.e("getDateTime() : ", chats.getDate());
        chatsArrayList.add(chats);
        Collections.sort(chatsArrayList, new Comparator<Chats>() {
            @Override
            public int compare(Chats lhs, Chats rhs) {
                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = dateFormat.parse(lhs.getDate());
                    date2 = dateFormat.parse(rhs.getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                assert date2 != null;
                return date2.compareTo(date1);
            }
        });
        recycle_message_adapter = new Recycle_Message_Adapter(chatsArrayList, ChatActivity.this, bitmap);
        recyclerView.setAdapter(recycle_message_adapter);
    }

    private void sortArrayMessages(ArrayList<Chats> chatsArrayList) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss:SS a", Locale.US);
        Collections.sort(chatsArrayList, new Comparator<Chats>() {
            @Override
            public int compare(Chats lhs, Chats rhs) {
                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = dateFormat.parse(lhs.getDate());
                    date2 = dateFormat.parse(rhs.getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                assert date2 != null;
                return date2.compareTo(date1);
            }
        });
        recycle_message_adapter = new Recycle_Message_Adapter(chatsArrayList, ChatActivity.this, bitmap);
        recyclerView.setAdapter(recycle_message_adapter);
        progress.setVisibility(View.GONE);
    }

    private void dateOfPersonFriend(Person person) {
        map.put("name", person.getName());
        map.put("img_url", person.getImg_profile());
        map.put("id_friend", person.getId());
        map.put("name_sender", "account_user.getName()");
        map.put("tokenID", person.getTokenID());
    }

    private void getSpecifyPersonInfo(String ID) {

        Log.e("String ", ID);

        db.collection("users").whereEqualTo("id", ID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (document.exists()) {
                            Person person = document.toObject(Person.class);
                            Log.e("String2 ", person.getId());
                            dateOfPersonFriend(person);
                            //Toast.makeText(getBaseContext(),"getSpecifyPersonInfo Successful",Toast.LENGTH_SHORT).show();
                        }
                    }

                } else {
                    Toast.makeText(getBaseContext(), "person[0] unSuccessful", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Location_REQUEST_CODE:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                }
                return;

            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                } else {
                    Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                }

            case MY_Record_PERMISSION_CODE:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                return;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (recycle_message_adapter != null) {
            recycle_message_adapter.stoppingPlayer();
        }
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
