package com.example.project276.UI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.project276.Model.Inspection;
import com.example.project276.Model.Restaurant;
import com.example.project276.Model.RestaurantManager;
import com.example.project276.R;

import java.util.List;
import java.util.Objects;

//Displays details about a single restaurant and its list of inspections
public class RestaurantActivity extends AppCompatActivity {

    public static final String INSPECTION = "inspection" ;
    List<Inspection> inspections;
    public static final String NAME = "name" ;
    public static final String lat = "latitude";
    public static final String lon = "longitude";
    private RestaurantManager manager;
    private static boolean isclicked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Restaurant Information");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int restaurantIndex = getIntent().getIntExtra(MainActivity.restaurantIndex,-1);
        Log.d("RestaurantActivity", "onCreate: restaurantIndex = " + restaurantIndex);

        manager = RestaurantManager.getManager();

        Restaurant restaurant = null;


        if (restaurantIndex == -1) {
            Log.e("RestaurantActivity", " No restaurant");
        }
        else {
             restaurant = RestaurantManager.getIndex(restaurantIndex);
        }

        if (restaurant != null) {
            inspections = restaurant.getInspectionList();
            clickThePosition(restaurant);
            setRestaurantInformation(restaurant);
            populateInspectionList(restaurant);
            registerClickCallback(restaurant);
            initButton(restaurant);
           // isRestuarantFavourite(restaurant);
        }
        setupDefaultIntent();
    }

    private void isRestuarantFavourite(Restaurant restaurant) {
        ImageView img = (ImageView) findViewById(R.id.star);
        if(restaurant.isFavourite() == true){
            img.setVisibility(View.VISIBLE);
        }
    }

    private void initButton(final Restaurant restaurant) {
        Button favbutn = (Button) findViewById(R.id.favouritebutton);

        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        //https://stackoverflow.com/questions/7383752/how-to-save-the-state-of-the-tooglebutton-on-off-selection
//        final SharedPreferences preferences = getPreferences(MODE_PRIVATE);
//        boolean tgpref = preferences.getBoolean("tgpref", true);  //default is false
//        if (tgpref = false)
//        {
//            toggleButton.setChecked(false);
//        }
//        else
//        {
//            toggleButton.setChecked(true);
//        }
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){

                    isclicked = true;
                    Log.d("RestaurantActivity","isclicked TRUE" );
                    Toast.makeText(RestaurantActivity.this, "Added to Favourites", Toast.LENGTH_SHORT).show();
//                    SharedPreferences.Editor editor = preferences.edit();
//                    editor.putBoolean("tgpref", true);
//                    editor.commit();
                }
                else{

                    isclicked = false;
                    Log.d("RestaurantActivity","isclicked false" );
                    Toast.makeText(RestaurantActivity.this, "Removed from Favourites", Toast.LENGTH_SHORT).show();
//                    SharedPreferences.Editor editor = preferences.edit();
//                    editor.putBoolean("tgpref", false);
//                    editor.commit();
                }
            }
        });
//        favbutn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (isclicked == false) {
//                    isclicked = true;
//                    Log.d("RestaurantActivity","isclicked TRUE" );
//                    Toast.makeText(RestaurantActivity.this, "Added to Favourites", Toast.LENGTH_SHORT).show();
//                }
//                if(isclicked == true){
//                    isclicked = false;
//                    Log.d("RestaurantActivity","isclicked false" );
//                    Toast.makeText(RestaurantActivity.this, "Removed from Favourites", Toast.LENGTH_SHORT).show();
//                }
//
//            //    Toast.makeText(RestaurantActivity.this, "Yep", Toast.LENGTH_LONG).show();
//            }
//        });

        if(isclicked == true){
            Log.d("RestaurantActivity","IS CLICK == TO TRUE ");
            restaurant.setFavourite(true);
            Toast.makeText(RestaurantActivity.this, "Yep Its a favourite", Toast.LENGTH_LONG).show();
            isRestuarantFavourite(restaurant);
        }
        //        restaurant.setFavourite(restaurant1.isFavourite());
//        Log.d("RestaurantActivity","Restaraunt should = true? ");
//        if(restaurant.isFavourite()== true){
//            Log.d("RestaurantActivity","Restaraunt == true");
//        }
//        if(favbutn.isPressed()){
//            RestaurantActivity.this.finish();
//            restaurant.setFavourite(true);
//            Toast.makeText(RestaurantActivity.this, "Yep", Toast.LENGTH_LONG).show();
//        }
    }


    private void setRestaurantInformation(Restaurant restaurant) {
        TextView txtViewRestaurantName = findViewById(R.id.txtRestaurantName);
        txtViewRestaurantName.setText(
                getString(R.string.restaurant_name,restaurant.getName())
        );
        TextView txtViewAddress = findViewById(R.id.txtRestaurantAddress);
        txtViewAddress.setText(
                getString(R.string.restaurant_address,restaurant.getAddress())
        );
        TextView txtViewPosition = findViewById(R.id.txtRestaurantPosition);
        String strPosition = "" + restaurant.getLatitude() + "," + restaurant.getLongitude();
        txtViewPosition.setText(
                getString(R.string.restaurant_position,strPosition)
        );
    }

    //https://stackoverflow.com/questions/22062681/private-class-array-adapter-warning
    private class CustomAdapter extends ArrayAdapter<Inspection> {
        public CustomAdapter() {
            super(RestaurantActivity.this, R.layout.inspection_item, inspections);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View sampleView = convertView;
            if (sampleView == null) {
                sampleView = getLayoutInflater().inflate(R.layout.inspection_item, parent, false);
            }
            Inspection currentInspection = inspections.get(position);

            // Set icon and background color for each sampleView
            String hazardRating = currentInspection.getHazardRating();
            ImageView imageview = (ImageView) sampleView.findViewById(R.id.imgViewViolationIcon);
            setResViolIconMessage(hazardRating, imageview, sampleView);

            // Set inspection date
            TextView inspectionDateTxt = sampleView.findViewById(R.id.txtInspectionDate);
            // Set non-critical violations text
            TextView nonCriticalViolationsTxt = sampleView.findViewById(R.id.txtNonCriticalViolations);
            // Set critical violations text
            TextView criticalViolationsTxt = sampleView.findViewById(R.id.txtCriticalViolations);
            setSingleRestaurantMes(inspectionDateTxt, nonCriticalViolationsTxt, criticalViolationsTxt, currentInspection );
            return sampleView;
        }

    }
    void setResViolIconMessage(String hazardRating,  ImageView imageview, View sampleView){

        if (hazardRating.equals("Low")) {
            imageview.setImageResource(R.drawable.green);
            sampleView.setBackgroundColor(0x68FF33);
        } else if (hazardRating.equals("Moderate")){
            imageview.setImageResource(R.drawable.yellow);
            sampleView.setBackgroundColor(0xFFEC33);
        } else if (hazardRating.equals("High")){
            imageview.setImageResource(R.drawable.red);
            sampleView.setBackgroundColor(0xFF3333);
        }

    }

    void setSingleRestaurantMes(TextView inspectionDateTxt, TextView nonCriticalViolationsTxt, TextView criticalViolationsTxt, Inspection currentInspection ){

        inspectionDateTxt.setText(
                getString(R.string.inspection_item_date, currentInspection.inspectionDate())
        );
        criticalViolationsTxt.setText(
                getString(R.string.inspection_item_critical_violations, "" + currentInspection.getNumCritical())
        );
        nonCriticalViolationsTxt.setText(
                getString(R.string.inspection_item_non_critical_violations, "" + currentInspection.getNumNonCritical())
        );
    }

    // Source
    // https://stackoverflow.com/questions/36457564/display-back-button-of-action-bar-is-not-going-back-in-android/36457747
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    

    private void populateInspectionList(Restaurant restaurant) {
        manager = RestaurantManager.getManager();
        ArrayAdapter<Inspection> adapter = new CustomAdapter();
        ListView list =  findViewById(R.id.txtInspectionList);
        list.setAdapter(adapter);
    }

    private void clickThePosition(final Restaurant restaurant) {
        TextView txtViewCoords = findViewById(R.id.txtRestaurantPosition);
        String coordsString = "" + restaurant.getLatitude() + "," + restaurant.getLongitude();
        txtViewCoords.setText(
                getString(R.string.restaurant_activity_position,coordsString)
        );

        txtViewCoords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get intent from MapsActivity
                Intent intent = MapsActivity.makeIntent(RestaurantActivity.this);

                // Launch maps activity to open restaurant marker with location data and name
                intent.putExtra(lat, restaurant.getLatitude());
                intent.putExtra(lon, restaurant.getLongitude());
                intent.putExtra(NAME, restaurant.getName());
                startActivity(intent);
            }
        });
    }

    private void registerClickCallback(final Restaurant restaurant) {
        ListView list = findViewById(R.id.txtInspectionList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Inspection selectedInspection = restaurant.getInspectionList().get(position);
                Intent intent = InspectionActivity.makeIntent(RestaurantActivity.this);
                intent.putExtra("inspection", selectedInspection);
                startActivity(intent);
            }
        });
    }

    private void setupDefaultIntent() {
        Intent i = new Intent();
        i.putExtra("result", 0);
        setResult(Activity.RESULT_OK, i);
    }

    public static Intent makeLaunchIntent(Context c) {
        return new Intent(c, RestaurantActivity.class);
    }


}
