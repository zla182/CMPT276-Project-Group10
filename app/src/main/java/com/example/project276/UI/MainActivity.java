package com.example.project276.UI;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project276.Model.AsyncTaskSearch;
import com.example.project276.Model.DBAdapter_Inspection;
import com.example.project276.Model.DBAdapter_Restaurant;
import com.example.project276.Model.Inspection;
import com.example.project276.Model.Restaurant;
import com.example.project276.Model.RestaurantManager;
import com.example.project276.Model.ViolationDetail;
import com.example.project276.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//Displays list of restaurants and some information about the last inspections
public class MainActivity extends AppCompatActivity {
    public static final String restaurantIndex = "restaurantIndex";
    private RestaurantManager manager = RestaurantManager.getManager();
    //stuff for the map, taken from
    //https://www.youtube.com/watch?v=M0bYvXlhgSI&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt&index=3
   public static final String TAG ="MainActivity";
   private static final int ERROR_DIALOG_REQUEST = 9001; // check if they have the googleplay services
    Button button;
    Button clearSearch;
    Button extraOptionsSearch;
    DBAdapter_Restaurant restDb = new DBAdapter_Restaurant(this);
    DBAdapter_Inspection inspectDb = new DBAdapter_Inspection(this);
    public static Intent makeIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Restaurants");
        //manager.setHazardLevelSearch(0);

        //readRestaurantData();
        populateListView();
        registerClickCallback();
        //startActivity(new Intent(this, MapsActivity.class)); //TODO: connect this to a button, or have it autolaunch
       //code from https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial
        if(isServicesOK()){
            init();
        }
    }
    //search bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        //set value in manager so SearchResultsActivity knows which activity launched the menu
        manager.setSearchFromMain(true);
        manager.setSearchFromMaps(false);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(new ComponentName(this,SearchResultsActivity.class)));
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(true);

        return true;
    }

    private void init() { //button for map
        Button buttmap = (Button) findViewById(R.id.MapButton);
        buttmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        button = findViewById(R.id.backbutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });
        clearSearch = findViewById(R.id.ClearSearchList);
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.setIsCurrentlySearched(false);
                finish();
                createRestaurantObjects();
            }
        });
        extraOptionsSearch = findViewById(R.id.ExtraOptionsButton);
        extraOptionsSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = SearchingOptionsActivity.makeIntent(MainActivity.this);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    //https://www.youtube.com/watch?v=M0bYvXlhgSI&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt&index=3
    public boolean isServicesOK(){ //checking for Google play services
        Log.d(TAG, "isServicesOK: checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (available == ConnectionResult.SUCCESS)
        {
            Log.d(TAG, "isServicesOK: google play services is working");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //if the user has an error but they can resolve it
            Log.d(TAG, "isServicesOK: theres an error but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,ERROR_DIALOG_REQUEST);
        dialog.show();
        }
        else {
            Toast.makeText(this, "you can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    // https://www.youtube.com/watch?v=WRANgDgM2Zg
    private void populateListView() {

        List<Restaurant> restaurantList = restaurantList();
        ArrayAdapter<Restaurant> adapter = new ListAdapter(restaurantList);
        ListView list =  findViewById(R.id.restaurantsListView);
        list.setAdapter(adapter);
    }

    public ArrayList<Restaurant> restaurantList() {
        ArrayList<Restaurant> newRestaurantList = new ArrayList<>();
        for (int i = 0; i < manager.getSize(); i++) {
            newRestaurantList.add(RestaurantManager.getIndex(i));
        }
        return newRestaurantList;
    };


    //https://stackoverflow.com/questions/22062681/private-class-array-adapter-warning
    private class ListAdapter extends ArrayAdapter<Restaurant> {
        public ListAdapter(List<Restaurant> restaurantList) {
            super(MainActivity.this, R.layout.restaurant_item, restaurantList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View viewSample = convertView;
            if (viewSample == null) {
                viewSample = getLayoutInflater().inflate(R.layout.restaurant_item, parent, false);
            }
            // Find restaurant
            Restaurant restaurantFound = RestaurantManager.getIndex(position);
            setRestaurantMessage(restaurantFound, viewSample);
            TextView inspectionDateLast = viewSample.findViewById(R.id.txtLastInspectionDate);
            ImageView imgHazardIcon = viewSample.findViewById(R.id.txtItemImageHazardRating);
            if (restaurantFound.getInspectionList().size() != 0) {
                Inspection lastInspection = restaurantFound.getInspectionList().get(0);
                TextView violationsNumLastInspection = viewSample.findViewById(R.id.txtItemNumViolations);
                inspectionDateLast.setText(
                        getString(R.string.main_activity_restaurant_last_inspection_date,
                                lastInspection.inspectionDate())
                );
                setInspectionHazardIcon(lastInspection,imgHazardIcon, viewSample);
                int numCrit = lastInspection.getNumCritical();
                int numNonCrit = lastInspection.getNumNonCritical();
                String totalViolations = "" + (lastInspection.getNumCritical() + lastInspection.getNumNonCritical());
                violationsNumLastInspection.setText(
                        getString(R.string.main_activity_restaurant_violations, totalViolations)
                );
            } else {
                // Exception
                setInspectionIconException( inspectionDateLast, imgHazardIcon, viewSample);
            }
            return viewSample;
        }
    }

    public void setRestaurantMessage(Restaurant restaurantFound, View viewSample){

        // RestaurantIcon
        ImageView imgRestaurant = viewSample.findViewById(R.id.txtRestaurantIcon);
        imgRestaurant.setImageResource(restaurantFound.getIconID());
        // Set restaurant name
        TextView restaurantName = viewSample.findViewById(R.id.txtRestaurantName);
        restaurantName.setText(getString(R.string.main_activity_restaurant_name,restaurantFound.getName()));

    }

     public void setInspectionHazardIcon(Inspection lastInspection,ImageView imgHazardIcon, View viewSample){
         switch (lastInspection.getHazardRating()) {
             case "Low":
                 imgHazardIcon.setImageResource(R.drawable.green);
                 viewSample.setBackgroundColor(0x68FF33);// green
                 break;
             case "Moderate":
                 imgHazardIcon.setImageResource(R.drawable.yellow);
                 viewSample.setBackgroundColor(0xFFEC33);//yellow
                 break;
             case "High":
                 imgHazardIcon.setImageResource(R.drawable.red);
                 viewSample.setBackgroundColor(0xFF3333);//red
                 break;
         }
     }

     public void setInspectionIconException( TextView inspectionDateLast,ImageView imgHazardIcon,  View viewSample){
         TextView violationsNumLastInspection = viewSample.findViewById(R.id.txtItemNumViolations);
         imgHazardIcon.setImageResource(R.drawable.exception);
         violationsNumLastInspection.setText(getString(R.string.main_activity_restaurant_violations_no_inspection));
         inspectionDateLast.setText(getString(R.string.main_activity_no_inspection));
     }

    private void registerClickCallback() {
        ListView list = findViewById(R.id.restaurantsListView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                Intent intent = RestaurantActivity.makeLaunchIntent(MainActivity.this);
                intent.putExtra(restaurantIndex, position);
                startActivity(intent);
            }
        });
    }


    /////////////////////////////////////////////////////////////////// Create Restaurant/Inspection/Violation objects from the database /////////////////////////////////////////////////
    //create restaurant objects from database
    public void createRestaurantObjects(){
        openDB();
        Cursor cursor = DBAdapter_Restaurant.getAllRows();
        manager.clearRestaurantList();

        if(cursor.moveToFirst()){
            do {
                //Process the data
                int id = cursor.getInt(DBAdapter_Restaurant.COL_ROWID);
                String trackingNumber = cursor.getString(DBAdapter_Restaurant.COL_TRACKING_NUMBER);
                String name = cursor.getString(DBAdapter_Restaurant.COL_NAME);
                String address = cursor.getString(DBAdapter_Restaurant.COL_ADDRESS);
                String city = cursor.getString(DBAdapter_Restaurant.COL_CITY);
                String factype = cursor.getString(DBAdapter_Restaurant.COL_FACTYPE);
                double latitude = cursor.getDouble(DBAdapter_Restaurant.COL_LATITUDE);
                double longitude = cursor.getDouble(DBAdapter_Restaurant.COL_LONGITUDE);

                Restaurant restaurant = new Restaurant();
                String line = ""+trackingNumber+","+name+","+address+","+city+","+factype+","+latitude+","+longitude;
                //method will parse the string and add the info to a new restaurant object
                readByLineRestaurantData(line, restaurant);
                //add constructed restaurant to manager
                RestaurantManager.add(restaurant);
            } while(cursor.moveToNext());
            //after restaurants are added, create the inspections
            createInspectionObjects();
            manager.sortByName();
        }
        cursor.close();
    }
    //create inspection and violation objects from database
    private void createInspectionObjects(){
        Cursor cursor = DBAdapter_Inspection.getAllRows();

        if(cursor.moveToFirst()){
            do {
                //Process the data
                int id = cursor.getInt(DBAdapter_Inspection.COL_ROWID);
                String trackingNumber = cursor.getString(DBAdapter_Inspection.COL_TRACKING_NUMBER);
                String date = cursor.getString(DBAdapter_Inspection.COL_DATE);
                String type = cursor.getString(DBAdapter_Inspection.COL_TYPE);
                String numcrit = Integer.toString(cursor.getInt(DBAdapter_Inspection.COL_NUMCRITICAL));
                String numnoncrit = Integer.toString(cursor.getInt(DBAdapter_Inspection.COL_NUMNONCRITICAL));
                String violations = cursor.getString(DBAdapter_Inspection.COL_VIOLATIONS);
                String hazrating = cursor.getString(DBAdapter_Inspection.COL_HAZARDRATING);

                //inspection
                Inspection inspection = new Inspection();
                String[] inspect = {trackingNumber,date,type, numcrit, numnoncrit, violations, hazrating};
                //method will accept array of correct inspection values and construct inspection object
                readByLineInspectionLists(inspect, inspection);
                //Violations
                InputStream inputStream1 = getResources().openRawResource(R.raw.violationdetails);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream1, StandardCharsets.UTF_8));
                String line = "";
                List<Integer> violationNumbers = new ArrayList<>();
                List<String> violationDescriptions = new ArrayList<>();
                try {
                    while ( (line = bufferedReader.readLine()) != null) {
                        String[] lineSplit = line.split(",");
                        violationNumbers.add(Integer.parseInt(lineSplit[0]));
                        violationDescriptions.add(lineSplit[1]);
                    }
                } catch (IOException e) {
                    Log.d("download", "ERROR FROM --> createInspectionObject() " + line, e);
                    e.printStackTrace();
                }
                String[] violationsList = violations.split("\\|");
                //if the violation number is valid
                if (!violationsList[0].equals("")) {
                    for (String violation : violationsList) {
                        String[] violSplit = violation.split(",");
                        int violationNumber = Integer.parseInt(violSplit[0]);
                        //create violation object and add to the inspection list of the inspection object
                        setViolationDetail(violSplit, false, false, violationNumber, violationNumbers, violationDescriptions, inspection);
                    }
                }
                //add to the correct restaurant
                for (int i = 0; i < RestaurantManager.getSize(); i++) {
                    if (trackingNumber.equals(RestaurantManager.getIndex(i).getTrackingNumber()))
                        RestaurantManager.getIndex(i).getInspectionList().add(inspection);
                }
            } while(cursor.moveToNext());
        }
        cursor.close();
        closeDB();
        manager.sortInspectionsByDate();

        Intent intent = MainActivity.makeIntent(MainActivity.this);
        startActivity(intent);
    }
    ////////////////////////////////////////////////////////////////////// Constructor Methods //////////////////////////////////////////////////////////////////////////////////////
    //takes a line and empty restaurant object then constructs a restaurant object
    public void readByLineRestaurantData(String line, Restaurant restaurantSample){
        line = line.replace("\"", "");
        String[] space=line.split(",");

        restaurantSample.setTrackingNumber(space[0]);
        restaurantSample.setName(space[1]);
        restaurantSample.setAddress(space[2]);
        restaurantSample.setCity(space[3]);
        restaurantSample.setType(space[4]);
        //validate latitude and longitude are type double
        double latitude = 0;
        try {
            latitude = Double.parseDouble(space[5]);
        } catch (NumberFormatException e){
            e.printStackTrace();
            Log.d("download", "ERROR FROM --> readByLineRestaurantData()" + line, e);
        }
        restaurantSample.setLatitude(latitude);
        double longitude = 0;
        try {
            longitude = Double.parseDouble(space[6]);
        } catch (NumberFormatException e){
            e.printStackTrace();
            Log.d("download", "ERROR FROM --> readByLineRestaurantData()" + line, e);
        }
        restaurantSample.setLongitude(longitude);
        restaurantSample.setImgId();
    }
    //takes an array of values for inspection and constructs inspection
    public void readByLineInspectionLists(String[] readByLine, Inspection inspection){
        inspection.setTrackingNumber(readByLine[0]);
        inspection.setInspectionDate(readByLine[1]);
        inspection.setInspectionType(readByLine[2]);
        inspection.setHazardRating(readByLine[6]);

        if(readByLine[3].equals("")) {
            inspection.setNumCritical(0);
        } else {
            try {
                inspection.setNumCritical(Integer.parseInt(readByLine[3]));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if(readByLine[4].equals("")) {
            inspection.setNumNonCritical(0);
        } else {
            try {
                inspection.setNumNonCritical(Integer.parseInt(readByLine[4]));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }
    // takes array of violation details and construct a violation object then adds it to the given inspection object
    public void setViolationDetail(String[] violSplit,boolean isCritical, boolean isRepeat, int violationNumber,List<Integer> violationNumbers, List<String> violationDescriptions, Inspection inspection){
        if (violSplit[1].equals("Critical")){
            isCritical = true;
        }
        if (violSplit[3].equals("Repeat")){
            isRepeat = true;
        }
        int descriptionIndex = violationNumbers.indexOf(violationNumber);
        String briefDesc = violationDescriptions.get(descriptionIndex);

        ViolationDetail violationDetailObject = new ViolationDetail(violationNumber, isCritical, violSplit[2], briefDesc, isRepeat);
        inspection.getViolationList().add(violationDetailObject);
    }

    /////////////////////////////////////////////////////////////////////////////All database related code //////////////////////////////////////////////////////////////////////
    private void openDB(){
        restDb = new DBAdapter_Restaurant(this);
        inspectDb = new DBAdapter_Inspection(this);
        restDb.open();
        inspectDb.open();
    }
    private void closeDB(){
        restDb.close();
        inspectDb.close();
    }
}