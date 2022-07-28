package com.example.project276.Model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.example.project276.R;
import com.example.project276.UI.MainActivity;
import com.example.project276.UI.MapsActivity;
import com.example.project276.UI.SearchResultsActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class AsyncTaskSearch extends AsyncTask<String, Void, Void> {

    private String queryString;
    private Context context;
    private SearchResultsActivity activity;

    DBAdapter_Restaurant dbRest;
    DBAdapter_Inspection dbInspect;
    RestaurantManager manager = RestaurantManager.getManager();

    public AsyncTaskSearch(String queryString, Context context, Activity activity) {
        this.queryString = queryString;
        this.context = context;
        this.activity = (SearchResultsActivity) activity;
    }

    @Override
    protected Void doInBackground(String... queries) {
        dbRest = new DBAdapter_Restaurant(context);
        dbInspect = new DBAdapter_Inspection(context);
        dbRest.open();
        dbInspect.open();

        Cursor cursorRest = dbRest.getRestaurantMatch(DBAdapter_Restaurant.KEY_RESTAURANT_NAME, queryString);
        //process Cursor and display results
        if (cursorRest != null) {
            //clear all restaurants from manager
            manager.clearRestaurantList();

            do {
                //Process the data
                String trackingNumber = cursorRest.getString(DBAdapter_Restaurant.COL_TRACKING_NUMBER);
                String name = cursorRest.getString(DBAdapter_Restaurant.COL_NAME);
                String address = cursorRest.getString(DBAdapter_Restaurant.COL_ADDRESS);
                String city = cursorRest.getString(DBAdapter_Restaurant.COL_CITY);
                String factype = cursorRest.getString(DBAdapter_Restaurant.COL_FACTYPE);
                double latitude = cursorRest.getDouble(DBAdapter_Restaurant.COL_LATITUDE);
                double longitude = cursorRest.getDouble(DBAdapter_Restaurant.COL_LONGITUDE);

                Restaurant restaurant = new Restaurant();
                String line = "" + trackingNumber + "," + name + "," + address + "," + city + "," + factype + "," + latitude + "," + longitude;
                //method will parse the string and add the info to a new restaurant object
                readByLineRestaurantData(line, restaurant);
                //add constructed restaurant to manager
                RestaurantManager.add(restaurant);

                //retrieve all the inspections that match the tracking number of the restaurants that was queried
                Cursor cursorInspect = dbInspect.getInspectionMatch(DBAdapter_Inspection.KEY_TRACKING_NUMBER, restaurant.getTrackingNumber());
                //if inspections were fetched from DB successfully
                if (cursorInspect != null) {
                    do {
                        //Process cursor and add inspections to restaurants
                        String trackingNumberInspect = cursorInspect.getString(DBAdapter_Inspection.COL_TRACKING_NUMBER);
                        String date = cursorInspect.getString(DBAdapter_Inspection.COL_DATE);
                        String type = cursorInspect.getString(DBAdapter_Inspection.COL_TYPE);
                        String numcrit = Integer.toString(cursorInspect.getInt(DBAdapter_Inspection.COL_NUMCRITICAL));
                        String numnoncrit = Integer.toString(cursorInspect.getInt(DBAdapter_Inspection.COL_NUMNONCRITICAL));
                        String violations = cursorInspect.getString(DBAdapter_Inspection.COL_VIOLATIONS);
                        String hazrating = cursorInspect.getString(DBAdapter_Inspection.COL_HAZARDRATING);

                        //inspection
                        Inspection inspection = new Inspection();
                        String[] inspect = {trackingNumberInspect, date, type, numcrit, numnoncrit, violations, hazrating};
                        //method will accept array of correct inspection values and construct inspection object
                        readByLineInspectionLists(inspect, inspection);

                        //Violations
                        InputStream inputStream1 = context.getResources().openRawResource(R.raw.violationdetails);
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream1, StandardCharsets.UTF_8));
                        String lineViol = "";
                        List<Integer> violationNumbers = new ArrayList<>();
                        List<String> violationDescriptions = new ArrayList<>();
                        try {
                            while ((lineViol = bufferedReader.readLine()) != null) {
                                String[] lineSplit = lineViol.split(",");
                                violationNumbers.add(Integer.parseInt(lineSplit[0]));
                                violationDescriptions.add(lineSplit[1]);
                            }
                        } catch (IOException e) {
                            Log.d("download", "ERROR FROM --> SearchResultsActivity() " + lineViol, e);
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
                        //add inspection to the restaurant
                        restaurant.getInspectionList().add(inspection);

                    } while (cursorInspect.moveToNext());
                    cursorInspect.close();
                    
                    //remove restaurants that don't fit user selection
                    sortInspectionsByDate(restaurant);
                    String[] haz = {"","Low","Moderate","High"};
                    String latestInspectionHazardLevel = restaurant.getSingleInspection(0).getHazardRating();
                    if(manager.getHazardLevelSearch()!=0){
                        if(!latestInspectionHazardLevel.equals(haz[manager.getHazardLevelSearch()])){
                            RestaurantManager.remove(RestaurantManager.getSize()-1);
                        }
                    }
                }
            } while (cursorRest.moveToNext());
            cursorRest.close();
            manager.sortByName();
            manager.sortInspectionsByDate();
        }
        return null;
    }

    public void sortInspectionsByDate(Restaurant restaurant) {
        Comparator<Inspection> compareByDate = new Comparator<Inspection>() {
            @Override
            public int compare(Inspection inspection1, Inspection inspection2) {
                return inspection1.getInspectionDate().compareTo(inspection2.getInspectionDate());
            }
        };
        Collections.sort(restaurant.inspectionList, compareByDate.reversed());
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
            Log.d("download", "ERROR FROM --> readByLineRestaurantData() -- Async Search" + line, e);
        }
        restaurantSample.setLatitude(latitude);
        double longitude = 0;
        try {
            longitude = Double.parseDouble(space[6]);
        } catch (NumberFormatException e){
            e.printStackTrace();
            Log.d("download", "ERROR FROM --> readByLineRestaurantData() -- Async Search" + line, e);
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


    @Override
    protected void onPostExecute(Void aVoid) {
        dbRest.close();
        dbInspect.close();

        activity.launchIntent();
    }
}
