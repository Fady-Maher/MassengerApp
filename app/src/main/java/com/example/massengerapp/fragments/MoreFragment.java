package com.example.massengerapp.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.massengerapp.ui.ProfileActivity;
import com.example.massengerapp.R;
import com.example.massengerapp.ui.SignInActivity;
import com.example.massengerapp.classes.RecordAudio;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MoreFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount acct = null;
    Button btn_play;
    final int MY_Record_PERMISSION_CODE = 0;


    ProgressDialog dialog;
    DownloadFileFromURL downloadFileFromURL;
    boolean downloading_status = false;

    private static double SPACE_KB = 1024;
    private static double SPACE_MB = 1024 * SPACE_KB;
    private static double SPACE_GB = 1024 * SPACE_MB;
    private static double SPACE_TB = 1024 * SPACE_GB;
    long total = 0;
    int lenghtOfFile = 0;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    RecordAudio recordAudio;

    public MoreFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MoreFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MoreFragment newInstance(String param1, String param2) {
        MoreFragment fragment = new MoreFragment();
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
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_more, container, false);


        initialGoogleAccount();

        recordAudio = new RecordAudio();

        btn_play = view.findViewById(R.id.btn_play);


        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                    Intent intent =new Intent(getActivity(), ProfileActivity.class);
                    getActivity().startActivity(intent);
            //      signOut();
/*
                String url = "https://firebasestorage.googleapis.com/v0/b/massengerapp-17135.appspot.com/o/records%2FrecordsChat%2F1632148383004?alt=media&token=c8469d4e-749c-40a0-b874-98289ccc44af";
                downloadFileFromURL = (DownloadFileFromURL) new DownloadFileFromURL().execute(url);
*/
            }
        });

        return view;
    }

    public String getFilename() {

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
                , "DOWNLOAD_AUDIO");
        Log.e("file : ", file.getAbsolutePath());
        if (!file.exists()) {
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".mp3");
    }


    /*******************************************/

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            settingProgressDialog();
            Toast.makeText(getActivity(), "start Downloading", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;

            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                lenghtOfFile = connection.getContentLength();
                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);
                // Output stream
                OutputStream output = new FileOutputStream(getFilename());
                byte data[] = new byte[1024];
                total = 0;
                while ((count = input.read(data)) != -1) {
                    if (downloading_status || !downloadFileFromURL.isCancelled()) {
                        total += count;
                        // publishing the progress....
                        // After this onProgressUpdate will be called
                        publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                        // writing data to file
                        output.write(data, 0, count);
                    } else {
                        break;
                    }
                }
                // flushing output
                output.flush();
                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        protected void onProgressUpdate(String... progress) {
            downloading_status = true;
            Log.e("progess : ", progress[0] + "");
            dialog.setProgressNumberFormat((bytes2String(total)) + "/" + (bytes2String(lenghtOfFile)));
            dialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            Toast.makeText(getActivity(), "finish download", Toast.LENGTH_SHORT).show();
            downloading_status = false;
            dialog.dismiss();
        }

    }

    private void settingProgressDialog() {
        total = 0;
        lenghtOfFile = 0;
        dialog = new ProgressDialog(getActivity(), R.style.AlertDialogCustom);
        dialog.setMessage("Download in progress ...");
        dialog.setTitle("Downloading Audio");
        dialog.setIndeterminate(false);
        dialog.setMax(100);
        dialog.setCancelable(false);
        dialog.setIcon(R.drawable.ic_save);
        dialog.setProgressNumberFormat((bytes2String(total)) + "/" + (bytes2String(lenghtOfFile)));
        dialog.setProgress(0);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(true);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadFileFromURL.cancel(true);
                downloading_status = false;  //add boolean check
                File fdelete = new File(getFilename());
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        Toast.makeText(getActivity(), "Cancel Downloading ", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed Cancel Downloading ", Toast.LENGTH_SHORT).show();
                    }
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    public static String bytes2String(long sizeInBytes) {

        NumberFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(2);

        try {
            if (sizeInBytes < SPACE_KB) {
                return nf.format(sizeInBytes) + " Byte(s)";
            } else if (sizeInBytes < SPACE_MB) {
                return nf.format(sizeInBytes / SPACE_KB) + " KB";
            } else if (sizeInBytes < SPACE_GB) {
                return nf.format(sizeInBytes / SPACE_MB) + " MB";
            } else if (sizeInBytes < SPACE_TB) {
                return nf.format(sizeInBytes / SPACE_GB) + " GB";
            } else {
                return nf.format(sizeInBytes / SPACE_TB) + " TB";
            }
        } catch (Exception e) {
            return sizeInBytes + " Byte(s)";
        }

    }


    /*******************************************/
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_Record_PERMISSION_CODE:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                return;

        }
    }


    private void initialGoogleAccount() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        acct = GoogleSignIn.getLastSignedInAccount(getActivity());
    }

    private void signOut() {
        if (acct != null) {
            FirebaseAuth.getInstance().signOut();
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(getActivity(), SignInActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            getActivity().finish();
                            Toast.makeText(getActivity(), "User account sign out. google", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
            Toast.makeText(getActivity(), "User account sign out. email", Toast.LENGTH_SHORT).show();

        }

    }
}