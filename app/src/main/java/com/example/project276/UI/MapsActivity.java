package com.example.project276.UI;

import android.annotation.SuppressLint;
import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorWindow;
import android.location.Location;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.project276.Model.AsyncTaskDownload;
import com.example.project276.Model.AsyncTaskSearch;
import com.example.project276.Model.DBAdapter_Inspection;
import com.example.project276.Model.DBAdapter_Restaurant;
import com.example.project276.Model.Inspection;
import com.example.project276.Model.ClusterItem;
import com.example.project276.Model.Restaurant;
import com.example.project276.Model.RestaurantManager;
import com.example.project276.Model.ViolationDetail;
import com.example.project276.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.fragment.app.FragmentManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.ClusterRenderer;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class MapsActivity extends AppCompatActivity
            implements
            OnMapReadyCallback,
            GoogleMap.OnCameraMoveStartedListener {

        private GoogleMap mMap;

        private Boolean mLocationPermissionsGranted = false;
        private FusedLocationProviderClient fusedLocationProviderClient;
        private ClusterManager<ClusterItem> clusterManager;
        private Marker singleMarker;
        private Boolean followUser = false;
        private static int updateLocation = 0;
        private static final String TAG = "MapActivity";
        private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
        private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
        private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
        public static final int ZOOM_STREETS = 15;
        public static final int ZOOM_CITY = 10;
        private RestaurantManager manager = RestaurantManager.getManager();
        DBAdapter_Restaurant restDb;
        DBAdapter_Inspection inspectDb;
        //extract from API
        Button button;
        Button clearListButton;
        Button backButton;
        String restaurantDataDate;
        JSONObject updateDateJSON;

        //shared preference variables
        private static final String SHAREDPREF_SET = "Latest Updates Set";
        private static final String SHAREDPREF_ITEM_LATEST = "Latest Update Date";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_maps);
            Objects.requireNonNull(getSupportActionBar()).setTitle("Map");

            //fixes error for cursor window being too big
            try {
                Field field = CursorWindow.class.getDeclaredField("sCursorWindowSize");
                field.setAccessible(true);
                field.set(null, 100*1024*1024);
            } catch (Exception e){
                e.printStackTrace();
            }
            //Runs tasks depending on the condition of latest updates
            try {
                initializeCorrectStateOfApp();
            } catch (IOException e) {
                e.printStackTrace();
            }

            getLocationPermissionFromUser();

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            assert mapFragment != null;
            mapFragment.getMapAsync(this);
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());

            //setup button for launching list
            wireLaunchListButton();
        }
        @Override
        public void onBackPressed(){
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
        }
        //search bar
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.options_menu, menu);

            //set value in manager so SearchResultsActivity knows which activity launched the menu
            manager.setSearchFromMain(false);
            manager.setSearchFromMaps(true);

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
    ////////////////////////////////////////////////////////////////////////////// Initial Condition Checking ////////////////////////////////////////////////////////////////////
        private void initializeCorrectStateOfApp() throws IOException {
            //if first time launching the app set the default restaurants
            if(getLastUpdatedTimeFromSharedPreferences().equals("2020-05-05T20:00:00")) {
                InputStream instreamOriginalRestaurants = getResources().openRawResource(R.raw.restaurants);
                readRestaurantData(instreamOriginalRestaurants);

                //initialize inspection list for restaurants
                InputStream instreamOriginalInspections = getResources().openRawResource(R.raw.inspections);
                InitInspectionLists(instreamOriginalInspections);

            }
            //skip this step if coming from a search
            if(!manager.getIsCurrentlySearched()) {
                //check to see if last update was more than 20 hours ago. If it was then call checkUpdate(). If it has not been 20 hours then just display current restaurants
                try {
                    if (moreThanTwentyHours(getLastUpdatedTimeFromSharedPreferences())) {
                        //check is there is new update and ask user if they want to update
                        checkUpdateDate();
                    } else {
                        //create from saved info in database
                        createRestaurantObjects();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.d("Error 20 Hours", "Error Checking if latest update was 20 hours ago" + e.toString());
                }
            }
        }
///////////////////////////////////////// Getters and setters for shared preferences ///////////////////////////////////////////////////////////////////////////////////////////////////////
        //get the most recent update time from shared preferences
        private String getLastUpdatedTimeFromSharedPreferences(){
            SharedPreferences prefs = getSharedPreferences(SHAREDPREF_SET, MODE_PRIVATE);
            return prefs.getString(SHAREDPREF_ITEM_LATEST, "2020-05-05T20:00:00");
        }
        //save the most recent time to shared preferences
        private void saveNewUpdateTimeTOSharedPreferences(String date){
            SharedPreferences prefs = getSharedPreferences(SHAREDPREF_SET, MODE_PRIVATE);
            @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = prefs.edit();
            editor.putString(SHAREDPREF_ITEM_LATEST, date);
            editor.apply();
        }
///////////////////////////// Time comparison functions checkUpdate() moreThanTwentyHours() newerThanLastUpdate() ///////////////////////////////////////////////////////////
        //Retrieve last_modified date from API and compare it to the last update date of shared preferences
        private void checkUpdateDate() {
            //Toast.makeText(MapsActivity.this, "checkUpdateDateIsRunnig" +"ng", Toast.LENGTH_SHORT).show();
            String urlRestaurants = "https://data.surrey.ca/api/3/action/package_show?id=restaurants";
            RequestQueue queue = Volley.newRequestQueue(MapsActivity.this);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, urlRestaurants, new JSONObject(), new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            //Handle Success
                            updateDateJSON = response;
                            try {
                                restaurantDataDate = updateDateJSON.getJSONObject("result")
                                        .getJSONArray("resources")
                                        .getJSONObject(0)
                                        .getString("last_modified");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //Once date is fetched, call moreThanTwentyHours() to check if it's been more than twenty hours
                            //This call will launch the DownloadFragment popup if it has been more than 20 hours
                            try {
                                //Launch the download fragment if it has been > 20 hours
                                if(newerThanLastUpdate(restaurantDataDate)){
                                    showDownloadOption();
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                                Log.d("errorMoreThanTwentyHours", e.toString());
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //Handle error
                            Log.d("error", ""+error.toString());
                        }
                    });
            queue.add(jsonObjectRequest);
        }

        //Checks if a time is more than 20 hours old from current time;
        private boolean moreThanTwentyHours(String lastDate) throws ParseException {
            //Check if there has been a new update within 20 hours
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            //20 hours in milliseconds
            final long MILLIS_IN_TWENTY_HOURS = 20 * 60 * 60 * 1000L;

            //convert the fetched date to milliseconds
            //String testWithinTwentyHours = "2020-11-17T08:00:00";
            long lastUpdatedMilliseconds = Objects.requireNonNull(sdf.parse(lastDate)).getTime();

            //current time in milliseconds
            long currentTimeMilliseconds = System.currentTimeMillis();

            //boolean value to check if its been more than 20 hours. Then return it
            return Math.abs(lastUpdatedMilliseconds - currentTimeMilliseconds) > MILLIS_IN_TWENTY_HOURS;
        }

        //Checks if the API last_modified time is newer than the sharedPreferences time
        private boolean newerThanLastUpdate(String timeToCheck) throws ParseException {
            @SuppressLint("SimpleDateFormat") Date lastUpdate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(getLastUpdatedTimeFromSharedPreferences());
            @SuppressLint("SimpleDateFormat") Date timeFromApi = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(timeToCheck);

            assert lastUpdate != null;
            return lastUpdate.before(timeFromApi);
        }
////////////////////////////////////////////////////////////////Launches the fragment for download options ////////////////////////////////////////////////////
        //Initiates the fragment to show user if they want to update or not
        private void showDownloadOption() {
            FragmentManager frag = getSupportFragmentManager();
            DownloadFragment dialog = new DownloadFragment();
            dialog.show(frag, "MessageDialog");
        }
///////////////////////////////////////////////////Set up Button to launch MainActivity ///////////////////////////////////////////////////////////////////////////
     //setup the button to launch MainActivity
        private void wireLaunchListButton() {
            button = findViewById(R.id.launchListViewButton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = MainActivity.makeIntent(MapsActivity.this);
                    finish();
                    startActivity(intent);
                }
            });
            backButton = findViewById(R.id.MapScreenBackButton);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MapsActivity.this, MainActivity.class));
                }
            });
            clearListButton = findViewById(R.id.ClearListButtonMaps);
            clearListButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    manager.setIsCurrentlySearched(false);
                    Intent intent = MapsActivity.makeIntent(MapsActivity.this);
                    finish();
                    manager.clearRestaurantList();
                    startActivity(intent);
                }
            });
        }
////////////////////////////////////////////////////// Initiate the Asynchronous Download ///////////////////////////////////////////////////////////////////////
        public void initiateDownloads() {
            ProgressBar bar = findViewById(R.id.loadingMessage);
            bar.setVisibility(View.VISIBLE);

            AsyncTaskDownload downloadTask = new AsyncTaskDownload("rest.csv","inspect.csv", MapsActivity.this,this);
            String restaurantURL = "https://data.surrey.ca/dataset/3c8cb648-0e80-4659-9078-ef4917b90ffb/resource/0e5d04a2-be9b-40fe-8de2-e88362ea916b/download/restaurants.csv";
            String inspectionsURL = "https://data.surrey.ca/dataset/948e994d-74f5-41a2-b3cb-33fa6a98aa96/resource/30b38b66-649f-4507-a632-d5f6f5fe87f1/download/fraser_health_restaurant_inspection_reports.csv";
            downloadTask.execute(restaurantURL,inspectionsURL);

            //save new update date to shared preference
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = new Date();
            String dateToSave = sdf.format(date);
            saveNewUpdateTimeTOSharedPreferences(dateToSave);
        }
/////////////////////////////////////////////////////Create Restaurant/Inspection/Violation objects from a csv file///////////////////////////////////////////////////////////
        //Construct restaurants for initial 8 restaurants
        public void readRestaurantsFile() {
            try {
                File path = getFileStreamPath("rest.csv");
                InputStream inputStreamReader = new FileInputStream(path);
                //use this method which accepts an InputStream of data to read from
                readRestaurantData(inputStreamReader);
                //Sort in Alphabetical Order
                manager.sortByName();
                readInspectionsFile();
            } catch (IOException e){
                e.printStackTrace();
                Log.d("download", "ERROR FROM --> readRestaurantsFile() method: "+ e.toString());
                Toast.makeText(MapsActivity.this,"Download Failed",Toast.LENGTH_SHORT).show();
            }
        }
        //Construct inspections for initial 8 restaurants
        private void readInspectionsFile() {
            try {
                File pathFileInsp = getFileStreamPath("inspect.csv");
                InputStream inputStreamInsp = new FileInputStream(pathFileInsp);
                InitInspectionLists(inputStreamInsp);
                Toast.makeText(MapsActivity.this,"Download Complete",Toast.LENGTH_SHORT).show();
            } catch (IOException e){
                e.printStackTrace();
                Log.d("download", "ERROR FROM --> readInspectionsFile(): "+ e.toString());
                Toast.makeText(MapsActivity.this,"Download Failed",Toast.LENGTH_SHORT).show();
            }
        }
      //Takes an InputStream as an argument to read from then saves the restaurants to manager
        private void readRestaurantData(InputStream inputStream) throws IOException {
            //Clear the restaurant list initially
            manager.clearRestaurantList();
            //Start reading from InputStream
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line = "";
            try {
                while ((line = reader.readLine()) != null) {
                    Restaurant restaurantSample = new Restaurant();
                    readByLineRestaurantData(line, restaurantSample);
                    // Adding the Restaurants to the manager
                    manager.add(restaurantSample);
                }
            } catch (IOException e) {
                Log.d("download", "ERROR FROM --> readRestaurantData()" + line, e);
                e.printStackTrace();
            }
            //Sort in Alphabetical Order
            manager.sortByName();
        }
    //takes an input stream and read from it and assigns it to restaurants
        private void InitInspectionLists(InputStream inputStream2) {
            InputStream inputStream1 = getResources().openRawResource(R.raw.violationdetails);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream1, StandardCharsets.UTF_8));
            String line = "";

            List<Integer> violationNumbers = new ArrayList<>();
            List<String> violationDescriptions = new ArrayList<>();

            try {
                while ( (line = bufferedReader.readLine()) != null) {
                    lineSplit(line, violationNumbers, violationDescriptions);
                }
            } catch (IOException e) {
                Log.d("download", "ERROR FROM --> InitInspectionLists() " + line, e);
                e.printStackTrace();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream2, StandardCharsets.UTF_8));
            line = "";
            try {
                while ( (line = reader.readLine()) != null) {
                    //Read line by line in CSV
                    line = line.replace("\"", "");
                    String[] readByLine = line.split(",", 7);
                    if(readByLine[0] == "") {
                        break;
                    }
                    if (!readByLine[5].equals("")) {
                        int indexOfLastComma = line.lastIndexOf(",");
                        int indexOfViolation = 0;
                        for (int j = 0; j <= 4; j++) {
                            indexOfViolation += (readByLine[j].length() + 1);
                        }
                        readByLine[5] = line.substring(indexOfViolation, indexOfLastComma);
                        readByLine[6] = line.substring(indexOfLastComma + 1);
                    }
                    Restaurant findRest = new Restaurant();

                    for (int i = 0; i < RestaurantManager.getSize(); i++) {
                        if (readByLine[0].equals(RestaurantManager.getIndex(i).getTrackingNumber()))
                            findRest = RestaurantManager.getIndex(i);
                    }
                    //create inspection object
                    Inspection inspection = new Inspection();
                    readByLineInspectionLists(readByLine, inspection);

                    String[] violationsList = readByLine[5].split("\\|");
                    //if the violation number is valid
                    if (!violationsList[0].equals("")) {
                        for (String violation : violationsList) {
                            String[] violSplit = violation.split(",");
                            int violationNumber = Integer.parseInt(violSplit[0]);
                            //create violation object and add to the inspection list of the inspection object
                            setViolationDetail(violSplit, false, false, violationNumber, violationNumbers, violationDescriptions, inspection);
                        }
                    }
                    //add inspection object to the correct restaurant
                    findRest.getInspectionList().add(inspection);
                    manager.sortInspectionsByDate();
                }
                //Remove loading bar after download complete
                ProgressBar bar = findViewById(R.id.loadingMessage);
                bar.setVisibility(View.INVISIBLE);
            } catch (IOException e){
                Log.d("download", "ERROR FROM --> InitInspectionLists() " + line, e);
                e.printStackTrace();
            }
        }
    //helper function to split violation up and add to violationNumbers list and violationDespriptionList
    public void lineSplit(String line, List<Integer> violationNumbers,List<String> violationDescriptions){
        String[] lineSplit = line.split(",");
        violationNumbers.add(Integer.parseInt(lineSplit[0]));
        violationDescriptions.add(lineSplit[1]);
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
                        lineSplit(line, violationNumbers, violationDescriptions);
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

        ProgressBar bar = findViewById(R.id.loadingMessage);
        bar.setVisibility(View.INVISIBLE);
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
//////////////////////////////////////////////////////////////// All Google Maps Related code ////////////////////////////////////////////////////////////////////////////////


    //https://stackoverflow.com/questions/40142331/how-to-request-location-permission-at-runtime
        private void getLocationPermissionFromUser() {
            Log.d("MapsActivity", "Working till get location permission");
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION};
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionsGranted = true;
                } else {
                    ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
                }
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }

        private void getDeviceLocation() {
            Log.d("MapsActivity", "Getting device location...");
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            try {
                if (mLocationPermissionsGranted) {
                    Task<Location> location = fusedLocationProviderClient.getLastLocation();
                    location.addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Log.d("MapsActivity", "Found ");
                                Location currentLocation = (Location) task.getResult();
                                followUser = true;
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), ZOOM_STREETS);
                            } else {
                                Log.d("MapsActivity", "NOT FOUND");
                                Toast.makeText(MapsActivity.this, "Unable to get", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            } catch (SecurityException e) {
                Log.e(TAG, "getDeviceLocation: " + e.getMessage());
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            Log.d("MapsActivity", "Working onRequestPermissionsResult");
            mLocationPermissionsGranted = false;
            switch (requestCode) {
                case LOCATION_PERMISSION_REQUEST_CODE: {
                    if (grantResults.length > 0) {
                        int i = 0;
                        while (i < grantResults.length) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                mLocationPermissionsGranted = false;
                                i++;
                                return;
                            }
                        }
                        mLocationPermissionsGranted = true;

                        // Restart activity with new permission
                        finish();
                        Intent refreshIntent = makeIntent(this);
                        overridePendingTransition(0, 0);
                        startActivity(refreshIntent);
                        overridePendingTransition(0, 0);
                    }
                }
            }
        }


        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            if (mLocationPermissionsGranted) {
                if (
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED
                                &&
                                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED
                ) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location location) {
                        // Don't constantly print to log
                        if (updateLocation % 5 == 0) {
                            Log.d(TAG, "onMyLocationChange: followUser: " + followUser + ", updateIter: " + updateLocation);
                        }

                        updateLocation++;
                        if (followUser && (updateLocation % 3 == 0)) {
                            if (updateLocation > 8) {
                                Log.d(TAG, "onMyLocationChange: moveCamera()");
                                moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_STREETS);
                            }
                        }
                    }
                });
            }

            // Configure map UI
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            //Set Custom InfoWindow Adapter
            CustomInfoAdapter adapter = new CustomInfoAdapter(MapsActivity.this);
            mMap.setInfoWindowAdapter(adapter);

            mMap.setOnCameraMoveStartedListener(this);

            setUpClusterer();
            clusterManager.cluster();
            registerClickCallback();

            // Move camera to Surrey first
            LatLng surrey = new LatLng(49.104431, -122.801094);
            moveCamera(surrey, ZOOM_CITY);

            // Get chosen restaurant from intent (if it exists)
            double[] chosenRestaurantLatLon = getChosenRestaurantLocation();
            String chosenRestaurantName = getIntent().getStringExtra(RestaurantActivity.NAME);

            Log.d(TAG, "onMapReady: chosenRestaurantName: " + chosenRestaurantName);
            Log.d(TAG, "onMapReady: chosenRestaurantLatLon = ["
                    + chosenRestaurantLatLon[0]
                    + "," + chosenRestaurantLatLon[1] + "]");

            LatLng restaurantCoord = null;
            if (chosenRestaurantLatLon[0] == -1 || chosenRestaurantLatLon[1] == -1) {
                Log.d(TAG, "onMapReady: Setting map to user's location");
                getDeviceLocation();
            } else {
                Log.d(TAG, "onMapReady: Setting map to chosen restaurant coords");
                Log.d(TAG, "onMapReady: chosen restaurant lat: "
                        + chosenRestaurantLatLon[0] + " chosen restaurant lon: "
                        + chosenRestaurantLatLon[1]);

                restaurantCoord = new LatLng(
                        chosenRestaurantLatLon[0],
                        chosenRestaurantLatLon[1]
                );

                // Move camera to chosen restaurant
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurantCoord, ZOOM_STREETS));

                Restaurant restaurant = manager.findRestaurantByLocation(chosenRestaurantLatLon[0],
                        chosenRestaurantLatLon[1], chosenRestaurantName);

                // Confirm we have the correct position
                if (restaurant.getLongitude() == chosenRestaurantLatLon[1] &&
                        restaurant.getLatitude() == chosenRestaurantLatLon[0]) {
                    // Add a marker and display it's window, delete when the user pans
                    singleMarker = mMap.addMarker(new MarkerOptions()
                            .position(restaurantCoord)
                            .icon(getHazardIcon(restaurant))
                            .title(chosenRestaurantName));
                    singleMarker.showInfoWindow();
                }
            }
        }

        private void setUpClusterer() {
            // Initialize new clusterManager
            clusterManager = new ClusterManager<ClusterItem>(this, mMap);

            mMap.setOnCameraIdleListener(clusterManager);
            mMap.setOnMarkerClickListener(clusterManager);
            clusterManager.setRenderer((ClusterRenderer<ClusterItem>) new MarkerClusterRenderer(getApplicationContext(), mMap, clusterManager));
            populateMapWithMarkers();
        }

        private void populateMapWithMarkers() {
            // Get Singleton RestaurantManager
            RestaurantManager manager = RestaurantManager.getManager();
            Log.d(TAG, "populateMapWithMarkers: Populating map with markers");

            for (Restaurant restaurant : manager) {
                ClusterItem clusterItem = new ClusterItem(
                        restaurant.getLatitude(),
                        restaurant.getLongitude(),
                        restaurant.getName(),
                        getHazardIcon(restaurant)
                );
                clusterManager.addItem(clusterItem);
            }
        }

        private BitmapDescriptor getHazardIcon(Restaurant restaurant) {
            Inspection RecentInspection = restaurant.getSingleInspection(0);
            BitmapDescriptor hazardIcon;
            if (RecentInspection != null) {
                String hazardLevel = RecentInspection.getHazardRating();
                if (hazardLevel.equals("Low")) {
                    hazardIcon = bitmapDescriptorFromVector(this, R.drawable.peg_green);
                } else if (hazardLevel.equals("Moderate")) {
                    hazardIcon = bitmapDescriptorFromVector(this, R.drawable.peg_yellow);
                } else {
                    hazardIcon = bitmapDescriptorFromVector(this, R.drawable.peg_red);
                }
            } else {
                hazardIcon = bitmapDescriptorFromVector(this, R.drawable.peg_no_inspection);
            }
            return hazardIcon;
        }

        private double[] getChosenRestaurantLocation() {
            // Return as [lat,long]
            double restaurantLatitude = getIntent()
                    .getDoubleExtra(RestaurantActivity.lat, -1);
            double restaurantLongitude = getIntent()
                    .getDoubleExtra(RestaurantActivity.lon, -1);
            return new double[]{restaurantLatitude, restaurantLongitude};
        }

        private void registerClickCallback() {
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    // Find the restaurant we need to open for RestaurantActivity
                    LatLng latLngF = marker.getPosition();
                    double lat = latLngF.latitude;
                    double lng = latLngF.longitude;
                    String restaurantName = marker.getTitle();
                    Restaurant restaurant = manager.findRestaurantByLocation(lat, lng, restaurantName);
                    int restaurantIndex = manager.findIndex(restaurant);

                    // Launch RestaurantActivity with the correct index
                    Intent intent = RestaurantActivity.makeLaunchIntent(MapsActivity.this);
                    intent.putExtra(MainActivity.restaurantIndex, restaurantIndex);
                    startActivity(intent);
                }
            });

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    moveCamera(marker.getPosition());
                    marker.showInfoWindow();
                    Log.d(TAG, "onMarkerClick: Marker clicked, setting followUser to false");
                    followUser = false;
                    return true;
                }
            });

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    followUser = false;
                }
            });

            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    followUser = true;
                    return false;
                }
            });

            clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<ClusterItem>() {
                @Override
                public boolean onClusterClick(Cluster<ClusterItem> cluster) {
                    moveCamera(cluster.getPosition(), ZOOM_STREETS);
                    followUser = false;
                    return true;
                }
            });
        }

        private void moveCamera(LatLng latLng, float zoom) {
            Log.d(TAG, "moveCamera (zoom): moving: " + latLng + ", zoom: " + zoom);
            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            mMap.animateCamera(location);
        }

        private void moveCamera(LatLng latLng) {
            Log.d(TAG, "moveCamera : moving: " + latLng);
            CameraUpdate location = CameraUpdateFactory.newLatLng(latLng);
            mMap.animateCamera(location);
        }

        public static Intent makeIntent(Context context) {
            return new Intent(context, MapsActivity.class);
        }

        @Override
        public void onCameraMoveStarted(int reason) {
            switch (reason) {
                case GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE:
                    followUser = false;
                    if (singleMarker != null) {
                        singleMarker.remove();
                        singleMarker = null;
                    }
            }
        }

        private class CustomInfoAdapter implements GoogleMap.InfoWindowAdapter {

            private Activity context;

            public CustomInfoAdapter(Activity context) {
                this.context = context;
            }

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View itemView = context.getLayoutInflater().inflate(R.layout.map_infor, null);

                // Find the restaurant to work with.
                LatLng latLngF = marker.getPosition();
                double lat = latLngF.latitude;
                double lng = latLngF.longitude;
                Restaurant restaurant = null;
                restaurant = manager.findRestaurantByLocation(lat, lng, marker.getTitle());
                if (restaurant == null) {
                    Log.d(TAG, "Cluster Click!");
                    return null;
                }

                ImageView logo = itemView.findViewById(R.id.info_item_restaurantLogo);
                logo.setImageResource(restaurant.getIconID());

                TextView restaurantNameText = itemView.findViewById(R.id.info_item_restaurantName);
                restaurantNameText.setText(restaurant.getName());

                TextView addressText = itemView.findViewById(R.id.info_item_address);
                addressText.setText(restaurant.getAddress());

                TextView lastInspectionText = itemView.findViewById(R.id.info_item_lastInspection);
                ImageView hazard = itemView.findViewById(R.id.info_item_hazardImage);

                Inspection recentInspection = null;
                if (restaurant.getInspectionSize() > 0) {
                    recentInspection = restaurant.getInspectionList().get(0);
                    lastInspectionText.setText(
                            getString(
                                    R.string.last_inspection_date,
                                    recentInspection.inspectionDate())
                    );

                    String level = recentInspection.getHazardRating();
                    if (level.equals("Low")) {
                        hazard.setImageResource(R.drawable.green);
                    } else if (level.equals("Moderate")) {
                        hazard.setImageResource(R.drawable.yellow);
                    } else {
                        hazard.setImageResource(R.drawable.red);
                    }
                } else {
                    // Leave text as default
                    hazard.setImageResource(R.drawable.exception);
                }
                return itemView;
            }
        }

        // For peg icon
        // Learned from:https://stackoverflow.com/questions/42365658/custom-marker-in-google-maps-in-android-with-vector-asset-icon
        private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
            Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
            vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
            Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }

        private class MarkerClusterRenderer extends DefaultClusterRenderer<ClusterItem> {

            public MarkerClusterRenderer(Context context, GoogleMap map,
                                         ClusterManager<ClusterItem> clusterManager) {
                super(context, map, MapsActivity.this.clusterManager);
            }

            @Override
            protected void onBeforeClusterItemRendered(ClusterItem item, MarkerOptions markerOptions) {
                markerOptions.icon(item.getHazard());
                markerOptions.title(item.getTitle());
                super.onBeforeClusterItemRendered(item, markerOptions);
            }

            @Override
            protected boolean shouldRenderAsCluster(Cluster<ClusterItem> cluster) {
                return (cluster.getSize() >= 8);
            }
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

