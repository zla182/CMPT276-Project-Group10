package com.example.project276.UI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project276.Model.Inspection;
import com.example.project276.Model.ViolationDetail;
import com.example.project276.R;

import java.util.List;
import java.util.Objects;

//Displays details about a single inspection of a restaurant
public class InspectionActivity extends AppCompatActivity {

    private static List<ViolationDetail> violationDetailList;
    private static List<ViolationDetail> getViolationDetailList() { return violationDetailList; }
    public String message(ViolationDetail violationDetail){
        String message = getString(R.string.inspection_activity_violation_description,
                "" + violationDetail.getViolationNumber(),
                "" + violationDetail.getIsCritical(),
                "" + violationDetail.getViolDetails(),
                "" + violationDetail.getIsRepeat()
        );
        return message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Inspection Details");
        // Get inspection passed from RestaurantActivity
        Inspection inspection = (Inspection) getIntent().getSerializableExtra(RestaurantActivity.INSPECTION);

        if (inspection != null) {
            violationDetailList = inspection.getViolationList();
            setInspectionInformation(inspection);
            ArrayAdapter<ViolationDetail> adapter = new MyListAdapter(inspection.getViolationList());
            ListView list = findViewById(R.id.violationsListView);
            list.setAdapter(adapter);
            registerClickCallback();
        }
    }

    private void setInspectionInformation(Inspection inspection) {
        ImageView imageView = findViewById(R.id.imageRestaurnat);
        TextView inspectionHazardRating = findViewById(R.id.txtHazardRating);
        inspectionHazardRating.setText(getString(R.string.inspection_activity_hazard_rating, inspection.getHazardRating()));
        inspectionHazardRating.setBackgroundColor(Color.GRAY);
        setInspectionImage(inspection,imageView,inspectionHazardRating);
        TextView inspectionDate = findViewById(R.id.txtInspectionDate);
        inspectionDate.setText(getString(R.string.inspection_activity_date,
                inspection.fullFormattedDate()));
        TextView inspectionType = findViewById(R.id.txtInspectionType);
        inspectionType.setText(getString(R.string.inspection_activity_type,
                inspection.getInspectionType()));
        TextView inspectionNumCritical = findViewById(R.id.txtcriticalViolationsNum);
        inspectionNumCritical.setText(getString(R.string.inspection_activity_critical_num,
                "" + inspection.getNumCritical()));
        TextView inspectionNumNonCritical = findViewById(R.id.txtnonCritViolationsNum);
        inspectionNumNonCritical.setText(getString(R.string.inspection_activity_non_critical_num,
                "" + inspection.getNumNonCritical()));
    }

    public void setInspectionImage(Inspection inspection,ImageView imageView,TextView inspectionHazardRating){
        if (inspection.getHazardRating().equals("Low") ) {
            imageView.setImageResource(R.drawable.green);
            inspectionHazardRating.setTextColor(Color.GREEN);
        }
        else if (inspection.getHazardRating().equals("Moderate") ) {
            imageView.setImageResource(R.drawable.yellow);
            inspectionHazardRating.setTextColor(Color.YELLOW);
        }
        else if (inspection.getHazardRating().equals("High")) {
            imageView.setImageResource(R.drawable.red);
            inspectionHazardRating.setTextColor(Color.RED);
        }
        else{
            imageView.setImageResource(R.drawable.exception);
            inspectionHazardRating.setTextColor(Color.WHITE);
        }
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


    //https://stackoverflow.com/questions/22062681/private-class-array-adapter-warning
    private class MyListAdapter extends ArrayAdapter<ViolationDetail> {

        public MyListAdapter(List<ViolationDetail> violationDetailList) {
            super(InspectionActivity.this, R.layout.violation_item, violationDetailList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View itemSingleton = convertView;
            if (itemSingleton == null) {
                itemSingleton = getLayoutInflater().inflate(R.layout.violation_item, parent, false);
            }

            // correct violationDetail
            ViolationDetail currentViolationDetail = InspectionActivity.getViolationDetailList().get(position);

            // Set violationDetail icon
            ImageView violationIcon = itemSingleton.findViewById(R.id.violationIcon);
            violationIcon.setImageResource(currentViolationDetail.getViolationImage());

            // Set violationDetail description
            TextView violationDescription = itemSingleton.findViewById(R.id.violationDescription);
            violationDescription.setText(
                    getString(R.string.violation_description,
                            currentViolationDetail.getDetailsDescriptions())
                    );

            // Set the severity icon
            ImageView violationSeverityIcon = itemSingleton.findViewById(R.id.violationSeverityIcon);
            violationSeverityIcon.setImageResource(R.drawable.exception);
            violationSeverityIcon.setBackgroundColor(Color.WHITE);
            setViolationImage(currentViolationDetail, violationSeverityIcon);
            return itemSingleton;
        }
    }
    void setViolationImage(ViolationDetail currentViolationDetail,ImageView violationSeverityIcon){
        if (currentViolationDetail.getIsCritical() == true) {
            violationSeverityIcon.setImageResource(R.drawable.dafault);
            violationSeverityIcon.setBackgroundColor(Color.RED);
        }
        else if(currentViolationDetail.getIsCritical() == false) {
            violationSeverityIcon.setImageResource(R.drawable.exclamation);
            violationSeverityIcon.setBackgroundColor(Color.GREEN);
        }
    }


    public static Intent makeIntent(Context context) {
        return new Intent(context, InspectionActivity.class);
    }

    private void registerClickCallback() {
        ListView list = findViewById(R.id.violationsListView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViolationDetail violationDetail = InspectionActivity.getViolationDetailList().get(position);
                Toast.makeText(InspectionActivity.this, message(violationDetail), Toast.LENGTH_LONG).show();
            }
        });
    }
}