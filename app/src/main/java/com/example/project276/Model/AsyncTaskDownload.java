package com.example.project276.Model;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.project276.R;
import com.example.project276.UI.MapsActivity;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class AsyncTaskDownload extends AsyncTask<String, Void, Void> {

    private String restFileName;
    private String inspectFileName;
    private Context context;
    private MapsActivity activity;

    DBAdapter_Restaurant rest_Db;
    DBAdapter_Inspection inspect_Db;
    RestaurantManager manager = RestaurantManager.getManager();

    public AsyncTaskDownload(String restFileName, String inspectFileName, Context context, Activity activity) {
        this.restFileName = restFileName;
        this.inspectFileName = inspectFileName;
        this.context = context;
        this.activity = (MapsActivity) activity;
    }

    @Override
    protected Void doInBackground(String... urls) {
        //open up db to write to
        rest_Db = new DBAdapter_Restaurant(context);
        inspect_Db = new DBAdapter_Inspection(context);
        rest_Db.open();
        inspect_Db.open();

        //delete everything in database
        rest_Db.deleteAll();
        inspect_Db.deleteAll();

        //delete all restaurants in manager
        manager.clearRestaurantList();

        InputStream restInstream = null;
        InputStream inspectInstream = null;
        try {
            restInstream = new URL(urls[0]).openStream();
            inspectInstream = new URL(urls[1]).openStream();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("download","failed to open input stream to urls");
        }
        BufferedReader restReader = new BufferedReader(new InputStreamReader(restInstream, StandardCharsets.UTF_8));

        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(restFileName, MODE_PRIVATE);
            String line = "";
            restReader.readLine();
            //write all restaurants to rest.csv and to Database
            while ((line = restReader.readLine()) != null) {
                //writing to file
                fos.write(line.getBytes());
                String lineSeparator = System.getProperty("line.separator");
                assert lineSeparator != null;
                fos.write(lineSeparator.getBytes());

                //writing to db. Skip the first line that is read since it is the header values (skip when i=0)
                writeRestLineToDb(line);
            }
            restReader.close();
            Log.d("download","download to restaurant file successful");
        } catch(IOException e) {
            e.printStackTrace();
            Log.d("download","failed writing to restaurant file");
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("download", "failed to close file output stream");
                }
            }
        }

        BufferedReader inspectReader = new BufferedReader(new InputStreamReader(inspectInstream, StandardCharsets.UTF_8));
        try {
            fos = context.openFileOutput(inspectFileName, MODE_PRIVATE);
            String line = "";
            inspectReader.readLine();
            //write all restaurants to inspect.csv
            while ((line = inspectReader.readLine()) != null) {
                //writing to file
                fos.write(line.getBytes());
                String lineSeparator = System.getProperty("line.separator");
                assert lineSeparator != null;
                fos.write(lineSeparator.getBytes());

                //writing to db
                writeInspectLineToDb(line);
            }
            inspectReader.close();
            Log.d("download","download to inspection file successful");
        } catch(IOException e) {
            e.printStackTrace();
            Log.d("download","failed writing to inspection file");
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("download", "failed to close file output stream");
                }
            }
        }
        return null;
    }
//Write restaurant info to Database
    private void writeRestLineToDb(String line) {
        line = line.replace("\"", "");
        String[] space=line.split(",");
        String trackNum = space[0];
        String name = space[1];
        String address = space[2];
        String city = space[3];
        String factype = space[4];
        //validate latitude and longitude are type double
        double latitude = 0;
        try {
            latitude = Double.parseDouble(space[5]);
        } catch (NumberFormatException e){
            e.printStackTrace();
        }
        double longitude = 0;
        try {
            longitude = Double.parseDouble(space[6]);
        } catch (NumberFormatException e){
            e.printStackTrace();
        }
        long newId = rest_Db.insertRow(trackNum,name,address,city,factype,latitude,longitude);

        //create restaurant object and add to manager
        Restaurant restaurant = new Restaurant();

        restaurant.setTrackingNumber(space[0]);
        restaurant.setName(space[1]);
        restaurant.setAddress(space[2]);
        restaurant.setCity(space[3]);
        restaurant.setType(space[4]);
        restaurant.setLatitude(latitude);
        restaurant.setLongitude(longitude);
        restaurant.setImgId();

        RestaurantManager.add(restaurant);
    }
    
//Write inspection info to Database
    private void writeInspectLineToDb(String line) {
        line = line.replace("\"", "");
        String[] readByLine = line.split(",", 7);
        if(readByLine[0].equals("")) {
            return;
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
        String trackNum = readByLine[0];
        String date = readByLine[1];
        String type = readByLine[2];
        int numCritical = 0;
        if(!readByLine[3].equals("")) {
            try {
                numCritical = Integer.parseInt(readByLine[3]);
            } catch(Exception e) {
                Log.d("download","numCritical exception "+e);
            }
        }
        int numNonCritical = 0;
        if(!readByLine[4].equals("")) {
            try {
                numNonCritical = Integer.parseInt(readByLine[3]);
            } catch(Exception e) {
                Log.d("download","numNonCritical exception "+e);
            }
        }
        String violations = readByLine[5];
        String hazardRating = readByLine[6];

        long newId = inspect_Db.insertRow(trackNum,date,type,numCritical,numNonCritical,violations,hazardRating);

        //construct and add inspection objects to the correct restaurant
        Inspection inspection = new Inspection();
        inspection.setTrackingNumber(trackNum);
        inspection.setInspectionDate(date);
        inspection.setInspectionType(type);
        inspection.setNumCritical(numCritical);
        inspection.setNumNonCritical(numNonCritical);
        inspection.setHazardRating(hazardRating);

        //Violations
        InputStream inputStream1 = context.getResources().openRawResource(R.raw.violationdetails);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream1, StandardCharsets.UTF_8));
        String lineViol = "";
        List<Integer> violationNumbers = new ArrayList<>();
        List<String> violationDescriptions = new ArrayList<>();
        try {
            while ( (lineViol = bufferedReader.readLine()) != null) {
                String[] lineSplit = lineViol.split(",");
                violationNumbers.add(Integer.parseInt(lineSplit[0]));
                violationDescriptions.add(lineSplit[1]);
            }
        } catch (IOException e) {
            Log.d("download", "ERROR FROM --> writeInspectLinetoDb() ---> violation construction failed " + line, e);
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
        //find restaurant and add
        for (int i = 0; i < RestaurantManager.getSize(); i++) {
            if (trackNum.equals(RestaurantManager.getIndex(i).getTrackingNumber()))
                RestaurantManager.getIndex(i).getInspectionList().add(inspection);
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
        rest_Db.close();
        inspect_Db.close();

        manager.sortByName();
        manager.sortInspectionsByDate();

        //after download complete, remove the loading wheel
        ProgressBar bar = activity.findViewById(R.id.loadingMessage);
        bar.setVisibility(View.INVISIBLE);
        Toast.makeText(context, "Download Completed", Toast.LENGTH_LONG).show();
    }
}
