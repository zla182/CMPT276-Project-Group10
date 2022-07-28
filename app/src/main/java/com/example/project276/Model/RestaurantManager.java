package com.example.project276.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class RestaurantManager implements Iterable<Restaurant>{

    private static List<Restaurant> restaurantList = new ArrayList<>();
    private static RestaurantManager manager;

    //searching variables to help with search functions
    private boolean searchFromMain = false;
    private boolean searchFromMaps = false;
    private boolean isCurrentlySearched = false;
    private int hazardLevelSearch = 0;

    @Override
    public Iterator<Restaurant> iterator() { return restaurantList.iterator(); }

    public static RestaurantManager getManager() {
        if (manager == null) {
            manager = new RestaurantManager();
        }
        return manager;
    }

    private RestaurantManager(){ }

    //Clear List
    public void clearRestaurantList(){restaurantList.clear();}

    //add restaurant
    public static void add(Restaurant restaurant){
        restaurantList.add(restaurant);
    }
    public static void remove(int index){restaurantList.remove(index);}

    //getters
    public static int getSize() { return restaurantList.size(); }
    public static Restaurant getIndex(int n){
        return restaurantList.get(n);
    }
    public boolean getSearchFromMain() {return searchFromMain;}
    public boolean getSearchFromMaps() {return searchFromMaps;}
    public boolean getIsCurrentlySearched() {return isCurrentlySearched;}
    public int getHazardLevelSearch() {return hazardLevelSearch;}

    //setters
    public void setSearchFromMain(boolean isSearchFromMain){ this.searchFromMain = isSearchFromMain; }
    public void setSearchFromMaps(boolean isSearchFromMaps){ this.searchFromMaps = isSearchFromMaps; }
    public void setIsCurrentlySearched(boolean searchedOrNot){ this.isCurrentlySearched = searchedOrNot; }
    public void setHazardLevelSearch(int hazLevel){this.hazardLevelSearch = hazLevel;}

    //sorting methods
    public void sortByName() {
        Comparator<Restaurant> compareByName = new Comparator<Restaurant>() { //Compares restaurant names
            @Override
            public int compare(Restaurant restaurant1, Restaurant restaurant2) {
                return restaurant1.getName().compareTo(restaurant2.getName());
            }
        };
        Collections.sort(restaurantList, compareByName);
    }

    public void sortInspectionsByDate() {
        Comparator<Inspection> compareByDate = new Comparator<Inspection>() {
            @Override
            public int compare(Inspection inspection1, Inspection inspection2) {
                return inspection1.getInspectionDate().compareTo(inspection2.getInspectionDate());
            }
        };

        for (Restaurant restaurant : restaurantList){
            Collections.sort(restaurant.inspectionList, compareByDate.reversed());
        }
    }

    public int findIndex(Restaurant restaurant){
        for(int i = 0 ; i < restaurantList.size() ; i++){
            if(restaurant == restaurantList.get(i)){
                return i;
            }
        }
        return -1;
    }

    public Restaurant findRestaurantByLocation(double lat, double lon, String restaurantName) {
        for (Restaurant restaurant : restaurantList) {
            if (
                    restaurant.getLatitude() == lat
                            && restaurant.getLongitude() == lon
                            && restaurant.getName().equals(restaurantName)
            )
            {
                return restaurant;
            }
        }
        return null;
    }

}