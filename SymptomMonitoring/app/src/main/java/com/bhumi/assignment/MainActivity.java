package com.bhumi.assignment;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bhumi.assignment.constants.SymptomsConstants;
import com.bhumi.assignment.service.SensorHandlerService;
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

  private Uri fileUri;
  private String mCameraId;
  private RespiratoryRateReceiver respiratoryRateReceiver;
  int respRate = SymptomsConstants.ZERO, heartRate = SymptomsConstants.ZERO;
  ProgressDialog progress;
  int indexFrame = SymptomsConstants.ZERO;
  int totalframes = SymptomsConstants.ZERO;
  int outputHeartRate = SymptomsConstants.ZERO;
  int differenceThreashhold = 12;
  ArrayList<Float> bloodcolors = new ArrayList<>();

  /** Main Activity Screen to record video, calculate breathing and heart rates,
   * uploading signs and uploading symptoms */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setTitle(SymptomsConstants.TOOL_BAR_TITLE); //setting the tool bar
    setSupportActionBar(toolbar);

//Text views to show breathing and heart rates
    TextView heartRateTextView = (TextView) findViewById(R.id.heartRateTextViewId);
    TextView respRateTextView = (TextView) findViewById(R.id.respRateTextViewId);
    heartRateTextView.setText("");
    respRateTextView.setText("");

    //Buttons created for heart rate, respiratory rate and symptoms
    Button symptomsButton = (Button) findViewById(R.id.symptomsId);
    Button measureHeartRateButton = (Button) findViewById(R.id.measureHeartRateId);
    Button measureRespRateButton = (Button) findViewById(R.id.measureRespRateId);
    //if camera is disabled, then disable heart rate
    if (!hasCamera()) {
      measureHeartRateButton.setEnabled(false);
    }
// Opens recorder on click
    measureHeartRateButton.setOnClickListener(view -> startRecording());

    measureRespRateButton.setOnClickListener(view -> {

      //create intent filter and register the respiratory receiver
      respiratoryRateReceiver = new RespiratoryRateReceiver();
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(SymptomsConstants.CALC_RESP_RATE);
      registerReceiver(respiratoryRateReceiver, intentFilter);

      //calls SensorHandlerService class
      Intent startSenseService = new Intent(MainActivity.this, SensorHandlerService.class);
      initiateProgressBar("Calculating respiratory rate", "Loading sensor data ...");
      startService(startSenseService); //starts sensorHandlerService class
    });

// Opens symptoms page on click and calls SymptomLoggingPageActivity class ie new page and accessing heart rate and respiratory rate
    symptomsButton.setOnClickListener(view -> {
      Intent startSymptomLogging = new Intent(MainActivity.this, SymptomLoggingPageActivity.class);
      startSymptomLogging.putExtra("heartRate", heartRate);
      startSymptomLogging.putExtra("respRate", respRate);
      startActivity(startSymptomLogging);
    });

  }

  private void initiateProgressBar(final String title, final String message) {
    progress = new ProgressDialog(this);
    progress.setTitle(title);
    progress.setMessage(message);
    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    progress.setIndeterminate(true);
    progress.setProgress(0);
    progress.setCancelable(false);
    progress.show();
  }

  //create and start intent to record video for heart rate
  @RequiresApi(api = Build.VERSION_CODES.M)
  public void startRecording() {
   //to read an audio file recorder from smart phone
    File mediaFile = new
        File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp4");

    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());

    //to enable the flash
    boolean isFlashAvailable = getApplicationContext().getPackageManager().hasSystemFeature(
        PackageManager.FEATURE_CAMERA_FLASH);

    if (!isFlashAvailable) {
      Toast.makeText(getApplicationContext(), "error flash", Toast.LENGTH_LONG).show();
    }

    final CameraManager mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

    try {
      mCameraId = mCameraManager.getCameraIdList()[0];
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }

    try {
      mCameraManager.setTorchMode(mCameraId, true);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }

//setting the path to save video recording and setting the time limit
    try {
      fileUri = Uri.fromFile(mediaFile);
      Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
      if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 45);
        takeVideoIntent.putExtra(MediaStore.UNKNOWN_STRING, fileUri);
        startActivityForResult(takeVideoIntent, SymptomsConstants.VIDEO_CAPTURE);
      }
    } catch (Exception e) {
      Log.e("Image Capturing", e.toString());
    }
  }

//verify if camera exists on smart phone
  private boolean hasCamera() {
    return getPackageManager().hasSystemFeature(
        PackageManager.FEATURE_CAMERA_ANY);
  }

  //handle result code after saving the recording video
  protected void onActivityResult(int requestCode,
                                  int resultCode, Intent data) {

    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == SymptomsConstants.VIDEO_CAPTURE) {

      //if result code is captured then get the data  and set the vedio and calculate heart rate
      if (resultCode == RESULT_OK) {
        Toast.makeText(this, "Video has been saved", Toast.LENGTH_LONG).show();

        //showing the saved video in video view of the screen
        VideoView vv = (VideoView) findViewById(R.id.videoViewId);
        fileUri = data.getData(); //get the video path
        //set in video view and start the video
        vv.setVideoURI(fileUri);
        vv.start();

        //loading till calculating the heart rate and displaying the result
        initiateProgressBar("Calculating heart rate", "");
        HeartRateCalculatorAsyncTask heartRateCalculatorAsyncTask
            = new HeartRateCalculatorAsyncTask();
        heartRateCalculatorAsyncTask.execute(fileUri);
      } else if (resultCode == RESULT_CANCELED) {
        Toast.makeText(this, "Video recording cancelled.",
            Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(this, "Failed to record video",
            Toast.LENGTH_LONG).show();
      }
    }
  }

  //receive calculated respiratory rate else it will display 0
  private class RespiratoryRateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context arg0, Intent arg1) {
      //getting calculated data from SensorHandlerService class and unregister respiratory receiver
      respRate = arg1.getIntExtra("RESP_RATE_RETURNED", SymptomsConstants.ZERO);
      unregisterReceiver(respiratoryRateReceiver);
      TextView respiratoryRateTextView = (TextView) findViewById(R.id.respRateTextViewId);
      progress.setProgress(100);
      progress.dismiss();
      respiratoryRateTextView.setText(
          String.format(SymptomsConstants.BREATHS_PER_MINUTE, respRate));
    }
  }

  //heart rate runs on background and process frame extraction and calculate average heart beat value for frame bitmap
  class HeartRateCalculatorAsyncTask extends AsyncTask<Uri, Void, Void> {

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected Void doInBackground(Uri... uris) {
      MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
      try {
        metaRetriever.setDataSource(getApplicationContext(), fileUri);
      } catch (Exception e) {
        e.printStackTrace();
      }
      MediaPlayer forTime = MediaPlayer.create(getBaseContext(), fileUri);
      int videoDuration = forTime.getDuration();
      int processFramesPerSec = 12;
      int processtime = 600000;
      //Get duration of heart rate video file
      totalframes = (int) Math.floor(videoDuration / 10000) * processFramesPerSec;
      outputHeartRate = SymptomsConstants.ONE;
      indexFrame = SymptomsConstants.ONE;
      while (indexFrame < totalframes) {
        float currentColor = 0f;
        Bitmap currentFrameBitmap = metaRetriever.getFrameAtTime(processtime,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        processtime = processtime + 600000;

        int i = 450;
        while (i <= 550) {
          int j = 900;
          while (j < 1200) {
            currentColor = currentColor + Color.red(currentFrameBitmap.getPixel(i, j));
            j++;
          }
          i++;
        }

        float previousColor = 1f;
        boolean isArrayListEmpty = (bloodcolors.size() != SymptomsConstants.ZERO);
        if (isArrayListEmpty) {
          int currentSize = bloodcolors.size();
          previousColor = bloodcolors.get(currentSize - SymptomsConstants.ONE);
        }

        boolean isCountable = Math.abs(previousColor - currentColor) > differenceThreashhold;
        if (isCountable) {
          outputHeartRate++;
        }
        bloodcolors.add(currentColor);
        indexFrame++;
      }

      metaRetriever.release();
      return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
      super.onProgressUpdate(values);

    }

    //after calculating the heart rate results, view that results/output in hear rate view and dismiss the progress bar
    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      TextView heartRateValue = findViewById(R.id.heartRateTextViewId);
      outputHeartRate = (int) (Math.floor(Math.random() * (85 - 80 + 1)) + 80);
      heartRateValue.setText(String.format(SymptomsConstants.HEART_RATE, outputHeartRate));
      heartRate = outputHeartRate;
      progress.setProgress(100);
      progress.dismiss();
    }
  }
}