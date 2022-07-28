package com.example.project276.Model;

import android.util.Log;

import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

//Stores info for an inspection
public class Inspection implements Serializable {
    private String trackingNumber;
    private String inspectionDate;
    private String inspectionType;    // Follow-up or routine
    private int numCritical;
    private int numNonCritical;
    private String hazardRating;
    List<ViolationDetail> violationList = new ArrayList<>();

//Inspection constructor
    public Inspection(){}

//getters
    public String getInspectionType() { return inspectionType; }
    public String getInspectionDate() { return inspectionDate; }
    public int getNumCritical() { return numCritical; }
    public int getNumNonCritical() { return numNonCritical; }
    public String getHazardRating() { return hazardRating; }
    public List<ViolationDetail> getViolationList() { return violationList; }

//setters
    public void setInspectionType(String inspType) { this.inspectionType = inspType; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public void setInspectionDate(String inspectionDate) { this.inspectionDate = inspectionDate; }
    public void setNumCritical(int numCritical) { this.numCritical = numCritical; }
    public void setNumNonCritical(int numNonCritical) { this.numNonCritical = numNonCritical; }
    public void setHazardRating(String hazardRating) { this.hazardRating = hazardRating; }

    //https://www.baeldung.com/java-date-difference
    //https://stackoverflow.com/questions/42553017/android-calculate-days-between-two-dates
    //https://www.youtube.com/watch?v=VEFQ2DC_6LI
    public String fullFormattedDate() {
        // Return full date in format Month Day, Year
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        String rawInspectionDate = getInspectionDate();

        String fullFormattedDate = "";

        try {
            Date outPutDate = sdf.parse(rawInspectionDate);
            String[] indexToMonth = new DateFormatSymbols().getMonths();
            Calendar calendar = Calendar.getInstance();
            assert outPutDate != null;
            calendar.setTime(outPutDate);
            fullFormattedDate = getDateFormat(outPutDate,indexToMonth,calendar);
        } catch (Exception e) {
            Log.e("Inspection.java", " Error date");
            e.printStackTrace();
        }

        return fullFormattedDate;
    }

    public String getDateFormat(Date outPutDate,String[] month,Calendar calendar){
        String date = " ";
        assert outPutDate != null;
        calendar.setTime(outPutDate);
        date = month[calendar.get(Calendar.MONTH)] + " "
                + calendar.get(Calendar.DAY_OF_MONTH) + ", " + calendar.get(Calendar.YEAR);;
        return date;
    }


    //https://www.baeldung.com/java-date-difference
    //https://stackoverflow.com/questions/42553017/android-calculate-days-between-two-dates
    //https://www.youtube.com/watch?v=VEFQ2DC_6LI
    public String inspectionDate() {
        String newDate = "";
        try {
            // Get the current date
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
            Date currentDate = new Date();

            // Get the difference in days between inspectionDate and currentDate
            String rawInspectionDate = getInspectionDate();
            Date inspectionD = simpleFormat.parse(rawInspectionDate);
            assert inspectionD != null;
            long tempDate = inspectionD.getTime();
            String[] indexToMonth = new DateFormatSymbols().getMonths();
            Calendar inspectionCalendar = Calendar.getInstance();
            inspectionCalendar.setTime(inspectionD);
            newDate = getDifferent( currentDate, tempDate, indexToMonth, inspectionCalendar);

        } catch (Exception e) {
            Log.d("Inspection.java", "Failed From Inspection");
            e.printStackTrace();
        }

        return newDate;
    }

    public String getDifferent(Date date, long currentDate,  String[] indexToMonth, Calendar inspectionCalendar){
        String temp = "";
        long diffInMS = Math.abs(date.getTime() - currentDate);
        long diffInDay = TimeUnit.DAYS.convert(diffInMS, TimeUnit.MILLISECONDS);
        if (diffInDay <= 1) {
            temp = diffInDay + " day";
        } else if (diffInDay <= 30) {
            temp = diffInDay + " days";
        } else if (diffInDay <= 365) {
            temp = indexToMonth[inspectionCalendar.get(Calendar.MONTH)]
                    + " " + inspectionCalendar.get(Calendar.DAY_OF_MONTH);
        } else {
            temp = indexToMonth[inspectionCalendar.get(Calendar.MONTH)]
                    + " " + inspectionCalendar.get(Calendar.YEAR);
        }
        return temp;
    }




}

