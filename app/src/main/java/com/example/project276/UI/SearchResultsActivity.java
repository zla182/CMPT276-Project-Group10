package com.example.project276.UI;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.project276.Model.AsyncTaskSearch;
import com.example.project276.Model.RestaurantManager;
import com.example.project276.R;

public class SearchResultsActivity extends Activity {

    RestaurantManager manager = RestaurantManager.getManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        handleIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            //get the query string the user typed in
            String query = intent.getStringExtra(SearchManager.QUERY);

            //launch async task to build restaurants
            AsyncTaskSearch searchTask = new AsyncTaskSearch(query, SearchResultsActivity.this, this);
            searchTask.execute(query);
        }
    }

    public void launchIntent(){
        Intent intentMain = MainActivity.makeIntent(SearchResultsActivity.this);

        manager.setIsCurrentlySearched(true);

        //check which activity to go back to
        if(manager.getSearchFromMain() && !manager.getSearchFromMaps()) {
            startActivity(intentMain);
        } else {
            //Delay to make sure MapsActivity runs with the correct restaurants in manager
            Handler handler = new Handler();
            handler.postDelayed(launchMapWithDelay,3000);
        }
        finish();
    }

    //method delays launch
    private final Runnable launchMapWithDelay = new Runnable() {
        @Override
        public void run() {
            Intent intentMaps = MapsActivity.makeIntent(SearchResultsActivity.this);
            startActivity(intentMaps);
        }
    };
}