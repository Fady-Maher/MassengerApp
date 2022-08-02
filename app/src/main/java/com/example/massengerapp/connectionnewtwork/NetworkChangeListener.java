package com.example.massengerapp.connectionnewtwork;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import com.example.massengerapp.R;

public class NetworkChangeListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int status = CheckNetwork.getConnectivityStatusString(context);

        // if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
        Log.e(" network reciever", "network reciever  " + status);
        if (status == CheckNetwork.NETWORK_STATUS_NOT_CONNECTED) {
            Log.e("network : ", "false");
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
            View viewDailog = LayoutInflater.from(context).inflate(R.layout.checknetwork, null);
            alertBuilder.setView(viewDailog);

            Button btn_check_network = viewDailog.findViewById(R.id.btn_retry);

            AlertDialog alertDialog = alertBuilder.create();
            alertDialog.show();
            alertDialog.setCancelable(false);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            btn_check_network.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                    onReceive(context, intent);
                }
            });
        } else {
            return;
        }
        //  }
    }
}


