package com.example.project276.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.project276.Model.RestaurantManager;
import com.example.project276.R;

public class SearchingOptionsActivity extends AppCompatActivity {
    RestaurantManager manager = RestaurantManager.getManager();

    public static Intent makeIntent(Context context){
        return new Intent(context, SearchingOptionsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching_options);

        manager.setHazardLevelSearch(0);
        createRadioButtons();
    }

    @SuppressLint("StringFormatInvalid")
    private void createRadioButtons(){
        RadioGroup group = findViewById(R.id.HazardLevelRadioGroup);
        int[] hazrdLevelOptions = getResources().getIntArray(R.array.hazard_levels);
        String[] hazLev = {"Any","Low","Moderate","High"};

        for (final int hazardLevel : hazrdLevelOptions) {
            RadioButton button = new RadioButton(this);
            button.setText(hazLev[hazardLevel]);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    manager.setHazardLevelSearch(hazardLevel);
                }
            });
            group.addView(button);

        }
        Button buttonDone = findViewById(R.id.DoneButtonOptions);
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}