package com.example.project276.Model;

import com.example.project276.R;

import java.util.ArrayList;
import java.util.List;

//Model to store info about a single restaurant
public class Restaurant {
    private String name;
    private int iconID;//I changed data type cause the images are tracking by id and id is int
    private String address;
    private double latitude;
    private double longitude;
    private String trackingNumber;
    private String city;
    private String facType;
    private boolean favourite=false;
    List<Inspection> inspectionList = new ArrayList<>();

    //constructor
    public Restaurant() {

    }

    //getters
    public String getName() { return name; }
    public int getIconID() { return iconID; }
    public List<Inspection> getInspectionList() {
        return inspectionList;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getFacType() { return facType; }
    public String getTrackingNumber() {
        return trackingNumber;
    }
    public Inspection getSingleInspection(int position) {
        if (inspectionList.size() <= position) {
            return null;
        }
        if(position< 0){
            return null;
        }
        else {
            return inspectionList.get(position);
        }
    }
    public int getInspectionSize() { return inspectionList.size(); }
    public boolean isFavourite() { return favourite; }

    //setters
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public void setCity(String city) {
        this.city = city;
    }
    public void setType(String facType) {
        this.facType = facType;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public void setImgId(){
        name = this.getName();
        if (name.contains("A&W")){
            iconID = R.drawable.a_and_w;
        }
        else if (name.matches("Lee Yuen Seafood Restaurant")){
            iconID =  R.drawable.lee_yuen;
        }
        else if (name.matches("The Unfindable Bar")){
            iconID =  R.drawable.the_unfindable_bar;
        }

        else if (name.matches("104 Sushi & Co.")){
            iconID =  R.drawable.sushi_and_co;
        } else if (name.matches("Zugba Flame Grilled Chicken")){
            iconID =  R.drawable.zugba_flame_grilled_chicken;
        }
        else if (name.contains("Boston Pizza")){
            iconID =  R.drawable.boston_pizza;
        }
        else if (name.contains("Coffee")){
            iconID =  R.drawable.coffee;
        }
        else if (name.contains("pizza") || name.contains("Pizza")){
            iconID =  R.drawable.restaurant_pizza;
        }
        else if (name.contains("5 Star Catering")){
            iconID =  R.drawable.logo5;
        }
        else if (name.contains("McDonald")){
            iconID =  R.drawable.mcdonald;
        }
        else if (name.contains("7-Eleven")){
            iconID =  R.drawable.seven_eleven;
        }
        else if (name.contains("Pizza Hut")){
            iconID =  R.drawable.pizza_hut;
        }
        else if (name.contains("KFC")){
            iconID =  R.drawable.kfc;
        }
        else if (name.contains("Subway #")){
            iconID =  R.drawable.subway;
        }
        else if (name.contains("Burger King")){
            iconID =  R.drawable.burger_king;
        }
        else {
            // Generic image if restaurant not found
            iconID =  R.drawable.exception;
        }
    }

}
  
