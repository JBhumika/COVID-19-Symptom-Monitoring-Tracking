package com.bhumi.assignment;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bhumi.assignment.constants.SymptomsConstants;
import com.bhumi.assignment.helper.DatabaseHelper;

public class SymptomLoggingPageActivity extends AppCompatActivity
    implements AdapterView.OnItemSelectedListener {

  float[] symptomRatingArray;
  RatingBar ratingBar;
  int respRate, heartRate;
  SQLiteDatabase db;
  Double latitude, longitude;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_symptom_logging_page);

    Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setTitle(SymptomsConstants.TOOL_BAR_TITLE);
    setSupportActionBar(toolbar);

//initialize array to cache symptom ratings
    symptomRatingArray = new float[10];

    //fetch the heart rate vale and respiratory value from intent, if not recorded then set 0
    Intent intent = getIntent();
    heartRate = intent.getIntExtra("heartRate", SymptomsConstants.ZERO);
    respRate = intent.getIntExtra("respRate", SymptomsConstants.ZERO);

//creating symptom text view
    TextView symptomLoggingHeaderTextView = (TextView) findViewById(
        R.id.symptomLoggingPageHeaderTextView);
    symptomLoggingHeaderTextView.setText("System Logging Page");

    ratingBar = (RatingBar) findViewById(R.id.ratingBar);

    //adding spinner to set one value from all sets of symptoms
    Spinner spin = (Spinner) findViewById(R.id.symptomsListSpinner);
    spin.setOnItemSelectedListener(this);

    //setting drop down value for all symptoms
    ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item,
        SymptomsConstants.Symptoms);
    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spin.setAdapter(aa);

    Button uploadSignsSymptoms = (Button) findViewById(R.id.uploadSignsSymptoms);

    //one row created till you upload symptoms buttons with heart rate and respiratory rate value
    uploadSignsSymptoms.setOnClickListener(v -> {

      DatabaseHelper helper = new DatabaseHelper(this);
      db = helper.getWritableDatabase();
      try {
        db.beginTransaction();
        db.execSQL("insert into users(HeartRate, " +
            "RespiratoryRate,Nausea,Headache,Diarrhea,Soar_Throat," +
            "Fever,Muscle_Ache,Loss_of_Smell_or_Taste,Cough," +
            "Shortness_of_Breath,Feeling_Tired) values ('" + heartRate
            + "'," +
            "'" + respRate + "','" + symptomRatingArray[0] + "','" + symptomRatingArray[1] +
            "','" + symptomRatingArray[2] + "','" + symptomRatingArray[3] + "'," +
            "'" + symptomRatingArray[4] + "','" + symptomRatingArray[5] + "'," +
            "'" + symptomRatingArray[6] + "','" + symptomRatingArray[7] + "'," +
            "'" + symptomRatingArray[6] + "','" + symptomRatingArray[7] + "');");
        db.setTransactionSuccessful();
        Toast.makeText(SymptomLoggingPageActivity.this, "Successfully stored in Database!",
            Toast.LENGTH_LONG).show();

      } catch (SQLiteException ignored) {
      } finally {
        db.endTransaction();
      }
    });
  }

  public void onItemSelected(AdapterView<?> arg0, View arg1, final int position, long id) {
    ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
      Toast.makeText(getApplicationContext(),
          SymptomsConstants.Symptoms[position] + " " + ratingBar.getRating(), Toast.LENGTH_LONG)
          .show();
      symptomRatingArray[position] = ratingBar.getRating();
    });
    ratingBar.setRating((float) 0.0);
  }

  @Override
  public void onNothingSelected(AdapterView<?> adapterView) {
  }
}