package com.bhumi.assignment.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

  private static final String dbName = "symptom_monitoring";

  private static final int version = 1;

  public DatabaseHelper(@Nullable final Context context) {
    super(context, dbName, null, version);
  }

  //create symptoms db called users to store symptoms,heart rate and respiratory rate( DS used: DB SQLite)
  @Override
  public void onCreate(final SQLiteDatabase db) {
    db.execSQL("CREATE TABLE IF NOT EXISTS users ("
        + " recID integer PRIMARY KEY autoincrement, "
        + " HeartRate numeric, "
        + " RespiratoryRate numeric, "
        + " Nausea numeric, "
        + " Headache numeric, "
        + " Diarrhea numeric, "
        + " Soar_Throat numeric, "
        + " Fever numeric, "
        + " Muscle_Ache numeric, "
        + " Loss_of_Smell_or_Taste numeric, "
        + " Cough numeric, "
        + " Shortness_of_Breath numeric, "
        + " Feeling_Tired numeric); ");
  }

  @Override
  public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {

  }
}
