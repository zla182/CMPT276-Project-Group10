package com.example.project276.Model;

import com.example.project276.R;

import java.io.Serializable;

//Stores info of a single violation
public class ViolationDetail implements Serializable {
    private int violNumber;
    private Boolean isCritical;
    private String detailsDescriptions;
    private String violDetails;
    private Boolean isRepeat;
    private int violationImage;

    //constructor
    public ViolationDetail(int violNumber, Boolean isCritical, String violDetails, String detailsDescriptions, Boolean isRepeat) {
        this.violNumber = violNumber;
        this.isCritical = isCritical;
        this.violDetails = violDetails;
        this.detailsDescriptions = detailsDescriptions;
        this.isRepeat = isRepeat;
        setViolationImage(violNumber);
    }

    //getters
    public int getViolationImage() {
            return violationImage;
    }
    public int getViolationNumber() {
        return violNumber;
    }
    public Boolean getIsCritical() {
        return isCritical;
    }
    public String getViolDetails() {
        return violDetails;
    }
    public String getDetailsDescriptions() { return detailsDescriptions; }
    public Boolean getIsRepeat() {
        return isRepeat;
    }

    //set
    public void setViolationImage(int violNumber) {
        if (violNumber == 101) {
            this.violationImage = R.drawable.under_construction;
        }
        else if (violNumber == 102) {
            this.violationImage = R.drawable.restaurant;
        }
        else if (violNumber == 103 || violNumber == 104 || violNumber == 212 || violNumber == 314 || violNumber == 501 || violNumber == 502 ) {
            this.violationImage = R.drawable.permit;
        }
        else if (violNumber == 201 || violNumber == 202 || violNumber == 203 || violNumber == 204 || violNumber == 205 || violNumber == 206 || violNumber == 208 || violNumber == 209 || violNumber == 210 || violNumber == 211) {
            this.violationImage = R.drawable.rotten_food;
        }
        else if (violNumber == 301 || violNumber == 302 || violNumber == 303 || violNumber == 307 || violNumber == 308 || violNumber == 310) {
            this.violationImage = R.drawable.tableware;
        }
        else if (violNumber == 304 || violNumber == 305) {
            this.violationImage = R.drawable.rat_image;
        }
        else if (violNumber == 306 || violNumber == 311) {
            this.violationImage = R.drawable.dirty_kitchen;
        }
        else if (violNumber == 309) {
            this.violationImage = R.drawable.cleaners;
        }
        else if (violNumber == 312) {
            this.violationImage = R.drawable.storage;
        }
        else if (violNumber == 313) {
            this.violationImage = R.drawable.dog;
        }
        else if (violNumber == 315) {
            this.violationImage = R.drawable.thermometer;
        }
        else if (violNumber == 401 || violNumber == 402 || violNumber == 403) {
            this.violationImage = R.drawable.washing_hands;
        }
        else if (violNumber == 404) {
            this.violationImage = R.drawable.no_smoking;
        }
        else {
            this.violationImage = R.drawable.generic;
        }
    }


}
