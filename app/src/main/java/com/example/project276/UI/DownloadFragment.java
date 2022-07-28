package com.example.project276.UI;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.project276.R;

import java.io.IOException;
import java.util.Objects;

public class DownloadFragment extends AppCompatDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //create the view to show
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.download_alert, null);

        //create a button listener
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:

                        // Call method from MapsActivity
                        try {
                            //((MapsActivity) Objects.requireNonNull(getActivity())).initiateDownloadRestaurants();
                            //((MapsActivity) Objects.requireNonNull(getActivity())).initiateDownloadsInspections();
                            ((MapsActivity) Objects.requireNonNull(getActivity())).initiateDownloads();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("error", "ERROR FROM DownloadFragment.java: " + e.toString());
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:

                        //Do nothing

                        break;
                }
                Log.i("TAG", "You clicked");
            }
        };

        //Build the alert dialog
        return new AlertDialog.Builder(getActivity())
                .setTitle("Update Found")
                .setView(v)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, listener)
                .create();
    }


}
